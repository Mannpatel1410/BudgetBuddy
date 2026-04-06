package chain;

import model.budget.Budget;
import model.transaction.Transaction;
import service.BudgetService;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class BudgetLimitChecker extends ValidationHandler {
    private final BudgetService budgetService = new BudgetService();
    private final dao.TransactionDAO transactionDAO = new dao.TransactionDAO();

    @Override
    public boolean validate(Transaction transaction) {
        if (transaction == null) {
            return false;
        }

        if (!"EXPENSE".equalsIgnoreCase(transaction.getType())) {
            return super.validate(transaction);
        }

        long userId = transactionDAO.findUserIdByAccountId(transaction.getAccountId());
        if (userId <= 0) {
            return super.validate(transaction);
        }

        LocalDate txDate = transaction.getTransactionDate() != null ? transaction.getTransactionDate() : LocalDate.now();
        String month = txDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        int year = txDate.getYear();

        List<Budget> budgets = budgetService.getBudgetsForMonth(userId, month, year);
        for (Budget budget : budgets) {
            if (budget.getCategoryId() == transaction.getCategoryId()) {
                double projected = budget.getSpentAmount() + transaction.getAmount();
                if (projected > budget.getLimitAmount()) {
                    return false;
                }
                break;
            }
        }

        return super.validate(transaction);
    }
}
