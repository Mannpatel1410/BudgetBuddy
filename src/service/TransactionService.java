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
import model.budget.Budget;
import model.transaction.Transaction;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionService {
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final BudgetService budgetService = new BudgetService();
    private final ValidationHandler validationChain;
    private CommandHistory commandHistory = new CommandHistory();

    public TransactionService() {
        validationChain = new AmountValidator();
        validationChain
                .setNext(new CategoryValidator())
                .setNext(new BudgetLimitChecker())
                .setNext(new DuplicateChecker());
    }

    public boolean addTransaction(String type, long accountId, long categoryId,
                                  double amount, String description, LocalDate date) {
        Transaction t = TransactionFactory.createTransaction(type, accountId, categoryId, amount, description, date);
        return addTransaction(t);
    }

    public boolean addTransaction(Transaction transaction) {
        if (!validationChain.validate(transaction)) {
            return false;
        }

        AddTransactionCommand cmd = new AddTransactionCommand(transaction, transactionDAO);
        commandHistory.executeCommand(cmd);

        recordBudgetSpending(transaction);
        return true;
    }

    public boolean editTransaction(Transaction transaction) {
        if (transaction == null || transaction.getId() <= 0) {
            return false;
        }
        if (!validationChain.validate(transaction)) {
            return false;
        }

        Transaction oldState = transactionDAO.findById(transaction.getId());
        if (oldState == null) {
            return false;
        }

        EditTransactionCommand cmd = new EditTransactionCommand(oldState, transaction, transactionDAO);
        commandHistory.executeCommand(cmd);
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
        Transaction existing = transactionDAO.findById(id);
        if (existing == null) {
            return;
        }
        DeleteTransactionCommand cmd = new DeleteTransactionCommand(existing, transactionDAO);
        commandHistory.executeCommand(cmd);
    }

    public void undo() {
        commandHistory.undo();
    }

    public void redo() {
        commandHistory.redo();
    }

    public boolean canUndo() {
        return commandHistory.canUndo();
    }

    public boolean canRedo() {
        return commandHistory.canRedo();
    }

    private void recordBudgetSpending(Transaction transaction) {
        if (!"EXPENSE".equalsIgnoreCase(transaction.getType())) {
            return;
        }

        long userId = transactionDAO.findUserIdByAccountId(transaction.getAccountId());
        if (userId <= 0) {
            return;
        }

        LocalDate txDate = transaction.getTransactionDate() != null ? transaction.getTransactionDate() : LocalDate.now();
        String month = txDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        int year = txDate.getYear();

        List<Budget> budgets = budgetService.getBudgetsForMonth(userId, month, year);
        for (Budget budget : budgets) {
            if (budget.getCategoryId() == transaction.getCategoryId()) {
                budgetService.recordSpending(budget.getId(), transaction.getAmount(), transaction);
                break;
            }
        }
    }
}
