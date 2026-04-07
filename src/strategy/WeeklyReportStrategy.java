package strategy;

import builder.ReportBuilder;
import model.report.Report;
import model.transaction.Transaction;

import java.time.LocalDate;
import java.util.List;

public class WeeklyReportStrategy implements ReportStrategy {

    @Override
    public Report generateReport(List<Transaction> transactions, long userId) {
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1);

        double totalIncome  = 0;
        double totalExpense = 0;

        for (Transaction t : transactions) {
            if (t.getTransactionDate() == null) continue;
            LocalDate txDate = t.getTransactionDate();
            if (!txDate.isBefore(weekStart) && !txDate.isAfter(now)) {
                if ("INCOME".equalsIgnoreCase(t.getType())) {
                    totalIncome += t.getAmount();
                } else if ("EXPENSE".equalsIgnoreCase(t.getType())) {
                    totalExpense += t.getAmount();
                }
            }
        }

        return new ReportBuilder(userId)
                .setReportType("WEEKLY")
                .setDateRange(weekStart, now)   // builder derives "weekStart to now" period string
                .setFormat("DEFAULT")
                .setTotalIncome(totalIncome)
                .setTotalExpense(totalExpense)
                .build();
    }
}
