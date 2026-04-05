package service;

import dao.CategoryDAO;
import model.category.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryService {
    private CategoryDAO categoryDAO = new CategoryDAO();

    public List<Category> buildCategoryTree(long userId) {
        List<Category> allCategories = categoryDAO.findByUserId(userId);
        Map<Long, Category> map = new HashMap<>();
        List<Category> roots = new ArrayList<>();

        for (Category c : allCategories) {
            map.put(c.getId(), c);
        }

        for (Category c : allCategories) {
            if (c.getParentCategoryId() == null) {
                roots.add(c);
            } else {
                Category parent = map.get(c.getParentCategoryId());
                if (parent != null) {
                    parent.addSubCategory(c);
                } else {
                    roots.add(c);
                }
            }
        }
        return roots;
    }

    public List<Category> getAllFlat(long userId) {
        return categoryDAO.findByUserId(userId);
    }

    public Category getById(long id) {
        return categoryDAO.findById(id);
    }

    public void addCategory(String name, String icon, Long parentId, long userId) {
        Category c = new Category();
        c.setUserId(userId);
        c.setParentCategoryId(parentId);
        c.setName(name);
        c.setIcon(icon);
        c.setDefault(false);
        categoryDAO.insert(c);
    }

    public void updateCategory(Category c) {
        categoryDAO.update(c);
    }

    public void deleteCategory(long id) {
        categoryDAO.delete(id);
    }
}
