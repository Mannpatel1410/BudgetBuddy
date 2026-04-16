package ui;

import model.User;
import service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginPanel extends JPanel {

    private static final Color GRAD_TOP = new Color(46, 160, 100);
    private static final Color GRAD_BOT = new Color(22,  90,  56);

    private final JTextField     emailField    = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    private final UserService userService = new UserService();
    private final MainFrame   mainFrame;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setOpaque(true);

        // ── Card ──────────────────────────────────────────────────────────────
        JPanel card = makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(32, 48, 32, 48));

        addLogo(card, "BudgetBuddy", "Personal Finance Manager");
        card.add(Box.createVerticalStrut(26));

        styleField(emailField);
        styleField(passwordField);
        card.add(fieldGroup("Email Address", emailField));
        card.add(Box.createVerticalStrut(14));
        card.add(fieldGroup("Password", passwordField));
        card.add(Box.createVerticalStrut(22));

        JButton loginBtn = primaryButton("Log In");
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(16));

        card.add(divider());
        card.add(Box.createVerticalStrut(12));

        JPanel signupRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        signupRow.setOpaque(false);
        signupRow.setAlignmentX(LEFT_ALIGNMENT);
        signupRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel noAcct = new JLabel("Don't have an account?");
        noAcct.setFont(new Font("SansSerif", Font.PLAIN, 13));
        noAcct.setForeground(new Color(100, 110, 120));
        JButton signupLink = linkButton("Sign Up");
        signupRow.add(noAcct);
        signupRow.add(signupLink);
        card.add(signupRow);

        add(card, new GridBagConstraints());

        loginBtn.addActionListener(e -> attemptLogin());
        emailField.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
        signupLink.addActionListener(e -> mainFrame.showPanel("signup"));
    }

    private void attemptLogin() {
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        User user = userService.login(email, password);
        if (user != null) {
            passwordField.setText("");
            mainFrame.onLoginSuccess(user);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid email or password.", "Login Failed",
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

    // ─────────────────────────────────────────────────────────────────────────
    // Shared static helpers (called by SignupPanel too)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * White rounded card with a soft drop shadow.
     *
     * Shadow geometry: the component is 8px taller and 5px wider than the
     * card body so the shadow peeks out at the right and bottom without the
     * card body ever being clipped.  getPreferredSize() adds those offsets so
     * GridBagLayout always gives us the right total size.
     */
    static JPanel makeCard() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int W = getWidth(), H = getHeight();
                // Shadow — peeks out 5px right, 8px bottom
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(5, 7, W - 5, H - 5, 22, 22);
                // Card body — ends before the shadow peek-out
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, W - 6, H - 8, 22, 22);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                // 500 wide; +6 height so shadow doesn't clip card body content
                return new Dimension(500, d.height + 6);
            }
        };
    }

    static void addLogo(JPanel card, String appName, String subtitle) {
        JLabel logo = new JLabel(appName, SwingConstants.CENTER);
        logo.setFont(new Font("SansSerif", Font.BOLD, 26));   // 26pt — leaves room
        logo.setForeground(new Color(39, 174, 96));
        logo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel(subtitle, SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.ITALIC, 12));
        sub.setForeground(new Color(140, 150, 160));
        sub.setAlignmentX(CENTER_ALIGNMENT);

        card.add(logo);
        card.add(Box.createVerticalStrut(5));
        card.add(sub);
    }

    static void styleField(JTextField f) {
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 226), 1, true),
                BorderFactory.createEmptyBorder(9, 13, 9, 13)));
        f.setBackground(new Color(252, 253, 254));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
    }

    static JPanel fieldGroup(String labelText, JComponent field) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        group.setAlignmentX(LEFT_ALIGNMENT);
        group.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(new Color(70, 80, 95));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 5, 0));
        field.setAlignmentX(LEFT_ALIGNMENT);

        group.add(lbl);
        group.add(field);
        return group;
    }

    static JButton primaryButton(String text) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? new Color(20, 110, 60)
                         : hovered               ? new Color(27, 130, 70)
                         :                         new Color(39, 174, 96);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(200, 44));  // BoxLayout uses preferred height
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    static JButton linkButton(String text) {
        JButton btn = new JButton("<html><u>" + text + "</u></html>");
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(new Color(39, 174, 96));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    static JPanel divider() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JSeparator left  = new JSeparator();
        JSeparator right = new JSeparator();
        left.setForeground(new Color(210, 218, 226));
        right.setForeground(new Color(210, 218, 226));

        JLabel lbl = new JLabel("  or  ");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(new Color(160, 170, 180));

        gc.gridx = 0; row.add(left,  gc);
        gc.gridx = 1; gc.weightx = 0; row.add(lbl, gc);
        gc.gridx = 2; gc.weightx = 1; row.add(right, gc);
        return row;
    }
}
