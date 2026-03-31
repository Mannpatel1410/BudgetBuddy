package dao;

import factory.AccountFactory;
import model.account.*;
import db.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    private Connection conn = DatabaseManager.getInstance().getConnection();

    public void insert(Account account) {
        String sql = "INSERT INTO account (user_id, account_name, account_type, balance, overdraft_limit, interest_rate, credit_limit) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, account.getUserId());
            ps.setString(2, account.getAccountName());
            ps.setString(3, account.getAccountType());
            ps.setDouble(4, account.getBalance());
            ps.setDouble(5, account instanceof CheckingAccount ? ((CheckingAccount) account).getOverdraftLimit() : 0.0);
            ps.setDouble(6, account instanceof SavingsAccount ? ((SavingsAccount) account).getInterestRate() : 0.0);
            ps.setDouble(7, account instanceof CreditCardAccount ? ((CreditCardAccount) account).getCreditLimit() : 0.0);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) account.setId(keys.getLong(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Account findById(long id) {
        String sql = "SELECT * FROM account WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<Account> findByUserId(long userId) {
        String sql = "SELECT * FROM account WHERE user_id = ?";
        List<Account> accounts = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) accounts.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return accounts;
    }

    public void update(Account account) {
        String sql = "UPDATE account SET balance=?, overdraft_limit=?, interest_rate=?, credit_limit=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, account.getBalance());
            ps.setDouble(2, account instanceof CheckingAccount ? ((CheckingAccount) account).getOverdraftLimit() : 0.0);
            ps.setDouble(3, account instanceof SavingsAccount ? ((SavingsAccount) account).getInterestRate() : 0.0);
            ps.setDouble(4, account instanceof CreditCardAccount ? ((CreditCardAccount) account).getCreditLimit() : 0.0);
            ps.setLong(5, account.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(long id) {
        String sql = "DELETE FROM account WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        String type = rs.getString("account_type");
        Account account = AccountFactory.createAccount(type, rs.getLong("user_id"), rs.getString("account_name"));
        account.setId(rs.getLong("id"));
        account.setBalance(rs.getDouble("balance"));
        account.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (account instanceof CheckingAccount)
            ((CheckingAccount) account).setOverdraftLimit(rs.getDouble("overdraft_limit"));
        if (account instanceof SavingsAccount)
            ((SavingsAccount) account).setInterestRate(rs.getDouble("interest_rate"));
        if (account instanceof CreditCardAccount)
            ((CreditCardAccount) account).setCreditLimit(rs.getDouble("credit_limit"));
        return account;
    }
}
