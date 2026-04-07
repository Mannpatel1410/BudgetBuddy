package ui;

import iterator.TransactionIterator;
import model.report.Report;
import model.transaction.Transaction;
import service.ReportService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Predicate;

public class ReportPanel extends JPanel {
    private JComboBox<String> reportTypeCombo;
    private JComboBox<String> filterByCombo;
    private JButton generateBtn;
    private JButton exportPdfBtn;
    private JButton exportCsvBtn;
    private JTable reportHistoryTable;
    private DefaultTableModel tableModel;

    private final ReportService reportService = new ReportService();
    private final long currentUserId = 1L;
    private Report currentReport = null;

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Top bar: type selector + filter + buttons ─────────────────────────
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        reportTypeCombo = new JComboBox<>(new String[]{"MONTHLY", "WEEKLY", "CATEGORY"});
        filterByCombo   = new JComboBox<>(new String[]{"All", "By Category", "By Type", "By Amount"});
        generateBtn  = new JButton("Generate Report");
        exportPdfBtn = new JButton("Export PDF");
        exportCsvBtn = new JButton("Export CSV");

        // Export buttons start disabled until a report is generated
        exportPdfBtn.setEnabled(false);
        exportCsvBtn.setEnabled(false);

        topBar.add(new JLabel("Report Type:"));
        topBar.add(reportTypeCombo);
        topBar.add(new JLabel("Filter:"));
        topBar.add(filterByCombo);
        topBar.add(generateBtn);
        topBar.add(exportPdfBtn);
        topBar.add(exportCsvBtn);

        // ── Center: history table ─────────────────────────────────────────────
        tableModel = new DefaultTableModel(
                new Object[]{"Generated At", "Type", "Period", "Income", "Expense", "Net Savings"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        reportHistoryTable = new JTable(tableModel);

        add(topBar, BorderLayout.NORTH);
        add(new JScrollPane(reportHistoryTable), BorderLayout.CENTER);

        generateBtn.addActionListener(e -> generateReport());

        exportPdfBtn.addActionListener(e -> doExport("PDF", "report.pdf"));
        exportCsvBtn.addActionListener(e -> doExport("CSV", "report.csv"));

        loadHistory();
    }

    private void doExport(String format, String defaultFileName) {
        if (currentReport == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(defaultFileName));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            reportService.exportReport(currentReport, format,
                    chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void generateReport() {
        String type = (String) reportTypeCombo.getSelectedItem();
        Report report = reportService.generateReport(currentUserId, type);
        if (report != null) {
            // Apply the selected filter to show a summary in the dialog
            String filterChoice = (String) filterByCombo.getSelectedItem();
            String filterInfo   = buildFilterSummary(filterChoice);

            JOptionPane.showMessageDialog(this,
                    String.format("Report generated!%s%nIncome: $%.2f  Expense: $%.2f  Net: $%.2f",
                            filterInfo,
                            report.getTotalIncome(),
                            report.getTotalExpense(),
                            report.getNetSavings()),
                    "Report Result", JOptionPane.INFORMATION_MESSAGE);

            currentReport = report;
            exportPdfBtn.setEnabled(true);
            exportCsvBtn.setEnabled(true);
            loadHistory();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Report generation not available for this type yet.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Uses TransactionIterator with the user-selected filter and returns a
     * human-readable summary line appended to the report dialog.
     */
    private String buildFilterSummary(String filterChoice) {
        if ("All".equals(filterChoice)) return "";

        Predicate<Transaction> predicate;
        String label;
        switch (filterChoice) {
            case "By Category":
                predicate = TransactionIterator.byCategory(1L); // default category 1
                label = "category 1";
                break;
            case "By Type":
                predicate = TransactionIterator.byType("EXPENSE");
                label = "type EXPENSE";
                break;
            case "By Amount":
                predicate = TransactionIterator.byAmountGreaterThan(100);
                label = "amount > $100";
                break;
            default:
                return "";
        }

        List<Transaction> filtered = reportService.getFilteredTransactions(currentUserId, predicate);
        return String.format("%n[Filter: %s — %d transaction(s)]", label, filtered.size());
    }

    private void loadHistory() {
        tableModel.setRowCount(0);
        List<Report> reports = reportService.getReportHistory(currentUserId);
        for (Report r : reports) {
            tableModel.addRow(new Object[]{
                    r.getGeneratedAt() != null ? r.getGeneratedAt().toString() : "",
                    r.getReportType(),
                    r.getPeriod(),
                    String.format("$%.2f", r.getTotalIncome()),
                    String.format("$%.2f", r.getTotalExpense()),
                    String.format("$%.2f", r.getNetSavings())
            });
        }
    }
}
