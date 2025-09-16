package quizcore.admin;

import quizcore.db.DBConnection;
import quizcore.utils.GradientPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;

public class Settings extends JFrame {

    private final JSpinner spNumQuestions;
    private final JSpinner spTimeMinutes;
    private final JCheckBox cb5050;
    private final JCheckBox cbSkip;
    private final JCheckBox cbAddTime;
    private final JCheckBox cbHint;
    private final JSpinner spAbilityUses;
    private final JCheckBox cbNegative;
    private final JSpinner spNegativeValue;
    private final JCheckBox cbAutoGrading;
    private final JSpinner spPassPercent;
    private final JCheckBox cbShowRules;

    private final JButton btnSave;
    private final JButton btnReset;
    private final JButton btnClose;

    private final JLabel lblPreview;

    public Settings() {
        setTitle("Settings - QuizCore");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Settings", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        root.add(title, BorderLayout.NORTH);

        JPanel main = new JPanel(new GridBagLayout());
        main.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.NONE;

        Dimension labelPref = new Dimension(300, 22);
        Dimension smallField = new Dimension(140, 26);
        Dimension previewPref = new Dimension(820, 140);

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        main.add(makeLabel("Number of Questions:" , labelPref), gbc);
        spNumQuestions = new JSpinner(new SpinnerNumberModel(35, 1, 1000, 1));
        compactSpinner(spNumQuestions, smallField);
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        main.add(spNumQuestions, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        main.add(makeLabel("Time Limit (minutes):", labelPref), gbc);
        spTimeMinutes = new JSpinner(new SpinnerNumberModel(30, 1, 10000, 1));
        compactSpinner(spTimeMinutes, smallField);
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        main.add(spTimeMinutes, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTHEAST;
        main.add(makeLabel("Abilities:", labelPref), gbc);
        JPanel pAbilities = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); pAbilities.setOpaque(false);
        cb5050 = new JCheckBox("50-50"); cbSkip = new JCheckBox("Skip"); cbAddTime = new JCheckBox("Add Time"); cbHint = new JCheckBox("Hint");
        pAbilities.add(cb5050); pAbilities.add(cbSkip); pAbilities.add(cbAddTime); pAbilities.add(cbHint);
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        main.add(pAbilities, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        main.add(makeLabel("Ability Uses (per quiz):", labelPref), gbc);
        spAbilityUses = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        compactSpinner(spAbilityUses, smallField);
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        main.add(spAbilityUses, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        main.add(makeLabel("Negative Marking Enabled:", labelPref), gbc);
        cbNegative = new JCheckBox();
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        main.add(cbNegative, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        main.add(makeLabel("Negative Value (per wrong):", labelPref), gbc);
        spNegativeValue = new JSpinner(new SpinnerNumberModel(0.00, 0.00, 10.00, 0.25));
        JSpinner.NumberEditor negEditor = new JSpinner.NumberEditor(spNegativeValue, "0.00");
        spNegativeValue.setEditor(negEditor);
        compactSpinner(spNegativeValue, smallField);
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        main.add(spNegativeValue, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        main.add(makeLabel("Auto Grading:", labelPref), gbc);
        cbAutoGrading = new JCheckBox();
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        main.add(cbAutoGrading, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        main.add(makeLabel("Pass Percentage (%):", labelPref), gbc);
        spPassPercent = new JSpinner(new SpinnerNumberModel(40.00, 0.00, 100.00, 0.50));
        JSpinner.NumberEditor passEditor = new JSpinner.NumberEditor(spPassPercent, "0.00");
        spPassPercent.setEditor(passEditor);
        compactSpinner(spPassPercent, smallField);
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        main.add(spPassPercent, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        main.add(makeLabel("Show Rules on Start:", labelPref), gbc);
        cbShowRules = new JCheckBox();
        gbc.gridx = 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        main.add(cbShowRules, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        lblPreview = new JLabel();
        lblPreview.setBorder(BorderFactory.createTitledBorder("Preview"));
        lblPreview.setPreferredSize(previewPref);
        main.add(lblPreview, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        row++;

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        wrapper.setOpaque(false);
        wrapper.add(main);
        root.add(wrapper, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10)); bottom.setOpaque(false);
        btnReset = new JButton("Reset to Defaults");
        btnSave = new JButton("Save");
        btnClose = new JButton("Close");
        bottom.add(btnReset); bottom.add(btnSave); bottom.add(btnClose);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);

        ActionListener previewUpdater = e -> updatePreview();
        addSpinnerChange(spNumQuestions, previewUpdater);
        addSpinnerChange(spTimeMinutes, previewUpdater);
        addSpinnerChange(spAbilityUses, previewUpdater);
        addSpinnerChange(spNegativeValue, previewUpdater);
        addSpinnerChange(spPassPercent, previewUpdater);

        ItemListener item = e -> updatePreview();
        cb5050.addItemListener(item); cbSkip.addItemListener(item); cbAddTime.addItemListener(item); cbHint.addItemListener(item);
        cbNegative.addItemListener(item); cbAutoGrading.addItemListener(item); cbShowRules.addItemListener(item);

        btnSave.addActionListener(e -> saveSettings());
        btnReset.addActionListener(e -> resetDefaults());
        btnClose.addActionListener(e -> dispose());

        loadSettings();
        updatePreview();
    }

    private JLabel makeLabel(String text, Dimension pref) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setPreferredSize(pref);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return l;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return l;
    }

    private void compactSpinner(JSpinner s, Dimension pref) {
        s.setPreferredSize(pref);
        JComponent ed = s.getEditor();
        if (ed instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) ed).getTextField();
            tf.setHorizontalAlignment(SwingConstants.CENTER);
            tf.setColumns(5);
        }
    }

    private void compactSpinner(JSpinner s) {
        compactSpinner(s, new Dimension(140, 26));
    }

    private void addSpinnerChange(JSpinner s, ActionListener al) {
        s.addChangeListener(e -> al.actionPerformed(null));
    }

    private void updatePreview() {
        int nq = ((Number) spNumQuestions.getValue()).intValue();
        int tm = ((Number) spTimeMinutes.getValue()).intValue();
        String abilities = "";
        if (cb5050.isSelected()) abilities += "50-50 ";
        if (cbSkip.isSelected()) abilities += "Skip ";
        if (cbAddTime.isSelected()) abilities += "AddTime ";
        if (cbHint.isSelected()) abilities += "Hint ";
        int uses = ((Number) spAbilityUses.getValue()).intValue();
        boolean neg = cbNegative.isSelected();
        double negVal = ((Number) spNegativeValue.getValue()).doubleValue();
        boolean auto = cbAutoGrading.isSelected();
        double pass = ((Number) spPassPercent.getValue()).doubleValue();
        boolean rules = cbShowRules.isSelected();

        StringBuilder sb = new StringBuilder("<html>");
        sb.append("<b>Quiz:</b> ").append(nq).append(" questions, ").append(tm).append(" min<br>");
        sb.append("<b>Abilities:</b> ").append(abilities.isEmpty() ? "None" : abilities).append(" (uses=").append(uses).append(")<br>");
        sb.append("<b>Negative:</b> ").append(neg ? ("Yes (" + formatDecimal(negVal) + ")") : "No").append("<br>");
        sb.append("<b>AutoGrading:</b> ").append(auto ? "On" : "Off").append("<br>");
        sb.append("<b>Pass %:</b> ").append(formatDecimal(pass)).append("<br>");
        sb.append("<b>Show Rules:</b> ").append(rules ? "Yes" : "No");
        sb.append("</html>");
        lblPreview.setText(sb.toString());
    }

    private String formatDecimal(double d) {
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(d);
    }

    private void loadSettings() {
        String sql = "SELECT num_questions, time_minutes, ability_5050, ability_skip, ability_addtime, ability_hint, ability_uses, " +
                     "negative_enabled, negative_value, auto_grading, pass_percentage, show_rules FROM settings WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                spNumQuestions.setValue(rs.getInt("num_questions"));
                spTimeMinutes.setValue(rs.getInt("time_minutes"));
                cb5050.setSelected(rs.getBoolean("ability_5050"));
                cbSkip.setSelected(rs.getBoolean("ability_skip"));
                cbAddTime.setSelected(rs.getBoolean("ability_addtime"));
                cbHint.setSelected(rs.getBoolean("ability_hint"));
                spAbilityUses.setValue(rs.getInt("ability_uses"));
                cbNegative.setSelected(rs.getBoolean("negative_enabled"));
                spNegativeValue.setValue(rs.getBigDecimal("negative_value").doubleValue());
                cbAutoGrading.setSelected(rs.getBoolean("auto_grading"));
                spPassPercent.setValue(rs.getBigDecimal("pass_percentage").doubleValue());
                cbShowRules.setSelected(rs.getBoolean("show_rules"));
            } else {
                resetDefaults();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load settings: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            resetDefaults();
        }
        updatePreview();
    }

    private void saveSettings() {
        int nq = ((Number) spNumQuestions.getValue()).intValue();
        int tm = ((Number) spTimeMinutes.getValue()).intValue();
        boolean a5050 = cb5050.isSelected();
        boolean askip = cbSkip.isSelected();
        boolean aadd = cbAddTime.isSelected();
        boolean ahint = cbHint.isSelected();
        int uses = ((Number) spAbilityUses.getValue()).intValue();
        boolean neg = cbNegative.isSelected();
        double negVal = ((Number) spNegativeValue.getValue()).doubleValue();
        boolean auto = cbAutoGrading.isSelected();
        double pass = ((Number) spPassPercent.getValue()).doubleValue();
        boolean rules = cbShowRules.isSelected();

        if (nq <= 0) { JOptionPane.showMessageDialog(this, "Number of questions must be > 0"); return; }
        if (pass < 0 || pass > 100) { JOptionPane.showMessageDialog(this, "Pass % must be 0-100"); return; }

        String sql = "INSERT INTO settings (id, num_questions, time_minutes, ability_5050, ability_skip, ability_addtime, ability_hint, ability_uses, " +
                     "negative_enabled, negative_value, auto_grading, pass_percentage, show_rules) VALUES (1, ?,?,?,?,?,?,?,?,?,?,?,?) " +
                     "ON DUPLICATE KEY UPDATE num_questions=VALUES(num_questions), time_minutes=VALUES(time_minutes), ability_5050=VALUES(ability_5050), " +
                     "ability_skip=VALUES(ability_skip), ability_addtime=VALUES(ability_addtime), ability_hint=VALUES(ability_hint), ability_uses=VALUES(ability_uses), " +
                     "negative_enabled=VALUES(negative_enabled), negative_value=VALUES(negative_value), auto_grading=VALUES(auto_grading), pass_percentage=VALUES(pass_percentage), show_rules=VALUES(show_rules)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nq);
            ps.setInt(2, tm);
            ps.setBoolean(3, a5050);
            ps.setBoolean(4, askip);
            ps.setBoolean(5, aadd);
            ps.setBoolean(6, ahint);
            ps.setInt(7, uses);
            ps.setBoolean(8, neg);
            ps.setBigDecimal(9, BigDecimal.valueOf(negVal));
            ps.setBoolean(10, auto);
            ps.setBigDecimal(11, BigDecimal.valueOf(pass));
            ps.setBoolean(12, rules);
            int r = ps.executeUpdate();
            if (r > 0) {
                JOptionPane.showMessageDialog(this, "Settings saved.");
            } else {
                JOptionPane.showMessageDialog(this, "Settings save returned 0 rows affected.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save settings: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetDefaults() {
        spNumQuestions.setValue(35);
        spTimeMinutes.setValue(30);
        cb5050.setSelected(true);
        cbSkip.setSelected(true);
        cbAddTime.setSelected(true);
        cbHint.setSelected(false);
        spAbilityUses.setValue(1);
        cbNegative.setSelected(false);
        spNegativeValue.setValue(0.00);
        cbAutoGrading.setSelected(true);
        spPassPercent.setValue(40.00);
        cbShowRules.setSelected(true);
        updatePreview();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Settings s = new Settings();
            s.setVisible(true);
        });
    }
}
