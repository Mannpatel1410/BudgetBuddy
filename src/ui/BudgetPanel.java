package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import model.budget.Budget;
import model.category.Category;
import model.Notification;
import service.BudgetService;
import service.CategoryService;
import service.NotificationService;
import java.util.List;

public class BudgetPanel extends JPanel {
    private JPanel budgetListPanel;
    private JButton addBudgetBtn;
    private JButton cloneBtn;
    private JLabel notificationBell;
    private JComboBox<String> monthCombo;
    private JComboBox<Integer> yearCombo;
    private BudgetService budgetService;
    private CategoryService categoryService;
    private NotificationService notificationService;
    private long currentUserId;

    private static final String[] MONTHS = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    public BudgetPanel() {
        budgetService = new BudgetService();
        categoryService = new CategoryService();
        notificationService = new NotificationService();
        setLayout(new BorderLayout(10, 10));

        // Top panel with controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        monthCombo = new JComboBox<>(MONTHS);
        yearCombo = new JComboBox<>(new Integer[]{2024, 2025, 2026, 2027});
        addBudgetBtn = new JButton("Add Budget");
        cloneBtn = new JButton("Clone Previous Month");
        JButton refreshBtn = new JButton("Refresh");

        // Notification bell
        notificationBell = new JLabel("Alerts (0)");
        notificationBell.setForeground(new Color(244, 67, 54));
        notificationBell.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notificationBell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showNotifications();
            }
        });

        topPanel.add(new JLabel("Month:"));
        topPanel.add(monthCombo);
        topPanel.add(new JLabel("Year:"));
        topPanel.add(yearCombo);
        topPanel.add(addBudgetBtn);
        topPanel.add(cloneBtn);
        topPanel.add(refreshBtn);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(notificationBell);

        budgetListPanel = new JPanel();
        budgetListPanel.setLayout(new BoxLayout(budgetListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(budgetListPanel);

        addBudgetBtn.addActionListener(e -> addBudget());
        cloneBtn.addActionListener(e -> clonePreviousMonth());
        refreshBtn.addActionListener(e -> loadBudgets());

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
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

            Category cat = categoryService.getById(b.getCategoryId());
            String catName = cat != null ? cat.getName() : "Unknown";

            JLabel nameLabel = new JLabel(catName);
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
            nameLabel.setPreferredSize(new Dimension(130, 30));

            JProgressBar progressBar = new JProgressBar(0, 100);
            int pct = (int) Math.min(b.getPercentageUsed(), 100);
            progressBar.setValue(pct);
            progressBar.setStringPainted(true);
            progressBar.setString("$" + String.format("%.0f", b.getSpentAmount())
                    + " / $" + String.format("%.0f", b.getLimitAmount())
                    + " (" + String.format("%.0f", b.getPercentageUsed()) + "%)");

            switch (b.getStatusColor()) {
                case "GREEN": progressBar.setForeground(new Color(76, 175, 80)); break;
                case "YELLOW": progressBar.setForeground(new Color(255, 193, 7)); break;
                case "RED": progressBar.setForeground(new Color(244, 67, 54)); break;
            }

            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JLabel statusLabel = new JLabel(b.getStatus());
            statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 11f));
            switch (b.getStatusColor()) {
                case "GREEN": statusLabel.setForeground(new Color(76, 175, 80)); break;
                case "YELLOW": statusLabel.setForeground(new Color(255, 193, 7)); break;
                case "RED": statusLabel.setForeground(new Color(244, 67, 54)); break;
            }

            JButton deleteBtn = new JButton("X");
            deleteBtn.setMargin(new Insets(2, 6, 2, 6));
            deleteBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Delete budget for " + catName + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    budgetService.deleteBudget(b.getId());
                    loadBudgets();
                }
            });

            rightPanel.add(statusLabel);
            rightPanel.add(deleteBtn);

            row.add(nameLabel, BorderLayout.WEST);
            row.add(progressBar, BorderLayout.CENTER);
            row.add(rightPanel, BorderLayout.EAST);

            budgetListPanel.add(row);
        }

        updateNotificationBell();
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
                    if (limit <= 0) {
                        JOptionPane.showMessageDialog(this, "Limit must be greater than 0.");
                        return;
                    }
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
                    JOptionPane.showMessageDialog(this, "Invalid amount. Please enter a number.");
                }
            }
        }
    }

    private void clonePreviousMonth() {
        String currentMonth = (String) monthCombo.getSelectedItem();
        int currentYear = (int) yearCombo.getSelectedItem();

        // Calculate previous month
        int monthIndex = monthCombo.getSelectedIndex();
        String prevMonth;
        int prevYear;
        if (monthIndex == 0) {
            prevMonth = MONTHS[11];
            prevYear = currentYear - 1;
        } else {
            prevMonth = MONTHS[monthIndex - 1];
            prevYear = currentYear;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Clone all budgets from " + prevMonth + " " + prevYear
                + " to " + currentMonth + " " + currentYear + "?",
                "Clone Budgets", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            List<Budget> cloned = budgetService.cloneFromPreviousMonth(
                    currentUserId, prevMonth, prevYear, currentMonth, currentYear);
            if (cloned.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No budgets found for " + prevMonth + " " + prevYear);
            } else {
                JOptionPane.showMessageDialog(this, cloned.size() + " budgets cloned successfully!");
                loadBudgets();
            }
        }
    }

    private void updateNotificationBell() {
        List<Notification> unread = notificationService.getUnread(currentUserId);
        notificationBell.setText("Alerts (" + unread.size() + ")");
        if (unread.size() > 0) {
            notificationBell.setForeground(new Color(244, 67, 54));
        } else {
            notificationBell.setForeground(Color.GRAY);
        }
    }

    private void showNotifications() {
        List<Notification> notifications = notificationService.getNotifications(currentUserId);

        if (notifications.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No notifications.");
            return;
        }

        JPanel notifPanel = new JPanel();
        notifPanel.setLayout(new BoxLayout(notifPanel, BoxLayout.Y_AXIS));

        for (Notification n : notifications) {
            JPanel row = new JPanel(new BorderLayout(5, 5));
            row.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            String prefix = n.isRead() ? "" : "[NEW] ";
            JLabel msgLabel = new JLabel("<html><b>" + prefix + n.getAlertType() + "</b><br>"
                    + n.getMessage() + "<br><i>" + n.getCreatedAt() + "</i></html>");

            if (!n.isRead()) {
                row.setBackground(new Color(255, 245, 238));
                row.setOpaque(true);
            }

            row.add(msgLabel, BorderLayout.CENTER);
            notifPanel.add(row);
        }

        JScrollPane scrollPane = new JScrollPane(notifPanel);
        scrollPane.setPreferredSize(new Dimension(450, 300));

        Object[] options = {"Mark All Read", "Close"};
        int result = JOptionPane.showOptionDialog(this, scrollPane, "Notifications",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);

        if (result == 0) {
            notificationService.markAllAsRead(currentUserId);
            updateNotificationBell();
        }
    }
}