package observer;

import model.budget.Budget;
import model.transaction.Transaction;

import javax.swing.SwingUtilities;

public class DashboardUpdater implements BudgetObserver {
    private Runnable budgetRefreshCallback;
    private Runnable dashboardRefreshCallback;

    public DashboardUpdater() {}

    public DashboardUpdater(Runnable budgetRefresh, Runnable dashboardRefresh) {
        this.budgetRefreshCallback = budgetRefresh;
        this.dashboardRefreshCallback = dashboardRefresh;
    }

    public void setBudgetRefreshCallback(Runnable callback) {
        this.budgetRefreshCallback = callback;
    }

    public void setDashboardRefreshCallback(Runnable callback) {
        this.dashboardRefreshCallback = callback;
    }

    @Override
    public void update(Budget budget, Transaction transaction) {
        SwingUtilities.invokeLater(() -> {
            if (budgetRefreshCallback != null) {
                budgetRefreshCallback.run();
            }
            if (dashboardRefreshCallback != null) {
                dashboardRefreshCallback.run();
            }
        });
        System.out.println("[Dashboard] Refreshed — budget updated for category " + budget.getCategoryId());
    }
}