package dao;

import model.report.Report;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import db.DatabaseManager;

public class ReportDAO {
    private Connection conn = DatabaseManager.getInstance().getConnection();

    public void insert(Report r) {
        String sql = "INSERT INTO report (user_id, report_type, period, format, total_income, total_expense, net_savings) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, r.getUserId());
            ps.setString(2, r.getReportType());
            ps.setString(3, r.getPeriod());
            ps.setString(4, r.getFormat());
            ps.setDouble(5, r.getTotalIncome());
            ps.setDouble(6, r.getTotalExpense());
            ps.setDouble(7, r.getNetSavings());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    r.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert report", e);
        }
    }

    public Report findById(long id) {
        String sql = "SELECT * FROM report WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find report by id", e);
        }
        return null;
    }

    public List<Report> findByUserId(long userId) {
        String sql = "SELECT * FROM report WHERE user_id = ? ORDER BY generated_at DESC";
        List<Report> reports = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reports.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find reports by user", e);
        }
        return reports;
    }

    public void delete(long id) {
        String sql = "DELETE FROM report WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete report", e);
        }
    }

    private Report mapRow(ResultSet rs) throws SQLException {
        Report r = new Report();
        r.setId(rs.getLong("id"));
        r.setUserId(rs.getLong("user_id"));
        r.setReportType(rs.getString("report_type"));
        r.setPeriod(rs.getString("period"));
        r.setFormat(rs.getString("format"));
        r.setTotalIncome(rs.getDouble("total_income"));
        r.setTotalExpense(rs.getDouble("total_expense"));
        r.setNetSavings(rs.getDouble("net_savings"));
        Timestamp generatedAt = rs.getTimestamp("generated_at");
        if (generatedAt != null) {
            r.setGeneratedAt(generatedAt.toLocalDateTime());
        }
        return r;
    }
}
