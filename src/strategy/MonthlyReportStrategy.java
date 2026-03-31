package strategy;

import model.report.Report;
import model.transaction.Transaction;
import java.time.LocalDateTime;
import java.util.List;

public class MonthlyReportStrategy implements ReportStrategy {

    @Override
    public Report generateReport(List<Transaction> transactions, long userId) {
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction t : transactions) {
            if ("INCOME".equalsIgnoreCase(t.getType())) {
                totalIncome += t.getAmount();
            } else if ("EXPENSE".equalsIgnoreCase(t.getType())) {
                totalExpense += t.getAmount();
            }
        }

        double netSavings = totalIncome - totalExpense;

        Report report = new Report();
        report.setUserId(userId);
        report.setReportType("MONTHLY");
        report.setPeriod("MONTHLY");
        report.setFormat("DEFAULT");
        report.setTotalIncome(totalIncome);
        report.setTotalExpense(totalExpense);
        report.setNetSavings(netSavings);
        report.setGeneratedAt(LocalDateTime.now());
        return report;
    }
}
