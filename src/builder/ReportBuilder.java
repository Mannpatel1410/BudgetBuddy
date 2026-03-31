package builder;

import model.report.Report;
import model.category.Category;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReportBuilder {
    private long userId;
    private String reportType;
    private String period;
    private String format;
    private double totalIncome;
    private double totalExpense;
    private List<Category> categories;
    private LocalDate startDate;
    private LocalDate endDate;

    public ReportBuilder(long userId) {
        this.userId = userId;
    }

    public ReportBuilder setReportType(String reportType) {
        this.reportType = reportType;
        return this;
    }

    public ReportBuilder setPeriod(String period) {
        this.period = period;
        return this;
    }

    public ReportBuilder setFormat(String format) {
        this.format = format;
        return this;
    }

    public ReportBuilder setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
        return this;
    }

    public ReportBuilder setTotalExpense(double totalExpense) {
        this.totalExpense = totalExpense;
        return this;
    }

    public ReportBuilder setCategories(List<Category> categories) {
        this.categories = categories;
        return this;
    }

    public ReportBuilder setDateRange(LocalDate start, LocalDate end) {
        this.startDate = start;
        this.endDate = end;
        return this;
    }

    public Report build() {
        Report report = new Report();
        report.setUserId(userId);
        report.setReportType(reportType);
        // If an explicit date range was provided, derive the period string from it;
        // otherwise fall back to whatever was set via setPeriod()
        if (startDate != null && endDate != null) {
            report.setPeriod(startDate + " to " + endDate);
        } else {
            report.setPeriod(period);
        }
        report.setFormat(format);
        report.setTotalIncome(totalIncome);
        report.setTotalExpense(totalExpense);
        report.setNetSavings(totalIncome - totalExpense);
        report.setGeneratedAt(LocalDateTime.now());
        // categories is used in Milestone 3 by CategoryReportStrategy for per-category filtering
        return report;
    }
}
