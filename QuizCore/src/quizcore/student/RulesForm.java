package quizcore.student;

import quizcore.db.DBConnection;
import quizcore.utils.GradientPanel;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.Objects;

public class RulesForm extends JFrame {

    private final JTextArea rulesArea;
    private final int studentId;
    private final String studentName;
    private final String studentMobile;
    private final String subject;

    public RulesForm(int studentId, String studentName, String studentMobile, String subject) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentMobile = studentMobile;
        this.subject = subject;

        setTitle("Rules - QuizCore");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Exam Rules", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        root.add(title, BorderLayout.NORTH);

        rulesArea = new JTextArea();
        rulesArea.setEditable(false);
        rulesArea.setLineWrap(true);
        rulesArea.setWrapStyleWord(true);
        rulesArea.setFont(new Font("SansSerif", Font.PLAIN, 15));

        JScrollPane sp = new JScrollPane(rulesArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        root.add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 10));
        JButton btnStart = new JButton("Start Exam");
        JButton btnCancel = new JButton("Cancel");
        bottom.add(btnStart);
        bottom.add(btnCancel);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);

        btnCancel.addActionListener(e -> dispose());

        btnStart.addActionListener(e -> {
            dispose();
            boolean launched = tryLaunchQuizPanel(this, studentId, studentName, studentMobile, subject);
            if (!launched) {
                JOptionPane.showMessageDialog(null,
                        "Quiz panel could not be launched. Please check that quiz panel exists (quizcore.student.QuizPanel).",
                        "Launch Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        loadRulesFromDB();
    }

    private boolean tryLaunchQuizPanel(Frame parent, int studentId, String name, String mobile, String subject) {
        try {
            Class<?> cls = Class.forName("quizcore.student.QuizPanel");

            try {
                Method m = cls.getMethod("showQuiz", Frame.class, int.class, String.class, String.class, String.class);
                m.invoke(null, parent, studentId, name, mobile, subject);
                return true;
            } catch (NoSuchMethodException ignored) { }

            try {
                Method m = cls.getMethod("showQuiz", int.class, String.class, String.class, String.class);
                m.invoke(null, studentId, name, mobile, subject);
                return true;
            } catch (NoSuchMethodException ignored) { }

            try {
                Constructor<?> c = cls.getConstructor(Frame.class, int.class, String.class, String.class, String.class);
                Object inst = c.newInstance(parent, studentId, name, mobile, subject);
                if (inst instanceof JFrame) {
                    ((JFrame) inst).setLocationRelativeTo(parent);
                    ((JFrame) inst).setVisible(true);
                    return true;
                } else if (inst instanceof JDialog) {
                    ((JDialog) inst).setLocationRelativeTo(parent);
                    ((JDialog) inst).setVisible(true);
                    return true;
                }
            } catch (NoSuchMethodException ignored) { }

            try {
                Constructor<?> c = cls.getConstructor(int.class, String.class, String.class, String.class);
                Object inst = c.newInstance(studentId, name, mobile, subject);
                if (inst instanceof JFrame) {
                    ((JFrame) inst).setLocationRelativeTo(parent);
                    ((JFrame) inst).setVisible(true);
                    return true;
                } else if (inst instanceof JDialog) {
                    ((JDialog) inst).setLocationRelativeTo(parent);
                    ((JDialog) inst).setVisible(true);
                    return true;
                }
            } catch (NoSuchMethodException ignored) { }

            try {
                Constructor<?> c = cls.getConstructor();
                Object inst = c.newInstance();
                try {
                    Method ms = cls.getMethod("setStudentContext", int.class, String.class, String.class, String.class);
                    ms.invoke(inst, studentId, name, mobile, subject);
                    Method show = cls.getMethod("showWindow");
                    show.invoke(inst);
                    return true;
                } catch (NoSuchMethodException ignored2) { }
                if (inst instanceof JFrame) {
                    ((JFrame) inst).setLocationRelativeTo(parent);
                    ((JFrame) inst).setVisible(true);
                    return true;
                } else if (inst instanceof JDialog) {
                    ((JDialog) inst).setLocationRelativeTo(parent);
                    ((JDialog) inst).setVisible(true);
                    return true;
                }
            } catch (NoSuchMethodException ignored) { }

            try {
                Method m = cls.getMethod("main", String[].class);
                m.invoke(null, (Object) new String[] {});
                return true;
            } catch (NoSuchMethodException ignored) { }

            return false;
        } catch (ClassNotFoundException cnf) {
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void loadRulesFromDB() {
        StringBuilder sb = new StringBuilder();
        int numQuestions = 35;
        int timeMinutes = 30;
        boolean negativeEnabled = false;
        double negativeValue = 0.25;
        boolean ability5050 = true;
        boolean abilitySkip = true;
        boolean abilityAddTime = true;
        boolean abilityHint = false;
        int abilityUses = 1;
        boolean autoGrading = true;
        double passPercentage = 40.0;
        boolean showRules = true;

        String sql = "SELECT num_questions, time_minutes, negative_enabled, negative_value, " +
                "ability_5050, ability_skip, ability_addtime, ability_hint, ability_uses, auto_grading, pass_percentage, show_rules " +
                "FROM settings WHERE id = 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                numQuestions = rs.getInt("num_questions");
                timeMinutes = rs.getInt("time_minutes");
                negativeEnabled = rs.getBoolean("negative_enabled");
                try {
                    negativeValue = rs.getBigDecimal("negative_value").doubleValue();
                } catch (Exception ex) {
                    negativeValue = rs.getDouble("negative_value");
                }
                ability5050 = rs.getBoolean("ability_5050");
                abilitySkip = rs.getBoolean("ability_skip");
                abilityAddTime = rs.getBoolean("ability_addtime");
                abilityHint = rs.getBoolean("ability_hint");
                abilityUses = rs.getInt("ability_uses");
                autoGrading = rs.getBoolean("auto_grading");
                try {
                    passPercentage = rs.getBigDecimal("pass_percentage").doubleValue();
                } catch (Exception ex) {
                    passPercentage = rs.getDouble("pass_percentage");
                }
                showRules = rs.getBoolean("show_rules");
            }
        } catch (SQLException ex) {
            sb.append("⚠ Failed to load settings from DB: ").append(ex.getMessage()).append("\n\n");
        }

        if (!showRules) {
            sb.append("Rules are disabled by admin (show_rules = false).\n");
            rulesArea.setText(sb.toString());
            return;
        }

        sb.append("✅ Number of Questions – ").append(numQuestions).append("\n");
        sb.append("✅ Time Limit – ").append(timeMinutes).append(" minutes\n\n");

        sb.append("📌 Marking Scheme:\n");
        sb.append("   • Correct Answer = +1\n");
        if (negativeEnabled) {
            sb.append("   • Wrong Answer = -").append(formatDecimal(negativeValue)).append("\n");
        } else {
            sb.append("   • Wrong Answer = 0\n");
        }
        sb.append("   • Not Attempted = 0\n\n");

        sb.append("📌 Abilities / Lifelines (enabled):\n");
        if (ability5050) sb.append("   • 50-50 → 2 wrong options will be hidden.\n");
        if (abilitySkip)  sb.append("   • Skip → You can skip the question.\n");
        if (abilityAddTime) sb.append("   • Add Time → Get extra time (once if enabled).\n");
        if (abilityHint) sb.append("   • Hint → A small clue will be shown.\n");
        if (!ability5050 && !abilitySkip && !abilityAddTime && !abilityHint) sb.append("   • (No abilities enabled)\n");
        sb.append("   • Ability Uses: Max ").append(abilityUses).append(" per quiz\n\n");

        sb.append("👉 Auto Submit – Quiz will automatically submit when time is up.\n");
        sb.append("👉 Passing Criteria – ").append(formatDecimal(passPercentage)).append("%\n");
        sb.append("👉 Auto Grading – ").append(autoGrading ? "Enabled" : "Disabled").append("\n\n");

        sb.append("⚠ Other Instructions:\n");
        sb.append("   • Once an answer is submitted, it cannot be changed.\n");
        sb.append("   • Quiz must be completed in one attempt.\n");
        sb.append("   • Keep the application in foreground; time will continue running while quiz is active.\n");

        rulesArea.setText(sb.toString());
        rulesArea.setCaretPosition(0);
    }

    private String formatDecimal(double v) {
        if (v == (long) v) return String.format("%d", (long) v);
        return String.format("%.2f", v);
    }

    public static void showForm(Frame parent, int studentId, String name, String mobile, String subject) {
        RulesForm rf = new RulesForm(studentId, name, mobile, subject);
        rf.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RulesForm(1, "Demo Student", "9999999999", "Java").setVisible(true));
    }
}
