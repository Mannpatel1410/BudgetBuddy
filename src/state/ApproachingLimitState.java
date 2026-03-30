package state;


import model.budget.Budget;

public class ApproachingLimitState implements BudgetState {

    @Override
    public void handleTransaction(Budget budget, double amount) {
        double pct = budget.getPercentageUsed();
        System.out.println("[Budget WARNING] You've used " + String.format("%.1f", pct)
                + "% of your budget. Remaining: $" + budget.getRemaining());
    }

    @Override
    public String getStatusColor() {
        return "YELLOW";
    }

    @Override
    public String getAlertMessage() {
        return "Warning: You have used over 80% of your budget!";
    }

    @Override
    public String getStateName() {
        return "APPROACHING_LIMIT";
    }
}