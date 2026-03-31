package model.transaction;

import java.time.LocalDate;

public class TransferTransaction extends Transaction {
    private long toAccountId;

    public TransferTransaction() {
        this.type = "TRANSFER";
    }

    public TransferTransaction(long accountId, long categoryId, double amount, String description, LocalDate transactionDate) {
        super(accountId, categoryId, "TRANSFER", amount, description, transactionDate);
    }

    public long getToAccountId() { return toAccountId; }
    public void setToAccountId(long toAccountId) { this.toAccountId = toAccountId; }
}
