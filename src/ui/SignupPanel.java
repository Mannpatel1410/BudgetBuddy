package ui;

import service.UserService;

import javax.swing.*;
import java.awt.*;

public class SignupPanel extends JPanel {
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField phoneField;
    private JButton registerBtn;

    private final UserService userService = new UserService();
    private final MainFrame mainFrame;

    public SignupPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
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

        registerBtn = new JButton("Register");
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        add(registerBtn, gbc);

        JButton backBtn = new JButton("Back to Login");
        gbc.gridy = 6;
        add(backBtn, gbc);

        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String phone = phoneField.getText().trim();
            boolean success = userService.register(name, email, password, phone);
            if (success) {
                JOptionPane.showMessageDialog(this, "Account created! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                switchToLogin();
            } else {
                JOptionPane.showMessageDialog(this, "Email already in use.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        backBtn.addActionListener(e -> switchToLogin());
    }

    private void switchToLogin() {
        mainFrame.showPanel("login");
    }
}
