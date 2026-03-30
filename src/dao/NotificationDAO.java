package dao;

import model.Notification;
import db.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
    private Connection getConnection() {
        return DatabaseManager.getInstance().getConnection();
    }

    public void insert(Notification n) {
        String sql = "INSERT INTO notification (user_id, budget_id, message, alert_type, is_read) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, n.getUserId());
            ps.setLong(2, n.getBudgetId());
            ps.setString(3, n.getMessage());
            ps.setString(4, n.getAlertType());
            ps.setBoolean(5, n.isRead());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) n.setId(keys.getLong(1));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert notification", e);
        }
    }

    public List<Notification> findByUserId(long userId) {
        String sql = "SELECT * FROM notification WHERE user_id = ? ORDER BY created_at DESC";
        List<Notification> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find notifications", e);
        }
        return list;
    }

    public List<Notification> findUnreadByUserId(long userId) {
        String sql = "SELECT * FROM notification WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC";
        List<Notification> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find unread notifications", e);
        }
        return list;
    }

    public void markAsRead(long id) {
        String sql = "UPDATE notification SET is_read = TRUE WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark notification as read", e);
        }
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getLong("id"));
        n.setUserId(rs.getLong("user_id"));
        n.setBudgetId(rs.getLong("budget_id"));
        n.setMessage(rs.getString("message"));
        n.setAlertType(rs.getString("alert_type"));
        n.setRead(rs.getBoolean("is_read"));
        n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return n;
    }
}
