package model.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Category {
    private long id;
    private Long userId;
    private Long parentCategoryId;
    private String name;
    private String icon;
    private boolean isDefault;
    private List<Category> children;

    public Category() {
        this.children = new ArrayList<>();
    }

    public Category(long id, Long userId, Long parentCategoryId, String name, String icon, boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.parentCategoryId = parentCategoryId;
        this.name = name;
        this.icon = icon;
        this.isDefault = isDefault;
        this.children = new ArrayList<>();
    }

    // Composite: add child category
    public void addSubCategory(Category child) {
        child.setParentCategoryId(this.id);
        this.children.add(child);
    }

    // Composite: remove child category
    public void removeSubCategory(Category child) {
        this.children.remove(child);
    }

    // Composite: get children
    public List<Category> getChildren() {
        return children;
    }

    // Composite: recursive spending rollup from leaves to root
    public double getSpendingTotal(Map<Long, Double> spendingMap) {
        double total = spendingMap.getOrDefault(this.id, 0.0);
        for (Category child : children) {
            total += child.getSpendingTotal(spendingMap);
        }
        return total;
    }

    // Composite: check if leaf node
    public boolean isLeaf() {
        return children.isEmpty();
    }

    // Composite: recursive search by id
    public Category findById(long targetId) {
        if (this.id == targetId) return this;
        for (Category child : children) {
            Category found = child.findById(targetId);
            if (found != null) return found;
        }
        return null;
    }

    // Composite: collect all leaf nodes
    public List<Category> getLeaves() {
        List<Category> leaves = new ArrayList<>();
        if (isLeaf()) {
            leaves.add(this);
        } else {
            for (Category child : children) {
                leaves.addAll(child.getLeaves());
            }
        }
        return leaves;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getParentCategoryId() { return parentCategoryId; }
    public void setParentCategoryId(Long parentCategoryId) { this.parentCategoryId = parentCategoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    @Override
    public String toString() { return name; }
}
