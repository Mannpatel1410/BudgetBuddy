package decorator;

import model.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TagDecorator extends TransactionDecorator {
    private List<String> tags;

    public TagDecorator(Transaction transaction, List<String> tags) {
        super(transaction);
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }

    @Override
    public String getDescription() {
        return wrappedTransaction.getDescription() + " [Tags: " + String.join(", ", tags) + "]";
    }

    @Override
    public String getTags() {
        return String.join(",", tags);
    }
}
