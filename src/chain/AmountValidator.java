package chain;

import model.transaction.Transaction;

public class AmountValidator extends ValidationHandler {
    @Override
    public boolean validate(Transaction transaction) {
        if (transaction == null || transaction.getAmount() <= 0) {
            return false;
        }
        return super.validate(transaction);
    }
}
