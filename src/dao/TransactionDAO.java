package dao;

// import db.DatabaseManager;
import factory.TransactionFactory;
import model.transaction.Transaction;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    // private final Connection conn = DatabaseManager.getInstance().getConnection();
    private final Connection conn = null;

    public void insert(Transaction t) {
        String sql = "INSERT INTO `transaction` (account_id, category_id, type, amount, description, is_recurring, tags, tax_rate, transaction_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, t.getAccountId());
            ps.setLong(2, t.getCategoryId());
            ps.setString(3, t.getType());
            ps.setDouble(4, t.getAmount());
            ps.setString(5, t.getDescription());
            ps.setBoolean(6, t.isRecurring());
            ps.setString(7, t.getTags());
            ps.setDouble(8, t.getTaxRate());
            if (t.getTransactionDate() != null) {
                ps.setDate(9, Date.valueOf(t.getTransactionDate()));
            } else {
                ps.setDate(9, null);
            }

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    t.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert transaction", e);
        }
    }

    public Transaction findById(long id) {
        String sql = "SELECT * FROM `transaction` WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find transaction by id", e);
        }
        return null;
    }

    public List<Transaction> findByAccountId(long accountId) {
        return findByLong("SELECT * FROM `transaction` WHERE account_id = ?", accountId);
    }

    public List<Transaction> findByCategoryId(long categoryId) {
        return findByLong("SELECT * FROM `transaction` WHERE category_id = ?", categoryId);
    }

    public List<Transaction> findByUserId(long userId) {
        String sql = "SELECT t.* FROM `transaction` t JOIN account a ON t.account_id = a.id WHERE a.user_id = ?";
        return findByLong(sql, userId);
    }

    public void update(Transaction t) {
        String sql = "UPDATE `transaction` SET account_id=?, category_id=?, type=?, amount=?, description=?, is_recurring=?, tags=?, tax_rate=?, transaction_date=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, t.getAccountId());
            ps.setLong(2, t.getCategoryId());
            ps.setString(3, t.getType());
            ps.setDouble(4, t.getAmount());
            ps.setString(5, t.getDescription());
            ps.setBoolean(6, t.isRecurring());
            ps.setString(7, t.getTags());
            ps.setDouble(8, t.getTaxRate());
            if (t.getTransactionDate() != null) {
                ps.setDate(9, Date.valueOf(t.getTransactionDate()));
            } else {
                ps.setDate(9, null);
            }
            ps.setLong(10, t.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update transaction", e);
        }
    }

    public void delete(long id) {
        String sql = "DELETE FROM `transaction` WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete transaction", e);
        }
    }

    private List<Transaction> findByLong(String sql, long param) {
        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to run transaction query", e);
        }
        return transactions;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Date txDate = rs.getDate("transaction_date");
        Transaction tx = TransactionFactory.createTransaction(
                rs.getString("type"),
                rs.getLong("account_id"),
                rs.getLong("category_id"),
                rs.getDouble("amount"),
                rs.getString("description"),
                txDate != null ? txDate.toLocalDate() : null
        );

        tx.setId(rs.getLong("id"));
        tx.setRecurring(rs.getBoolean("is_recurring"));
        tx.setTags(rs.getString("tags"));
        tx.setTaxRate(rs.getDouble("tax_rate"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            tx.setCreatedAt(createdAt.toLocalDateTime());
        }
        return tx;
    }
}
