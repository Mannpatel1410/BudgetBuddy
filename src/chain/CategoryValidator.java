package chain;

import db.DatabaseManager;
import model.transaction.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoryValidator extends ValidationHandler {
    @Override
    public boolean validate(Transaction transaction) {
        if (transaction == null || transaction.getCategoryId() <= 0) {
            return false;
        }

        String sql = "SELECT 1 FROM category WHERE id = ?";
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, transaction.getCategoryId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            return false;
        }

        return super.validate(transaction);
    }
}
