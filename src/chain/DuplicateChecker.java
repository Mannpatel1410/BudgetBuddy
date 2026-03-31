package chain;

// import db.DatabaseManager;
import model.transaction.Transaction;

public class DuplicateChecker extends ValidationHandler {
    @Override
    public boolean validate(Transaction transaction) {
        if (transaction == null) {
            return false;
        }

        // String sql = "SELECT 1 FROM `transaction` WHERE account_id=? AND category_id=? AND type=? AND amount=? AND description=? AND transaction_date=? LIMIT 1";
        // try {
        //     Connection conn = DatabaseManager.getInstance().getConnection();
        //     ...
        // } catch (SQLException e) {
        //     return false;
        // }

        return super.validate(transaction);
    }
}
