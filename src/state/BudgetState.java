package state;

import model.budget.Budget;

public interface BudgetState {
    void handleTransaction(Budget budget, double amount);
    String getStatusColor();
    String getAlertMessage();
    String getStateName();
}