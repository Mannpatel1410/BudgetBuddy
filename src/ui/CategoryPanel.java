package ui;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
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
    private JButton deleteCategoryBtn;
    private CategoryService categoryService;
    private long currentUserId;

    public CategoryPanel() {
        categoryService = new CategoryService();
        setLayout(new BorderLayout(10, 10));

        rootNode = new DefaultMutableTreeNode("Categories");
        treeModel = new DefaultTreeModel(rootNode);
        categoryTree = new JTree(treeModel);
        JScrollPane treeScroll = new JScrollPane(categoryTree);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addCategoryBtn = new JButton("Add Category");
        addSubCategoryBtn = new JButton("Add Subcategory");
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
        deleteCategoryBtn.addActionListener(e -> deleteCategory());

        buttonPanel.add(addCategoryBtn);
        buttonPanel.add(addSubCategoryBtn);
        buttonPanel.add(deleteCategoryBtn);

        add(new JLabel("  Category Manager", JLabel.LEFT), BorderLayout.NORTH);
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
    }

    private DefaultMutableTreeNode buildTreeNode(Category category) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(category);
        for (Category child : category.getChildren()) {
            node.add(buildTreeNode(child));
        }
        return node;
    }

    private void addCategory(Long parentId) {
        String name = JOptionPane.showInputDialog(this, "Category name:");
        if (name != null && !name.trim().isEmpty()) {
            String icon = JOptionPane.showInputDialog(this, "Icon (optional):");
            categoryService.addCategory(name.trim(), icon, parentId, currentUserId);
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
                    "Delete '" + cat.getName() + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                categoryService.deleteCategory(cat.getId());
                loadTree(currentUserId);
            }
        }
    }
}

