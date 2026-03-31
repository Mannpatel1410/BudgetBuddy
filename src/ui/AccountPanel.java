package ui;

import model.account.Account;
import service.AccountService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AccountPanel extends JPanel {
    private JTable accountTable;
    private JButton addAccountBtn;
    private JComboBox<String> accountTypeCombo;

    private final AccountService accountService = new AccountService();
    private final long currentUserId = 1L;
    private DefaultTableModel tableModel;

    public AccountPanel() {
        setLayout(new BorderLayout(10, 10));

        tableModel = new DefaultTableModel(new Object[]{"Name", "Type", "Balance"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        accountTable = new JTable(tableModel);
        add(new JScrollPane(accountTable), BorderLayout.CENTER);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addAccountBtn = new JButton("Add Account");
        topBar.add(addAccountBtn);
        add(topBar, BorderLayout.NORTH);

        addAccountBtn.addActionListener(e -> showAddAccountDialog());

        loadAccounts();
    }

    private void loadAccounts() {
        tableModel.setRowCount(0);
        List<Account> accounts = accountService.getAccountsByUser(currentUserId);
        for (Account a : accounts) {
            tableModel.addRow(new Object[]{a.getAccountName(), a.getAccountType(), a.getBalance()});
        }
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
