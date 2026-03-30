package dao;

import model.category.Category;
import db.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private Connection getConnection() {
        return DatabaseManager.getInstance().getConnection();
    }

    public void insert(Category c) {
        String sql = "INSERT INTO category (user_id, parent_category_id, name, icon, is_default) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (c.getUserId() != null) ps.setLong(1, c.getUserId());
            else ps.setNull(1, Types.BIGINT);

            if (c.getParentCategoryId() != null) ps.setLong(2, c.getParentCategoryId());
            else ps.setNull(2, Types.BIGINT);

            ps.setString(3, c.getName());
            ps.setString(4, c.getIcon());
            ps.setBoolean(5, c.isDefault());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) c.setId(keys.getLong(1));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert category", e);
        }
    }

    public Category findById(long id) {
        String sql = "SELECT * FROM category WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find category", e);
        }
        return null;
    }

    public List<Category> findByUserId(long userId) {
        String sql = "SELECT * FROM category WHERE user_id = ? OR is_default = TRUE";
        List<Category> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find categories", e);
        }
        return list;
    }

    public List<Category> findDefaults() {
        String sql = "SELECT * FROM category WHERE is_default = TRUE";
        List<Category> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find default categories", e);
        }
        return list;
    }

    public List<Category> findByParentId(Long parentId) {
        String sql;
        if (parentId == null) {
            sql = "SELECT * FROM category WHERE parent_category_id IS NULL";
        } else {
            sql = "SELECT * FROM category WHERE parent_category_id = ?";
        }
        List<Category> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (parentId != null) ps.setLong(1, parentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find categories by parent", e);
        }
        return list;
    }

    public void update(Category c) {
        String sql = "UPDATE category SET name = ?, icon = ?, parent_category_id = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getIcon());
            if (c.getParentCategoryId() != null) ps.setLong(3, c.getParentCategoryId());
            else ps.setNull(3, Types.BIGINT);
            ps.setLong(4, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update category", e);
        }
    }

    public void delete(long id) {
        String sql = "DELETE FROM category WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete category", e);
        }
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getLong("id"));
        long uid = rs.getLong("user_id");
        c.setUserId(rs.wasNull() ? null : uid);
        long pid = rs.getLong("parent_category_id");
        c.setParentCategoryId(rs.wasNull() ? null : pid);
        c.setName(rs.getString("name"));
        c.setIcon(rs.getString("icon"));
        c.setDefault(rs.getBoolean("is_default"));
        return c;
    }
}