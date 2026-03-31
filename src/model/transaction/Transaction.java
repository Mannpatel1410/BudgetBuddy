package model.transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Transaction {
    protected long id;
    protected long accountId;
    protected long categoryId;
    protected String type;
    protected double amount;
    protected String description;
    protected boolean isRecurring;
    protected String tags;
    protected double taxRate;
    protected LocalDate transactionDate;
    protected LocalDateTime createdAt;

    public Transaction() {}

    public Transaction(long accountId, long categoryId, String type, double amount,
                       String description, LocalDate transactionDate) {
        this.accountId = accountId;
        this.categoryId = categoryId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
        this.createdAt = LocalDateTime.now();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getAccountId() { return accountId; }
    public void setAccountId(long id) { this.accountId = id; }
    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long id) { this.categoryId = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String desc) { this.description = desc; }
    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean r) { isRecurring = r; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double rate) { this.taxRate = rate; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate d) { this.transactionDate = d; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }
}