package quizcore.student;

import quizcore.dao.QuestionDAO;
import quizcore.db.DBConnection;
import quizcore.model.Question;
import quizcore.utils.GradientPanel;
import quizcore.admin.StudentResults;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Constructor;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class QuizPanel extends JFrame {

    private final int studentId;
    private final String studentName;
    private final String studentMobile;
    private final String subjectRequested;

    private final List<Question> questions = new ArrayList<>();
    private final Map<Integer, String> answers = new HashMap<>();

    private int currentIndex = 0;
    private int totalTimeSeconds = 0;
    private int remainingSeconds = 0;
    private boolean addTimeUsed = false;
    private int ability5050Uses = 0;
    private boolean ability5050Enabled = false, abilitySkipEnabled = false, abilityAddTimeEnabled = false, abilityHintEnabled = false;
    private double passPercentage = 40.0;
    private int skipUsesLeft = 0;

    private final JLabel lblSubject = new JLabel("", SwingConstants.LEFT);
    private final JLabel lblTimer = new JLabel("", SwingConstants.CENTER);
    private final JLabel lblAnswered = new JLabel("Answered: 0");
    private final JLabel lblNotAnswered = new JLabel("Not Answered: 0");
    private final JLabel lblRemaining = new JLabel("Remaining: 0");

    private final JTextArea taQuestion = new JTextArea();
    private final JRadioButton rbA = new JRadioButton();
    private final JRadioButton rbB = new JRadioButton();
    private final JRadioButton rbC = new JRadioButton();
    private final JRadioButton rbD = new JRadioButton();
    private final ButtonGroup bgOptions = new ButtonGroup();

    private final JButton btn50_50 = new JButton("50-50");
    private final JButton btnSkip = new JButton("Skip");
    private final JButton btnAddTime = new JButton("Add Time");
    private final JButton btnHint = new JButton("Hint");
    private final JButton btnForceQuit = new JButton("Force Quit");
    private final JButton btnPrev = new JButton("Previous");
    private final JButton btnClear = new JButton("Clear Answer");
    private final JButton btnNext = new JButton("Next");
    private final JButton btnSubmit = new JButton("Submit");

    private javax.swing.Timer timer;

    public QuizPanel(int studentId, String studentName, String studentMobile, String subjectRequested) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentMobile = studentMobile;
        this.subjectRequested = subjectRequested;

        setTitle("Quiz - QuizCore");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);
        setSize(1920, 1080);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        lblSubject.setText("Subject: " + (subjectRequested == null ? "All" : subjectRequested));
        lblSubject.setFont(new Font("SansSerif", Font.BOLD, 35));
        lblTimer.setFont(new Font("SansSerif", Font.BOLD, 35));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BorderLayout());

        top.add(lblSubject, BorderLayout.WEST);
        top.add(lblTimer, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel();
        statusPanel.setOpaque(false);
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        lblAnswered.setFont(new Font("SansSerif", Font.PLAIN, 20));
        lblNotAnswered.setFont(new Font("SansSerif", Font.PLAIN, 20));
        lblRemaining.setFont(new Font("SansSerif", Font.PLAIN, 20));
        statusPanel.add(lblAnswered);
        statusPanel.add(lblNotAnswered);
        statusPanel.add(lblRemaining);

        top.add(statusPanel, BorderLayout.SOUTH);

        root.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(6, 6));
        center.setOpaque(false);

        taQuestion.setEditable(false);
        taQuestion.setLineWrap(true);
        taQuestion.setWrapStyleWord(true);
        taQuestion.setFont(new Font("SansSerif", Font.BOLD, 30));
        taQuestion.setOpaque(false);
        taQuestion.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane spQ = new JScrollPane(taQuestion, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        spQ.getViewport().setOpaque(false);
        spQ.setOpaque(false);
        spQ.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        spQ.setPreferredSize(new Dimension(1200, 100));
        center.add(spQ, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 2, 6));
        optionsPanel.setOpaque(false);

        rbA.setActionCommand("A");
        rbB.setActionCommand("B");
        rbC.setActionCommand("C");
        rbD.setActionCommand("D");
        bgOptions.add(rbA);
        bgOptions.add(rbB);
        bgOptions.add(rbC);
        bgOptions.add(rbD);

        Font optionFont = new Font("SansSerif", Font.PLAIN, 18);
        rbA.setFont(optionFont);
        rbB.setFont(optionFont);
        rbC.setFont(optionFont);
        rbD.setFont(optionFont);

        optionsPanel.add(makeOptionPanel(rbA));
        optionsPanel.add(makeOptionPanel(rbB));
        optionsPanel.add(makeOptionPanel(rbC));
        optionsPanel.add(makeOptionPanel(rbD));

        JScrollPane spOpt = new JScrollPane(optionsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        spOpt.getViewport().setOpaque(false);
        spOpt.setOpaque(false);
        spOpt.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        spOpt.setPreferredSize(new Dimension(1400, 420));
        center.add(spOpt, BorderLayout.CENTER);


        root.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        JPanel abilities = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        abilities.setOpaque(false);
        btn50_50.setPreferredSize(new Dimension(95, 32));
        btnSkip.setPreferredSize(new Dimension(95, 32));
        btnAddTime.setPreferredSize(new Dimension(95, 32));
        btnHint.setPreferredSize(new Dimension(95, 32));
        abilities.add(btn50_50);
        abilities.add(btnSkip);
        abilities.add(btnAddTime);
        abilities.add(btnHint);
        bottom.add(abilities, BorderLayout.WEST);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        nav.setOpaque(false);
        btnPrev.setPreferredSize(new Dimension(100, 36));
        btnClear.setPreferredSize(new Dimension(110, 36));
        btnNext.setPreferredSize(new Dimension(100, 36));
        btnSubmit.setPreferredSize(new Dimension(100, 36));
        nav.add(btnPrev);
        nav.add(btnClear);
        nav.add(btnNext);
        nav.add(btnSubmit);
        bottom.add(nav, BorderLayout.CENTER);

        JPanel rightBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        rightBottom.setOpaque(false);
        btnForceQuit.setPreferredSize(new Dimension(110, 36));
        rightBottom.add(btnForceQuit);
        bottom.add(rightBottom, BorderLayout.EAST);

        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);

        loadSettings();
        loadQuestions();

        btnPrev.addActionListener(e -> gotoPrev());
        btnNext.addActionListener(e -> gotoNext());
        btnClear.addActionListener(e -> {
            answers.remove(currentIndex);
            bgOptions.clearSelection();
            updateStatusLabels();
        });

        btn50_50.addActionListener(e -> do50_50());
        btnSkip.addActionListener(e -> doSkip());
        btnAddTime.addActionListener(e -> doAddTime());
        btnHint.addActionListener(e -> doHintVisual());
        btnForceQuit.addActionListener(e -> doForceQuit());
        btnSubmit.addActionListener(e -> doSubmit());

        ActionListener optSel = e -> {
            String sel = bgOptions.getSelection() == null ? null : bgOptions.getSelection().getActionCommand();
            if (sel != null) {
                answers.put(currentIndex, sel);
                updateStatusLabels();
            }
        };
        rbA.addActionListener(optSel);
        rbB.addActionListener(optSel);
        rbC.addActionListener(optSel);
        rbD.addActionListener(optSel);

        startTimer();

        if (!questions.isEmpty()) {
            showQuestion(0);
        } else {
            JOptionPane.showMessageDialog(this, "No questions found for subject: " + subjectRequested, "No Questions", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel makeOptionPanel(JRadioButton rb) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        p.setOpaque(false);
        rb.setFont(new Font("SansSerif", Font.PLAIN, 20));
        rb.setOpaque(false);
        p.add(rb);
        return p;
    }

    private void loadSettings() {
        String sql = "SELECT num_questions, time_minutes, ability_5050, ability_skip, ability_addtime, ability_hint, ability_uses, pass_percentage FROM settings WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int minutes = rs.getInt("time_minutes");
                remainingSeconds = minutes <= 0 ? 30 * 60 : minutes * 60;
                totalTimeSeconds = remainingSeconds;
                ability5050Enabled = rs.getBoolean("ability_5050");
                abilitySkipEnabled = rs.getBoolean("ability_skip");
                abilityAddTimeEnabled = rs.getBoolean("ability_addtime");
                abilityHintEnabled = rs.getBoolean("ability_hint");
                ability5050Uses = rs.getInt("ability_uses");
                skipUsesLeft = rs.getInt("ability_uses");

                try {
                    passPercentage = rs.getBigDecimal("pass_percentage").doubleValue();
                } catch (Exception ex) {
                    passPercentage = rs.getDouble("pass_percentage");
                }
            } else {
                remainingSeconds = 30 * 60;
                totalTimeSeconds = remainingSeconds;
                ability5050Enabled = true;
                abilitySkipEnabled = true;
                abilityAddTimeEnabled = true;
                abilityHintEnabled = false;
                ability5050Uses = 1;
                passPercentage = 40.0;
            }
        } catch (SQLException ex) {
            remainingSeconds = 30 * 60;
            totalTimeSeconds = remainingSeconds;
            ability5050Enabled = true;
            abilitySkipEnabled = true;
            abilityAddTimeEnabled = true;
            abilityHintEnabled = false;
            ability5050Uses = 1;
            passPercentage = 40.0;
        }

        btn50_50.setEnabled(ability5050Enabled && ability5050Uses > 0);
        btnSkip.setEnabled(abilitySkipEnabled && skipUsesLeft > 0);
        btnAddTime.setEnabled(abilityAddTimeEnabled && !addTimeUsed);
        btnHint.setEnabled(abilityHintEnabled);
        lblTimer.setText("Time: " + formatTime(remainingSeconds));
    }

    private void loadQuestions() {
        int num = 20;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT num_questions FROM settings WHERE id = 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) num = rs.getInt(1);
            if (num <= 0) num = 20;
        } catch (Exception e) {
            num = 20;
        }

        List<Question> list = QuestionDAO.getRandomQuestions(subjectRequested, num);
        questions.clear();
        if (list != null) questions.addAll(list);
    }

    private void showQuestion(int idx) {
        if (idx < 0 || idx >= questions.size()) return;
        currentIndex = idx;
        Question q = questions.get(idx);
        taQuestion.setText((idx + 1) + ". " + safe(q.getQuestionText()));

        rbA.setText("A) " + safe(q.getOptionA()));
        rbB.setText("B) " + safe(q.getOptionB()));
        rbC.setText("C) " + safe(q.getOptionC()));
        rbD.setText("D) " + safe(q.getOptionD()));

        bgOptions.clearSelection();
        String sel = answers.get(idx);
        if (sel != null) {
            switch (sel) {
                case "A": rbA.setSelected(true); break;
                case "B": rbB.setSelected(true); break;
                case "C": rbC.setSelected(true); break;
                case "D": rbD.setSelected(true); break;
            }
        }
        updateStatusLabels();
    }

    private String safe(String s) { return s == null ? "" : s; }

    private void updateStatusLabels() {
        int answered = answers.size();
        int total = questions.size();
        int remaining = total - answered;
        lblAnswered.setText("Answered: " + answered);
        lblNotAnswered.setText("Not Answered: " + (total - answered));
        lblRemaining.setText("Remaining: " + remaining);
    }

    private void do50_50() {
        if (!ability5050Enabled || ability5050Uses <= 0) {
            JOptionPane.showMessageDialog(this, "50-50 not available.", "50-50", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Question q = questions.get(currentIndex);
        String correct = q.getCorrectAnswer();
        List<Integer> wrongIdx = new ArrayList<>();
        wrongIdx.clear();
        if (!"A".equalsIgnoreCase(correct)) wrongIdx.add(0);
        if (!"B".equalsIgnoreCase(correct)) wrongIdx.add(1);
        if (!"C".equalsIgnoreCase(correct)) wrongIdx.add(2);
        if (!"D".equalsIgnoreCase(correct)) wrongIdx.add(3);

        if (wrongIdx.size() <= 1) {
            JOptionPane.showMessageDialog(this, "Not enough wrong options to hide.", "50-50", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Collections.shuffle(wrongIdx);
        disableOption(wrongIdx.get(0));
        disableOption(wrongIdx.get(1));
        ability5050Uses--;
        btn50_50.setEnabled(ability5050Uses > 0);
    }

    private void disableOption(int idx) {
        switch (idx) {
            case 0 -> rbA.setEnabled(false);
            case 1 -> rbB.setEnabled(false);
            case 2 -> rbC.setEnabled(false);
            default -> rbD.setEnabled(false);
        }
    }

    private void enableAllOptions() {
        rbA.setEnabled(true);
        rbB.setEnabled(true);
        rbC.setEnabled(true);
        rbD.setEnabled(true);
    }

    private void doSkip() {
    if (!abilitySkipEnabled || skipUsesLeft <= 0) {
        JOptionPane.showMessageDialog(this, "Skip not available.", "Skip", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    answers.put(currentIndex, null);

    skipUsesLeft--;
    if (skipUsesLeft <= 0) {
        btnSkip.setEnabled(false);
    }

    updateStatusLabels();
    gotoNext();
}


    private void doAddTime() {
        if (!abilityAddTimeEnabled || addTimeUsed) {
            JOptionPane.showMessageDialog(this, "Add Time not available.", "Add Time", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        remainingSeconds += 60;
        addTimeUsed = true;
        btnAddTime.setEnabled(false);
        lblTimer.setText("Time: " + formatTime(remainingSeconds));
    }

    private void doHintVisual() {
        if (!abilityHintEnabled) {
            JOptionPane.showMessageDialog(this, "Hint not enabled.", "Hint", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Color old = btnHint.getBackground();
        btnHint.setBackground(new Color(180, 230, 180));
        javax.swing.Timer t = new javax.swing.Timer(700, ev -> btnHint.setBackground(old));
        t.setRepeats(false);
        t.start();

        Question q = questions.get(currentIndex);
        String correct = q.getCorrectAnswer();
        String hintText = "";
        if ("A".equalsIgnoreCase(correct)) hintText = q.getOptionA();
        else if ("B".equalsIgnoreCase(correct)) hintText = q.getOptionB();
        else if ("C".equalsIgnoreCase(correct)) hintText = q.getOptionC();
        else if ("D".equalsIgnoreCase(correct)) hintText = q.getOptionD();
        if (hintText == null || hintText.isEmpty()) hintText = "(no hint available)";
        hintText = hintText.length() > 120 ? hintText.substring(0, 120) + "..." : hintText;
        JOptionPane.showMessageDialog(this, "Hint: " + hintText, "Hint", JOptionPane.INFORMATION_MESSAGE);
    }

    private void doForceQuit() {
        int r = JOptionPane.showConfirmDialog(this, "Force Quit will close the application immediately and will NOT save results. Continue?", "Force Quit", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) System.exit(0);
    }

    private void startTimer() {
        lblTimer.setText("Time: " + formatTime(remainingSeconds));
        timer = new javax.swing.Timer(1000, e -> {
            remainingSeconds--;
            lblTimer.setText("Time: " + formatTime(remainingSeconds));
            if (remainingSeconds <= 0) {
                timer.stop();
                doAutoSubmit();
            }
        });
        timer.setInitialDelay(0);
        timer.start();
    }

    private void doAutoSubmit() {
        JOptionPane.showMessageDialog(this, "Time is up. Auto-submitting the quiz.", "Time Up", JOptionPane.INFORMATION_MESSAGE);
        doSubmit();
    }

    private String formatTime(int seconds) {
        int m = Math.max(0, seconds / 60);
        int s = Math.max(0, seconds % 60);
        return String.format("%02d:%02d", m, s);
    }

    private void gotoNext() {
        enableAllOptions();
        int next = currentIndex + 1;
        if (next >= questions.size()) next = 0;
        showQuestion(next);
    }

    private void gotoPrev() {
        enableAllOptions();
        int prev = currentIndex - 1;
        if (prev < 0) prev = questions.size() - 1;
        showQuestion(prev);
    }

    private void doSubmit() {
        if (timer != null) timer.stop();

        int total = questions.size();
        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            String correct = q.getCorrectAnswer();
            String given = answers.get(i);
            if (given != null && given.equalsIgnoreCase(correct)) score++;
        }
        double perc = total == 0 ? 0.0 : (score * 100.0 / total);
        String status = perc >= passPercentage ? "Pass" : "Fail";
        int timeTaken = totalTimeSeconds - remainingSeconds;

        boolean saved = saveResultToDB(studentName, studentMobile, subjectRequested, score, total, perc, status, timeTaken);

        if (saved) {
            JOptionPane.showMessageDialog(this, "Quiz submitted. Score: " + score + " / " + total + " (" + new DecimalFormat("#0.00").format(perc) + "%)\nSaved to DB.", "Submitted", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Quiz submitted. Score: " + score + " / " + total + " (" + new DecimalFormat("#0.00").format(perc) + "%)\n(But save to DB failed).", "Submitted", JOptionPane.WARNING_MESSAGE);
        }
        
        try {
            StudentResultForm sf = new StudentResultForm(studentMobile);
            sf.setVisible(true);
        } catch (Exception ex) {
            try {
                StudentResults sr = new StudentResults();
                sr.setVisible(true);
            } catch (Exception ignore) {}
        }


        dispose();
    }

    private boolean saveResultToDB(String name, String mobile, String subject, int score, int total, double perc, String status, int timeTakenSeconds) {
        String sql = "INSERT INTO results (student_name, mobile, subject, score, total_marks, percentage, status, time_taken_seconds, attempted_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, mobile);
            ps.setString(3, subject == null ? "All" : subject);
            ps.setInt(4, score);
            ps.setInt(5, total);
            ps.setBigDecimal(6, new java.math.BigDecimal(String.format("%.2f", perc)));
            ps.setString(7, status);
            ps.setInt(8, timeTakenSeconds);
            int r = ps.executeUpdate();
            return r > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            QuizPanel qp = new QuizPanel(1, "Demo Student", "9999999999", "Java");
            qp.setVisible(true);
        });
    }
}
