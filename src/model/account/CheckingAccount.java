package model.account;

public class CheckingAccount extends Account {
    private double overdraftLimit;

    public CheckingAccount() { this.accountType = "CHECKING"; }

    @Override
    public void deposit(double amount) {
        balance += amount;
    }

    @Override
    public void withdraw(double amount) {
        if (balance - amount < -overdraftLimit) {
            throw new IllegalArgumentException("Withdrawal exceeds overdraft limit");
        }
        balance -= amount;
    }

    public double getOverdraftLimit() { return overdraftLimit; }
    public void setOverdraftLimit(double overdraftLimit) { this.overdraftLimit = overdraftLimit; }
}
