package strategy;

import builder.ReportBuilder;
import dao.CategoryDAO;
import model.category.Category;
import model.report.Report;
import model.transaction.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class CategoryReportStrategy implements ReportStrategy {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    public Report generateReport(List<Transaction> transactions, long userId) {
        Map<Long, Double> categoryTotals = new HashMap<>();
        double totalIncome  = 0;
        double totalExpense = 0;

        for (Transaction t : transactions) {
            if ("INCOME".equalsIgnoreCase(t.getType())) {
                totalIncome += t.getAmount();
                categoryTotals.merge(t.getCategoryId(), t.getAmount(), Double::sum);
            } else if ("EXPENSE".equalsIgnoreCase(t.getType())) {
                totalExpense += t.getAmount();
                categoryTotals.merge(t.getCategoryId(), -t.getAmount(), Double::sum);
            }
        }

        // Fetch Category objects and use the Composite pattern to compute per-category net
        List<Category> categories = categoryDAO.findByUserId(userId);
        StringJoiner sj = new StringJoiner(", ");
        for (Category c : categories) {
            double net = c.getSpendingTotal(categoryTotals); // Composite: rolls up children
            if (net != 0) {
                sj.add(c.getName() + ":" + String.format("%.2f", net));
            }
        }

        return new ReportBuilder(userId)
                .setReportType("CATEGORY")
                .setPeriod(sj.toString())
                .setFormat("DEFAULT")
                .setTotalIncome(totalIncome)
                .setTotalExpense(totalExpense)
                .setCategories(categories)
                .build();
    }
}
