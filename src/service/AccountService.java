package service;

import dao.AccountDAO;
import factory.AccountFactory;
import model.account.Account;
import java.util.List;

public class AccountService {
    private AccountDAO accountDAO = new AccountDAO();

    public Account createAccount(String type, long userId, String name) {
        Account account = AccountFactory.createAccount(type, userId, name);
        accountDAO.insert(account);
        return account;
    }

    public List<Account> getAccountsByUser(long userId) {
        return accountDAO.findByUserId(userId);
    }

    public void deposit(long accountId, double amount) {
        Account account = accountDAO.findById(accountId);
        if (account == null) throw new IllegalArgumentException("Account not found: " + accountId);
        account.deposit(amount);
        accountDAO.update(account);
    }

    public void withdraw(long accountId, double amount) {
        Account account = accountDAO.findById(accountId);
        if (account == null) throw new IllegalArgumentException("Account not found: " + accountId);
        account.withdraw(amount);
        accountDAO.update(account);
    }
}
