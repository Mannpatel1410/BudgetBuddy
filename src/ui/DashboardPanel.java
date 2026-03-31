package ui;

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

    private final TransactionService transactionService = new TransactionService();
    private final long currentUserId = 1L;

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
        northPanel.add(topBar,    BorderLayout.NORTH);
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

        add(northPanel,  BorderLayout.NORTH);
        add(scrollPane,  BorderLayout.CENTER);

        loadDashboard(currentUserId);
    }

    public void loadDashboard(long userId) {
        List<Transaction> transactions = transactionService.getTransactionsByUser(userId);

        double totalIncome  = 0;
        double totalExpense = 0;

        // Aggregate per "YYYY-MM" bucket
        Map<String, double[]> monthly = new LinkedHashMap<>();

        for (Transaction t : transactions) {
            String bucket = t.getTransactionDate() != null
                    ? t.getTransactionDate().getYear() + "-"
                      + String.format("%02d", t.getTransactionDate().getMonthValue())
                    : "Unknown";

            monthly.putIfAbsent(bucket, new double[]{0, 0});

            if ("INCOME".equalsIgnoreCase(t.getType())) {
                totalIncome += t.getAmount();
                monthly.get(bucket)[0] += t.getAmount();
            } else if ("EXPENSE".equalsIgnoreCase(t.getType())) {
                totalExpense += t.getAmount();
                monthly.get(bucket)[1] += t.getAmount();
            }
        }

        double netSavings = totalIncome - totalExpense;

        totalIncomeLabel.setText(String.format("$%.2f", totalIncome));
        totalExpenseLabel.setText(String.format("$%.2f", totalExpense));
        netSavingsLabel.setText(String.format("$%.2f", netSavings));
        netSavingsLabel.setForeground(netSavings >= 0 ? new Color(33, 150, 243) : new Color(244, 67, 54));

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

        card.add(titleLabel,  BorderLayout.NORTH);
        card.add(valueLabel,  BorderLayout.CENTER);
        return card;
    }
}
