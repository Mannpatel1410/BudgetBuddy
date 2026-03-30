package service;

import dao.BudgetDAO;
import model.budget.Budget;
import model.transaction.Transaction;
import observer.AlertNotifier;

import java.util.List;

public class BudgetService {
    private BudgetDAO budgetDAO = new BudgetDAO();
    private AlertNotifier alertNotifier = new AlertNotifier();

    public void createBudget(long userId, long categoryId, double limit, String month, int year) {
        Budget budget = new Budget(userId, categoryId, limit, month, year);
        budget.addObserver(alertNotifier);
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
            // Budget loaded fresh from DB so observers are empty — register once here
            budget.addObserver(alertNotifier);
            budget.addSpending(amount, transaction);
            budgetDAO.update(budget);
        }
    }

    public void deleteBudget(long id) {
        budgetDAO.delete(id);
    }
}