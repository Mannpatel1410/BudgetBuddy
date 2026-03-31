package service;

import chain.AmountValidator;
import chain.BudgetLimitChecker;
import chain.CategoryValidator;
import chain.DuplicateChecker;
import chain.ValidationHandler;
import command.AddTransactionCommand;
import command.CommandHistory;
import command.DeleteTransactionCommand;
import command.EditTransactionCommand;
import dao.TransactionDAO;
import factory.TransactionFactory;
import model.transaction.Transaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final ValidationHandler validationChain;
    private final CommandHistory commandHistory = new CommandHistory();

    public TransactionService() {
        validationChain = new AmountValidator();
        validationChain
                .setNext(new CategoryValidator())
                .setNext(new BudgetLimitChecker())
                .setNext(new DuplicateChecker());
    }

    public boolean addTransaction(String type, long accountId, long categoryId,
                                  double amount, String description, LocalDate date) {
        Transaction transaction = TransactionFactory.createTransaction(
                type, accountId, categoryId, amount, description, date
        );

        if (!validationChain.validate(transaction)) {
            return false;
        }

        commandHistory.execute(new AddTransactionCommand(transactionDAO, transaction));
        return true;
    }

    public boolean editTransaction(Transaction transaction) {
        if (transaction == null || transaction.getId() <= 0) {
            return false;
        }
        if (!validationChain.validate(transaction)) {
            return false;
        }

        commandHistory.execute(new EditTransactionCommand(transactionDAO, transaction));
        return true;
    }

    public List<Transaction> getTransactionsByUser(long userId) {
        return transactionDAO.findByUserId(userId);
    }

    public List<Transaction> filterByCategory(long userId, long categoryId) {
        List<Transaction> all = transactionDAO.findByUserId(userId);
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction transaction : all) {
            if (transaction.getCategoryId() == categoryId) {
                filtered.add(transaction);
            }
        }
        return filtered;
    }

    public void deleteTransaction(long id) {
        commandHistory.execute(new DeleteTransactionCommand(transactionDAO, id));
    }

    public boolean undo() {
        return commandHistory.undo();
    }

    public boolean redo() {
        return commandHistory.redo();
    }
}
