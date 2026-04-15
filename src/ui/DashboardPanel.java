package ui;

import facade.DashboardFacade;
import model.transaction.Transaction;
import service.TransactionService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private JLabel totalIncomeLabel;
    private JLabel totalExpenseLabel;
    private JLabel netSavingsLabel;
    private JTable summaryTable;
    private DefaultTableModel tableModel;
    private BarChartPanel barChartPanel;

    private final DashboardFacade dashboardFacade = new DashboardFacade();
    private final TransactionService transactionService = new TransactionService();
    private long currentUserId = 1L;

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Top: 3 summary cards ──────────────────────────────────────────────
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 10, 0));

        totalIncomeLabel  = new JLabel("$0.00", SwingConstants.CENTER);
        totalExpenseLabel = new JLabel("$0.00", SwingConstants.CENTER);
        netSavingsLabel   = new JLabel("$0.00", SwingConstants.CENTER);

        cardsPanel.add(buildCard("Total Income",  totalIncomeLabel,  new Color(76, 175, 80)));
        cardsPanel.add(buildCard("Total Expense", totalExpenseLabel, new Color(244, 67, 54)));
        cardsPanel.add(buildCard("Net Savings",   netSavingsLabel,   new Color(33, 150, 243)));

        // ── Controls ──────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        topBar.add(refreshBtn);
        refreshBtn.addActionListener(e -> loadDashboard(currentUserId));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topBar,     BorderLayout.NORTH);
        northPanel.add(cardsPanel, BorderLayout.CENTER);

        // ── Center: monthly breakdown table ──────────────────────────────────
        tableModel = new DefaultTableModel(
                new Object[]{"Month", "Income", "Expense", "Net Savings"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        summaryTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(summaryTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Monthly Breakdown",
                TitledBorder.LEFT, TitledBorder.TOP));

        // ── South: bar chart (Java2D) ─────────────────────────────────────────
        barChartPanel = new BarChartPanel();
        barChartPanel.setPreferredSize(new Dimension(0, 180));
        barChartPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Income vs Expense by Month",
                TitledBorder.LEFT, TitledBorder.TOP));

        add(northPanel,    BorderLayout.NORTH);
        add(scrollPane,    BorderLayout.CENTER);
        add(barChartPanel, BorderLayout.SOUTH);
    }

    public void loadDashboard(long userId) {
        this.currentUserId = userId;

        double totalIncome  = dashboardFacade.getTotalIncome(userId);
        double totalExpense = dashboardFacade.getTotalExpense(userId);
        double netSavings   = dashboardFacade.getNetSavings(userId);

        totalIncomeLabel.setText(String.format("$%.2f", totalIncome));
        totalExpenseLabel.setText(String.format("$%.2f", totalExpense));
        netSavingsLabel.setText(String.format("$%.2f", netSavings));
        netSavingsLabel.setForeground(netSavings >= 0 ? new Color(33, 150, 243) : new Color(244, 67, 54));

        // Aggregate per "YYYY-MM" bucket for monthly breakdown
        List<Transaction> transactions = transactionService.getTransactionsByUser(userId);
        Map<String, double[]> monthly = new LinkedHashMap<>();

        for (Transaction t : transactions) {
            String bucket = t.getTransactionDate() != null
                    ? t.getTransactionDate().getYear() + "-"
                      + String.format("%02d", t.getTransactionDate().getMonthValue())
                    : "Unknown";

            monthly.putIfAbsent(bucket, new double[]{0, 0});

            if ("INCOME".equalsIgnoreCase(t.getType())) {
                monthly.get(bucket)[0] += t.getAmount();
            } else if ("EXPENSE".equalsIgnoreCase(t.getType())) {
                monthly.get(bucket)[1] += t.getAmount();
            }
        }

        tableModel.setRowCount(0);
        for (Map.Entry<String, double[]> entry : monthly.entrySet()) {
            double inc = entry.getValue()[0];
            double exp = entry.getValue()[1];
            tableModel.addRow(new Object[]{
                    entry.getKey(),
                    String.format("$%.2f", inc),
                    String.format("$%.2f", exp),
                    String.format("$%.2f", inc - exp)
            });
        }

        barChartPanel.setData(monthly);
    }

    private JPanel buildCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 2, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 12f));
        titleLabel.setForeground(Color.GRAY);

        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 20f));
        valueLabel.setForeground(accentColor);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // ── Inner class: Java2D bar chart ─────────────────────────────────────────

    private static class BarChartPanel extends JPanel {

        private Map<String, double[]> data = new LinkedHashMap<>();
        private double cachedMaxVal = 1;

        void setData(Map<String, double[]> data) {
            this.data = data;
            cachedMaxVal = 1;
            for (double[] vals : data.values()) {
                cachedMaxVal = Math.max(cachedMaxVal, Math.max(vals[0], vals[1]));
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width  = getWidth();
            int height = getHeight();
            int padLeft = 50, padRight = 20, padTop = 20, padBottom = 30;
            int chartWidth  = width  - padLeft - padRight;
            int chartHeight = height - padTop  - padBottom;

            int n        = data.size();
            int groupW   = chartWidth / Math.max(n, 1);
            int barW     = Math.max(4, groupW / 3);
            int groupIdx = 0;

            Font labelFont = g2.getFont().deriveFont(9f);

            // Draw Y-axis baseline
            g2.setColor(Color.DARK_GRAY);
            g2.drawLine(padLeft, padTop, padLeft, padTop + chartHeight);
            g2.drawLine(padLeft, padTop + chartHeight, padLeft + chartWidth, padTop + chartHeight);

            for (Map.Entry<String, double[]> entry : data.entrySet()) {
                double inc = entry.getValue()[0];
                double exp = entry.getValue()[1];

                int x = padLeft + groupIdx * groupW + (groupW - 2 * barW) / 2;

                int incH = (int) (inc / cachedMaxVal * chartHeight);
                g2.setColor(new Color(76, 175, 80));
                g2.fillRect(x, padTop + chartHeight - incH, barW, incH);

                int expH = (int) (exp / cachedMaxVal * chartHeight);
                g2.setColor(new Color(244, 67, 54));
                g2.fillRect(x + barW + 2, padTop + chartHeight - expH, barW, expH);

                g2.setColor(Color.DARK_GRAY);
                g2.setFont(labelFont);
                String label = entry.getKey();
                int labelX = padLeft + groupIdx * groupW + (groupW - g2.getFontMetrics().stringWidth(label)) / 2;
                g2.drawString(label, labelX, padTop + chartHeight + 14);

                groupIdx++;
            }

            // Legend
            int legendX = padLeft + chartWidth - 120;
            int legendY = padTop + 10;
            g2.setColor(new Color(76, 175, 80));
            g2.fillRect(legendX, legendY, 12, 12);
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(g2.getFont().deriveFont(10f));
            g2.drawString("Income", legendX + 16, legendY + 10);

            g2.setColor(new Color(244, 67, 54));
            g2.fillRect(legendX + 70, legendY, 12, 12);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("Expense", legendX + 86, legendY + 10);
        }
    }
}
