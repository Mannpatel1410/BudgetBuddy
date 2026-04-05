package service;

import dao.BudgetDAO;
import model.budget.Budget;
import model.transaction.Transaction;
import observer.AlertNotifier;
import observer.DashboardUpdater;
import prototype.BudgetTemplate;

import java.util.List;

public class BudgetService {
    private BudgetDAO budgetDAO = new BudgetDAO();
    private AlertNotifier alertNotifier = new AlertNotifier();
    private DashboardUpdater dashboardUpdater;
    private BudgetTemplate budgetTemplate = new BudgetTemplate();

    public BudgetService() {}

    public void setDashboardUpdater(DashboardUpdater updater) {
        this.dashboardUpdater = updater;
    }

    public void createBudget(long userId, long categoryId, double limit, String month, int year) {
        Budget budget = new Budget(userId, categoryId, limit, month, year);
        budget.addObserver(alertNotifier);
        if (dashboardUpdater != null) {
            budget.addObserver(dashboardUpdater);
        }
        budgetDAO.insert(budget);
    }

    public List<Budget> getBudgetsForMonth(long userId, String month, int year) {
        return budgetDAO.findByUserIdAndMonth(userId, month, year);
    }

    public List<Budget> getAllBudgets(long userId) {
        return budgetDAO.findByUserId(userId);
    }

    public void recordSpending(long budgetId, double amount, Transaction transaction) {
        Budget budget = budgetDAO.findById(budgetId);
        if (budget != null) {
            budget.addObserver(alertNotifier);
            if (dashboardUpdater != null) {
                budget.addObserver(dashboardUpdater);
            }
            budget.addSpending(amount, transaction);
            budgetDAO.update(budget);
        }
    }

    public List<Budget> cloneFromPreviousMonth(long userId, String prevMonth, int prevYear,
                                                String newMonth, int newYear) {
        return budgetTemplate.cloneFromPreviousMonth(userId, prevMonth, prevYear, newMonth, newYear);
    }

    public void deleteBudget(long id) {
        budgetDAO.delete(id);
    }
}

