package command;

import model.transaction.Transaction;
import dao.TransactionDAO;

public class EditTransactionCommand implements TransactionCommand {
    private Transaction oldState;
    private Transaction newState;
    private TransactionDAO transactionDAO;

    public EditTransactionCommand(Transaction oldState, Transaction newState, TransactionDAO dao) {
        this.oldState = oldState;
        this.newState = newState;
        this.transactionDAO = dao;
    }

    @Override
    public void execute() {
        transactionDAO.update(newState);
    }

    @Override
    public void undo() {
        transactionDAO.update(oldState);
    }
}
