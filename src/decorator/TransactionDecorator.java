package decorator;

import model.transaction.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class TransactionDecorator extends Transaction {
    protected Transaction wrappedTransaction;

    public TransactionDecorator(Transaction transaction) {
        this.wrappedTransaction = transaction;
        this.id = transaction.getId();
        this.accountId = transaction.getAccountId();
        this.categoryId = transaction.getCategoryId();
        this.type = transaction.getType();
        this.amount = transaction.getAmount();
        this.description = transaction.getDescription();
        this.isRecurring = transaction.isRecurring();
        this.tags = transaction.getTags();
        this.taxRate = transaction.getTaxRate();
        this.transactionDate = transaction.getTransactionDate();
        this.createdAt = transaction.getCreatedAt();
    }

    @Override
    public long getId() { return wrappedTransaction.getId(); }

    @Override
    public void setId(long id) { wrappedTransaction.setId(id); }

    @Override
    public long getAccountId() { return wrappedTransaction.getAccountId(); }

    @Override
    public void setAccountId(long accountId) { wrappedTransaction.setAccountId(accountId); }

    @Override
    public long getCategoryId() { return wrappedTransaction.getCategoryId(); }

    @Override
    public void setCategoryId(long categoryId) { wrappedTransaction.setCategoryId(categoryId); }

    @Override
    public String getType() { return wrappedTransaction.getType(); }

    @Override
    public void setType(String type) { wrappedTransaction.setType(type); }

    @Override
    public double getAmount() { return wrappedTransaction.getAmount(); }

    @Override
    public void setAmount(double amount) { wrappedTransaction.setAmount(amount); }

    @Override
    public String getDescription() { return wrappedTransaction.getDescription(); }

    @Override
    public void setDescription(String description) { wrappedTransaction.setDescription(description); }

    @Override
    public boolean isRecurring() { return wrappedTransaction.isRecurring(); }

    @Override
    public void setRecurring(boolean recurring) { wrappedTransaction.setRecurring(recurring); }

    @Override
    public String getTags() { return wrappedTransaction.getTags(); }

    @Override
    public void setTags(String tags) { wrappedTransaction.setTags(tags); }

    @Override
    public double getTaxRate() { return wrappedTransaction.getTaxRate(); }

    @Override
    public void setTaxRate(double taxRate) { wrappedTransaction.setTaxRate(taxRate); }

    @Override
    public LocalDate getTransactionDate() { return wrappedTransaction.getTransactionDate(); }

    @Override
    public void setTransactionDate(LocalDate transactionDate) { wrappedTransaction.setTransactionDate(transactionDate); }

    @Override
    public LocalDateTime getCreatedAt() { return wrappedTransaction.getCreatedAt(); }

    @Override
    public void setCreatedAt(LocalDateTime createdAt) { wrappedTransaction.setCreatedAt(createdAt); }
}
