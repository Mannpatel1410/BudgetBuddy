package command;

import model.transaction.Transaction;
import dao.TransactionDAO;

public class AddTransactionCommand implements TransactionCommand {
    private Transaction transaction;
    private TransactionDAO transactionDAO;

    public AddTransactionCommand(Transaction transaction, TransactionDAO dao) {
        this.transaction = transaction;
        this.transactionDAO = dao;
    }

    @Override
    public void execute() {
        transactionDAO.insert(transaction);
    }

    @Override
    public void undo() {
        transactionDAO.delete(transaction.getId());
    }
}
