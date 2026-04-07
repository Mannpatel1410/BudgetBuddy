package ui;

import model.User;
import service.UserService;

import javax.swing.*;
import java.awt.*;

public class ProfilePanel extends JPanel {
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField phoneField;
    private JButton saveBtn;

    private final UserService userService = new UserService();
    private User currentUser;

    public ProfilePanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Profile", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Name:"), gbc);
        nameField = new JTextField(20);
        gbc.gridx = 1;
        add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Email:"), gbc);
        emailField = new JTextField(20);
        gbc.gridx = 1;
        add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Phone:"), gbc);
        phoneField = new JTextField(20);
        gbc.gridx = 1;
        add(phoneField, gbc);

        saveBtn = new JButton("Save");
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            if (currentUser == null) {
                JOptionPane.showMessageDialog(this, "No profile loaded.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            currentUser.setName(nameField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            String password = new String(passwordField.getPassword());
            if (!password.isEmpty()) {
                currentUser.setPassword(password);
            }
            currentUser.setPhone(phoneField.getText().trim());
            try {
                userService.updateProfile(currentUser);
                JOptionPane.showMessageDialog(this, "Profile updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to update profile: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
    }
}