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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class TransactionPanel extends JPanel {

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final Color HEADER_DARK  = new Color(44,  62,  80);
    private static final Color GREEN        = new Color(39, 174,  96);
    private static final Color RED          = new Color(192,  57,  43);
    private static final Color BLUE_BORDER  = new Color(41,  98, 163);

    private final TransactionService transactionService;
    private final CategoryService    categoryService;
    private long currentUserId;

    private JTable             transactionTable;
    private DefaultTableModel  tableModel;
    private JButton            addBtn, deleteBtn, undoBtn, redoBtn;
    private JComboBox<String>  filterCombo;
    private JTextField         fromDateField, toDateField;
    private JLabel             incomeLabel, expenseLabel, netLabel;
    private List<Transaction>  displayedTransactions;
    private List<Category>     filterCategories;
    private boolean            reloadingFilter = false;

    public TransactionPanel() {
        this.transactionService      = new TransactionService();
        this.categoryService         = new CategoryService();
        this.displayedTransactions   = new ArrayList<>();
        this.filterCategories        = new ArrayList<>();

        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 246, 248));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        // ── Top bar: title + action buttons ──────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel pageTitle = new JLabel("Transactions");
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        pageTitle.setForeground(HEADER_DARK);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnRow.setOpaque(false);
        addBtn    = filledBtn("+ Add",  GREEN);
        deleteBtn = filledBtn("Delete", RED);
        undoBtn   = outlineBtn("Undo",  BLUE_BORDER);
        redoBtn   = outlineBtn("Redo",  BLUE_BORDER);
        btnRow.add(addBtn);
        btnRow.add(deleteBtn);
        btnRow.add(undoBtn);
        btnRow.add(redoBtn);

        topBar.add(pageTitle, BorderLayout.WEST);
        topBar.add(btnRow,    BorderLayout.EAST);

        // ── Filter bar ────────────────────────────────────────────────────────
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBar.setOpaque(false);
        filterBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        filterCombo = new JComboBox<>(new String[]{"All"});
        filterCombo.setPreferredSize(new Dimension(160, 28));
        filterCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));

        fromDateField = placeholderField("YYYY-MM-DD", 10);
        toDateField   = placeholderField("YYYY-MM-DD", 10);

        JButton applyBtn = new JButton("Filter");
        applyBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        applyBtn.setFocusPainted(false);
        applyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel catLbl  = filterLabel("Category:");
        JLabel fromLbl = filterLabel("From:");
        JLabel toLbl   = filterLabel("To:");

        filterBar.add(catLbl);
        filterBar.add(filterCombo);
        filterBar.add(Box.createHorizontalStrut(6));
        filterBar.add(fromLbl);
        filterBar.add(fromDateField);
        filterBar.add(toLbl);
        filterBar.add(toDateField);
        filterBar.add(applyBtn);

        // ── Table ─────────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(
                new Object[]{"Date", "Type", "Category", "Amount", "Description"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        transactionTable = new JTable(tableModel);

        JTableHeader header = transactionTable.getTableHeader();
        header.setBackground(HEADER_DARK);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 34));
        header.setReorderingAllowed(false);

        transactionTable.setRowHeight(30);
        transactionTable.setShowVerticalLines(false);
        transactionTable.setGridColor(new Color(235, 235, 235));
        transactionTable.setIntercellSpacing(new Dimension(0, 1));
        transactionTable.setSelectionBackground(new Color(41, 98, 163));
        transactionTable.setSelectionForeground(Color.WHITE);
        transactionTable.setFont(new Font("SansSerif", Font.PLAIN, 13));

        transactionTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (!sel) {
                    String type = String.valueOf(tableModel.getValueAt(row, 1));
                    switch (type) {
                        case "INCOME":   setBackground(new Color(237, 250, 238)); break;
                        case "EXPENSE":  setBackground(new Color(255, 244, 244)); break;
                        case "TRANSFER": setBackground(new Color(240, 246, 255)); break;
                        default:         setBackground(Color.WHITE);              break;
                    }
                    if (col == 1) {
                        // Type badge: bold colored text
                        switch (type) {
                            case "INCOME":   setForeground(new Color(27, 152, 80));  break;
                            case "EXPENSE":  setForeground(new Color(200, 40, 40));  break;
                            case "TRANSFER": setForeground(new Color(41, 98, 163));  break;
                            default:         setForeground(Color.BLACK);             break;
                        }
                        setFont(new Font("SansSerif", Font.BOLD, 13));
                        setHorizontalAlignment(SwingConstants.LEFT);
                    } else if (col == 3) {
                        // Amount: bold + colored + right-aligned
                        String t2 = String.valueOf(tableModel.getValueAt(row, 1));
                        setForeground("EXPENSE".equals(t2)
                                ? new Color(200, 40, 40) : new Color(27, 152, 80));
                        setFont(new Font("SansSerif", Font.BOLD, 13));
                        setHorizontalAlignment(SwingConstants.RIGHT);
                    } else {
                        setForeground(new Color(50, 55, 65));
                        setFont(new Font("SansSerif", Font.PLAIN, 13));
                        setHorizontalAlignment(SwingConstants.LEFT);
                    }
                }
                return this;
            }
        });

        transactionTable.getColumnModel().getColumn(0).setPreferredWidth(95);
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        transactionTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        transactionTable.getColumnModel().getColumn(4).setPreferredWidth(260);

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        // ── Summary footer ────────────────────────────────────────────────────
        JPanel footer = new JPanel(new GridLayout(1, 3, 0, 0));
        footer.setBackground(HEADER_DARK);
        footer.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        incomeLabel  = footerLabel("Income: $0.00",  new Color(100, 210, 140));
        expenseLabel = footerLabel("Expense: $0.00", new Color(240, 120, 100));
        netLabel     = footerLabel("Net: $0.00",     Color.WHITE);
        netLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        footer.add(incomeLabel);
        footer.add(expenseLabel);
        footer.add(netLabel);

        // ── Assemble ──────────────────────────────────────────────────────────
        JPanel northStack = new JPanel();
        northStack.setLayout(new BoxLayout(northStack, BoxLayout.Y_AXIS));
        northStack.setOpaque(false);
        northStack.add(topBar);
        northStack.add(filterBar);

        add(northStack,  BorderLayout.NORTH);
        add(scrollPane,  BorderLayout.CENTER);
        add(footer,      BorderLayout.SOUTH);

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
                JOptionPane.showMessageDialog(this, "Select a transaction to delete.");
                return;
            }
            if (selected >= displayedTransactions.size()) return;
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete this transaction?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            long txId = displayedTransactions.get(selected).getId();
            try {
                transactionService.deleteTransaction(txId);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                refreshTable();
            }
        });

        undoBtn.addActionListener(e -> {
            if (!transactionService.canUndo()) {
                JOptionPane.showMessageDialog(this, "Nothing to undo."); return;
            }
            try { transactionService.undo(); }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Undo failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            } finally { refreshTable(); }
        });

        redoBtn.addActionListener(e -> {
            if (!transactionService.canRedo()) {
                JOptionPane.showMessageDialog(this, "Nothing to redo."); return;
            }
            try { transactionService.redo(); }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Redo failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            } finally { refreshTable(); }
        });

        filterCombo.addActionListener(e -> { if (!reloadingFilter) refreshTable(); });
        applyBtn.addActionListener(e -> refreshTable());
        fromDateField.addActionListener(e -> refreshTable());
        toDateField.addActionListener(e -> refreshTable());
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void setUserId(long userId) {
        this.currentUserId = userId;
        reloadFilterCategories();
        refreshTable();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

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

        // Load by category filter
        int selectedIndex = filterCombo.getSelectedIndex();
        List<Transaction> all;
        if (selectedIndex <= 0) {
            all = transactionService.getTransactionsByUser(currentUserId);
        } else {
            Category selectedCat = filterCategories.get(selectedIndex - 1);
            all = transactionService.filterByCategory(currentUserId, selectedCat.getId());
        }

        // Apply date range
        LocalDate from = parseDate(fromDateField.getText().trim());
        LocalDate to   = parseDate(toDateField.getText().trim());
        displayedTransactions = new ArrayList<>();
        for (Transaction t : all) {
            LocalDate d = t.getTransactionDate();
            if (d == null) { displayedTransactions.add(t); continue; }
            if (from != null && d.isBefore(from)) continue;
            if (to   != null && d.isAfter(to))   continue;
            displayedTransactions.add(t);
        }

        // Populate table + tally totals
        double totalIncome = 0, totalExpense = 0;
        for (Transaction t : displayedTransactions) {
            String date = t.getTransactionDate() == null ? ""
                    : t.getTransactionDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            Category cat = categoryService.getById(t.getCategoryId());
            String catName = cat != null ? cat.getName() : String.valueOf(t.getCategoryId());
            tableModel.addRow(new Object[]{
                    date, t.getType(), catName,
                    String.format("$%.2f", t.getAmount()),
                    t.getDescription()
            });
            if ("INCOME".equals(t.getType()))  totalIncome  += t.getAmount();
            if ("EXPENSE".equals(t.getType())) totalExpense += t.getAmount();
        }

        double net = totalIncome - totalExpense;
        incomeLabel .setText(String.format("Income: $%.2f",  totalIncome));
        expenseLabel.setText(String.format("Expense: $%.2f", totalExpense));
        netLabel    .setText(String.format("Net: $%.2f",     net));
        netLabel.setForeground(net >= 0 ? new Color(100, 210, 140) : new Color(240, 120, 100));
    }

    // ── Widget helpers ────────────────────────────────────────────────────────

    private static JButton filledBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }

    private static JButton outlineBtn(String text, Color borderColor) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(borderColor);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        return btn;
    }

    private static JTextField placeholderField(String placeholder, int cols) {
        JTextField f = new JTextField(placeholder, cols);
        f.setFont(new Font("SansSerif", Font.PLAIN, 12));
        f.setForeground(Color.GRAY);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(Color.BLACK); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) { f.setText(placeholder); f.setForeground(Color.GRAY); }
            }
        });
        return f;
    }

    private static JLabel filterLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(new Color(70, 80, 95));
        return lbl;
    }

    private static JLabel footerLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(color);
        return lbl;
    }

    private static LocalDate parseDate(String text) {
        if (text == null || text.isEmpty() || text.equals("YYYY-MM-DD")) return null;
        try { return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE); }
        catch (DateTimeParseException ignored) { return null; }
    }
}
