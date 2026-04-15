package ui;

import iterator.TransactionIterator;
import model.report.Report;
import model.transaction.Transaction;
import service.ReportService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;

public class ReportPanel extends JPanel {
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private JComboBox<String> reportTypeCombo;
    private JComboBox<String> filterByCombo;
    private JButton generateBtn;
    private JButton exportPdfBtn;
    private JButton exportCsvBtn;
    private JTable reportHistoryTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    private final ReportService reportService = new ReportService();
    private long currentUserId;
    private Report currentReport = null;

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Top bar ───────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        reportTypeCombo = new JComboBox<>(new String[]{"MONTHLY", "WEEKLY", "CATEGORY"});
        filterByCombo   = new JComboBox<>(new String[]{"All Transactions", "Expenses Only", "Income Only", "Large (>$100)"});
        generateBtn  = new JButton("Generate Report");
        exportPdfBtn = new JButton("Export PDF");
        exportCsvBtn = new JButton("Export CSV");

        exportPdfBtn.setEnabled(false);
        exportCsvBtn.setEnabled(false);

        styleButton(generateBtn, new Color(52, 152, 219));
        styleButton(exportPdfBtn, new Color(231, 76, 60));
        styleButton(exportCsvBtn, new Color(39, 174, 96));

        topBar.add(new JLabel("Report Type:"));
        topBar.add(reportTypeCombo);
        topBar.add(new JLabel("Filter:"));
        topBar.add(filterByCombo);
        topBar.add(generateBtn);
        topBar.add(exportPdfBtn);
        topBar.add(exportCsvBtn);

        // ── Status label ──────────────────────────────────────────────────────
        statusLabel = new JLabel(" ");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC, 12f));
        statusLabel.setForeground(new Color(100, 100, 100));
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        statusBar.add(statusLabel);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topBar, BorderLayout.CENTER);
        northPanel.add(statusBar, BorderLayout.SOUTH);

        // ── History table ─────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(
                new Object[]{"Generated At", "Type", "Period", "Income", "Expense", "Net Savings"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        reportHistoryTable = new JTable(tableModel);

        // Style header
        JTableHeader header = reportHistoryTable.getTableHeader();
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 12f));
        header.setPreferredSize(new Dimension(0, 30));
        header.setReorderingAllowed(false);

        reportHistoryTable.setRowHeight(26);
        reportHistoryTable.setShowVerticalLines(false);
        reportHistoryTable.setGridColor(new Color(235, 235, 235));
        reportHistoryTable.setIntercellSpacing(new Dimension(0, 1));
        reportHistoryTable.setSelectionBackground(new Color(210, 230, 255));
        reportHistoryTable.setFont(reportHistoryTable.getFont().deriveFont(12f));

        // Alternating rows + colored/right-aligned amount columns
        reportHistoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                    setForeground(Color.BLACK);
                    setFont(getFont().deriveFont(Font.PLAIN));
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                return this;
            }
        });

        // Amount columns: Income (col 3) green, Expense (col 4) red, Net (col 5) conditional
        DefaultTableCellRenderer amountRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setFont(getFont().deriveFont(Font.BOLD));
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                    if (col == 3) setForeground(new Color(40, 140, 50));
                    else if (col == 4) setForeground(new Color(200, 40, 40));
                    else if (col == 5) {
                        // Net savings: green if positive/zero, red if negative
                        if (value != null) {
                            String v = value.toString().replace("$", "").replace(",", "");
                            try {
                                double d = Double.parseDouble(v);
                                setForeground(d >= 0 ? new Color(40, 140, 50) : new Color(200, 40, 40));
                            } catch (NumberFormatException ignored) {
                                setForeground(Color.BLACK);
                            }
                        }
                    }
                }
                return this;
            }
        };
        reportHistoryTable.getColumnModel().getColumn(3).setCellRenderer(amountRenderer);
        reportHistoryTable.getColumnModel().getColumn(4).setCellRenderer(amountRenderer);
        reportHistoryTable.getColumnModel().getColumn(5).setCellRenderer(amountRenderer);

        // Column widths
        reportHistoryTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        reportHistoryTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        reportHistoryTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        reportHistoryTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        reportHistoryTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        reportHistoryTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        add(northPanel, BorderLayout.NORTH);
        add(new JScrollPane(reportHistoryTable), BorderLayout.CENTER);

        // ── Listeners ─────────────────────────────────────────────────────────
        generateBtn.addActionListener(e -> generateReport());
        exportPdfBtn.addActionListener(e -> doExport("PDF", "report.pdf"));
        exportCsvBtn.addActionListener(e -> doExport("CSV", "report.csv"));
    }

    public void setUserId(long userId) {
        this.currentUserId = userId;
        loadHistory();
    }

    private void generateReport() {
        String type   = (String) reportTypeCombo.getSelectedItem();
        String filter = (String) filterByCombo.getSelectedItem();
        statusLabel.setText("Generating " + type + " report (" + filter + ")...");
        statusLabel.setForeground(new Color(100, 100, 100));

        try {
            Report report = reportService.generateReport(currentUserId, type, getFilterPredicate());
            if (report != null) {
                currentReport = report;
                exportPdfBtn.setEnabled(true);
                exportCsvBtn.setEnabled(true);
                loadHistory();

                String filterNote = "All Transactions".equals(filter) ? "" : "  [" + filter + "]";
                statusLabel.setText(String.format(
                        "Income $%.2f  |  Expense $%.2f  |  Net $%.2f%s",
                        report.getTotalIncome(), report.getTotalExpense(),
                        report.getNetSavings(), filterNote));
                statusLabel.setForeground(new Color(39, 174, 96));
            } else {
                statusLabel.setText("No transactions found for this period / filter.");
                statusLabel.setForeground(new Color(200, 40, 40));
            }
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            statusLabel.setForeground(new Color(200, 40, 40));
        }
    }

    private void doExport(String format, String defaultFileName) {
        if (currentReport == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(defaultFileName));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                reportService.exportReport(currentReport, format,
                        chooser.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Predicate<Transaction> getFilterPredicate() {
        switch ((String) filterByCombo.getSelectedItem()) {
            case "Expenses Only":  return TransactionIterator.byType("EXPENSE");
            case "Income Only":    return TransactionIterator.byType("INCOME");
            case "Large (>$100)":  return TransactionIterator.byAmountGreaterThan(100);
            default:               return null; // "All Transactions" → no filter
        }
    }

    private void loadHistory() {
        tableModel.setRowCount(0);
        try {
            List<Report> reports = reportService.getReportHistory(currentUserId);
            for (Report r : reports) {
                String genAt = r.getGeneratedAt() != null
                        ? r.getGeneratedAt().format(DISPLAY_FMT) : "";
                tableModel.addRow(new Object[]{
                        genAt,
                        r.getReportType(),
                        r.getPeriod(),
                        String.format("$%.2f", r.getTotalIncome()),
                        String.format("$%.2f", r.getTotalExpense()),
                        String.format("$%.2f", r.getNetSavings())
                });
            }
        } catch (Exception ex) {
            // DB not available — silently skip
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 12f));
    }
}
