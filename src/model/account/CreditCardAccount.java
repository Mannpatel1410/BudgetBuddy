package model.account;

public class CreditCardAccount extends Account {
    private double creditLimit;
    private double apr;

    public CreditCardAccount() { this.accountType = "CREDIT_CARD"; }

    @Override
    public void deposit(double amount) {
        // A deposit on a credit card is a payment — reduces amount owed
        balance -= amount;
    }

    @Override
    public void withdraw(double amount) {
        if (balance + amount > creditLimit) {
            throw new IllegalArgumentException("Transaction exceeds credit limit");
        }
        balance += amount;
    }

    // Minimum payment: 2% of balance or $25, whichever is greater
    public double calculateMinPayment() {
        if (balance <= 0) return 0;
        return Math.max(balance * 0.02, 25.0);
    }

    public double getCreditLimit() { return creditLimit; }
    public void setCreditLimit(double creditLimit) { this.creditLimit = creditLimit; }

    public double getApr() { return apr; }
    public void setApr(double apr) { this.apr = apr; }
}
