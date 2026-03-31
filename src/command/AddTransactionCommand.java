package command;

import dao.TransactionDAO;
import model.transaction.Transaction;

public class AddTransactionCommand implements TransactionCommand {
    private final TransactionDAO transactionDAO;
    private final Transaction transaction;

    public AddTransactionCommand(TransactionDAO transactionDAO, Transaction transaction) {
        this.transactionDAO = transactionDAO;
        this.transaction = transaction;
    }

    @Override
    public void execute() {
        transactionDAO.insert(transaction);
    }

    @Override
    public void undo() {
        if (transaction.getId() > 0) {
            transactionDAO.delete(transaction.getId());
        }
    }
}
