package command;

import dao.TransactionDAO;
import model.transaction.Transaction;

public class EditTransactionCommand implements TransactionCommand {
    private final TransactionDAO transactionDAO;
    private final Transaction updated;
    private Transaction previous;

    public EditTransactionCommand(TransactionDAO transactionDAO, Transaction updated) {
        this.transactionDAO = transactionDAO;
        this.updated = updated;
    }

    @Override
    public void execute() {
        previous = transactionDAO.findById(updated.getId());
        transactionDAO.update(updated);
    }

    @Override
    public void undo() {
        if (previous != null) {
            transactionDAO.update(previous);
        }
    }
}
