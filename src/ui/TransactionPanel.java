package ui;

import model.category.Category;
import model.transaction.Transaction;
import service.CategoryService;
import service.TransactionService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransactionPanel extends JPanel {
    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private long currentUserId;

    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JButton addBtn;
    private JButton deleteBtn;
    private JButton undoBtn;
    private JButton redoBtn;
    private JComboBox<String> filterCombo;
    private List<Transaction> displayedTransactions;
    private List<Category> filterCategories;
    private boolean reloadingFilter = false;

    public TransactionPanel() {
        this.transactionService = new TransactionService();
        this.categoryService    = new CategoryService();
        this.displayedTransactions = new ArrayList<>();
        this.filterCategories      = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Toolbar ───────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addBtn    = new JButton("Add");
        deleteBtn = new JButton("Delete");
        undoBtn   = new JButton("Undo");
        redoBtn   = new JButton("Redo");
        filterCombo = new JComboBox<>(new String[]{"All"});

        topBar.add(addBtn);
        topBar.add(deleteBtn);
        topBar.add(undoBtn);
        topBar.add(redoBtn);
        topBar.add(new JLabel("Category:"));
        topBar.add(filterCombo);

        // ── Table ─────────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(
                new Object[]{"Date", "Type", "Category", "Amount", "Description"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        transactionTable = new JTable(tableModel);

        // Style header
        JTableHeader header = transactionTable.getTableHeader();
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 12f));
        header.setPreferredSize(new Dimension(0, 30));
        header.setReorderingAllowed(false);

        transactionTable.setRowHeight(26);
        transactionTable.setShowVerticalLines(false);
        transactionTable.setGridColor(new Color(235, 235, 235));
        transactionTable.setIntercellSpacing(new Dimension(0, 1));
        transactionTable.setSelectionBackground(new Color(210, 230, 255));
        transactionTable.setFont(transactionTable.getFont().deriveFont(12f));

        // Color rows by transaction type
        transactionTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel) {
                    String type = String.valueOf(tableModel.getValueAt(row, 1));
                    switch (type) {
                        case "INCOME":   setBackground(new Color(237, 250, 238)); break;
                        case "EXPENSE":  setBackground(new Color(255, 244, 244)); break;
                        case "TRANSFER": setBackground(new Color(240, 246, 255)); break;
                        default:         setBackground(Color.WHITE);              break;
                    }
                    // Bold + colored Type column text
                    if (col == 1) {
                        switch (type) {
                            case "INCOME":   setForeground(new Color(40, 140, 50));  break;
                            case "EXPENSE":  setForeground(new Color(200, 40, 40));  break;
                            case "TRANSFER": setForeground(new Color(33, 110, 200)); break;
                            default:         setForeground(Color.BLACK);             break;
                        }
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        setForeground(Color.BLACK);
                        setFont(getFont().deriveFont(Font.PLAIN));
                    }
                    // Right-align Amount column
                    setHorizontalAlignment(col == 3 ? SwingConstants.RIGHT : SwingConstants.LEFT);
                }
                return this;
            }
        });

        // Column widths
        transactionTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        transactionTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        transactionTable.getColumnModel().getColumn(4).setPreferredWidth(250);

        add(topBar, BorderLayout.NORTH);
        add(new JScrollPane(transactionTable), BorderLayout.CENTER);

        // ── Listeners ─────────────────────────────────────────────────────────
        addBtn.addActionListener(e -> {
            if (currentUserId <= 0) return;
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            TransactionDialog dialog = new TransactionDialog(parentFrame, transactionService, currentUserId);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            if (dialog.isSaved()) refreshTable();
        });

        deleteBtn.addActionListener(e -> {
            int selected = transactionTable.getSelectedRow();
            if (selected < 0) {
                JOptionPane.showMessageDialog(this, "Select a transaction row to delete.");
                return;
            }
            if (selected >= displayedTransactions.size()) return;
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete this transaction?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            long transactionId = displayedTransactions.get(selected).getId();
            try {
                transactionService.deleteTransaction(transactionId);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                refreshTable();
            }
        });

        undoBtn.addActionListener(e -> {
            if (!transactionService.canUndo()) {
                JOptionPane.showMessageDialog(this, "Nothing to undo.");
                return;
            }
            try {
                transactionService.undo();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Undo failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                refreshTable();
            }
        });

        redoBtn.addActionListener(e -> {
            if (!transactionService.canRedo()) {
                JOptionPane.showMessageDialog(this, "Nothing to redo.");
                return;
            }
            try {
                transactionService.redo();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Redo failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                refreshTable();
            }
        });

        filterCombo.addActionListener(e -> {
            if (!reloadingFilter) refreshTable();
        });
    }

    public void setUserId(long userId) {
        this.currentUserId = userId;
        reloadFilterCategories();
        refreshTable();
    }

    private void reloadFilterCategories() {
        reloadingFilter = true;
        filterCategories = categoryService.getAllFlat(currentUserId);
        filterCombo.removeAllItems();
        filterCombo.addItem("All");
        for (Category cat : filterCategories) filterCombo.addItem(cat.getName());
        reloadingFilter = false;
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        if (currentUserId <= 0) return;

        int selectedIndex = filterCombo.getSelectedIndex();
        if (selectedIndex <= 0) {
            displayedTransactions = transactionService.getTransactionsByUser(currentUserId);
        } else {
            Category selectedCat = filterCategories.get(selectedIndex - 1);
            displayedTransactions = transactionService.filterByCategory(currentUserId, selectedCat.getId());
        }

        for (Transaction t : displayedTransactions) {
            String date = t.getTransactionDate() == null ? ""
                    : t.getTransactionDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            Category cat = categoryService.getById(t.getCategoryId());
            String catName = cat != null ? cat.getName() : String.valueOf(t.getCategoryId());
            tableModel.addRow(new Object[]{
                    date,
                    t.getType(),
                    catName,
                    String.format("$%.2f", t.getAmount()),
                    t.getDescription()
            });
        }
    }
}
