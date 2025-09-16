package quizcore.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class SplashScreen extends JFrame {

    private final JProgressBar progressBar;
    private final int DURATION_MS = 2000;
    private final int STEPS = 100;
    private final int DELAY_MS = DURATION_MS / STEPS;

    public SplashScreen() {
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        ImageIcon icon = null;
        try {
            java.net.URL imgUrl = getClass().getResource("/quizcore/resources/SplashScreenbg.jpg");
            if (imgUrl != null) {
                icon = new ImageIcon(imgUrl);
            } else {
                icon = new ImageIcon("resources/splash.jpg");
            }
        } catch (Exception ex) {
            icon = null;
        }

        JLabel imageLabel;
        if (icon != null) {
            Image img = icon.getImage();
            int width = 800;
            int height = 450;
            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            imageLabel = new JLabel(new ImageIcon(scaled), SwingConstants.CENTER);
            imageLabel.setPreferredSize(new Dimension(width, height));
        } else {
            imageLabel = new JLabel();
            imageLabel.setPreferredSize(new Dimension(800, 450));
        }

        add(imageLabel, BorderLayout.CENTER);

        progressBar = new JProgressBar(0, STEPS);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(800, 20));
        add(progressBar, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    public void startAndShow() {
        setVisible(true);

        Timer timer = new Timer(DELAY_MS, null);
        timer.addActionListener(e -> {
            int value = progressBar.getValue() + 1;
            progressBar.setValue(value);
            if (value >= progressBar.getMaximum()) {
                ((Timer) e.getSource()).stop();
                dispose();
                try {
                    EventQueue.invokeLater(() -> {
                        try {
                            Class<?> cls = Class.forName("quizcore.main.WelcomeScreen");
                            Object obj = cls.getDeclaredConstructor().newInstance();
                            if (obj instanceof JFrame) {
                                ((JFrame) obj).setVisible(true);
                            }
                        } catch (ClassNotFoundException cnfe) {
                       } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        timer.setInitialDelay(0);
        timer.start();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            SplashScreen splash = new SplashScreen();
            splash.startAndShow();
        });
    }
}
