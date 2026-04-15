package strategy;

import builder.ReportBuilder;
import dao.CategoryDAO;
import model.category.Category;
import model.report.Report;
import model.transaction.Transaction;

import java.util.ArrayList;
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
        List<Transaction> included = new ArrayList<>(transactions);

        for (Transaction t : transactions) {
            if ("INCOME".equalsIgnoreCase(t.getType())) {
                totalIncome += t.getAmount();
                categoryTotals.merge(t.getCategoryId(), t.getAmount(), Double::sum);
            } else if ("EXPENSE".equalsIgnoreCase(t.getType())) {
                totalExpense += t.getAmount();
                categoryTotals.merge(t.getCategoryId(), -t.getAmount(), Double::sum);
            }
        }

        List<Category> categories = categoryDAO.findByUserId(userId);
        Map<Long, String> catNames = new HashMap<>();
        for (Category c : categories) {
            catNames.put(c.getId(), c.getName());
        }

        StringJoiner sj = new StringJoiner(", ");
        for (Category c : categories) {
            double net = c.getSpendingTotal(categoryTotals);
            if (net != 0) {
                sj.add(c.getName() + ":" + String.format("%.2f", net));
            }
        }

        return new ReportBuilder(userId)
                .setReportType("CATEGORY")
                .setPeriod(sj.toString())
                .setFormat("PDF")
                .setTotalIncome(totalIncome)
                .setTotalExpense(totalExpense)
                .setCategories(categories)
                .setTransactions(included)
                .setCategoryNames(catNames)
                .build();
    }
}
