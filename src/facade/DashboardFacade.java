package facade;

import model.account.Account;
import model.budget.Budget;
import model.transaction.Transaction;
import service.AccountService;
import service.BudgetService;
import service.CategoryService;
import service.TransactionService;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class DashboardFacade {
    private AccountService accountService;
    private TransactionService transactionService;
    private BudgetService budgetService;
    private CategoryService categoryService;

    public DashboardFacade() {
        accountService = new AccountService();
        transactionService = new TransactionService();
        budgetService = new BudgetService();
        categoryService = new CategoryService();
    }

    public double getTotalBalance(long userId) {
        List<Account> accounts = accountService.getAccountsByUser(userId);
        double total = 0;
        for (Account account : accounts) {
            total += account.getBalance();
        }
        return total;
    }

    public double getTotalIncome(long userId) {
        List<Transaction> transactions = transactionService.getTransactionsByUser(userId);
        double total = 0;
        for (Transaction t : transactions) {
            if ("INCOME".equalsIgnoreCase(t.getType())) {
                total += t.getAmount();
            }
        }
        return total;
    }

    public double getTotalExpense(long userId) {
        List<Transaction> transactions = transactionService.getTransactionsByUser(userId);
        double total = 0;
        for (Transaction t : transactions) {
            if ("EXPENSE".equalsIgnoreCase(t.getType())) {
                total += t.getAmount();
            }
        }
        return total;
    }

    public double getNetSavings(long userId) {
        return getTotalIncome(userId) - getTotalExpense(userId);
    }

    public int getActiveBudgetCount(long userId) {
        LocalDate now = LocalDate.now();
        String month = now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        int year = now.getYear();
        List<Budget> budgets = budgetService.getBudgetsForMonth(userId, month, year);
        return budgets.size();
    }

    public int getOverBudgetCount(long userId) {
        LocalDate now = LocalDate.now();
        String month = now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        int year = now.getYear();
        List<Budget> budgets = budgetService.getBudgetsForMonth(userId, month, year);
        int count = 0;
        for (Budget budget : budgets) {
            if ("EXCEEDED".equals(budget.getStatus())) {
                count++;
            }
        }
        return count;
    }
}
