package ui;

import model.transaction.Transaction;
import service.TransactionService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransactionPanel extends JPanel {
    private final TransactionService transactionService;
    private final long currentUserId;

    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JButton addBtn;
    private JButton deleteBtn;
    private JButton undoBtn;
    private JButton redoBtn;
    private JComboBox<String> filterCombo;
    private List<Transaction> displayedTransactions;

    public TransactionPanel() {
        this.transactionService = new TransactionService();
        this.currentUserId = 1L;
        this.displayedTransactions = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addBtn = new JButton("Add");
        deleteBtn = new JButton("Delete");
        undoBtn = new JButton("Undo");
        redoBtn = new JButton("Redo");
        filterCombo = new JComboBox<>(new String[]{"All", "1", "2", "3"});

        topBar.add(addBtn);
        topBar.add(deleteBtn);
        topBar.add(undoBtn);
        topBar.add(redoBtn);
        topBar.add(new JLabel("Category:"));
        topBar.add(filterCombo);

        tableModel = new DefaultTableModel(new Object[]{"Date", "Type", "Category", "Amount", "Description"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transactionTable = new JTable(tableModel);
        add(topBar, BorderLayout.NORTH);
        add(new JScrollPane(transactionTable), BorderLayout.CENTER);

        addBtn.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            TransactionDialog dialog = new TransactionDialog(parentFrame, transactionService);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshTable();
            }
        });

        deleteBtn.addActionListener(e -> {
            int selected = transactionTable.getSelectedRow();
            if (selected < 0) {
                JOptionPane.showMessageDialog(this, "Select a transaction row to delete.");
                return;
            }
            if (selected >= displayedTransactions.size()) {
                return;
            }

            long transactionId = displayedTransactions.get(selected).getId();
            transactionService.deleteTransaction(transactionId);
            refreshTable();
        });

        undoBtn.addActionListener(e -> {
            if (!transactionService.undo()) {
                JOptionPane.showMessageDialog(this, "Nothing to undo.");
                return;
            }
            refreshTable();
        });

        redoBtn.addActionListener(e -> {
            if (!transactionService.redo()) {
                JOptionPane.showMessageDialog(this, "Nothing to redo.");
                return;
            }
            refreshTable();
        });

        filterCombo.addActionListener(e -> refreshTable());

        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);

        String filter = String.valueOf(filterCombo.getSelectedItem());
        if ("All".equals(filter)) {
            displayedTransactions = transactionService.getTransactionsByUser(currentUserId);
        } else {
            long categoryId = Long.parseLong(filter);
            displayedTransactions = transactionService.filterByCategory(currentUserId, categoryId);
        }

        for (Transaction transaction : displayedTransactions) {
            String date = transaction.getTransactionDate() == null ? "" : transaction.getTransactionDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            tableModel.addRow(new Object[]{
                    date,
                    transaction.getType(),
                    transaction.getCategoryId(),
                    transaction.getAmount(),
                    transaction.getDescription()
            });
        }
    }
}
