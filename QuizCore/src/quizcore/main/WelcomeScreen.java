package quizcore.main;

import quizcore.utils.GradientPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class WelcomeScreen extends JFrame {

    public WelcomeScreen() {
        setTitle("Welcome - QuizCore");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 450);
        setLocationRelativeTo(null);
        setResizable(false);

        GradientPanel bgPanel = new GradientPanel();
        bgPanel.setLayout(new BorderLayout());

        JLabel title = new JLabel("Welcome to QuizCore", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(Color.DARK_GRAY);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        bgPanel.add(title, BorderLayout.NORTH);

        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 100, 40));
        iconPanel.setOpaque(false);

        JLabel studentLabel = createIconLabel("/quizcore/resources/student.png", "Student");
        studentLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                try {
                    quizcore.student.StudentForm.main(new String[]{});
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JLabel facultyLabel = createIconLabel("/quizcore/resources/faculty.png", "Faculty");
        facultyLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                try {
                    quizcore.admin.AdminLogin.main(new String[]{});
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        iconPanel.add(studentLabel);
        iconPanel.add(facultyLabel);
        bgPanel.add(iconPanel, BorderLayout.CENTER);

        JLabel footer = new JLabel("QuizCore v1.0", SwingConstants.CENTER);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footer.setForeground(Color.DARK_GRAY);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        bgPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(bgPanel);
    }

    private JLabel createIconLabel(String resourcePath, String text) {
        ImageIcon icon = null;
        try {
            java.net.URL imgUrl = getClass().getResource(resourcePath);
            if (imgUrl != null) {
                icon = new ImageIcon(new ImageIcon(imgUrl).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        JLabel label = new JLabel(text, icon, SwingConstants.CENTER);
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setVerticalTextPosition(SwingConstants.BOTTOM);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return label;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WelcomeScreen ws = new WelcomeScreen();
            ws.setVisible(true);
        });
    }
}
