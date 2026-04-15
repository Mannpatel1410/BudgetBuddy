package ui;

import model.User;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private LoginPanel loginPanel;
    private SignupPanel signupPanel;
    private ProfilePanel profilePanel;
    private AccountPanel accountPanel;
    private TransactionPanel transactionPanel;
    private CategoryPanel categoryPanel;
    private BudgetPanel budgetPanel;
    private DashboardPanel dashboardPanel;
    private ReportPanel reportPanel;

    private JPanel appPanel;
    private CardLayout contentLayout;
    private JPanel contentPanel;

    private User currentUser;

    // Nav button registry — key = panel name, value = sidebar button
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private static final Color NAV_DEFAULT = new Color(52, 73, 94);
    private static final Color NAV_ACTIVE  = new Color(74, 110, 145);

    public MainFrame() {
        setTitle("BudgetBuddy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        loginPanel = new LoginPanel(this);
        signupPanel = new SignupPanel(this);
        profilePanel = new ProfilePanel();
        accountPanel = new AccountPanel();
        transactionPanel = new TransactionPanel();
        categoryPanel = new CategoryPanel();
        budgetPanel = new BudgetPanel();
        dashboardPanel = new DashboardPanel();
        reportPanel = new ReportPanel();

        appPanel = buildAppPanel();

        mainPanel.add(loginPanel, "login");
        mainPanel.add(signupPanel, "signup");
        mainPanel.add(appPanel, "app");

        setContentPane(mainPanel);
        cardLayout.show(mainPanel, "login");
    }

    private JPanel buildAppPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);

        contentPanel.add(dashboardPanel,   "dashboard");
        contentPanel.add(accountPanel,     "accounts");
        contentPanel.add(transactionPanel, "transactions");
        contentPanel.add(categoryPanel,    "categories");
        contentPanel.add(budgetPanel,      "budgets");
        contentPanel.add(reportPanel,      "reports");
        contentPanel.add(profilePanel,     "profile");

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(45, 62, 80));
        sidebar.setPreferredSize(new Dimension(150, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        String[][] navItems = {
            {"Dashboard",    "dashboard"},
            {"Accounts",     "accounts"},
            {"Transactions", "transactions"},
            {"Categories",   "categories"},
            {"Budgets",      "budgets"},
            {"Reports",      "reports"},
            {"Profile",      "profile"}
        };

        for (String[] item : navItems) {
            JButton btn = createNavButton(item[0]);
            navButtons.put(item[1], btn);
            String panelKey = item[1];
            btn.addActionListener(e -> setActivePanel(panelKey));
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(5));
        }

        JButton logoutBtn = createNavButton("Logout");
        logoutBtn.setBackground(new Color(192, 57, 43));
        logoutBtn.addActionListener(e -> {
            currentUser = null;
            navButtons.values().forEach(b -> b.setBackground(NAV_DEFAULT));
            cardLayout.show(mainPanel, "login");
        });
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutBtn);

        panel.add(sidebar, BorderLayout.WEST);
        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JButton createNavButton(String label) {
        JButton btn = new JButton(label);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(NAV_DEFAULT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        return btn;
    }

    /** Show the named content panel and highlight its sidebar button. */
    private void setActivePanel(String panelName) {
        contentLayout.show(contentPanel, panelName);
        navButtons.values().forEach(b -> b.setBackground(NAV_DEFAULT));
        JButton active = navButtons.get(panelName);
        if (active != null) {
            active.setBackground(NAV_ACTIVE);
            active.setFont(active.getFont().deriveFont(Font.BOLD));
        }
        // Restore plain font on all others
        navButtons.forEach((key, btn) -> {
            if (!key.equals(panelName)) btn.setFont(btn.getFont().deriveFont(Font.PLAIN));
        });
    }

    public void setCurrentUser(User user) { this.currentUser = user; }
    public User getCurrentUser()          { return currentUser; }

    public void showPanel(String name) {
        if ("login".equals(name) || "signup".equals(name)) {
            cardLayout.show(mainPanel, name);
        } else {
            cardLayout.show(mainPanel, "app");
            setActivePanel(name);
        }
    }

    public void onLoginSuccess(User user) {
        setCurrentUser(user);
        accountPanel.setUserId(user.getId());
        transactionPanel.setUserId(user.getId());
        categoryPanel.loadTree(user.getId());
        budgetPanel.setUserId(user.getId());
        budgetPanel.loadBudgets();
        reportPanel.setUserId(user.getId());
        dashboardPanel.loadDashboard(user.getId());
        profilePanel.loadProfile(user.getId());
        cardLayout.show(mainPanel, "app");
        setActivePanel("dashboard");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
