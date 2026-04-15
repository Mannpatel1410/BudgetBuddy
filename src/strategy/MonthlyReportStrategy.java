package strategy;

import builder.ReportBuilder;
import dao.CategoryDAO;
import model.category.Category;
import model.report.Report;
import model.transaction.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonthlyReportStrategy implements ReportStrategy {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    public Report generateReport(List<Transaction> transactions, long userId) {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);

        double totalIncome  = 0;
        double totalExpense = 0;
        List<Transaction> filtered = new ArrayList<>();

        for (Transaction t : transactions) {
            if (t.getTransactionDate() == null) continue;
            LocalDate txDate = t.getTransactionDate();
            if (!txDate.isBefore(monthStart) && !txDate.isAfter(now)) {
                filtered.add(t);
                if ("INCOME".equalsIgnoreCase(t.getType())) {
                    totalIncome += t.getAmount();
                } else if ("EXPENSE".equalsIgnoreCase(t.getType())) {
                    totalExpense += t.getAmount();
                }
            }
        }

        Map<Long, String> catNames = new HashMap<>();
        for (Category c : categoryDAO.findByUserId(userId)) {
            catNames.put(c.getId(), c.getName());
        }

        String period = now.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        return new ReportBuilder(userId)
                .setReportType("MONTHLY")
                .setPeriod(period)
                .setFormat("PDF")
                .setTotalIncome(totalIncome)
                .setTotalExpense(totalExpense)
                .setTransactions(filtered)
                .setCategoryNames(catNames)
                .build();
    }
}
