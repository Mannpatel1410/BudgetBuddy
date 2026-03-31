package ui;

import javax.swing.*;
import java.awt.*;
import model.budget.Budget;
import model.category.Category;
import service.BudgetService;
import service.CategoryService;
import java.util.List;

public class BudgetPanel extends JPanel {
    private JPanel budgetListPanel;
    private JButton addBudgetBtn;
    private JComboBox<String> monthCombo;
    private JComboBox<Integer> yearCombo;
    private BudgetService budgetService;
    private CategoryService categoryService;
    private long currentUserId;

    private static final String[] MONTHS = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    public BudgetPanel() {
        budgetService = new BudgetService();
        categoryService = new CategoryService();
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        monthCombo = new JComboBox<>(MONTHS);
        yearCombo = new JComboBox<>(new Integer[]{2024, 2025, 2026, 2027});
        addBudgetBtn = new JButton("Add Budget");
        JButton refreshBtn = new JButton("Refresh");

        topPanel.add(new JLabel("Month:"));
        topPanel.add(monthCombo);
        topPanel.add(new JLabel("Year:"));
        topPanel.add(yearCombo);
        topPanel.add(addBudgetBtn);
        topPanel.add(refreshBtn);

        budgetListPanel = new JPanel();
        budgetListPanel.setLayout(new BoxLayout(budgetListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(budgetListPanel);

        addBudgetBtn.addActionListener(e -> addBudget());
        refreshBtn.addActionListener(e -> loadBudgets());

        add(new JLabel("  Budget Manager", JLabel.LEFT), BorderLayout.NORTH);
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setUserId(long userId) {
        this.currentUserId = userId;
    }

    public void loadBudgets() {
        budgetListPanel.removeAll();
        String month = (String) monthCombo.getSelectedItem();
        int year = (int) yearCombo.getSelectedItem();

        List<Budget> budgets = budgetService.getBudgetsForMonth(currentUserId, month, year);

        if (budgets.isEmpty()) {
            budgetListPanel.add(new JLabel("  No budgets set for " + month + " " + year));
        }

        for (Budget b : budgets) {
            JPanel row = new JPanel(new BorderLayout(10, 5));
            row.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

            Category cat = new dao.CategoryDAO().findById(b.getCategoryId());
            String catName = cat != null ? cat.getName() : "Unknown";

            JLabel nameLabel = new JLabel(catName);
            nameLabel.setPreferredSize(new Dimension(120, 30));

            JProgressBar progressBar = new JProgressBar(0, 100);
            int pct = (int) Math.min(b.getPercentageUsed(), 100);
            progressBar.setValue(pct);
            progressBar.setStringPainted(true);
            progressBar.setString("$" + String.format("%.0f", b.getSpentAmount())
                    + " / $" + String.format("%.0f", b.getLimitAmount()));

            switch (b.getStatusColor()) {
                case "GREEN": progressBar.setForeground(new Color(76, 175, 80)); break;
                case "YELLOW": progressBar.setForeground(new Color(255, 193, 7)); break;
                case "RED": progressBar.setForeground(new Color(244, 67, 54)); break;
            }

            JLabel statusLabel = new JLabel(b.getStatus());
            statusLabel.setPreferredSize(new Dimension(150, 30));

            row.add(nameLabel, BorderLayout.WEST);
            row.add(progressBar, BorderLayout.CENTER);
            row.add(statusLabel, BorderLayout.EAST);

            budgetListPanel.add(row);
        }

        budgetListPanel.revalidate();
        budgetListPanel.repaint();
    }

    private void addBudget() {
        List<Category> categories = categoryService.getAllFlat(currentUserId);
        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No categories found. Add categories first.");
            return;
        }

        String[] catNames = categories.stream().map(Category::getName).toArray(String[]::new);
        String selected = (String) JOptionPane.showInputDialog(this, "Select category:",
                "Add Budget", JOptionPane.PLAIN_MESSAGE, null, catNames, catNames[0]);

        if (selected != null) {
            String limitStr = JOptionPane.showInputDialog(this, "Enter budget limit ($):");
            if (limitStr != null) {
                try {
                    double limit = Double.parseDouble(limitStr);
                    Category cat = categories.stream()
                            .filter(c -> c.getName().equals(selected))
                            .findFirst().orElse(null);
                    if (cat != null) {
                        String month = (String) monthCombo.getSelectedItem();
                        int year = (int) yearCombo.getSelectedItem();
                        budgetService.createBudget(currentUserId, cat.getId(), limit, month, year);
                        loadBudgets();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid amount.");
                }
            }
        }
    }
}