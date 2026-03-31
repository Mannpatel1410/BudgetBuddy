package command;

import dao.TransactionDAO;
import model.transaction.Transaction;

public class DeleteTransactionCommand implements TransactionCommand {
    private final TransactionDAO transactionDAO;
    private final long transactionId;
    private Transaction deletedBackup;

    public DeleteTransactionCommand(TransactionDAO transactionDAO, long transactionId) {
        this.transactionDAO = transactionDAO;
        this.transactionId = transactionId;
    }

    @Override
    public void execute() {
        deletedBackup = transactionDAO.findById(transactionId);
        transactionDAO.delete(transactionId);
    }

    @Override
    public void undo() {
        if (deletedBackup != null) {
            transactionDAO.insert(deletedBackup);
        }
    }
}
