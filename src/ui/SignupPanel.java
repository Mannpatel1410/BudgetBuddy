package ui;

import service.UserService;

import javax.swing.*;
import java.awt.*;

public class SignupPanel extends JPanel {

    private static final Color GRAD_TOP = new Color(46, 160, 100);
    private static final Color GRAD_BOT = new Color(22,  90,  56);

    private final JTextField     nameField     = new JTextField();
    private final JTextField     emailField    = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JTextField     phoneField    = new JTextField();

    private final UserService userService = new UserService();
    private final MainFrame   mainFrame;

    public SignupPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setOpaque(true);

        // ── Card ──────────────────────────────────────────────────────────────
        JPanel card = LoginPanel.makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(26, 48, 26, 48));

        LoginPanel.addLogo(card, "BudgetBuddy", "Create your free account");
        card.add(Box.createVerticalStrut(20));

        LoginPanel.styleField(nameField);
        LoginPanel.styleField(emailField);
        LoginPanel.styleField(passwordField);
        LoginPanel.styleField(phoneField);

        card.add(LoginPanel.fieldGroup("Full Name",     nameField));
        card.add(Box.createVerticalStrut(12));
        card.add(LoginPanel.fieldGroup("Email Address", emailField));
        card.add(Box.createVerticalStrut(12));
        card.add(LoginPanel.fieldGroup("Password",      passwordField));
        card.add(Box.createVerticalStrut(12));
        card.add(LoginPanel.fieldGroup("Phone Number",  phoneField));
        card.add(Box.createVerticalStrut(20));

        JButton registerBtn = LoginPanel.primaryButton("Create Account");
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(14));

        card.add(LoginPanel.divider());
        card.add(Box.createVerticalStrut(12));

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        backRow.setOpaque(false);
        backRow.setAlignmentX(LEFT_ALIGNMENT);
        backRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel haveAcct = new JLabel("Already have an account?");
        haveAcct.setFont(new Font("SansSerif", Font.PLAIN, 13));
        haveAcct.setForeground(new Color(100, 110, 120));
        JButton loginLink = LoginPanel.linkButton("Log In");
        backRow.add(haveAcct);
        backRow.add(loginLink);
        card.add(backRow);

        add(card, new GridBagConstraints());

        registerBtn.addActionListener(e -> attemptRegister());
        loginLink.addActionListener(e -> mainFrame.showPanel("login"));
    }

    private void attemptRegister() {
        String name     = nameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String phone    = phoneField.getText().trim();

        boolean success = userService.register(name, email, password, phone);
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Account created! Please log in.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            mainFrame.showPanel("login");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Email already in use.", "Registration Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(new GradientPaint(0, 0, GRAD_TOP, 0, getHeight(), GRAD_BOT));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}
