package factory;

import model.transaction.ExpenseTransaction;
import model.transaction.IncomeTransaction;
import model.transaction.Transaction;
import model.transaction.TransferTransaction;

import java.time.LocalDate;

public class TransactionFactory {
    public static Transaction createTransaction(String type, long accountId, long categoryId,
                                                double amount, String description, LocalDate date) {
        if (type == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }

        switch (type.trim().toUpperCase()) {
            case "INCOME":
                return new IncomeTransaction(accountId, categoryId, amount, description, date);
            case "EXPENSE":
                return new ExpenseTransaction(accountId, categoryId, amount, description, date);
            case "TRANSFER":
                return new TransferTransaction(accountId, categoryId, amount, description, date);
            default:
                throw new IllegalArgumentException("Unsupported transaction type: " + type);
        }
    }
}
