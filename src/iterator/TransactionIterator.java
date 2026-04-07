package iterator;

import model.transaction.Transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class TransactionIterator implements Iterator<Transaction> {

    private final List<Transaction> filtered;
    private int position = 0;

    public TransactionIterator(List<Transaction> transactions, Predicate<Transaction> filter) {
        this.filtered = new ArrayList<>();
        for (Transaction t : transactions) {
            if (filter.test(t)) {
                filtered.add(t);
            }
        }
    }

    @Override
    public boolean hasNext() {
        return position < filtered.size();
    }

    @Override
    public Transaction next() {
        return filtered.get(position++);
    }

    // ── Convenience factory methods for common predicates ──────────────────

    public static Predicate<Transaction> byCategory(long categoryId) {
        return t -> t.getCategoryId() == categoryId;
    }

    public static Predicate<Transaction> byType(String type) {
        return t -> type.equalsIgnoreCase(t.getType());
    }

    public static Predicate<Transaction> byAmountGreaterThan(double amount) {
        return t -> t.getAmount() > amount;
    }
}
