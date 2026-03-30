package observer;

import model.budget.Budget;
import model.transaction.Transaction;

public class DashboardUpdater implements BudgetObserver {

    @Override
    public void update(Budget budget, Transaction transaction) {
        System.out.println("[Dashboard] Budget updated for category " + budget.getCategoryId());
    }
}