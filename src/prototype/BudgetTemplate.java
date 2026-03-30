package prototype;

import model.budget.Budget;
import java.util.ArrayList;
import java.util.List;

public class BudgetTemplate {

    public List<Budget> cloneFromPreviousMonth(List<Budget> previousBudgets, String newMonth, int newYear) {
        List<Budget> cloned = new ArrayList<>();
        for (Budget b : previousBudgets) {
            Budget copy = b.clone();
            copy.setMonth(newMonth);
            copy.setYear(newYear);
            cloned.add(copy);
        }
        return cloned;
    }
}