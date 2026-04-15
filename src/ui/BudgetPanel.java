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
        int currentYear = java.time.LocalDate.now().getYear();
        int currentMonthIndex = java.time.LocalDate.now().getMonthValue() - 1;

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        monthCombo = new JComboBox<>(MONTHS);
        monthCombo.setSelectedIndex(currentMonthIndex);
        yearCombo = new JComboBox<>(new Integer[]{currentYear - 1, currentYear, currentYear + 1, currentYear + 2});
        yearCombo.setSelectedItem(currentYear);
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
        budgetListPanel.setBackground(new Color(245, 245, 245));
        budgetListPanel.setOpaque(true);
        budgetListPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String month = (String) monthCombo.getSelectedItem();
        int year = (int) yearCombo.getSelectedItem();

        List<Budget> budgets = budgetService.getBudgetsForMonth(currentUserId, month, year);

        if (budgets.isEmpty()) {
            JLabel emptyLabel = new JLabel("No budgets set for " + month + " " + year, SwingConstants.CENTER);
            emptyLabel.setFont(emptyLabel.getFont().deriveFont(Font.ITALIC, 13f));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            budgetListPanel.add(Box.createVerticalStrut(30));
            budgetListPanel.add(emptyLabel);
        }

        for (Budget b : budgets) {
            Color accent = statusColor(b);
            String statusText = statusLabel(b);

            Category cat = categoryService.getById(b.getCategoryId());
            String catName = cat != null ? cat.getName() : "Unknown";

            // ── Card ──────────────────────────────────────────────────────────
            JPanel card = new JPanel(new BorderLayout(14, 0));
            card.setBackground(Color.WHITE);
            card.setOpaque(true);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 5, 0, 0, accent),
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1)
                ),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
            ));

            // ── Left: name + remaining ─────────────────────────────────────────
            JPanel leftPanel = new JPanel();
            leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
            leftPanel.setOpaque(false);
            leftPanel.setPreferredSize(new Dimension(145, 56));

            JLabel nameLabel = new JLabel(catName);
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 13f));

            double remaining = b.getLimitAmount() - b.getSpentAmount();
            String remainTxt = remaining >= 0
                    ? String.format("$%.0f left", remaining)
                    : String.format("$%.0f over", -remaining);
            JLabel remainLabel = new JLabel(remainTxt);
            remainLabel.setFont(remainLabel.getFont().deriveFont(Font.PLAIN, 11f));
            remainLabel.setForeground(remaining >= 0 ? new Color(100, 100, 100) : new Color(244, 67, 54));

            leftPanel.add(nameLabel);
            leftPanel.add(Box.createVerticalStrut(5));
            leftPanel.add(remainLabel);

            // ── Center: progress bar ───────────────────────────────────────────
            JProgressBar bar = new JProgressBar(0, 100);
            bar.setValue((int) Math.min(b.getPercentageUsed(), 100));
            bar.setStringPainted(true);
            bar.setString(String.format("$%.0f / $%.0f  (%.0f%%)",
                    b.getSpentAmount(), b.getLimitAmount(), b.getPercentageUsed()));
            bar.setFont(bar.getFont().deriveFont(Font.BOLD, 11f));
            bar.setPreferredSize(new Dimension(0, 30));
            // Force BasicProgressBarUI so setForeground() works on all platforms
            bar.setUI(new javax.swing.plaf.basic.BasicProgressBarUI() {
                @Override protected Color getSelectionForeground() { return Color.WHITE; }
                @Override protected Color getSelectionBackground() { return Color.WHITE; }
            });
            bar.setForeground(accent);
            bar.setBackground(new Color(225, 225, 225));

            // ── Right: status badge + delete ───────────────────────────────────
            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
            rightPanel.setOpaque(false);
            rightPanel.setPreferredSize(new Dimension(160, 56));

            JLabel statusLbl = new JLabel("● " + statusText, SwingConstants.RIGHT);
            statusLbl.setFont(statusLbl.getFont().deriveFont(Font.BOLD, 11f));
            statusLbl.setForeground(accent);
            statusLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

            JButton deleteBtn = new JButton("Delete");
            deleteBtn.setFont(deleteBtn.getFont().deriveFont(Font.PLAIN, 11f));
            deleteBtn.setForeground(new Color(180, 50, 50));
            deleteBtn.setFocusPainted(false);
            deleteBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
            deleteBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Delete budget for " + catName + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    budgetService.deleteBudget(b.getId());
                    loadBudgets();
                }
            });

            rightPanel.add(statusLbl);
            rightPanel.add(Box.createVerticalStrut(6));
            rightPanel.add(deleteBtn);

            card.add(leftPanel, BorderLayout.WEST);
            card.add(bar,       BorderLayout.CENTER);
            card.add(rightPanel, BorderLayout.EAST);

            budgetListPanel.add(card);
            budgetListPanel.add(Box.createVerticalStrut(8));
        }

        updateNotificationBell();
        budgetListPanel.revalidate();
        budgetListPanel.repaint();
    }

    private Color statusColor(Budget b) {
        switch (b.getStatusColor()) {
            case "GREEN":  return new Color(56, 161, 63);
            case "YELLOW": return new Color(230, 130, 0);
            case "RED":    return new Color(220, 50, 50);
            default:       return Color.GRAY;
        }
    }

    private String statusLabel(Budget b) {
        switch (b.getStatus()) {
            case "UNDER_LIMIT":       return "On Track";
            case "APPROACHING_LIMIT": return "Approaching Limit";
            case "EXCEEDED":          return "Exceeded";
            default:                  return b.getStatus();
        }
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