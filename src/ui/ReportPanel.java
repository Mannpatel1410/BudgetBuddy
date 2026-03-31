package ui;

import model.report.Report;
import service.ReportService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReportPanel extends JPanel {
    private JComboBox<String> reportTypeCombo;
    private JButton generateBtn;
    private JTable reportHistoryTable;
    private DefaultTableModel tableModel;

    private final ReportService reportService = new ReportService();
    private final long currentUserId = 1L;

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Top bar: type selector + generate button ──────────────────────────
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reportTypeCombo = new JComboBox<>(new String[]{"MONTHLY", "WEEKLY", "CATEGORY"});
        generateBtn = new JButton("Generate Report");

        topBar.add(new JLabel("Report Type:"));
        topBar.add(reportTypeCombo);
        topBar.add(generateBtn);

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

        loadHistory();
    }

    private void generateReport() {
        String type = (String) reportTypeCombo.getSelectedItem();
        Report report = reportService.generateReport(currentUserId, type);
        if (report != null) {
            JOptionPane.showMessageDialog(this,
                    String.format("Report generated!\nIncome: $%.2f  Expense: $%.2f  Net: $%.2f",
                            report.getTotalIncome(), report.getTotalExpense(), report.getNetSavings()),
                    "Report Result", JOptionPane.INFORMATION_MESSAGE);
            loadHistory();
        } else {
            JOptionPane.showMessageDialog(this, "Report generation not available for this type yet.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
        }
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
