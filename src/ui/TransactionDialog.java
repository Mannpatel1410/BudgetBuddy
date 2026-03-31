package ui;

import service.TransactionService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class TransactionDialog extends JDialog {
    private JComboBox<String> typeCombo;
    private JTextField amountField;
    private JTextField descriptionField;
    private JComboBox<String> categoryCombo;
    private JButton saveBtn;
    private JButton cancelBtn;

    private final TransactionService transactionService;
    private boolean saved;

    public TransactionDialog(JFrame parent) {
        this(parent, new TransactionService());
    }

    public TransactionDialog(JFrame parent, TransactionService transactionService) {
        super(parent, "Add Transaction", true);
        this.transactionService = transactionService;
        this.saved = false;

        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        typeCombo = new JComboBox<>(new String[]{"INCOME", "EXPENSE", "TRANSFER"});
        amountField = new JTextField();
        descriptionField = new JTextField();
        categoryCombo = new JComboBox<>(new String[]{"1", "2", "3"});

        form.add(new JLabel("Type"));
        form.add(typeCombo);
        form.add(new JLabel("Amount"));
        form.add(amountField);
        form.add(new JLabel("Description"));
        form.add(descriptionField);
        form.add(new JLabel("Category"));
        form.add(categoryCombo);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveBtn = new JButton("Save");
        cancelBtn = new JButton("Cancel");
        buttons.add(saveBtn);
        buttons.add(cancelBtn);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> onSave());
        cancelBtn.addActionListener(e -> dispose());

        pack();
    }

    private void onSave() {
        try {
            String type = String.valueOf(typeCombo.getSelectedItem());
            double amount = Double.parseDouble(amountField.getText().trim());
            String description = descriptionField.getText().trim();
            long categoryId = Long.parseLong(String.valueOf(categoryCombo.getSelectedItem()));

            if (description.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Description is required.");
                return;
            }

            boolean success = transactionService.addTransaction(
                    type,
                    1L,
                    categoryId,
                    amount,
                    description,
                    LocalDate.now()
            );

            if (!success) {
                JOptionPane.showMessageDialog(this, "Transaction failed validation.");
                return;
            }

            saved = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Amount must be a valid number.");
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
