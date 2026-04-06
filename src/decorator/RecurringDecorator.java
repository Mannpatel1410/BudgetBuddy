package decorator;

import model.transaction.Transaction;

public class RecurringDecorator extends TransactionDecorator {
    private String frequency;

    public RecurringDecorator(Transaction transaction, String frequency) {
        super(transaction);
        this.frequency = frequency;
        wrappedTransaction.setRecurring(true);
    }

    @Override
    public String getDescription() {
        return wrappedTransaction.getDescription() + " [Recurring: " + frequency + "]";
    }

    @Override
    public boolean isRecurring() {
        return true;
    }

    public String getFrequency() {
        return frequency;
    }
}
