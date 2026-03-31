package service;

import dao.ReportDAO;
import dao.TransactionDAO;
import strategy.CategoryReportStrategy;
import strategy.MonthlyReportStrategy;
import strategy.ReportStrategy;
import strategy.WeeklyReportStrategy;
import model.report.Report;
import model.transaction.Transaction;
import java.util.List;

public class ReportService {
    private ReportDAO reportDAO = new ReportDAO();
    private TransactionDAO transactionDAO = new TransactionDAO();
    private ReportStrategy currentStrategy;

    public void setStrategy(ReportStrategy strategy) {
        this.currentStrategy = strategy;
    }

    public Report generateReport(long userId, String strategyType) {
        switch (strategyType.toUpperCase()) {
            case "MONTHLY":
                currentStrategy = new MonthlyReportStrategy();
                break;
            case "WEEKLY":
                currentStrategy = new WeeklyReportStrategy();
                break;
            case "CATEGORY":
                currentStrategy = new CategoryReportStrategy();
                break;
            default:
                currentStrategy = new MonthlyReportStrategy();
        }

        List<Transaction> transactions = transactionDAO.findByUserId(userId);
        Report report = currentStrategy.generateReport(transactions, userId);
        if (report != null) {
            reportDAO.insert(report);
        }
        return report;
    }

    public List<Report> getReportHistory(long userId) {
        return reportDAO.findByUserId(userId);
    }
}
