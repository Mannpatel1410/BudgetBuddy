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

/**
 * Aggregates all received transactions (date filtering is done by ReportService
 * before calling this strategy). The period label is also overridden by the service.
 */
public class MonthlyReportStrategy implements ReportStrategy {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    public Report generateReport(List<Transaction> transactions, long userId) {
        double totalIncome  = 0;
        double totalExpense = 0;

        for (Transaction t : transactions) {
            if ("INCOME".equalsIgnoreCase(t.getType())) {
                totalIncome += t.getAmount();
            } else if ("EXPENSE".equalsIgnoreCase(t.getType())) {
                totalExpense += t.getAmount();
            }
        }

        Map<Long, String> catNames = new HashMap<>();
        for (Category c : categoryDAO.findByUserId(userId)) {
            catNames.put(c.getId(), c.getName());
        }

        return new ReportBuilder(userId)
                .setReportType("MONTHLY")
                .setPeriod("")          // service will override with selected month label
                .setFormat("PDF")
                .setTotalIncome(totalIncome)
                .setTotalExpense(totalExpense)
                .setTransactions(new ArrayList<>(transactions))
                .setCategoryNames(catNames)
                .build();
    }
}
