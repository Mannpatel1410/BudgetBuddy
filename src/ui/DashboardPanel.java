package ui;

import facade.DashboardFacade;
import model.transaction.Transaction;
import service.TransactionService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final Color PAGE_BG      = new Color(245, 246, 248);
    private static final Color HEADER_DARK  = new Color(44,  62,  80);
    private static final Color CARD_GREEN   = new Color(39, 174,  96);
    private static final Color CARD_RED     = new Color(231,  76,  60);
    private static final Color CARD_BLUE    = new Color(41,  128, 185);
    private static final Color ROW_ALT      = new Color(248, 249, 250);

    private JLabel totalIncomeLabel;
    private JLabel totalExpenseLabel;
    private JLabel netSavingsLabel;
    private JTable summaryTable;
    private DefaultTableModel tableModel;
    private BarChartPanel barChartPanel;

    private final DashboardFacade     dashboardFacade     = new DashboardFacade();
    private final TransactionService  transactionService  = new TransactionService();
    private long currentUserId = 1L;

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ── Header ────────────────────────────────────────────────────────────
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(HEADER_DARK);

        JButton refreshBtn = new JButton("↺  Refresh");
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        refreshBtn.setBackground(new Color(52, 152, 219));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setOpaque(true);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadDashboard(currentUserId));

        headerRow.add(title,      BorderLayout.WEST);
        headerRow.add(refreshBtn, BorderLayout.EAST);

        // ── Summary cards ─────────────────────────────────────────────────────
        totalIncomeLabel  = new JLabel("$0.00");
        totalExpenseLabel = new JLabel("$0.00");
        netSavingsLabel   = new JLabel("$0.00");

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 14, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.add(buildCard("TOTAL INCOME",  totalIncomeLabel,  CARD_GREEN));
        cardsPanel.add(buildCard("TOTAL EXPENSE", totalExpenseLabel, CARD_RED));
        cardsPanel.add(buildCard("NET SAVINGS",   netSavingsLabel,   CARD_BLUE));

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setOpaque(false);
        northPanel.add(headerRow);
        northPanel.add(Box.createVerticalStrut(14));
        northPanel.add(cardsPanel);

        // ── Monthly breakdown table ───────────────────────────────────────────
        tableModel = new DefaultTableModel(
                new Object[]{"Month", "Income", "Expense", "Net Savings"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        summaryTable = new JTable(tableModel);

        JTableHeader th = summaryTable.getTableHeader();
        th.setBackground(HEADER_DARK);
        th.setForeground(Color.WHITE);
        th.setFont(new Font("SansSerif", Font.BOLD, 13));
        th.setPreferredSize(new Dimension(0, 36));
        th.setReorderingAllowed(false);

        summaryTable.setRowHeight(30);
        summaryTable.setShowVerticalLines(false);
        summaryTable.setGridColor(new Color(235, 235, 235));
        summaryTable.setIntercellSpacing(new Dimension(0, 1));
        summaryTable.setSelectionBackground(new Color(41, 98, 163));
        summaryTable.setSelectionForeground(Color.WHITE);
        summaryTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        summaryTable.setBackground(Color.WHITE);

        summaryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                    setForeground(new Color(50, 50, 50));
                    setFont(new Font("SansSerif", Font.PLAIN, 13));
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                return this;
            }
        });

        DefaultTableCellRenderer amtRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                setHorizontalAlignment(SwingConstants.RIGHT);
                setFont(new Font("SansSerif", Font.BOLD, 13));
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                    if      (col == 1) setForeground(CARD_GREEN);
                    else if (col == 2) setForeground(CARD_RED);
                    else if (col == 3 && v != null) {
                        try {
                            double d = Double.parseDouble(
                                    v.toString().replace("$","").replace(",",""));
                            setForeground(d >= 0 ? CARD_GREEN : CARD_RED);
                        } catch (NumberFormatException ex) { setForeground(new Color(50,50,50)); }
                    }
                }
                return this;
            }
        };
        summaryTable.getColumnModel().getColumn(1).setCellRenderer(amtRenderer);
        summaryTable.getColumnModel().getColumn(2).setCellRenderer(amtRenderer);
        summaryTable.getColumnModel().getColumn(3).setCellRenderer(amtRenderer);

        summaryTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        summaryTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        summaryTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        summaryTable.getColumnModel().getColumn(3).setPreferredWidth(110);

        JScrollPane tableScroll = new JScrollPane(summaryTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(Color.WHITE);

        // ── Bar chart ─────────────────────────────────────────────────────────
        barChartPanel = new BarChartPanel();

        // ── Section wrappers ──────────────────────────────────────────────────
        JPanel tableSection = buildSection("Monthly Breakdown",          tableScroll);
        JPanel chartSection = buildSection("Income vs Expense by Month", barChartPanel);
        chartSection.setPreferredSize(new Dimension(0, 300));

        add(northPanel,    BorderLayout.NORTH);
        add(tableSection,  BorderLayout.CENTER);
        add(chartSection,  BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Data loading
    // ─────────────────────────────────────────────────────────────────────────

    public void loadDashboard(long userId) {
        this.currentUserId = userId;

        double totalIncome  = dashboardFacade.getTotalIncome(userId);
        double totalExpense = dashboardFacade.getTotalExpense(userId);
        double netSavings   = dashboardFacade.getNetSavings(userId);

        totalIncomeLabel.setText(String.format("$%.2f", totalIncome));
        totalExpenseLabel.setText(String.format("$%.2f", totalExpense));
        netSavingsLabel.setText(String.format("$%.2f", netSavings));

        List<Transaction> transactions = transactionService.getTransactionsByUser(userId);
        Map<String, double[]> monthly  = new LinkedHashMap<>();

        for (Transaction t : transactions) {
            String bucket = t.getTransactionDate() != null
                    ? t.getTransactionDate().getYear() + "-"
                      + String.format("%02d", t.getTransactionDate().getMonthValue())
                    : "Unknown";
            monthly.putIfAbsent(bucket, new double[]{0, 0});
            if      ("INCOME" .equalsIgnoreCase(t.getType())) monthly.get(bucket)[0] += t.getAmount();
            else if ("EXPENSE".equalsIgnoreCase(t.getType())) monthly.get(bucket)[1] += t.getAmount();
        }

        tableModel.setRowCount(0);
        for (Map.Entry<String, double[]> e : monthly.entrySet()) {
            double inc = e.getValue()[0], exp = e.getValue()[1];
            tableModel.addRow(new Object[]{
                    e.getKey(),
                    String.format("$%.2f", inc),
                    String.format("$%.2f", exp),
                    String.format("$%.2f", inc - exp)
            });
        }

        barChartPanel.setData(monthly);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Solid-color card with white title + large white value. */
    private JPanel buildCard(String titleText, JLabel valueLabel, Color bg) {
        RoundedCard card = new RoundedCard(bg);
        card.setLayout(new BorderLayout(4, 10));
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel lbl = new JLabel(titleText);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(new Color(220, 235, 255));

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        card.add(lbl,        BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    /** Section with a styled title label above a white rounded card containing content. */
    private JPanel buildSection(String titleText, JComponent content) {
        JPanel section = new JPanel(new BorderLayout(0, 6));
        section.setOpaque(false);

        JLabel sectionLbl = new JLabel(titleText);
        sectionLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        sectionLbl.setForeground(HEADER_DARK);
        sectionLbl.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));

        ShadowCard wrapper = new ShadowCard();
        wrapper.setLayout(new BorderLayout());
        wrapper.add(content, BorderLayout.CENTER);

        section.add(sectionLbl, BorderLayout.NORTH);
        section.add(wrapper,    BorderLayout.CENTER);
        return section;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Painted panel helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Solid-filled rounded rectangle panel (used for summary cards). */
    private static class RoundedCard extends JPanel {
        private final Color bg;
        RoundedCard(Color bg) { this.bg = bg; setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // soft shadow
            g2.setColor(new Color(0, 0, 0, 28));
            g2.fillRoundRect(3, 5, getWidth() - 4, getHeight() - 5, 16, 16);
            // card body
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 5, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** White rounded panel with a subtle drop shadow (used for table/chart sections). */
    private static class ShadowCard extends JPanel {
        ShadowCard() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // shadow
            g2.setColor(new Color(0, 0, 0, 18));
            g2.fillRoundRect(3, 5, getWidth() - 4, getHeight() - 4, 12, 12);
            // white card body
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 5, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bar chart (Java2D)
    // ─────────────────────────────────────────────────────────────────────────

    private static class BarChartPanel extends JPanel {

        private Map<String, double[]> data = new LinkedHashMap<>();
        private double maxVal = 1;

        BarChartPanel() {
            setOpaque(false);
        }

        void setData(Map<String, double[]> data) {
            this.data = data;
            maxVal = 1;
            for (double[] v : data.values())
                maxVal = Math.max(maxVal, Math.max(v[0], v[1]));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            int padL = 70, padR = 24, padT = 36, padB = 40;
            int cW = W - padL - padR;
            int cH = H - padT - padB;
            if (cW <= 0 || cH <= 0) return;

            // Chart area background
            g2.setColor(new Color(250, 251, 252));
            g2.fillRect(padL, padT, cW, cH);

            // ── Y-axis gridlines + labels ──────────────────────────────────────
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();
            int ticks = 4;
            for (int i = 0; i <= ticks; i++) {
                int y   = padT + cH - (int)((double) i / ticks * cH);
                double v = maxVal * i / ticks;
                String lbl = v >= 1000
                        ? String.format("$%.0fK", v / 1000)
                        : String.format("$%.0f", v);

                // gridline
                g2.setColor(i == 0 ? new Color(180, 185, 190) : new Color(218, 222, 226));
                Stroke old = g2.getStroke();
                if (i > 0) g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 1f, new float[]{4, 4}, 0));
                g2.drawLine(padL, y, padL + cW, y);
                g2.setStroke(old);

                // axis label
                g2.setColor(new Color(110, 120, 130));
                g2.drawString(lbl, padL - fm.stringWidth(lbl) - 6,
                        y + fm.getAscent() / 2 - 1);
            }

            // ── Bars ──────────────────────────────────────────────────────────
            int n      = data.size();
            int groupW = cW / Math.max(n, 1);
            // each group has 2 bars + a gap in the middle; leave margins on each side
            int barW   = Math.max(14, Math.min(36, groupW * 2 / 5));
            int gap    = Math.max(4, barW / 5);
            int groupBlockW = 2 * barW + gap;

            Font valFont = new Font("SansSerif", Font.BOLD, 9);
            int groupIdx = 0;

            for (Map.Entry<String, double[]> entry : data.entrySet()) {
                double inc = entry.getValue()[0];
                double exp = entry.getValue()[1];

                int groupX = padL + groupIdx * groupW + (groupW - groupBlockW) / 2;

                // ── Income bar ────────────────────────────────────────────────
                int incH = (int)(inc / maxVal * cH);
                int incX = groupX;
                int incY = padT + cH - incH;

                if (incH > 0) {
                    g2.setColor(CARD_GREEN);
                    // rounded top, flat bottom
                    g2.fillRoundRect(incX, incY, barW, incH, 8, 8);
                    if (incH > 8)
                        g2.fillRect(incX, incY + 4, barW, incH - 4); // flatten bottom half

                    // value label above bar
                    g2.setFont(valFont);
                    fm = g2.getFontMetrics();
                    String iLbl = formatAmt(inc);
                    int ix = incX + (barW - fm.stringWidth(iLbl)) / 2;
                    g2.setColor(new Color(25, 120, 65));
                    g2.drawString(iLbl, ix, incY - 4);
                }

                // ── Expense bar ───────────────────────────────────────────────
                int expH = (int)(exp / maxVal * cH);
                int expX = groupX + barW + gap;
                int expY = padT + cH - expH;

                if (expH > 0) {
                    g2.setColor(CARD_RED);
                    g2.fillRoundRect(expX, expY, barW, expH, 8, 8);
                    if (expH > 8)
                        g2.fillRect(expX, expY + 4, barW, expH - 4);

                    g2.setFont(valFont);
                    fm = g2.getFontMetrics();
                    String eLbl = formatAmt(exp);
                    int ex = expX + (barW - fm.stringWidth(eLbl)) / 2;
                    g2.setColor(new Color(170, 40, 30));
                    g2.drawString(eLbl, ex, expY - 4);
                }

                // ── X-axis label ──────────────────────────────────────────────
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                fm = g2.getFontMetrics();
                g2.setColor(new Color(90, 100, 110));
                String xLbl = entry.getKey();
                int lx = padL + groupIdx * groupW + (groupW - fm.stringWidth(xLbl)) / 2;
                g2.drawString(xLbl, lx, padT + cH + 18);

                groupIdx++;
            }

            // ── Axes ──────────────────────────────────────────────────────────
            g2.setColor(new Color(160, 168, 176));
            g2.drawLine(padL, padT, padL, padT + cH);
            g2.drawLine(padL, padT + cH, padL + cW, padT + cH);

            // ── Legend (top-right of chart area) ──────────────────────────────
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            fm = g2.getFontMetrics();
            int lx = padL + cW - 140;
            int ly = padT - 24;

            g2.setColor(CARD_GREEN);
            g2.fillRoundRect(lx, ly, 12, 12, 4, 4);
            g2.setColor(new Color(60, 70, 80));
            g2.drawString("Income", lx + 16, ly + 10);

            g2.setColor(CARD_RED);
            g2.fillRoundRect(lx + 76, ly, 12, 12, 4, 4);
            g2.setColor(new Color(60, 70, 80));
            g2.drawString("Expense", lx + 92, ly + 10);
        }

        private static String formatAmt(double v) {
            if (v >= 1_000_000) return String.format("$%.1fM", v / 1_000_000);
            if (v >= 1_000)     return String.format("$%.1fK", v / 1_000);
            return String.format("$%.0f", v);
        }
    }
}
