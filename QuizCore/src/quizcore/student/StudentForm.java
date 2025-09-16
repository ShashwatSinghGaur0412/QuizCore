package quizcore.student;

import quizcore.db.DBConnection;
import quizcore.utils.GradientPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Date;

public class StudentForm extends JFrame {

    private final JTextField tfFullName;
    private final JTextField tfFather;
    private final JTextField tfMother;
    private final JSpinner spDOB;
    private final JRadioButton rbMale;
    private final JRadioButton rbFemale;
    private final JRadioButton rbOther;
    private final JTextField tfMobile;
    private final JTextField tfEmail;
    private final JTextArea taAddress;
    private final JComboBox<String> cbDepartment;
    private final JComboBox<String> cbCourseYear;
    private final JComboBox<String> cbSubject;
    private final JComboBox<String> cbReason;
    private final JButton btnSaveAndNext;
    private final JButton btnClear;
    private final JButton btnCancel;

    public StudentForm() {
        setTitle("Student - QuizCore");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Student Registration", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        root.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Dimension labelPref = new Dimension(180, 26);
        Dimension fieldPref = new Dimension(380, 26);
        Dimension addrPref = new Dimension(380, 80);

        tfFullName = new JTextField(); tfFather = new JTextField(); tfMother = new JTextField();
        spDOB = new JSpinner(new javax.swing.SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spDOB, "yyyy-MM-dd");
        spDOB.setEditor(dateEditor);

        rbMale = new JRadioButton("Male", true);
        rbFemale = new JRadioButton("Female");
        rbOther = new JRadioButton("Other");
        ButtonGroup bg = new ButtonGroup(); bg.add(rbMale); bg.add(rbFemale); bg.add(rbOther);

        tfMobile = new JTextField(); tfEmail = new JTextField();
        taAddress = new JTextArea(); taAddress.setLineWrap(true); taAddress.setWrapStyleWord(true);

        cbDepartment = new JComboBox<>(new String[] {"Select", "CSE", "IT", "ECE", "ME", "Civil", "Other"});
        cbCourseYear = new JComboBox<>(new String[] {"Select", "B.Tech 1st Year", "B.Tech 2nd Year", "B.Tech 3rd Year", "B.Tech 4th Year", "Diploma", "Other"});
        cbSubject = new JComboBox<>();
        cbReason = new JComboBox<>(new String[] {"Practice", "Preparation", "For Fun", "Assessment"});

        tfFullName.setPreferredSize(fieldPref); tfFather.setPreferredSize(fieldPref); tfMother.setPreferredSize(fieldPref);
        spDOB.setPreferredSize(new Dimension(200, 26));
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); genderPanel.setOpaque(false);
        genderPanel.add(rbMale); genderPanel.add(rbFemale); genderPanel.add(rbOther);
        genderPanel.setPreferredSize(fieldPref);

