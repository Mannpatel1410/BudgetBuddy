package service;

import dao.ReportDAO;
import dao.TransactionDAO;
import iterator.TransactionIterator;
import model.report.Report;
import model.transaction.Transaction;
import strategy.CategoryReportStrategy;
import strategy.MonthlyReportStrategy;
import strategy.ReportStrategy;
import strategy.WeeklyReportStrategy;
import template.CSVExport;
import template.ExportTemplate;
import template.PDFExport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ReportService {
    private ReportDAO reportDAO = new ReportDAO();
    private TransactionDAO transactionDAO = new TransactionDAO();
    private ReportStrategy currentStrategy;

    public void setStrategy(ReportStrategy strategy) {
        this.currentStrategy = strategy;
    }

    public Report generateReport(long userId, String strategyType) {
        return generateReport(userId, strategyType, null);
    }

    public Report generateReport(long userId, String strategyType, Predicate<Transaction> preFilter) {
        switch (strategyType.toUpperCase()) {
            case "MONTHLY":  currentStrategy = new MonthlyReportStrategy();  break;
            case "WEEKLY":   currentStrategy = new WeeklyReportStrategy();   break;
            case "CATEGORY": currentStrategy = new CategoryReportStrategy(); break;
            default:         currentStrategy = new MonthlyReportStrategy();
        }

        List<Transaction> transactions = transactionDAO.findByUserId(userId);
        if (preFilter != null) {
            transactions = transactions.stream().filter(preFilter).collect(Collectors.toList());
        }
        Report report = currentStrategy.generateReport(transactions, userId);
        if (report != null) {
            reportDAO.insert(report);
        }
        return report;
    }

    public List<Report> getReportHistory(long userId) {
        return reportDAO.findByUserId(userId);
    }

    public void exportReport(Report report, String format, String filePath) {
        ExportTemplate exporter;
        if ("PDF".equalsIgnoreCase(format)) {
            exporter = new PDFExport();
        } else {
            exporter = new CSVExport();
        }
        exporter.export(report, filePath);
    }

    public List<Transaction> getFilteredTransactions(long userId, Predicate<Transaction> filter) {
        List<Transaction> all = transactionDAO.findByUserId(userId);
        TransactionIterator iter = new TransactionIterator(all, filter);
        List<Transaction> result = new ArrayList<>();
        while (iter.hasNext()) {
            result.add(iter.next());
        }
        return result;
    }
}
