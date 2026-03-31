package chain;

import model.transaction.Transaction;

public abstract class ValidationHandler {
    protected ValidationHandler nextHandler;

    public ValidationHandler setNext(ValidationHandler handler) {
        this.nextHandler = handler;
        return handler;
    }

    public boolean validate(Transaction transaction) {
        if (nextHandler != null) {
            return nextHandler.validate(transaction);
        }
        return true;
    }
}
