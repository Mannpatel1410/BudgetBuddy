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
    private JTable accountTable;
    private JButton addAccountBtn;
    private JComboBox<String> accountTypeCombo;
    private JLabel totalBalanceLabel;

    private final AccountService accountService = new AccountService();
    private long currentUserId = 1L;
    private DefaultTableModel tableModel;

    public AccountPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Table ─────────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(new Object[]{"Account Name", "Type", "Balance"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        accountTable = new JTable(tableModel);

        // Style header
        JTableHeader header = accountTable.getTableHeader();
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 12f));
        header.setPreferredSize(new Dimension(0, 30));
        header.setReorderingAllowed(false);

        accountTable.setRowHeight(28);
        accountTable.setShowVerticalLines(false);
        accountTable.setGridColor(new Color(235, 235, 235));
        accountTable.setIntercellSpacing(new Dimension(0, 1));
        accountTable.setSelectionBackground(new Color(210, 230, 255));
        accountTable.setFont(accountTable.getFont().deriveFont(13f));

        // Color balance column: green positive, red negative, right-aligned
        accountTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (!sel && value != null) {
                    String v = value.toString().replace("$", "").replace(",", "");
                    try {
                        double d = Double.parseDouble(v);
                        setForeground(d >= 0 ? new Color(40, 140, 50) : new Color(200, 40, 40));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } catch (NumberFormatException ignored) {
                        setForeground(Color.BLACK);
                    }
                }
                return this;
            }
        });

        // Alternate row coloring
        accountTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel) setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                return this;
            }
        });

        // Re-apply balance renderer after default renderer (column-level overrides default)
        accountTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                    if (value != null) {
                        String v = value.toString().replace("$", "").replace(",", "");
                        try {
                            double d = Double.parseDouble(v);
                            setForeground(d >= 0 ? new Color(40, 140, 50) : new Color(200, 40, 40));
                            setFont(getFont().deriveFont(Font.BOLD));
                        } catch (NumberFormatException ignored) { setForeground(Color.BLACK); }
                    }
                }
                return this;
            }
        });

        // Column widths
        accountTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        accountTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        accountTable.getColumnModel().getColumn(2).setPreferredWidth(120);

        add(new JScrollPane(accountTable), BorderLayout.CENTER);

        // ── Top bar ───────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addAccountBtn = new JButton("Add Account");
        topBar.add(addAccountBtn);
        add(topBar, BorderLayout.NORTH);

        // ── Footer: total balance ─────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        footer.add(new JLabel("Total Balance:"));
        totalBalanceLabel = new JLabel("$0.00");
        totalBalanceLabel.setFont(totalBalanceLabel.getFont().deriveFont(Font.BOLD, 13f));
        footer.add(totalBalanceLabel);
        add(footer, BorderLayout.SOUTH);

        addAccountBtn.addActionListener(e -> showAddAccountDialog());
    }

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
        totalBalanceLabel.setForeground(total >= 0 ? new Color(40, 140, 50) : new Color(200, 40, 40));
    }

    private void showAddAccountDialog() {
        JTextField nameField = new JTextField(15);
        accountTypeCombo = new JComboBox<>(new String[]{"CHECKING", "SAVINGS", "CREDIT_CARD"});

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Account Name:"));
        form.add(nameField);
        form.add(new JLabel("Account Type:"));
        form.add(accountTypeCombo);

        int result = JOptionPane.showConfirmDialog(this, form, "Add Account", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String type = (String) accountTypeCombo.getSelectedItem();
            if (!name.isEmpty()) {
                accountService.createAccount(type, currentUserId, name);
                loadAccounts();
            }
        }
    }
}
