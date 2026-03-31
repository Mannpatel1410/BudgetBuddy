package strategy;

import model.report.Report;
import model.transaction.Transaction;
import java.util.List;

public class CategoryReportStrategy implements ReportStrategy {

    @Override
    public Report generateReport(List<Transaction> transactions, long userId) {
        // TODO (Milestone 3): Group by category, sum per category
        return null;
    }
}
