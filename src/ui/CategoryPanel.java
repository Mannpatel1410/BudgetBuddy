package ui;

import model.category.Category;
import service.CategoryService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.List;

public class CategoryPanel extends JPanel {

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final Color HEADER_DARK = new Color(44,  62,  80);
    private static final Color GREEN       = new Color(39, 174,  96);
    private static final Color BLUE        = new Color(41,  98, 163);
    private static final Color ORANGE      = new Color(211,  84,   0);
    private static final Color RED         = new Color(192,  57,  43);

    // Dot palette cycling by category name hash
    private static final Color[] DOT_COLORS = {
        new Color(39,  174,  96),   // green
        new Color(41,  128, 185),   // blue
        new Color(211,  84,   0),   // orange
        new Color(142,  68, 173),   // purple
        new Color(231,  76,  60),   // red
        new Color(22,  160, 133),   // teal
        new Color(243, 156,  18),   // amber
        new Color(52,   73,  94),   // navy
    };

    private JTree                  categoryTree;
    private DefaultTreeModel       treeModel;
    private DefaultMutableTreeNode rootNode;
    private JButton                addCategoryBtn;
    private JButton                addSubCategoryBtn;
    private JButton                editCategoryBtn;
    private JButton                deleteCategoryBtn;
    private final CategoryService  categoryService = new CategoryService();
    private long                   currentUserId;

    public CategoryPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 246, 248));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        // ── Top bar ──────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel pageTitle = new JLabel("Categories");
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        pageTitle.setForeground(HEADER_DARK);
        topBar.add(pageTitle, BorderLayout.WEST);

        // ── Tree ─────────────────────────────────────────────────────────────
        rootNode = new DefaultMutableTreeNode("Categories");
        treeModel = new DefaultTreeModel(rootNode);
        categoryTree = new JTree(treeModel);
        categoryTree.setCellRenderer(new CategoryTreeRenderer());
        categoryTree.setRowHeight(28);
        categoryTree.setFont(new Font("SansSerif", Font.PLAIN, 13));
        categoryTree.setBackground(Color.WHITE);
        categoryTree.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane treeScroll = new JScrollPane(categoryTree);
        treeScroll.setBorder(BorderFactory.createEmptyBorder());
        treeScroll.getViewport().setBackground(Color.WHITE);

        // ── Button bar ────────────────────────────────────────────────────────
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        addCategoryBtn    = filledBtn("+ Add Category",    GREEN);
        addSubCategoryBtn = filledBtn("+ Add Subcategory", BLUE);
        editCategoryBtn   = filledBtn("Edit",              ORANGE);
        deleteCategoryBtn = filledBtn("Delete",            RED);

        buttonPanel.add(addCategoryBtn);
        buttonPanel.add(addSubCategoryBtn);
        buttonPanel.add(editCategoryBtn);
        buttonPanel.add(deleteCategoryBtn);

        add(topBar,      BorderLayout.NORTH);
        add(treeScroll,  BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // ── Listeners ─────────────────────────────────────────────────────────
        addCategoryBtn.addActionListener(e -> addCategory(null));

        addSubCategoryBtn.addActionListener(e -> {
            DefaultMutableTreeNode selected =
                    (DefaultMutableTreeNode) categoryTree.getLastSelectedPathComponent();
            if (selected != null && selected.getUserObject() instanceof Category) {
                Category parent = (Category) selected.getUserObject();
                addCategory(parent.getId());
            } else {
                JOptionPane.showMessageDialog(this, "Select a parent category first.");
            }
        });

        editCategoryBtn.addActionListener(e -> editCategory());
        deleteCategoryBtn.addActionListener(e -> deleteCategory());
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void loadTree(long userId) {
        this.currentUserId = userId;
        rootNode.removeAllChildren();
        List<Category> roots = categoryService.buildCategoryTree(userId);
        for (Category root : roots) {
            rootNode.add(buildTreeNode(root));
        }
        treeModel.reload();
        expandAllNodes(categoryTree, 0, categoryTree.getRowCount());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private DefaultMutableTreeNode buildTreeNode(Category category) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(category);
        for (Category child : category.getChildren()) {
            node.add(buildTreeNode(child));
        }
        return node;
    }

    private void expandAllNodes(JTree tree, int startRow, int rowCount) {
        for (int i = startRow; i < rowCount; i++) tree.expandRow(i);
        if (tree.getRowCount() != rowCount)
            expandAllNodes(tree, rowCount, tree.getRowCount());
    }

    private void addCategory(Long parentId) {
        String name = JOptionPane.showInputDialog(this, "Category name:");
        if (name != null && !name.trim().isEmpty()) {
            categoryService.addCategory(name.trim(), null, parentId, currentUserId);
            loadTree(currentUserId);
        }
    }

    private void editCategory() {
        DefaultMutableTreeNode selected =
                (DefaultMutableTreeNode) categoryTree.getLastSelectedPathComponent();
        if (selected == null || !(selected.getUserObject() instanceof Category)) {
            JOptionPane.showMessageDialog(this, "Select a category to edit.");
            return;
        }
        Category cat = (Category) selected.getUserObject();
        if (cat.isDefault()) {
            JOptionPane.showMessageDialog(this, "Cannot edit default categories.");
            return;
        }
        String newName = JOptionPane.showInputDialog(this, "New name:", cat.getName());
        if (newName != null && !newName.trim().isEmpty()) {
            cat.setName(newName.trim());
            categoryService.updateCategory(cat);
            loadTree(currentUserId);
        }
    }

    private void deleteCategory() {
        DefaultMutableTreeNode selected =
                (DefaultMutableTreeNode) categoryTree.getLastSelectedPathComponent();
        if (selected == null || !(selected.getUserObject() instanceof Category)) return;
        Category cat = (Category) selected.getUserObject();
        if (cat.isDefault()) {
            JOptionPane.showMessageDialog(this, "Cannot delete default categories.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete '" + cat.getName() + "' and all subcategories?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            categoryService.deleteCategory(cat.getId());
            loadTree(currentUserId);
        }
    }

    // ── Widget helpers ────────────────────────────────────────────────────────

    private static JButton filledBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }

    // ── Colored dot Icon ──────────────────────────────────────────────────────

    private static class DotIcon implements Icon {
        private final Color color;
        DotIcon(Color color) { this.color = color; }
        @Override public int getIconWidth()  { return 12; }
        @Override public int getIconHeight() { return 12; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x + 1, y + 1, 10, 10);
            g2.dispose();
        }
    }

    // ── Tree renderer ─────────────────────────────────────────────────────────

    private static class CategoryTreeRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            setFont(new Font("SansSerif", Font.PLAIN, 13));

            if (value instanceof DefaultMutableTreeNode) {
                Object obj = ((DefaultMutableTreeNode) value).getUserObject();
                if (obj instanceof Category) {
                    Category cat = (Category) obj;
                    int childCount = ((DefaultMutableTreeNode) value).getChildCount();

                    // "Food (3)" for parents, "Groceries" for leaves
                    String display = (childCount > 0)
                            ? cat.getName() + "  (" + childCount + ")"
                            : cat.getName();
                    setText(display);

                    // Colored dot: pick from palette by name hash
                    int idx = Math.abs(cat.getName().hashCode()) % DOT_COLORS.length;
                    setIcon(new DotIcon(DOT_COLORS[idx]));

                    if (cat.isDefault() && !sel) {
                        setForeground(new Color(90, 90, 90));
                        setFont(new Font("SansSerif", Font.ITALIC, 13));
                    } else if (!sel) {
                        setForeground(new Color(44, 62, 80));
                    }
                }
            }
            return this;
        }
    }
}
