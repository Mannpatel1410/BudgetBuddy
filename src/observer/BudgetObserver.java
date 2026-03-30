package observer;

import model.budget.Budget;
import model.transaction.Transaction;

public interface BudgetObserver {
    void update(Budget budget, Transaction transaction);
}