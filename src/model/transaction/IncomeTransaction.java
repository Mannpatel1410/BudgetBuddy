package model.transaction;

import java.time.LocalDate;

public class IncomeTransaction extends Transaction {
    private String source;

    public IncomeTransaction() {
        this.type = "INCOME";
    }

    public IncomeTransaction(long accountId, long categoryId, double amount, String description, LocalDate transactionDate) {
        super(accountId, categoryId, "INCOME", amount, description, transactionDate);
    }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
