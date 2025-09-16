package quizcore.admin;

import quizcore.db.DBConnection;
import quizcore.utils.GradientPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;


public class AddQuestion extends JDialog {

    private final JComboBox<String> cbSubject;
    private final JTextArea taQuestion;
    private final JTextField tfA;
    private final JTextField tfB;
    private final JTextField tfC;
    private final JTextField tfD;
    private final JComboBox<String> cbCorrect;

    public AddQuestion(Frame owner) {
        super(owner, "Add Question", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(850, 550);
        setResizable(false);
        setLocationRelativeTo(owner);

        GradientPanel bg = new GradientPanel();
        bg.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel lblTitle = new JLabel("Add Question", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTitle.setForeground(Color.DARK_GRAY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        bg.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        int row = 1;

        cbSubject = new JComboBox<>();
        cbSubject.setEditable(true);
        loadSubjectsFromDB();
        addLabelAndComponent(bg, "Subject:", cbSubject, gbc, row++);

        taQuestion = new JTextArea(5, 30);
        taQuestion.setLineWrap(true);
        taQuestion.setWrapStyleWord(true);
        JScrollPane spQuestion = new JScrollPane(taQuestion, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        addLabelAndComponent(bg, "Question Text:", spQuestion, gbc, row++);

        tfA = new JTextField();
        addLabelAndComponent(bg, "Option A:", tfA, gbc, row++);

        tfB = new JTextField();
        addLabelAndComponent(bg, "Option B:", tfB, gbc, row++);

        tfC = new JTextField();
        addLabelAndComponent(bg, "Option C:", tfC, gbc, row++);

        tfD = new JTextField();
        addLabelAndComponent(bg, "Option D:", tfD, gbc, row++);

        cbCorrect = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        addLabelAndComponent(bg, "Correct Option:", cbCorrect, gbc, row++);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 6));
        btnPanel.setOpaque(false);
        JButton btnSave = new JButton("Save");
        JButton btnClear = new JButton("Clear");
        JButton btnClose = new JButton("Close");
        btnPanel.add(btnSave);
        btnPanel.add(btnClear);
        btnPanel.add(btnClose);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        bg.add(btnPanel, gbc);

        btnSave.addActionListener(this::onSave);
        btnClear.addActionListener(e -> clearForm());
        btnClose.addActionListener(e -> dispose());

        setContentPane(bg);
    }

    private void loadSubjectsFromDB() {
    cbSubject.removeAllItems();
    String sql = "SELECT DISTINCT subject FROM questions WHERE subject IS NOT NULL AND TRIM(subject) <> '' ORDER BY subject";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        boolean found = false;
        while (rs.next()) {
            String s = rs.getString(1);
            cbSubject.addItem(s);
            found = true;
        }
        if (!found) {
            cbSubject.addItem("Other");
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
        cbSubject.removeAllItems();
        cbSubject.addItem("Java");
        cbSubject.addItem("Python");
        cbSubject.addItem("C++");
        cbSubject.addItem("Other");
        JOptionPane.showMessageDialog(this, "Failed to load subjects: " + ex.getMessage(), "DB Error", JOptionPane.WARNING_MESSAGE);
    }
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

    private void onSave(ActionEvent evt) {
        String subject = cbSubject.getEditor().getItem().toString().trim();
        String question = taQuestion.getText().trim();
        String a = tfA.getText().trim();
        String b = tfB.getText().trim();
        String c = tfC.getText().trim();
        String d = tfD.getText().trim();
        String correct = cbCorrect.getSelectedItem().toString();

        if (subject.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter/select Subject.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (question.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the question text.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all four options.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO questions (subject, question_text, option_a, option_b, option_c, option_d, correct_option) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, subject);
            ps.setString(2, question);
            ps.setString(3, a);
            ps.setString(4, b);
            ps.setString(5, c);
            ps.setString(6, d);
            ps.setString(7, correct);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Question saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSubjectsFromDB();

                cbSubject.setSelectedItem(subject);

                taQuestion.setText("");
                tfA.setText("");
                tfB.setText("");
                tfC.setText("");
                tfD.setText("");
                cbCorrect.setSelectedIndex(0);

            } else {
                JOptionPane.showMessageDialog(this, "Failed to save question. No rows inserted.", "Error", JOptionPane.ERROR_MESSAGE);
            }


        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        taQuestion.setText("");
        tfA.setText("");
        tfB.setText("");
        tfC.setText("");
        tfD.setText("");
        cbCorrect.setSelectedIndex(0);
    }

    public static void showDialog(Frame parent) {
        AddQuestion dlg = new AddQuestion(parent);
        dlg.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame testFrame = new JFrame("AddQuestion - Test Host");
            testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            testFrame.setSize(300, 200);
            testFrame.setLocationRelativeTo(null);
            testFrame.setVisible(true);

            AddQuestion.showDialog(testFrame);
            testFrame.dispose();
        });
    }
}
