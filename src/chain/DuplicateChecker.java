package chain;

import dao.TransactionDAO;
import model.transaction.Transaction;

import java.util.List;
import java.util.Objects;

public class DuplicateChecker extends ValidationHandler {
    private final TransactionDAO transactionDAO = new TransactionDAO();

    @Override
    public boolean validate(Transaction transaction) {
        if (transaction == null) {
            return false;
        }

        List<Transaction> existing = transactionDAO.findByAccountId(transaction.getAccountId());
        for (Transaction t : existing) {
            if (t.getId() == transaction.getId()) {
                continue;
            }
            boolean sameAmount = Double.compare(t.getAmount(), transaction.getAmount()) == 0;
            boolean sameDate = Objects.equals(t.getTransactionDate(), transaction.getTransactionDate());
            boolean sameDescription = Objects.equals(normalize(t.getDescription()), normalize(transaction.getDescription()));
            if (sameAmount && sameDate && sameDescription) {
                return false;
            }
        }

        return super.validate(transaction);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
