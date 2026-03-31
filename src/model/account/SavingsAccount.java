package model.account;

public class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount() { this.accountType = "SAVINGS"; }

    @Override
    public void deposit(double amount) {
        balance += amount;
    }

    @Override
    public void withdraw(double amount) {
        if (amount > balance) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        balance -= amount;
    }

    // Returns the interest earned for one period based on current balance
    public double calculateInterest() {
        return balance * interestRate;
    }

    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }
}
