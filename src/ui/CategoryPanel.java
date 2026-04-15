package ui;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import model.category.Category;
import service.CategoryService;
import java.util.List;

public class CategoryPanel extends JPanel {
    private JTree categoryTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private JButton addCategoryBtn;
    private JButton addSubCategoryBtn;
    private JButton editCategoryBtn;
    private JButton deleteCategoryBtn;
    private CategoryService categoryService;
    private long currentUserId;

    public CategoryPanel() {
        categoryService = new CategoryService();
        setLayout(new BorderLayout(10, 10));

        rootNode = new DefaultMutableTreeNode("Categories");
        treeModel = new DefaultTreeModel(rootNode);
        categoryTree = new JTree(treeModel);
        categoryTree.setCellRenderer(new CategoryTreeRenderer());
        categoryTree.setRowHeight(26);
        categoryTree.setFont(categoryTree.getFont().deriveFont(13f));
        JScrollPane treeScroll = new JScrollPane(categoryTree);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addCategoryBtn = new JButton("Add Category");
        addSubCategoryBtn = new JButton("Add Subcategory");
        editCategoryBtn = new JButton("Edit");
        deleteCategoryBtn = new JButton("Delete");

        addCategoryBtn.addActionListener(e -> addCategory(null));
        addSubCategoryBtn.addActionListener(e -> {
            DefaultMutableTreeNode selected = (DefaultMutableTreeNode) categoryTree.getLastSelectedPathComponent();
            if (selected != null && selected.getUserObject() instanceof Category) {
                Category parent = (Category) selected.getUserObject();
                addCategory(parent.getId());
            } else {
                JOptionPane.showMessageDialog(this, "Select a parent category first.");
            }
        });
        editCategoryBtn.addActionListener(e -> editCategory());
        deleteCategoryBtn.addActionListener(e -> deleteCategory());

        buttonPanel.add(addCategoryBtn);
        buttonPanel.add(addSubCategoryBtn);
        buttonPanel.add(editCategoryBtn);
        buttonPanel.add(deleteCategoryBtn);

        add(treeScroll, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void loadTree(long userId) {
        this.currentUserId = userId;
        rootNode.removeAllChildren();
        List<Category> roots = categoryService.buildCategoryTree(userId);
        for (Category root : roots) {
            DefaultMutableTreeNode node = buildTreeNode(root);
            rootNode.add(node);
        }
        treeModel.reload();
        expandAllNodes(categoryTree, 0, categoryTree.getRowCount());
    }

    private DefaultMutableTreeNode buildTreeNode(Category category) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(category);
        for (Category child : category.getChildren()) {
            node.add(buildTreeNode(child));
        }
        return node;
    }

    private void expandAllNodes(JTree tree, int startRow, int rowCount) {
        for (int i = startRow; i < rowCount; i++) {
            tree.expandRow(i);
        }
        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }

    private void addCategory(Long parentId) {
        String name = JOptionPane.showInputDialog(this, "Category name:");
        if (name != null && !name.trim().isEmpty()) {
            String icon = JOptionPane.showInputDialog(this, "Icon (optional):");
            categoryService.addCategory(name.trim(), icon, parentId, currentUserId);
            loadTree(currentUserId);
        }
    }

    private void editCategory() {
        DefaultMutableTreeNode selected = (DefaultMutableTreeNode) categoryTree.getLastSelectedPathComponent();
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
            String newIcon = JOptionPane.showInputDialog(this, "New icon:", cat.getIcon());
            cat.setName(newName.trim());
            cat.setIcon(newIcon);
            categoryService.updateCategory(cat);
            loadTree(currentUserId);
        }
    }

    private void deleteCategory() {
        DefaultMutableTreeNode selected = (DefaultMutableTreeNode) categoryTree.getLastSelectedPathComponent();
        if (selected != null && selected.getUserObject() instanceof Category) {
            Category cat = (Category) selected.getUserObject();
            if (cat.isDefault()) {
                JOptionPane.showMessageDialog(this, "Cannot delete default categories.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete '" + cat.getName() + "' and all subcategories?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                categoryService.deleteCategory(cat.getId());
                loadTree(currentUserId);
            }
        }
    }

    // Custom renderer: shows "Name  (icon)" — icon as a small grey hint, not a prefix
    private static class CategoryTreeRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            setFont(getFont().deriveFont(13f));
            if (value instanceof DefaultMutableTreeNode) {
                Object obj = ((DefaultMutableTreeNode) value).getUserObject();
                if (obj instanceof Category) {
                    Category cat = (Category) obj;
                    String display = cat.getName();
                    if (cat.getIcon() != null && !cat.getIcon().isEmpty()) {
                        display = cat.getName() + "  [" + cat.getIcon() + "]";
                    }
                    setText(display);
                    if (cat.isDefault() && !sel) {
                        setForeground(new Color(100, 100, 100));
                    }
                }
            }
            return this;
        }
    }
}
