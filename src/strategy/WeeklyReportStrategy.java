package strategy;

import model.report.Report;
import model.transaction.Transaction;
import java.util.List;

public class WeeklyReportStrategy implements ReportStrategy {

    @Override
    public Report generateReport(List<Transaction> transactions, long userId) {
        // TODO (Milestone 3): Filter to current week, sum income/expense
        return null;
    }
}
