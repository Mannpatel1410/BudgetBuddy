package ui;

import model.account.Account;
import service.AccountService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class AccountPanel extends JPanel {

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final Color HEADER_DARK  = new Color(44,  62,  80);
    private static final Color GREEN        = new Color(39, 174,  96);
    private static final Color ROW_ALT      = new Color(248, 249, 250);

    private JTable             accountTable;
    private JButton            addAccountBtn;
    private JComboBox<String>  accountTypeCombo;
    private JLabel             totalBalanceLabel;

    private final AccountService accountService = new AccountService();
    private long currentUserId = 1L;
    private DefaultTableModel tableModel;

    public AccountPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 246, 248));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        // ── Top bar: title left, Add Account button right ─────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel pageTitle = new JLabel("Accounts");
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        pageTitle.setForeground(HEADER_DARK);

        addAccountBtn = filledBtn("+ Add Account", GREEN);
        addAccountBtn.addActionListener(e -> showAddAccountDialog());

        topBar.add(pageTitle,     BorderLayout.WEST);
        topBar.add(addAccountBtn, BorderLayout.EAST);

        // ── Table ─────────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(
                new Object[]{"Account Name", "Type", "Balance"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        accountTable = new JTable(tableModel);

        JTableHeader header = accountTable.getTableHeader();
        header.setBackground(HEADER_DARK);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 34));
        header.setReorderingAllowed(false);

        accountTable.setRowHeight(30);
        accountTable.setShowVerticalLines(false);
        accountTable.setGridColor(new Color(235, 235, 235));
        accountTable.setIntercellSpacing(new Dimension(0, 1));
        accountTable.setSelectionBackground(new Color(41, 98, 163));
        accountTable.setSelectionForeground(Color.WHITE);
        accountTable.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // Default renderer: alternating rows
        accountTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                    setForeground(new Color(50, 55, 65));
                    setFont(new Font("SansSerif", Font.PLAIN, 13));
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                return this;
            }
        });

        // Type column: colored badge "● TYPE"
        accountTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (!sel && v != null) {
                    setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                    switch (v.toString()) {
                        case "CHECKING":
                            setForeground(new Color(27, 152, 80));
                            setText("● Checking");
                            break;
                        case "SAVINGS":
                            setForeground(new Color(41, 128, 185));
                            setText("● Savings");
                            break;
                        case "CREDIT_CARD":
                            setForeground(new Color(211, 84, 0));
                            setText("● Credit Card");
                            break;
                        default:
                            setForeground(Color.GRAY);
                            setText("● " + v);
                    }
                    setFont(new Font("SansSerif", Font.BOLD, 12));
                }
                return this;
            }
        });

        // Balance column: green/red + right-aligned
        accountTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 14));
                setHorizontalAlignment(SwingConstants.RIGHT);
                setFont(new Font("SansSerif", Font.BOLD, 13));
                if (!sel && v != null) {
                    setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                    try {
                        double d = Double.parseDouble(
                                v.toString().replace("$","").replace(",",""));
                        setForeground(d >= 0 ? new Color(27, 152, 80) : new Color(200, 40, 40));
                    } catch (NumberFormatException ignored) { setForeground(Color.BLACK); }
                }
                return this;
            }
        });

        accountTable.getColumnModel().getColumn(0).setPreferredWidth(240);
        accountTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        accountTable.getColumnModel().getColumn(2).setPreferredWidth(130);

        JScrollPane scrollPane = new JScrollPane(accountTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        // ── Footer: prominent Total Balance card ──────────────────────────────
        JPanel footer = new JPanel(new BorderLayout(0, 0));
        footer.setBackground(HEADER_DARK);
        footer.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel footerCaption = new JLabel("TOTAL BALANCE");
        footerCaption.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footerCaption.setForeground(new Color(160, 185, 210));

        totalBalanceLabel = new JLabel("$0.00");
        totalBalanceLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        totalBalanceLabel.setForeground(Color.WHITE);
        totalBalanceLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        footer.add(footerCaption,     BorderLayout.WEST);
        footer.add(totalBalanceLabel, BorderLayout.EAST);

        add(topBar,     BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(footer,     BorderLayout.SOUTH);
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    public void setUserId(long userId) {
        this.currentUserId = userId;
        loadAccounts();
    }

    private void loadAccounts() {
        tableModel.setRowCount(0);
        List<Account> accounts = accountService.getAccountsByUser(currentUserId);
        double total = 0;
        for (Account a : accounts) {
            tableModel.addRow(new Object[]{
                    a.getAccountName(),
                    a.getAccountType(),
                    String.format("$%.2f", a.getBalance())
            });
            total += a.getBalance();
        }
        totalBalanceLabel.setText(String.format("$%.2f", total));
        totalBalanceLabel.setForeground(total >= 0 ? new Color(100, 210, 140) : new Color(240, 120, 100));
    }

    private void showAddAccountDialog() {
        JTextField nameField = new JTextField(15);
        accountTypeCombo = new JComboBox<>(new String[]{"CHECKING", "SAVINGS", "CREDIT_CARD"});

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Account Name:"));  form.add(nameField);
        form.add(new JLabel("Account Type:"));  form.add(accountTypeCombo);

        int result = JOptionPane.showConfirmDialog(this, form,
                "Add Account", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String type = (String) accountTypeCombo.getSelectedItem();
            if (!name.isEmpty()) {
                accountService.createAccount(type, currentUserId, name);
                loadAccounts();
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static JButton filledBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        return btn;
    }
}
