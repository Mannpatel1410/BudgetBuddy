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
    protected LocalDate transactionDate;
    protected LocalDateTime createdAt;

    public Transaction() {}

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
    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate d) { this.transactionDate = d; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }
}