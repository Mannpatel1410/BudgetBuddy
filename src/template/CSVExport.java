package template;

import model.report.Report;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Exports a Report as a CSV file using Java's standard BufferedWriter.
 * Content is accumulated across the four template steps and flushed in save().
 */
public class CSVExport extends ExportTemplate {

    private StringBuilder csv;

    @Override
    protected void prepareHeader(Report report) {
        csv = new StringBuilder();
        // Header row describing the columns
        csv.append("Report Type,Period,Total Income,Total Expense,Net Savings\n");
    }

    @Override
    protected void writeBody(Report report) {
        csv.append(String.format("%s,%s,%.2f,%.2f,%.2f\n",
                report.getReportType(),
                report.getPeriod(),
                report.getTotalIncome(),
                report.getTotalExpense(),
                report.getNetSavings()));
    }

    @Override
    protected void formatFooter(Report report) {
        // Footer row with generation timestamp
        String generatedAt = report.getGeneratedAt() != null
                ? report.getGeneratedAt().toString() : "N/A";
        csv.append("Generated,").append(generatedAt).append("\n");
    }

    @Override
    protected void save(Report report, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(csv.toString());
            JOptionPane.showMessageDialog(null,
                    "Report exported to CSV successfully:\n" + filePath,
                    "Export CSV", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "CSV export failed: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
