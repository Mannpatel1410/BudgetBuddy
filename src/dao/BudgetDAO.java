package dao;

import model.budget.Budget;
import state.*;
import db.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAO {
    private Connection getConnection() {
        return DatabaseManager.getInstance().getConnection();
    }

    public void insert(Budget b) {
        String sql = "INSERT INTO budget (user_id, category_id, limit_amount, spent_amount, status, month, year) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, b.getUserId());
            ps.setLong(2, b.getCategoryId());
            ps.setDouble(3, b.getLimitAmount());
            ps.setDouble(4, b.getSpentAmount());
            ps.setString(5, b.getStatus());
            ps.setString(6, b.getMonth());
            ps.setInt(7, b.getYear());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) b.setId(keys.getLong(1));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert budget", e);
        }
    }

    public Budget findById(long id) {
        String sql = "SELECT * FROM budget WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find budget", e);
        }
        return null;
    }

    public List<Budget> findByUserId(long userId) {
        String sql = "SELECT * FROM budget WHERE user_id = ?";
        List<Budget> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find budgets", e);
        }
        return list;
    }

    public List<Budget> findByUserIdAndMonth(long userId, String month, int year) {
        String sql = "SELECT * FROM budget WHERE user_id = ? AND month = ? AND year = ?";
        List<Budget> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, month);
            ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find budgets by month", e);
        }
        return list;
    }

    public void update(Budget b) {
        String sql = "UPDATE budget SET spent_amount = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDouble(1, b.getSpentAmount());
            ps.setString(2, b.getStatus());
            ps.setLong(3, b.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update budget", e);
        }
    }

    public void delete(long id) {
        String sql = "DELETE FROM budget WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete budget", e);
        }
    }

    private Budget mapRow(ResultSet rs) throws SQLException {
        Budget b = new Budget();
        b.setId(rs.getLong("id"));
        b.setUserId(rs.getLong("user_id"));
        b.setCategoryId(rs.getLong("category_id"));
        b.setLimitAmount(rs.getDouble("limit_amount"));
        b.setSpentAmount(rs.getDouble("spent_amount"));
        b.setMonth(rs.getString("month"));
        b.setYear(rs.getInt("year"));

        String status = rs.getString("status");
        switch (status) {
            case "EXCEEDED": b.setCurrentState(new ExceededState()); break;
            case "APPROACHING_LIMIT": b.setCurrentState(new ApproachingLimitState()); break;
            default: b.setCurrentState(new UnderLimitState()); break;
        }
        return b;
    }
}