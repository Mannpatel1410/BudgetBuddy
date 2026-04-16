package prototype;

import model.budget.Budget;
import dao.BudgetDAO;
import observer.AlertNotifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BudgetTemplate {
    private BudgetDAO budgetDAO = new BudgetDAO();

    public List<Budget> cloneFromPreviousMonth(long userId, String prevMonth, int prevYear,
                                                String newMonth, int newYear) {
        List<Budget> previousBudgets = budgetDAO.findByUserIdAndMonth(userId, prevMonth, prevYear);
        List<Budget> clonedBudgets = new ArrayList<>();

        // Collect category IDs that already have a budget in the target month
        // to avoid inserting duplicates
        List<Budget> existing = budgetDAO.findByUserIdAndMonth(userId, newMonth, newYear);
        Set<Long> existingCategoryIds = existing.stream()
                .map(Budget::getCategoryId)
                .collect(Collectors.toSet());

        for (Budget original : previousBudgets) {
            if (existingCategoryIds.contains(original.getCategoryId())) {
                continue; // skip — budget for this category already exists this month
            }
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
