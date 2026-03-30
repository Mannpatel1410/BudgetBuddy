package state;

import model.budget.Budget;

public class ExceededState implements BudgetState {

    @Override
    public void handleTransaction(Budget budget, double amount) {
        System.out.println("[Budget EXCEEDED] You are $"
                + String.format("%.2f", Math.abs(budget.getRemaining()))
                + " over your budget limit!");
    }

    @Override
    public String getStatusColor() {
        return "RED";
    }

    @Override
    public String getAlertMessage() {
        return "Alert: You have exceeded your budget!";
    }

    @Override
    public String getStateName() {
        return "EXCEEDED";
    }
}