        tfMobile.setPreferredSize(fieldPref); tfEmail.setPreferredSize(fieldPref);
        JScrollPane spAddr = new JScrollPane(taAddress, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        spAddr.setPreferredSize(addrPref);

        cbDepartment.setPreferredSize(fieldPref); cbCourseYear.setPreferredSize(fieldPref);
        cbSubject.setPreferredSize(fieldPref); cbReason.setPreferredSize(fieldPref);

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(makeLabel("Full Name *:", labelPref), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(tfFullName, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(makeLabel("Father's Name:", labelPref), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(tfFather, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(makeLabel("Mother's Name:", labelPref), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(tfMother, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(makeLabel("Date of Birth:", labelPref), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(spDOB, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(makeLabel("Gender:", labelPref), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(genderPanel, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(makeLabel("Mobile Number *:", labelPref), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(tfMobile, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(makeLabel("Email (optional):", labelPref), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(tfEmail, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTHEAST;
        centerPanel.add(makeLabel("Address:", labelPref), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(spAddr, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(makeLabel("Department:", labelPref), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(cbDepartment, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(makeLabel("Course / Year:", labelPref), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(cbCourseYear, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(makeLabel("Exam Subject *:", labelPref), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(cbSubject, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(makeLabel("Reason for attempt:", labelPref), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(cbReason, gbc);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8)); wrapper.setOpaque(false);
        centerPanel.setPreferredSize(new Dimension(820, centerPanel.getPreferredSize().height));
        wrapper.add(centerPanel);
        root.add(wrapper, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10)); bottom.setOpaque(false);
        btnSaveAndNext = new JButton("Save & Continue"); btnClear = new JButton("Clear"); btnCancel = new JButton("Cancel");
        bottom.add(btnSaveAndNext); bottom.add(btnClear); bottom.add(btnCancel);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);

        btnClear.addActionListener(e -> clearForm());
        btnCancel.addActionListener(e -> dispose());
        btnSaveAndNext.addActionListener(e -> onSaveAndContinue());

        loadSubjectsFromDB();
    }

    private JLabel makeLabel(String text, Dimension pref) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setPreferredSize(pref);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return l;
    }

    private void clearForm() {
        tfFullName.setText("");
        tfFather.setText("");
        tfMother.setText("");
        spDOB.setValue(new Date());
        rbMale.setSelected(true);
        tfMobile.setText("");
        tfEmail.setText("");
        taAddress.setText("");
        cbDepartment.setSelectedIndex(0);
        cbCourseYear.setSelectedIndex(0);
        if (cbSubject.getItemCount() > 0) cbSubject.setSelectedIndex(0);
        cbReason.setSelectedIndex(0);
    }

    private void loadSubjectsFromDB() {
        cbSubject.removeAllItems();
        cbSubject.addItem("Select");
        cbSubject.addItem("All (Mix)");
        String sql = "SELECT DISTINCT subject FROM questions WHERE subject IS NOT NULL AND TRIM(subject)<>'' ORDER BY subject";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cbSubject.addItem(rs.getString("subject"));
        } catch (SQLException ex) {
            cbSubject.addItem("Java");
            cbSubject.addItem("Python");
            cbSubject.addItem("C++");
            cbSubject.addItem("JavaScript");
        }
    }

    private void onSaveAndContinue() {
        String fullName = tfFullName.getText().trim();
        String mobile = tfMobile.getText().trim();
        String subject = (String) cbSubject.getSelectedItem();

        if (fullName.isEmpty()) { JOptionPane.showMessageDialog(this, "Please enter full name.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        if (mobile.isEmpty()) { JOptionPane.showMessageDialog(this, "Please enter mobile number.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        if (!mobile.matches("\\d{6,15}")) { JOptionPane.showMessageDialog(this, "Please enter a valid mobile number (digits only).", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        if (subject == null || subject.equals("Select")) { JOptionPane.showMessageDialog(this, "Please choose an exam subject.", "Warning", JOptionPane.WARNING_MESSAGE); return; }

        Date dobDate = (Date) spDOB.getValue();
        String father = tfFather.getText().trim();
        String mother = tfMother.getText().trim();
        String gender = rbMale.isSelected() ? "Male" : rbFemale.isSelected() ? "Female" : "Other";
        String email = tfEmail.getText().trim();
        String address = taAddress.getText().trim();
        String department = (String) cbDepartment.getSelectedItem();
        String course = (String) cbCourseYear.getSelectedItem();
        String reason = (String) cbReason.getSelectedItem();

        int studentId = getStudentIdByMobile(mobile);
        if (studentId > 0) {
            updateStudent(studentId, fullName, father, mother, dobDate, gender, email, address, department, course);
        } else {
            studentId = insertStudent(fullName, father, mother, dobDate, gender, mobile, email, address,
                    "Select".equals(department) ? null : department,
                    "Select".equals(course) ? null : course, subject, reason);
        }

        if (studentId > 0) {
            try {
                Class<?> cls = Class.forName("quizcore.student.RulesForm");
                java.lang.reflect.Method m = cls.getMethod("showForm", Frame.class, int.class, String.class, String.class, String.class);
                m.invoke(null, this, studentId, fullName, mobile, subject);
            } catch (ClassNotFoundException cnf) {
                JOptionPane.showMessageDialog(this, "Saved (id=" + studentId + "). Rules form not found.", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Saved (id=" + studentId + "). (Rules form launch failed.)", "Saved", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save student. Check logs.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getStudentIdByMobile(String mobile) {
        String sql = "SELECT id FROM students WHERE mobile = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mobile);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    private int insertStudent(String fullName, String father, String mother, Date dobDate, String gender,
                              String mobile, String email, String address, String department, String course,
                              String subject, String reason) {
        String sql = "INSERT INTO students (full_name, father_name, mother_name, dob, gender, mobile, email, address, department, course_year, subject, reason) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, fullName);
            ps.setString(2, (father == null || father.isEmpty()) ? null : father);
            ps.setString(3, (mother == null || mother.isEmpty()) ? null : mother);

            if (dobDate == null) ps.setNull(4, Types.DATE);
            else ps.setDate(4, new java.sql.Date(dobDate.getTime()));

            ps.setString(5, gender);
            ps.setString(6, mobile);
            ps.setString(7, (email == null || email.isEmpty()) ? null : email);
            ps.setString(8, (address == null || address.isEmpty()) ? null : address);
            ps.setString(9, department);
            ps.setString(10, course);
            ps.setString(11, subject);
            ps.setString(12, reason);

            int r = ps.executeUpdate();
            if (r > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) return gk.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            if (ex.getSQLState() != null && ex.getSQLState().startsWith("23")) {
                JOptionPane.showMessageDialog(this, "Mobile already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return -1;
    }

    private void updateStudent(int id, String fullName, String father, String mother, Date dobDate, String gender,
                               String email, String address, String department, String course) {
        String sql = "UPDATE students SET full_name = ?, father_name = ?, mother_name = ?, dob = ?, gender = ?, email = ?, address = ?, department = ?, course_year = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, (father == null || father.isEmpty()) ? null : father);
            ps.setString(3, (mother == null || mother.isEmpty()) ? null : mother);
            if (dobDate == null) ps.setNull(4, Types.DATE);
            else ps.setDate(4, new java.sql.Date(dobDate.getTime()));
            ps.setString(5, gender);
            ps.setString(6, (email == null || email.isEmpty()) ? null : email);
            ps.setString(7, (address == null || address.isEmpty()) ? null : address);
            ps.setString(8, department);
            ps.setString(9, course);
            ps.setInt(10, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StudentForm f = new StudentForm();
            f.setVisible(true);
        });
    }
}
