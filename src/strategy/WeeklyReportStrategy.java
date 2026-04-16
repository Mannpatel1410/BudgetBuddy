package strategy;

import builder.ReportBuilder;
import dao.CategoryDAO;
import model.category.Category;
import model.report.Report;
import model.transaction.Transaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Filters transactions to the current calendar week (Monday → today) regardless
 * of what the service pre-filtered.  The service calls clearPeriod() before
 * invoking this strategy so that all user transactions arrive here unfiltered.
 * The period label is set to the actual date range ("2026-04-13 to 2026-04-15").
 */
public class WeeklyReportStrategy implements ReportStrategy {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    public Report generateReport(List<Transaction> transactions, long userId) {
        LocalDate today     = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1); // Monday

        double totalIncome  = 0;
        double totalExpense = 0;
        List<Transaction> filtered = new ArrayList<>();

        for (Transaction t : transactions) {
            if (t.getTransactionDate() == null) continue;
            LocalDate d = t.getTransactionDate();
            if (d.isBefore(weekStart) || d.isAfter(today)) continue;

            filtered.add(t);
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
                .setReportType("WEEKLY")
                .setDateRange(weekStart, today)   // period = "2026-04-13 to 2026-04-15"
                .setFormat("PDF")
                .setTotalIncome(totalIncome)
                .setTotalExpense(totalExpense)
                .setTransactions(filtered)
                .setCategoryNames(catNames)
                .build();
    }
}
