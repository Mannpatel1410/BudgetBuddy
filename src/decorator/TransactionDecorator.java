package decorator;

import model.transaction.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class TransactionDecorator extends Transaction {
    protected final Transaction wrapped;

    public TransactionDecorator(Transaction wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public long getId() { return wrapped.getId(); }
    @Override
    public void setId(long id) { wrapped.setId(id); }
    @Override
    public long getAccountId() { return wrapped.getAccountId(); }
    @Override
    public void setAccountId(long accountId) { wrapped.setAccountId(accountId); }
    @Override
    public long getCategoryId() { return wrapped.getCategoryId(); }
    @Override
    public void setCategoryId(long categoryId) { wrapped.setCategoryId(categoryId); }
    @Override
    public String getType() { return wrapped.getType(); }
    @Override
    public void setType(String type) { wrapped.setType(type); }
    @Override
    public double getAmount() { return wrapped.getAmount(); }
    @Override
    public void setAmount(double amount) { wrapped.setAmount(amount); }
    @Override
    public String getDescription() { return wrapped.getDescription(); }
    @Override
    public void setDescription(String description) { wrapped.setDescription(description); }
    @Override
    public boolean isRecurring() { return wrapped.isRecurring(); }
    @Override
    public void setRecurring(boolean recurring) { wrapped.setRecurring(recurring); }
    @Override
    public String getTags() { return wrapped.getTags(); }
    @Override
    public void setTags(String tags) { wrapped.setTags(tags); }
    @Override
    public double getTaxRate() { return wrapped.getTaxRate(); }
    @Override
    public void setTaxRate(double taxRate) { wrapped.setTaxRate(taxRate); }
    @Override
    public LocalDate getTransactionDate() { return wrapped.getTransactionDate(); }
    @Override
    public void setTransactionDate(LocalDate transactionDate) { wrapped.setTransactionDate(transactionDate); }
    @Override
    public LocalDateTime getCreatedAt() { return wrapped.getCreatedAt(); }
    @Override
    public void setCreatedAt(LocalDateTime createdAt) { wrapped.setCreatedAt(createdAt); }
}
