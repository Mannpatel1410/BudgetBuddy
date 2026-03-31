package decorator;

import model.transaction.Transaction;

public class TaxDecorator extends TransactionDecorator {
    public TaxDecorator(Transaction wrapped, double taxRate) {
        super(wrapped);
        wrapped.setTaxRate(taxRate);
    }

    @Override
    public double getAmount() {
        return wrapped.getAmount() * (1 + wrapped.getTaxRate());
    }
}
