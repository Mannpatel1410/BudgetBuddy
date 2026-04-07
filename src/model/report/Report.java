package model.report;

import model.category.Category;

import java.time.LocalDateTime;
import java.util.List;

public class Report {
    private long id;
    private long userId;
    private String reportType;
    private String period;
    private String format;
    private double totalIncome;
    private double totalExpense;
    private double netSavings;
    private LocalDateTime generatedAt;
    private List<Category> categories;

    public Report() {}

    public Report(long userId, String reportType, String period, String format,
                    double totalIncome, double totalExpense, double netSavings, LocalDateTime generatedAt) {
        this.userId = userId;
        this.reportType = reportType;
        this.period = period;
        this.format = format;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netSavings = netSavings;
        this.generatedAt = generatedAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public double getTotalIncome() { return totalIncome; }
    public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }
    public double getTotalExpense() { return totalExpense; }
    public void setTotalExpense(double totalExpense) { this.totalExpense = totalExpense; }
    public double getNetSavings() { return netSavings; }
    public void setNetSavings(double netSavings) { this.netSavings = netSavings; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public List<Category> getCategories() { return categories; }
    public void setCategories(List<Category> categories) { this.categories = categories; }
}
