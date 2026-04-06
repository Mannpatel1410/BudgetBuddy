package decorator;

import model.transaction.Transaction;

public class TaxDecorator extends TransactionDecorator {
    private double taxRate;

    public TaxDecorator(Transaction transaction, double taxRate) {
        super(transaction);
        this.taxRate = taxRate;
    }

    @Override
    public double getAmount() {
        return wrappedTransaction.getAmount() + (wrappedTransaction.getAmount() * taxRate / 100.0);
    }

    @Override
    public String getDescription() {
        return wrappedTransaction.getDescription() + " [Tax: " + taxRate + "%]";
    }

    @Override
    public double getTaxRate() {
        return taxRate;
    }
}
