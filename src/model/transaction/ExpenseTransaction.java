package model.transaction;

import java.time.LocalDate;

public class ExpenseTransaction extends Transaction {
    private String vendor;

    public ExpenseTransaction() {
        this.type = "EXPENSE";
    }

    public ExpenseTransaction(long accountId, long categoryId, double amount, String description, LocalDate transactionDate) {
        super(accountId, categoryId, "EXPENSE", amount, description, transactionDate);
    }

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
}
