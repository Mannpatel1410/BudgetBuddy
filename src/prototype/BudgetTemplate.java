package prototype;

import model.budget.Budget;
import dao.BudgetDAO;
import observer.AlertNotifier;

import java.util.ArrayList;
import java.util.List;

public class BudgetTemplate {
    private BudgetDAO budgetDAO = new BudgetDAO();

    public List<Budget> cloneFromPreviousMonth(long userId, String prevMonth, int prevYear,
                                                String newMonth, int newYear) {
        List<Budget> previousBudgets = budgetDAO.findByUserIdAndMonth(userId, prevMonth, prevYear);
        List<Budget> clonedBudgets = new ArrayList<>();

        for (Budget original : previousBudgets) {
            Budget cloned = original.clone();
            cloned.setUserId(userId);
            cloned.setMonth(newMonth);
            cloned.setYear(newYear);
            cloned.addObserver(new AlertNotifier());
            budgetDAO.insert(cloned);
            clonedBudgets.add(cloned);
        }

        return clonedBudgets;
    }
}