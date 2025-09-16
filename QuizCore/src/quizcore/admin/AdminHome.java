package quizcore.admin;

import quizcore.db.DBConnection;
import quizcore.utils.GradientPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import quizcore.main.WelcomeScreen;


public class AdminHome extends JFrame {

    private final JButton btnAddQuestion;
    private final JButton btnManageQuestion;
    private final JButton btnAllQuestions;
    private final JButton btnStudentResults;
    private final JButton btnSettings;
    private final JButton centeredLogout;
    private final JLabel lblStats;
    private boolean moduleOpen = false;

    public AdminHome() {
        setTitle("Admin Home - QuizCore");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1920, 1080);
        setLocationRelativeTo(null);
        setResizable(true);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        root.add(topBar, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel appLabel = new JLabel("QuizCore", SwingConstants.CENTER);
        appLabel.setFont(new Font("SansSerif", Font.BOLD, 40));
        appLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subLabel = new JLabel("Admin Home", SwingConstants.CENTER);
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 25));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(appLabel);
        header.add(Box.createRigidArea(new Dimension(0, 6)));
        header.add(subLabel);
        centerWrapper.add(header, BorderLayout.NORTH);

        JPanel iconsArea = new JPanel(new GridBagLayout());
        iconsArea.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(40, 40, 20, 40);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;

        JPanel topRow = new JPanel(new GridLayout(1, 3, 120, 0));
        topRow.setOpaque(false);

        btnAddQuestion = makeIconLabelButton("Add Question", "/quizcore/resources/add.png", "quizcore.admin.AddQuestion");
        btnManageQuestion = makeIconLabelButton("Manage Questions", "/quizcore/resources/manage.png", "quizcore.admin.ManageQuestion");
        btnAllQuestions = makeIconLabelButton("All Questions", "/quizcore/resources/all.png", "quizcore.admin.AllQuestions");

        topRow.add(btnAddQuestion);
        topRow.add(btnManageQuestion);
        topRow.add(btnAllQuestions);

        iconsArea.add(topRow, gbc);

        gbc.gridy = 1;
        JPanel bottomRowContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 120, 0));
        bottomRowContainer.setOpaque(false);
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 120, 0));
        bottomRow.setOpaque(false);

        btnStudentResults = makeIconLabelButton("Student Results", "/quizcore/resources/results.png", "quizcore.admin.StudentResults");
        btnSettings = makeIconLabelButton("Settings", "/quizcore/resources/settings.png", "quizcore.admin.Settings");

        bottomRow.add(btnStudentResults);
        bottomRow.add(btnSettings);
        bottomRowContainer.add(bottomRow);
        iconsArea.add(bottomRowContainer, gbc);

        centerWrapper.add(iconsArea, BorderLayout.CENTER);

        JPanel logoutPanel = new JPanel();
        logoutPanel.setOpaque(false);
        centeredLogout = new JButton("Logout");
        centeredLogout.setPreferredSize(new Dimension(140, 36));
        logoutPanel.add(centeredLogout);
        centerWrapper.add(logoutPanel, BorderLayout.SOUTH);

        root.add(centerWrapper, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        lblStats = new JLabel("Loading stats...");
        lblStats.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        footer.add(lblStats, BorderLayout.WEST);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);

        centeredLogout.addActionListener(e -> {
            int opt = JOptionPane.showConfirmDialog(AdminHome.this,
                    "Are you sure you want to logout and return to Welcome screen?",
                    "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                AdminHome.this.setVisible(false);
                AdminHome.this.dispose();

                SwingUtilities.invokeLater(() -> {
                    try {
                        WelcomeScreen ws = new WelcomeScreen();
                        ws.setLocationRelativeTo(null);
                        ws.setVisible(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed to open Welcome screen: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        });

        loadStats();
    }

    private JButton makeIconLabelButton(String text, String iconPath, String classFQN) {
        JButton b = new JButton();
        b.setLayout(new BorderLayout());
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);

        ImageIcon ic = loadIcon(iconPath, 96, 96);
        JLabel iconLbl = new JLabel();
        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);
        if (ic != null) iconLbl.setIcon(ic);

        JLabel txt = new JLabel(text, SwingConstants.CENTER);
        txt.setFont(new Font("SansSerif", Font.PLAIN, 16));
        txt.setHorizontalAlignment(SwingConstants.CENTER);

        b.add(iconLbl, BorderLayout.CENTER);
        b.add(txt, BorderLayout.SOUTH);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.addActionListener(e -> openModuleFrameSafely(text, classFQN));
        return b;
    }

    private ImageIcon loadIcon(String path, int w, int h) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) {
                url = getClass().getResource("/" + path.replaceFirst("^/", ""));
            }
            if (url == null) return null;
            ImageIcon raw = new ImageIcon(url);
            Image img = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception ex) {
            return null;
        }
    }
    
    private void openModuleFrameSafely(final String friendlyName, final String classFQN) {
        if (moduleOpen) {
            JOptionPane.showMessageDialog(this, "Please close the open module before opening " + friendlyName + ".", "Module Open", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Class<?> cls = Class.forName(classFQN);

            try {
                Method m = cls.getMethod("showDialog", Frame.class);
                moduleOpen = true;
                setControlsEnabled(false);
                m.invoke(null, this);
                moduleOpen = false;
                setControlsEnabled(true);
                loadStats();
                return;
            } catch (NoSuchMethodException ignored) { }

            try {
                Constructor<?> c = cls.getConstructor(Frame.class);
                Object inst = c.newInstance(this);
                if (inst instanceof JDialog) {
                    final JDialog dlg = (JDialog) inst;
                    moduleOpen = true;
                    setControlsEnabled(false);
                    dlg.addWindowListener(new WindowAdapter() {
                        @Override public void windowClosed(WindowEvent e) { moduleOpen = false; setControlsEnabled(true); loadStats(); }
                        @Override public void windowClosing(WindowEvent e) { moduleOpen = false; setControlsEnabled(true); loadStats(); }
                    });
                    dlg.setLocationRelativeTo(this);
                    dlg.setVisible(true);
                    return;
                }
            } catch (NoSuchMethodException ignored) { }

            try {
                Object inst = cls.getDeclaredConstructor().newInstance();
                if (inst instanceof JFrame) {
                    final JFrame moduleFrame = (JFrame) inst;
                    moduleOpen = true;
                    setControlsEnabled(false);
                    moduleFrame.addWindowListener(new WindowAdapter() {
                        @Override public void windowClosed(WindowEvent e) { moduleOpen = false; setControlsEnabled(true); loadStats(); }
                        @Override public void windowClosing(WindowEvent e) { moduleOpen = false; setControlsEnabled(true); loadStats(); }
                    });
                    moduleFrame.setLocationRelativeTo(this);
                    moduleFrame.setVisible(true);
                    return;
                }
            } catch (NoSuchMethodException ignored) { }

            try {
                Method mmain = cls.getMethod("main", String[].class);
                mmain.invoke(null, (Object) new String[] {});
                JOptionPane.showMessageDialog(this, friendlyName + " launched (use its window).", "Info", JOptionPane.INFORMATION_MESSAGE);
                loadStats();
                return;
            } catch (NoSuchMethodException ignored) { }

            JOptionPane.showMessageDialog(this, "Cannot open module: unsupported class type.", "Error", JOptionPane.ERROR_MESSAGE);

        } catch (ClassNotFoundException cnf) {
            JOptionPane.showMessageDialog(this, "Module class not found: " + classFQN, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to open module: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            moduleOpen = false;
            setControlsEnabled(true);
        }
    }

    private void setControlsEnabled(boolean enabled) {
        btnAddQuestion.setEnabled(enabled);
        btnManageQuestion.setEnabled(enabled);
        btnAllQuestions.setEnabled(enabled);
        btnStudentResults.setEnabled(enabled);
        btnSettings.setEnabled(enabled);
        centeredLogout.setEnabled(enabled);
    }

    private void loadStats() {
        int qCount = 0, sCount = 0, rCount = 0;
        String qSql = "SELECT COUNT(*) FROM questions";
        String sSql = "SELECT COUNT(*) FROM students";
        String rSql = "SELECT COUNT(*) FROM results";
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement p = conn.prepareStatement(qSql); ResultSet rs = p.executeQuery()) { if (rs.next()) qCount = rs.getInt(1); }
            try (PreparedStatement p = conn.prepareStatement(sSql); ResultSet rs = p.executeQuery()) { if (rs.next()) sCount = rs.getInt(1); }
            try (PreparedStatement p = conn.prepareStatement(rSql); ResultSet rs = p.executeQuery()) { if (rs.next()) rCount = rs.getInt(1); }
        } catch (SQLException ex) {
            lblStats.setText("Stats: failed to load (" + ex.getMessage() + ")");
            return;
        }
        lblStats.setText("Stats: Questions = " + qCount + "   |   Students = " + sCount + "   |   Results = " + rCount);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminHome ah = new AdminHome();
            ah.setVisible(true);
        });
    }
}
