package quizcore.admin;

import quizcore.db.DBConnection;
import quizcore.utils.GradientPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminLogin extends JFrame {

    private final JTextField tfUsername;
    private final JPasswordField pfPassword;
    private final JButton btnLogin;
    private final JButton btnRegister;
    private final JButton btnClose;

    public AdminLogin() {
        setTitle("Faculty Login - QuizCore");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(850, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        GradientPanel root = new GradientPanel();
        root.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 14, 10, 14);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Faculty Login", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        root.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.gridx = 0; gbc.gridy = 1;
        root.add(new JLabel("Mobile (Username):"), gbc);
        tfUsername = new JTextField();
        tfUsername.setColumns(20);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.0;
        root.add(tfUsername, gbc);

        gbc.weightx = 0;
        gbc.gridx = 0; gbc.gridy = 2;
        root.add(new JLabel("Password:"), gbc);
        pfPassword = new JPasswordField();
        pfPassword.setColumns(20);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.0;
        root.add(pfPassword, gbc);

        JPanel pBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        pBtns.setOpaque(false);
        btnLogin = new JButton("Login");
        btnRegister = new JButton("Register");
        btnClose = new JButton("Close");
        pBtns.add(btnLogin);
        pBtns.add(btnRegister);
        pBtns.add(btnClose);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 0;
        root.add(pBtns, gbc);

        setContentPane(root);

        btnLogin.addActionListener(this::onLoginClicked);
        btnRegister.addActionListener(e -> {
            dispose();
            try {
                AdminRegister.main(new String[]{});
            } catch (Throwable t) {
                t.printStackTrace();
                JOptionPane.showMessageDialog(null, "Cannot open register: " + t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnClose.addActionListener(e -> dispose());
    }

    private void onLoginClicked(ActionEvent evt) {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter mobile and password.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT id, name FROM faculty WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    JOptionPane.showMessageDialog(this, "Login successful");
                    SwingUtilities.invokeLater(() -> {
                        AdminHome ah = new AdminHome();
                        ah.setVisible(true);
                    });
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid mobile or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminLogin al = new AdminLogin();
            al.setVisible(true);
        });
    }
}
