package quizcore.student;

import quizcore.db.DBConnection;
import quizcore.utils.GradientPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;

public class StudentResultForm extends JFrame {

    private final String mobile;
    private final int studentId;
    private final JLabel lblName = new JLabel();
    private final JLabel lblSubject = new JLabel();
    private final JLabel lblScore = new JLabel();
    private final JLabel lblPercentage = new JLabel();
    private final JLabel lblStatus = new JLabel();
    private final JLabel lblTimeTaken = new JLabel();
    private final JLabel lblAttemptedAt = new JLabel();
    private final JLabel lblNote = new JLabel("", SwingConstants.CENTER);
    private final DefaultTableModel historyModel = new DefaultTableModel(
            new String[]{"AttemptedAt", "Subject", "Score", "Total", "Percentage", "Status"}, 0);

    public StudentResultForm(String mobile) {
        this.mobile = mobile;
        this.studentId = 0;
        initUI();
        loadLatestAndHistory();
    }

    public StudentResultForm(int studentId) {
        this.studentId = studentId;
        this.mobile = null;
        initUI();
        loadLatestAndHistory();
    }

    private void initUI() {
        setTitle("Your Result - QuizCore");
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Result", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        root.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);

        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        top.add(new JLabel("Student Name:"), gbc);
        gbc.gridx = 1; top.add(lblName, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        top.add(new JLabel("Subject:"), gbc);
        gbc.gridx = 1; top.add(lblSubject, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        top.add(new JLabel("Score:"), gbc);
        gbc.gridx = 1; top.add(lblScore, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        top.add(new JLabel("Percentage:"), gbc);
        gbc.gridx = 1; top.add(lblPercentage, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        top.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; top.add(lblStatus, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        top.add(new JLabel("Time Taken (s):"), gbc);
        gbc.gridx = 1; top.add(lblTimeTaken, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        top.add(new JLabel("Attempted At:"), gbc);
        gbc.gridx = 1; top.add(lblAttemptedAt, gbc);

        center.add(top, BorderLayout.NORTH);

        JPanel mid = new JPanel(new BorderLayout(6,6));
        mid.setOpaque(false);
        JScrollPane histScroll = new JScrollPane(new JTable(historyModel));
        histScroll.setPreferredSize(new Dimension(1000, 220));
        mid.add(new JLabel("Attempt History (latest first)"), BorderLayout.NORTH);
        mid.add(histScroll, BorderLayout.CENTER);
        center.add(mid, BorderLayout.CENTER);

        lblNote.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblNote.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        center.add(lblNote, BorderLayout.SOUTH);

        root.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        bottom.setOpaque(false);
        JButton btnClose = new JButton("Close");
        JButton btnExport = new JButton("Export CSV");
        bottom.add(btnExport);
        bottom.add(btnClose);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);

        btnClose.addActionListener(e -> dispose());
        btnExport.addActionListener(e -> exportCsv());

    }

    private void loadLatestAndHistory() {
        String fetchLatest;
        String fetchHistory;
        if (studentId > 0) {
            fetchLatest = "SELECT student_name,mobile,subject,score,total_marks,percentage,status,time_taken_seconds,attempted_at " +
                    "FROM results WHERE id IN (SELECT id FROM results WHERE student_name IS NOT NULL AND id IS NOT NULL AND 1=1) " +
                    "AND id=(SELECT id FROM results WHERE id>0 AND id IS NOT NULL AND id IN (SELECT id FROM results WHERE 1=1) ORDER BY attempted_at DESC LIMIT 1)"; // defensive fallback
            fetchHistory = "SELECT student_name,mobile,subject,score,total_marks,percentage,status,time_taken_seconds,attempted_at FROM results WHERE id=? ORDER BY attempted_at DESC";
            // We actually prefer mobile path; for studentId only we fall back to mobile lookup after retrieving student's mobile
        } else {
            fetchLatest = "SELECT student_name,mobile,subject,score,total_marks,percentage,status,time_taken_seconds,attempted_at " +
                    "FROM results WHERE mobile = ? ORDER BY attempted_at DESC LIMIT 1";
            fetchHistory = "SELECT student_name,mobile,subject,score,total_marks,percentage,status,time_taken_seconds,attempted_at " +
                    "FROM results WHERE mobile = ? ORDER BY attempted_at DESC";
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (studentId > 0) {
                // fetch student's mobile using students table if exists
                String m = null;
                try (PreparedStatement p = conn.prepareStatement("SELECT mobile, full_name FROM students WHERE id = ?")) {
                    p.setInt(1, studentId);
                    try (ResultSet rs = p.executeQuery()) {
                        if (rs.next()) {
                            m = rs.getString("mobile");
                            lblName.setText(rs.getString("full_name"));
                        }
                    }
                } catch (SQLException ignore) {}

                if (m == null) {
                    try (PreparedStatement p = conn.prepareStatement("SELECT mobile, student_name FROM results WHERE student_name IS NOT NULL ORDER BY attempted_at DESC LIMIT 1")) {
                        try (ResultSet rs = p.executeQuery()) {
                            if (rs.next()) {
                                m = rs.getString("mobile");
                                lblName.setText(rs.getString("student_name"));
                            }
                        }
                    } catch (SQLException ignore) {}
                }
                if (m == null) {
                    showNoResult();
                    return;
                }
                loadByMobile(conn, m);
            } else {
                loadByMobile(conn, mobile);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadByMobile(Connection conn, String mobileVal) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT student_name,mobile,subject,score,total_marks,percentage,status,time_taken_seconds,attempted_at,note FROM results WHERE mobile = ? ORDER BY attempted_at DESC LIMIT 1")) {
            ps.setString(1, mobileVal);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    showNoResult();
                    return;
                }
                String sname = rs.getString("student_name");
                String subj = rs.getString("subject");
                int score = rs.getInt("score");
                int total = rs.getInt("total_marks");
                double pct = rs.getDouble("percentage");
                String status = rs.getString("status");
                int timeTaken = rs.getInt("time_taken_seconds");
                Timestamp at = rs.getTimestamp("attempted_at");
                String note = rs.getString("note");

                lblName.setText(sname != null ? sname : "—");
                lblSubject.setText(subj != null ? subj : "—");
                lblScore.setText(score + " / " + total);
                lblPercentage.setText(formatPct(pct) + "%");
                lblStatus.setText(status != null ? status : "—");
                lblTimeTaken.setText(String.valueOf(timeTaken));
                lblAttemptedAt.setText(at != null ? at.toString() : "—");

                setStatusColor(status, pct);

                String autoNote = computeNote(pct, status, timeTaken, note);
                lblNote.setText(autoNote);

            }
        }

        historyModel.setRowCount(0);
        try (PreparedStatement ph = conn.prepareStatement(
                "SELECT attempted_at, subject, score, total_marks, percentage, status FROM results WHERE mobile = ? ORDER BY attempted_at DESC LIMIT 20")) {
            ph.setString(1, mobileVal);
            try (ResultSet rh = ph.executeQuery()) {
                while (rh.next()) {
                    historyModel.addRow(new Object[]{
                            rh.getTimestamp("attempted_at"),
                            rh.getString("subject"),
                            rh.getInt("score"),
                            rh.getInt("total_marks"),
                            formatPct(rh.getDouble("percentage")) + "%",
                            rh.getString("status")
                    });
                }
            }
        }
    }

    private void showNoResult() {
        lblName.setText("No result found");
        lblSubject.setText("—");
        lblScore.setText("—");
        lblPercentage.setText("—");
        lblStatus.setText("—");
        lblTimeTaken.setText("—");
        lblAttemptedAt.setText("—");
        lblNote.setText("No attempts found for this student.");
    }

    private String computeNote(double pct, String status, int timeTakenSeconds, String storedNote) {
        if (storedNote != null && !storedNote.trim().isEmpty()) return storedNote;
        if ("Pass".equalsIgnoreCase(status)) {
            if (pct >= 90) return "Excellent! Outstanding performance. Keep it up!";
            if (pct >= 70) return "Great job! You're doing very well.";
            return "Good work — you passed. Review weak areas to improve further.";
        } else if ("Fail".equalsIgnoreCase(status)) {
            if (pct >= 40) return "Close! Small improvements will get you across the line.";
            return "Don't worry. Practice more and try again — you will improve.";
        } else {
            return "Result recorded.";
        }
    }

    private void setStatusColor(String status, double pct) {
        if ("Pass".equalsIgnoreCase(status)) {
            lblStatus.setForeground(new Color(12, 102, 27)); // green
            lblPercentage.setForeground(new Color(12, 102, 27));
        } else if ("Fail".equalsIgnoreCase(status)) {
            lblStatus.setForeground(new Color(180, 30, 30)); // red
            lblPercentage.setForeground(new Color(180, 30, 30));
        } else {
            lblStatus.setForeground(Color.DARK_GRAY);
            lblPercentage.setForeground(Color.DARK_GRAY);
        }
    }

    private String formatPct(double v) {
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(v);
    }

    private void exportCsv() {
        if (historyModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.", "Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("results.csv"));
        int opt = fc.showSaveDialog(this);
        if (opt != JFileChooser.APPROVE_OPTION) return;
        java.io.File f = fc.getSelectedFile();
        try (java.io.PrintWriter pw = new java.io.PrintWriter(f)) {
            // header
            for (int c = 0; c < historyModel.getColumnCount(); c++) {
                pw.print(escapeCsv(historyModel.getColumnName(c)));
                if (c + 1 < historyModel.getColumnCount()) pw.print(",");
            }
            pw.println();
            for (int r = 0; r < historyModel.getRowCount(); r++) {
                for (int c = 0; c < historyModel.getColumnCount(); c++) {
                    Object o = historyModel.getValueAt(r, c);
                    pw.print(escapeCsv(o == null ? "" : o.toString()));
                    if (c + 1 < historyModel.getColumnCount()) pw.print(",");
                }
                pw.println();
            }
            JOptionPane.showMessageDialog(this, "Exported to " + f.getAbsolutePath(), "Export", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Export", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escapeCsv(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StudentResultForm f = new StudentResultForm("9123456789");
            f.setVisible(true);
        });
    }
}
