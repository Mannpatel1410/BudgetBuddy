package template;

import model.report.Report;
import model.transaction.Transaction;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Exports a Report as a CSV file.
 * Includes a summary section followed by one row per transaction.
 */
public class CSVExport extends ExportTemplate {

    private StringBuilder csv;

    @Override
    protected void prepareHeader(Report report) {
        csv = new StringBuilder();
        csv.append("Report Type,Period,Total Income,Total Expense,Net Savings\n");
    }

    @Override
    protected void writeBody(Report report) {
        // Summary row
        csv.append(String.format("%s,%s,%.2f,%.2f,%.2f\n",
                nvl(report.getReportType()),
                escapeCsv(nvl(report.getPeriod())),
                report.getTotalIncome(),
                report.getTotalExpense(),
                report.getNetSavings()));

        // Transaction detail section
        List<Transaction> txns = report.getTransactions();
        if (txns == null || txns.isEmpty()) return;

        Map<Long, String> catNames = report.getCategoryNames();
        csv.append("\n");
        csv.append("Date,Type,Category,Amount,Description\n");
        for (Transaction t : txns) {
            String date = t.getTransactionDate() != null ? t.getTransactionDate().toString() : "";
            String cat  = (catNames != null)
                    ? catNames.getOrDefault(t.getCategoryId(), "Cat#" + t.getCategoryId())
                    : "Cat#" + t.getCategoryId();
            String desc = nvl(t.getDescription());
            csv.append(String.format("%s,%s,%s,%.2f,%s\n",
                    date,
                    t.getType(),
                    escapeCsv(cat),
                    t.getAmount(),
                    escapeCsv(desc)));
        }
    }

    @Override
    protected void formatFooter(Report report) {
        String generatedAt = report.getGeneratedAt() != null
                ? report.getGeneratedAt().toString() : "N/A";
        csv.append("\nGenerated,").append(generatedAt).append("\n");
    }

    @Override
    protected void save(Report report, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(csv.toString());
            JOptionPane.showMessageDialog(null,
                    "CSV exported successfully:\n" + filePath,
                    "Export CSV", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "CSV export failed: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Wrap value in double-quotes if it contains comma, quote, or newline. */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
