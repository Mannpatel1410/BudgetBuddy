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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;

public class ReportPanel extends JPanel {
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String[] MONTHS = {
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    private JComboBox<String>  reportTypeCombo;
    private JComboBox<String>  filterByCombo;
    private JComboBox<String>  monthCombo;
    private JComboBox<Integer> yearCombo;
    private JButton  generateBtn;
    private JButton  exportPdfBtn;
    private JButton  exportCsvBtn;
    private JTable   reportHistoryTable;
    private DefaultTableModel tableModel;
    private JLabel   statusLabel;
    private JLabel   periodNote;

    private final ReportService reportService = new ReportService();
    private long   currentUserId;
    private Report currentReport = null;

    // Cached per-category rows for the last CATEGORY report generated
    private List<Object[]> categoryBreakdownRows  = null;
    private String         categoryBreakdownPeriod = null; // "April 2025"

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        int curYear  = LocalDate.now().getYear();
        int curMonth = LocalDate.now().getMonthValue() - 1; // 0-based

        // ── Controls row (selectors) ──────────────────────────────────────────
        JPanel controlsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        reportTypeCombo = new JComboBox<>(new String[]{"MONTHLY", "WEEKLY", "CATEGORY"});
        monthCombo      = new JComboBox<>(MONTHS);
        yearCombo       = new JComboBox<>(new Integer[]{curYear - 1, curYear, curYear + 1});
        filterByCombo   = new JComboBox<>(new String[]{
            "All Transactions", "Expenses Only", "Income Only", "Large (>$100)"});
        monthCombo.setSelectedIndex(curMonth);
        yearCombo.setSelectedItem(curYear);

        controlsRow.add(new JLabel("Type:"));   controlsRow.add(reportTypeCombo);
        controlsRow.add(new JLabel("Month:"));  controlsRow.add(monthCombo);
        controlsRow.add(new JLabel("Year:"));   controlsRow.add(yearCombo);
        controlsRow.add(new JLabel("Filter:")); controlsRow.add(filterByCombo);

        // ── Buttons row (always on its own line — never wraps off-screen) ─────
        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        generateBtn  = new JButton("Generate Report");
        exportPdfBtn = new JButton("Export PDF");
        exportCsvBtn = new JButton("Export CSV");
        exportPdfBtn.setEnabled(false);
        exportCsvBtn.setEnabled(false);
        styleButton(generateBtn,  new Color(52, 152, 219));
        styleButton(exportPdfBtn, new Color(231, 76, 60));
        styleButton(exportCsvBtn, new Color(39, 174, 96));
        buttonsRow.add(generateBtn);
        buttonsRow.add(exportPdfBtn);
        buttonsRow.add(exportCsvBtn);

        // ── Hint row ──────────────────────────────────────────────────────────
        JPanel hintRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        periodNote  = new JLabel();
        statusLabel = new JLabel(" ");
        periodNote.setFont(periodNote.getFont().deriveFont(Font.ITALIC, 11f));
        periodNote.setForeground(new Color(120, 120, 120));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC, 12f));
        hintRow.add(periodNote);
        hintRow.add(new JSeparator(SwingConstants.VERTICAL));
        hintRow.add(statusLabel);

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.add(controlsRow);
        northPanel.add(buttonsRow);
        northPanel.add(hintRow);

        // ── History / breakdown table ─────────────────────────────────────────
        tableModel = new DefaultTableModel(
                new Object[]{"Generated At", "Type", "Period / Category", "Income", "Expense", "Net Savings"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        reportHistoryTable = new JTable(tableModel);

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
        reportHistoryTable.setSelectionBackground(new Color(41, 98, 163));
        reportHistoryTable.setSelectionForeground(Color.WHITE);
        reportHistoryTable.setFont(reportHistoryTable.getFont().deriveFont(12f));

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
                    else if (col == 5 && value != null) {
                        try {
                            double d = Double.parseDouble(
                                    value.toString().replace("$","").replace(",",""));
                            setForeground(d >= 0 ? new Color(40, 140, 50) : new Color(200, 40, 40));
                        } catch (NumberFormatException ignored) { setForeground(Color.BLACK); }
                    }
                }
                return this;
            }
        };
        reportHistoryTable.getColumnModel().getColumn(3).setCellRenderer(amountRenderer);
        reportHistoryTable.getColumnModel().getColumn(4).setCellRenderer(amountRenderer);
        reportHistoryTable.getColumnModel().getColumn(5).setCellRenderer(amountRenderer);

        reportHistoryTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        reportHistoryTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        reportHistoryTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        reportHistoryTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        reportHistoryTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        reportHistoryTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        add(northPanel, BorderLayout.NORTH);
        add(new JScrollPane(reportHistoryTable), BorderLayout.CENTER);

        // ── Listeners ─────────────────────────────────────────────────────────
        reportTypeCombo.addActionListener(e -> { updatePeriodNote(); loadHistory(); });
        // Changing month/year invalidates any stored category breakdown
        monthCombo.addActionListener(e -> {
            categoryBreakdownRows  = null;
            categoryBreakdownPeriod = null;
            updatePeriodNote();
            loadHistory();
        });
        yearCombo.addActionListener(e -> {
            categoryBreakdownRows  = null;
            categoryBreakdownPeriod = null;
            updatePeriodNote();
            loadHistory();
        });

        generateBtn.addActionListener(e -> generateReport());
        exportPdfBtn.addActionListener(e -> doExport("PDF", "report.pdf"));
        exportCsvBtn.addActionListener(e -> doExport("CSV", "report.csv"));

        updatePeriodNote();
    }

    public void setUserId(long userId) {
        this.currentUserId = userId;
        loadHistory();
    }

    // ── Report generation ─────────────────────────────────────────────────────

    private void generateReport() {
        String type   = (String) reportTypeCombo.getSelectedItem();
        String filter = (String) filterByCombo.getSelectedItem();

        applyPeriodToService(type);

        statusLabel.setText("Generating " + type + " report...");
        statusLabel.setForeground(new Color(100, 100, 100));

        try {
            Report report = reportService.generateReport(currentUserId, type, getFilterPredicate());
            if (report != null) {
                currentReport = report;
                exportPdfBtn.setEnabled(true);
                exportCsvBtn.setEnabled(true);

                // For CATEGORY: build per-category rows from in-memory Report
                if ("CATEGORY".equals(type)) {
                    categoryBreakdownRows  = buildCategoryRows(report);
                    categoryBreakdownPeriod = selectedPeriodLabel();
                }

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

    /**
     * Configures the date-range pre-filter on ReportService.
     *  - WEEKLY: clear (WeeklyReportStrategy handles its own Mon→today filter)
     *  - MONTHLY / CATEGORY: pre-filter to the selected month, override period label
     */
    private void applyPeriodToService(String type) {
        if ("WEEKLY".equals(type)) {
            reportService.clearPeriod();
        } else {
            int year  = (int) yearCombo.getSelectedItem();
            int month = monthCombo.getSelectedIndex() + 1; // 1-based
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end   = start.withDayOfMonth(start.lengthOfMonth());
            String label    = start.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            reportService.setPeriod(start, end, label);
        }
    }

    /** e.g. "April 2025" — matches what MONTHLY/CATEGORY store as period in the DB. */
    private String selectedPeriodLabel() {
        int year  = (int) yearCombo.getSelectedItem();
        int month = monthCombo.getSelectedIndex() + 1;
        return LocalDate.of(year, month, 1)
                .format(DateTimeFormatter.ofPattern("MMMM yyyy"));
    }

    // ── Category breakdown helpers ────────────────────────────────────────────

    /**
     * Builds one table row per category from the in-memory Report's transaction list.
     * Only categories that have at least one transaction are included.
     */
    private List<Object[]> buildCategoryRows(Report report) {
        Map<Long, Double> catIncome  = new LinkedHashMap<>();
        Map<Long, Double> catExpense = new LinkedHashMap<>();

        List<Transaction> txns = report.getTransactions();
        if (txns != null) {
            for (Transaction t : txns) {
                long catId = t.getCategoryId();
                if ("INCOME".equalsIgnoreCase(t.getType())) {
                    catIncome.merge(catId, t.getAmount(), Double::sum);
                } else if ("EXPENSE".equalsIgnoreCase(t.getType())) {
                    catExpense.merge(catId, t.getAmount(), Double::sum);
                }
            }
        }

        Map<Long, String> catNames = report.getCategoryNames();
        Set<Long> allIds = new LinkedHashSet<>();
        allIds.addAll(catIncome.keySet());
        allIds.addAll(catExpense.keySet());

        String genAt = report.getGeneratedAt() != null
                ? report.getGeneratedAt().format(DISPLAY_FMT) : "";

        List<Object[]> rows = new ArrayList<>();
        for (long catId : allIds) {
            String name = catNames != null ? catNames.getOrDefault(catId, "Unknown") : "Unknown";
            double inc = catIncome.getOrDefault(catId, 0.0);
            double exp = catExpense.getOrDefault(catId, 0.0);
            rows.add(new Object[]{
                genAt,
                "CATEGORY",
                name,
                String.format("$%.2f", inc),
                String.format("$%.2f", exp),
                String.format("$%.2f", inc - exp)
            });
        }
        return rows;
    }

    // ── History loading ───────────────────────────────────────────────────────

    /**
     * Populates the table filtered by the selected type + period:
     *  - MONTHLY  : period stored in DB == "April 2025"
     *  - WEEKLY   : period stored as "2026-04-13 to 2026-04-15" → parse start date to match month/year
     *  - CATEGORY : show the in-memory per-category breakdown (not DB rows)
     */
    private void loadHistory() {
        tableModel.setRowCount(0);
        if (currentUserId <= 0) return;

        String selectedType = (String) reportTypeCombo.getSelectedItem();

        // CATEGORY: show cached breakdown rows if they match the current period selection
        if ("CATEGORY".equals(selectedType)) {
            String label = selectedPeriodLabel();
            if (categoryBreakdownRows != null && label.equals(categoryBreakdownPeriod)) {
                for (Object[] row : categoryBreakdownRows) {
                    tableModel.addRow(row);
                }
            }
            return;
        }

        int selectedYear  = (int) yearCombo.getSelectedItem();
        int selectedMonth = monthCombo.getSelectedIndex() + 1; // 1-based

        try {
            List<Report> reports = reportService.getReportHistory(currentUserId);
            for (Report r : reports) {
                if (!selectedType.equalsIgnoreCase(r.getReportType())) continue;

                if ("WEEKLY".equals(selectedType)) {
                    // period = "2026-04-13 to 2026-04-15" — match by the start date's month/year
                    if (!weeklyPeriodMatchesSelection(r.getPeriod(), selectedYear, selectedMonth)) continue;
                } else {
                    // MONTHLY: period == "April 2025"
                    if (!selectedPeriodLabel().equals(r.getPeriod())) continue;
                }

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
        } catch (Exception ignored) {}
    }

    /**
     * Returns true when a WEEKLY period string ("2026-04-13 to 2026-04-15")
     * has its start date within the given year/month.
     */
    private boolean weeklyPeriodMatchesSelection(String period, int year, int month) {
        if (period == null) return false;
        String[] parts = period.split(" to ");
        if (parts.length < 1) return false;
        try {
            LocalDate start = LocalDate.parse(parts[0].trim());
            return start.getYear() == year && start.getMonthValue() == month;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Misc ─────────────────────────────────────────────────────────────────

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
            case "Expenses Only": return TransactionIterator.byType("EXPENSE");
            case "Income Only":   return TransactionIterator.byType("INCOME");
            case "Large (>$100)": return TransactionIterator.byAmountGreaterThan(100);
            default:              return null;
        }
    }

    private void updatePeriodNote() {
        String type  = (String) reportTypeCombo.getSelectedItem();
        String month = (String) monthCombo.getSelectedItem();
        int    year  = (int)    yearCombo.getSelectedItem();
        if ("WEEKLY".equals(type)) {
            LocalDate today  = LocalDate.now();
            LocalDate wStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
            periodNote.setText("Period: current week  (" + wStart + " to " + today + ")");
        } else {
            periodNote.setText("Period: " + month + " " + year);
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
