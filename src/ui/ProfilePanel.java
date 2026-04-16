package ui;

import model.User;
import service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProfilePanel extends JPanel {
    private JTextField     nameField;
    private JTextField     emailField;
    private JPasswordField passwordField;
    private JTextField     phoneField;
    private JButton        saveBtn;
    private JLabel         avatarLabel;   // shows initials circle
    private JLabel         displayName;

    private final UserService userService = new UserService();
    private User currentUser;

    public ProfilePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 248));

        // ── Centre card ───────────────────────────────────────────────────────
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(30, 40, 30, 40)));
        card.setMaximumSize(new Dimension(480, Integer.MAX_VALUE));

        // ── Avatar + name ─────────────────────────────────────────────────────
        avatarLabel = new JLabel("?", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(52, 152, 219));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                int x = (getWidth()  - fm.stringWidth(txt)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(txt, x, y);
                g2.dispose();
            }
        };
        avatarLabel.setFont(avatarLabel.getFont().deriveFont(Font.BOLD, 30f));
        avatarLabel.setPreferredSize(new Dimension(80, 80));
        avatarLabel.setMinimumSize(new Dimension(80, 80));
        avatarLabel.setMaximumSize(new Dimension(80, 80));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        displayName = new JLabel("Profile", SwingConstants.CENTER);
        displayName.setFont(displayName.getFont().deriveFont(Font.BOLD, 20f));
        displayName.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalStrut(8));
        card.add(avatarLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(displayName);
        card.add(Box.createVerticalStrut(24));
        card.add(makeSeparator());
        card.add(Box.createVerticalStrut(20));

        // ── Form fields ───────────────────────────────────────────────────────
        nameField     = styledTextField();
        emailField    = styledTextField();
        passwordField = new JPasswordField();
        styleField(passwordField);
        phoneField    = styledTextField();

        card.add(fieldRow("Name",     nameField));
        card.add(Box.createVerticalStrut(14));
        card.add(fieldRow("Email",    emailField));
        card.add(Box.createVerticalStrut(14));
        card.add(fieldRow("Password", passwordField));
        card.add(Box.createVerticalStrut(14));
        card.add(fieldRow("Phone",    phoneField));
        card.add(Box.createVerticalStrut(24));

        // ── Save button ───────────────────────────────────────────────────────
        saveBtn = new JButton("Save Changes");
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        saveBtn.setBackground(new Color(52, 152, 219));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(saveBtn.getFont().deriveFont(Font.BOLD, 13f));
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setOpaque(true);
        card.add(saveBtn);
        card.add(Box.createVerticalStrut(8));

        // Centre the card horizontally
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(245, 246, 248));
        wrapper.add(card, new GridBagConstraints());

        add(wrapper, BorderLayout.CENTER);

        // ── Save handler ──────────────────────────────────────────────────────
        saveBtn.addActionListener(e -> {
            if (currentUser == null) {
                JOptionPane.showMessageDialog(this, "No profile loaded.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            currentUser.setName(nameField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            String pwd = new String(passwordField.getPassword());
            if (!pwd.isEmpty()) currentUser.setPassword(pwd);
            currentUser.setPhone(phoneField.getText().trim());
            try {
                userService.updateProfile(currentUser);
                updateAvatar(currentUser.getName());
                JOptionPane.showMessageDialog(this,
                        "Profile updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to update: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public void loadProfile(long userId) {
        currentUser = userService.getUserById(userId);
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "User not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        nameField.setText(currentUser.getName());
        emailField.setText(currentUser.getEmail());
        passwordField.setText(currentUser.getPassword());
        phoneField.setText(currentUser.getPhone());
        updateAvatar(currentUser.getName());
    }

    private void updateAvatar(String name) {
        String initials = "?";
        if (name != null && !name.trim().isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            } else {
                initials = ("" + parts[0].charAt(0)).toUpperCase();
            }
        }
        avatarLabel.setText(initials);
        displayName.setText(name != null ? name : "Profile");
        avatarLabel.repaint();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JTextField styledTextField() {
        JTextField f = new JTextField();
        styleField(f);
        return f;
    }

    private void styleField(JTextField f) {
        f.setFont(f.getFont().deriveFont(13f));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
    }

    private JPanel fieldRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(10, 4));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
        lbl.setForeground(new Color(80, 80, 80));
        lbl.setPreferredSize(new Dimension(70, 20));

        row.add(lbl,   BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private JSeparator makeSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(230, 230, 230));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }
}
