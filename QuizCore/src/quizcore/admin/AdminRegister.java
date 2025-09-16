package quizcore.admin;

import quizcore.db.DBConnection;
import quizcore.utils.GradientPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class AdminRegister extends JFrame {

    private JTextField tfUsername;
    private JTextField tfName;
    private JTextField tfFather;
    private JTextField tfMother;
    private JSpinner spinnerDob;
    private JTextField tfEmail;
    private JTextArea taAddress;
    private JComboBox<String> cbDepartment;
    private JTextField tfSubjects;
    private JPasswordField pfPassword;
    private JPasswordField pfConfirm;

    public AdminRegister() {
        setTitle("Faculty Registration - QuizCore");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(850, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        GradientPanel bg = new GradientPanel();
        bg.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Faculty Registration", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitle.setForeground(Color.DARK_GRAY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        bg.add(lblTitle, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;

        int row = 1;

        tfUsername = new JTextField();
        addLabelAndComponent(bg, "Mobile Number (Username):", tfUsername, gbc, row++);
        tfName = new JTextField();
        addLabelAndComponent(bg, "Full Name:", tfName, gbc, row++);
        tfFather = new JTextField();
        addLabelAndComponent(bg, "Father's Name:", tfFather, gbc, row++);
        tfMother = new JTextField();
        addLabelAndComponent(bg, "Mother's Name:", tfMother, gbc, row++);

        spinnerDob = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinnerDob, "yyyy-MM-dd");
        spinnerDob.setEditor(dateEditor);
        addLabelAndComponent(bg, "Date of Birth (YYYY-MM-DD):", spinnerDob, gbc, row++);

        tfEmail = new JTextField();
        addLabelAndComponent(bg, "Email:", tfEmail, gbc, row++);

        taAddress = new JTextArea(3, 20);
        JScrollPane spAddress = new JScrollPane(taAddress);
        addLabelAndComponent(bg, "Address:", spAddress, gbc, row++);

        cbDepartment = new JComboBox<>(new String[]{"CSE", "IT", "ECE", "ME", "Civil", "Other"});
        addLabelAndComponent(bg, "Department:", cbDepartment, gbc, row++);

        tfSubjects = new JTextField();
        addLabelAndComponent(bg, "Subjects (comma separated):", tfSubjects, gbc, row++);

        pfPassword = new JPasswordField();
        addLabelAndComponent(bg, "Password:", pfPassword, gbc, row++);
        pfConfirm = new JPasswordField();
        addLabelAndComponent(bg, "Confirm Password:", pfConfirm, gbc, row++);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);
        JButton btnRegister = new JButton("Register");
        JButton btnBack = new JButton("Back to Login");
        btnPanel.add(btnRegister);
        btnPanel.add(btnBack);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        bg.add(btnPanel, gbc);

        btnRegister.addActionListener(this::onRegister);
        btnBack.addActionListener(e -> {
            dispose();
            try {
                AdminLogin.main(new String[]{});
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        setContentPane(bg);
    }

    private void addLabelAndComponent(JPanel parent, String labelText, Component comp, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        parent.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        parent.add(comp, gbc);
        gbc.weightx = 0;
    }

    private void onRegister(ActionEvent evt) {
        String username = tfUsername.getText().trim();
        String name = tfName.getText().trim();
        String father = tfFather.getText().trim();
        String mother = tfMother.getText().trim();
        java.util.Date dobVal = (java.util.Date) spinnerDob.getValue();
        java.sql.Date dob = new java.sql.Date(dobVal.getTime());
        String email = tfEmail.getText().trim();
        String address = taAddress.getText().trim();
        String department = (String) cbDepartment.getSelectedItem();
        String subjects = tfSubjects.getText().trim();
        String password = new String(pfPassword.getPassword());
        String confirm = new String(pfConfirm.getPassword());

        if (username.isEmpty() || name.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill required fields (Mobile, Name, Password).", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Password and Confirm Password do not match.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (username.length() < 7 || username.length() > 15) {
            int opt = JOptionPane.showConfirmDialog(this, "Mobile number length looks unusual. Continue?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String checkSql = "SELECT COUNT(*) FROM faculty WHERE username = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setString(1, username);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "User with this mobile number already exists. Try login.", "Duplicate", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            String insertSql = "INSERT INTO faculty (username, name, father_name, mother_name, dob, email, address, department, subjects, password) VALUES (?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, username);
                ps.setString(2, name);
                ps.setString(3, father.isEmpty() ? null : father);
                ps.setString(4, mother.isEmpty() ? null : mother);
                ps.setDate(5, dob);
                ps.setString(6, email.isEmpty() ? null : email);
                ps.setString(7, address.isEmpty() ? null : address);
                ps.setString(8, department == null ? null : department);
                ps.setString(9, subjects.isEmpty() ? null : subjects);
                ps.setString(10, password);

                int updated = ps.executeUpdate();
                if (updated > 0) {
                    JOptionPane.showMessageDialog(this, "Registration successful! You will be redirected to Login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    AdminLogin.main(new String[]{});
                } else {
                    JOptionPane.showMessageDialog(this, "Registration failed. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminRegister reg = new AdminRegister();
            reg.setVisible(true);
        });
    }
}
