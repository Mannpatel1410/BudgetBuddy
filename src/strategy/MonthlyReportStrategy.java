package strategy;

import builder.ReportBuilder;
import model.report.Report;
import model.transaction.Transaction;

import java.util.List;

public class MonthlyReportStrategy implements ReportStrategy {

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

        return new ReportBuilder(userId)
                .setReportType("MONTHLY")
                .setPeriod("MONTHLY")
                .setFormat("DEFAULT")
                .setTotalIncome(totalIncome)
                .setTotalExpense(totalExpense)
                .build();
    }
}
