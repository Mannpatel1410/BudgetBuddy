package decorator;

import model.transaction.Transaction;

public class TagDecorator extends TransactionDecorator {
    public TagDecorator(Transaction wrapped, String tag) {
        super(wrapped);
        String existing = wrapped.getTags() == null ? "" : wrapped.getTags().trim();
        if (existing.isEmpty()) {
            wrapped.setTags(tag);
        } else {
            wrapped.setTags(existing + "," + tag);
        }
    }
}
