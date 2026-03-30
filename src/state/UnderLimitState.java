package state;

import model.budget.Budget;

public class UnderLimitState implements BudgetState {

    @Override
    public void handleTransaction(Budget budget, double amount) {
        System.out.println("[Budget OK] Transaction of $" + amount + " processed. "
                + "Remaining: $" + (budget.getLimitAmount() - budget.getSpentAmount() - amount));
    }

    @Override
    public String getStatusColor() {
        return "GREEN";
    }

    @Override
    public String getAlertMessage() {
        return "Budget is within limits. You're on track!";
    }

    @Override
    public String getStateName() {
        return "UNDER_LIMIT";
    }
}