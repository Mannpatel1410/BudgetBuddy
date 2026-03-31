package decorator;

import model.transaction.Transaction;

public class RecurringDecorator extends TransactionDecorator {
    public RecurringDecorator(Transaction wrapped) {
        super(wrapped);
        wrapped.setRecurring(true);
    }
}
