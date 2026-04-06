package ui;

import decorator.RecurringDecorator;
import decorator.TagDecorator;
import decorator.TaxDecorator;
import factory.TransactionFactory;
import model.transaction.Transaction;
import service.TransactionService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionDialog extends JDialog {
    private JComboBox<String> typeCombo;
    private JTextField amountField;
    private JTextField descriptionField;
    private JComboBox<String> categoryCombo;
    private JCheckBox recurringCheck;
    private JComboBox<String> frequencyCombo;
    private JTextField tagsField;
    private JTextField taxRateField;
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

        JPanel form = new JPanel(new GridLayout(8, 2, 8, 8));
        typeCombo = new JComboBox<>(new String[]{"INCOME", "EXPENSE", "TRANSFER"});
        amountField = new JTextField();
        descriptionField = new JTextField();
        categoryCombo = new JComboBox<>(new String[]{"1", "2", "3"});
        recurringCheck = new JCheckBox();
        frequencyCombo = new JComboBox<>(new String[]{"DAILY", "WEEKLY", "MONTHLY"});
        tagsField = new JTextField();
        taxRateField = new JTextField("0");

        form.add(new JLabel("Type"));
        form.add(typeCombo);
        form.add(new JLabel("Amount"));
        form.add(amountField);
        form.add(new JLabel("Description"));
        form.add(descriptionField);
        form.add(new JLabel("Category"));
        form.add(categoryCombo);
        form.add(new JLabel("Recurring"));
        form.add(recurringCheck);
        form.add(new JLabel("Frequency"));
        form.add(frequencyCombo);
        form.add(new JLabel("Tags (comma separated)"));
        form.add(tagsField);
        form.add(new JLabel("Tax Rate (%)"));
        form.add(taxRateField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveBtn = new JButton("Save");
        cancelBtn = new JButton("Cancel");
        buttons.add(saveBtn);
        buttons.add(cancelBtn);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        recurringCheck.addActionListener(e -> frequencyCombo.setEnabled(recurringCheck.isSelected()));
        frequencyCombo.setEnabled(false);

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
            double taxRate = Double.parseDouble(taxRateField.getText().trim());

            if (description.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Description is required.");
                return;
            }

            Transaction transaction = TransactionFactory.createTransaction(
                    type,
                    1L,
                    categoryId,
                    amount,
                    description,
                    LocalDate.now()
            );

            if (recurringCheck.isSelected()) {
                String frequency = String.valueOf(frequencyCombo.getSelectedItem());
                transaction = new RecurringDecorator(transaction, frequency);
            }

            String tagsRaw = tagsField.getText().trim();
            if (!tagsRaw.isEmpty()) {
                List<String> tags = Arrays.stream(tagsRaw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                if (!tags.isEmpty()) {
                    transaction = new TagDecorator(transaction, tags);
                }
            }

            if (taxRate > 0) {
                transaction = new TaxDecorator(transaction, taxRate);
            }

            boolean success = transactionService.addTransaction(transaction);

            if (!success) {
                JOptionPane.showMessageDialog(this, "Transaction failed validation.");
                return;
            }

            saved = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Amount and tax must be valid numbers.");
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
