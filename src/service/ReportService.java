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

    // Optional period pre-filter set by the UI before calling generateReport
    private java.time.LocalDate periodStart;
    private java.time.LocalDate periodEnd;
    private String periodLabel; // overrides the strategy's period string when non-null

    public void setStrategy(ReportStrategy strategy) {
        this.currentStrategy = strategy;
    }

    /** Set a date range to pre-filter transactions. Pass null label to keep the strategy's own period string. */
    public void setPeriod(java.time.LocalDate start, java.time.LocalDate end, String label) {
        this.periodStart = start;
        this.periodEnd   = end;
        this.periodLabel = label;
    }

    /** Remove any previously set period filter (strategy will handle dates itself). */
    public void clearPeriod() {
        this.periodStart = null;
        this.periodEnd   = null;
        this.periodLabel = null;
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

        // Date-range pre-filter (set by UI via setPeriod)
        if (periodStart != null && periodEnd != null) {
            final java.time.LocalDate s = periodStart, e = periodEnd;
            transactions = transactions.stream()
                    .filter(t -> t.getTransactionDate() != null
                            && !t.getTransactionDate().isBefore(s)
                            && !t.getTransactionDate().isAfter(e))
                    .collect(Collectors.toList());
        }

        // Type / amount pre-filter
        if (preFilter != null) {
            transactions = transactions.stream().filter(preFilter).collect(Collectors.toList());
        }

        Report report = currentStrategy.generateReport(transactions, userId);
        if (report != null) {
            if (periodLabel != null) report.setPeriod(periodLabel);
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
