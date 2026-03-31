package strategy;

import model.report.Report;
import model.transaction.Transaction;
import java.util.List;

public interface ReportStrategy {
    Report generateReport(List<Transaction> transactions, long userId);
}
