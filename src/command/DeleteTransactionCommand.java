package command;

import model.transaction.Transaction;
import dao.TransactionDAO;

public class DeleteTransactionCommand implements TransactionCommand {
    private Transaction transaction;
    private TransactionDAO transactionDAO;

    public DeleteTransactionCommand(Transaction transaction, TransactionDAO dao) {
        this.transaction = transaction;
        this.transactionDAO = dao;
    }

    @Override
    public void execute() {
        transactionDAO.delete(transaction.getId());
    }

    @Override
    public void undo() {
        transactionDAO.insert(transaction);
    }
}
