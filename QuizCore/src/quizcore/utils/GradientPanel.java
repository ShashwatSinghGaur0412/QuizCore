package quizcore.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GradientPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private Color colorTop;
    private Color colorMiddle;
    private Color colorBottom;
    private boolean vertical;
    private Dimension prefSize;

    public GradientPanel() {
        this(new Color(139, 69, 19), new Color(245, 245, 220), Color.WHITE, true, new Dimension(800, 450));
    }
    
    public GradientPanel(Color top, Color middle, Color bottom, boolean vertical, Dimension pref) {
        this.colorTop = top;
        this.colorMiddle = middle;
        this.colorBottom = bottom;
        this.vertical = vertical;
        this.prefSize = pref != null ? pref : new Dimension(800, 450);
        setOpaque(true);
    }

    public Color getColorTop() {
        return colorTop;
    }

    public void setColorTop(Color colorTop) {
        this.colorTop = colorTop;
        repaint();
    }

    public Color getColorMiddle() {
        return colorMiddle;
    }

    public void setColorMiddle(Color colorMiddle) {
        this.colorMiddle = colorMiddle;
        repaint();
    }

    public Color getColorBottom() {
        return colorBottom;
    }

    public void setColorBottom(Color colorBottom) {
        this.colorBottom = colorBottom;
        repaint();
    }

    public boolean isVertical() {
        return vertical;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return prefSize;
    }

    public void setPreferredSizeDimension(Dimension d) {
        this.prefSize = d;
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        if (vertical) {
            GradientPaint gp1 = new GradientPaint(0, 0, colorTop, 0, h / 2f, colorMiddle);
            g2.setPaint(gp1);
            g2.fillRect(0, 0, w, h / 2);

            GradientPaint gp2 = new GradientPaint(0, h / 2f, colorMiddle, 0, h, colorBottom);
            g2.setPaint(gp2);
            g2.fillRect(0, h / 2, w, h - (h / 2));
        } else {
            GradientPaint gp1 = new GradientPaint(0, 0, colorTop, w / 2f, 0, colorMiddle);
            g2.setPaint(gp1);
            g2.fillRect(0, 0, w / 2, h);

            GradientPaint gp2 = new GradientPaint(w / 2f, 0, colorMiddle, w, 0, colorBottom);
            g2.setPaint(gp2);
            g2.fillRect(w / 2, 0, w - (w / 2), h);
        }

        g2.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("GradientPanel Preview");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GradientPanel gp = new GradientPanel(
                    new Color(139, 69, 19),
                    new Color(245, 245, 220),
                    Color.WHITE,
                    true,
                    new Dimension(800, 450)
            );

            frame.getContentPane().add(gp);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }
}
