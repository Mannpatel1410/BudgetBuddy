package chain;

// import db.DatabaseManager;
import model.transaction.Transaction;

public class BudgetLimitChecker extends ValidationHandler {
    @Override
    public boolean validate(Transaction transaction) {
        if (transaction == null) {
            return false;
        }

        // String budgetSql = "SELECT limit_amount FROM budget WHERE category_id = ? LIMIT 1";
        // String spentSql = "SELECT COALESCE(SUM(amount), 0) FROM `transaction` WHERE category_id = ?";
        // try {
        //     Connection conn = DatabaseManager.getInstance().getConnection();
        //     ...
        // } catch (SQLException e) {
        //     return false;
        // }

        return super.validate(transaction);
    }
}
