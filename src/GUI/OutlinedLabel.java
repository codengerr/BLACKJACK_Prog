package GUI;

import javax.swing.*;
import java.awt.*;

public class OutlinedLabel extends JLabel {

    public OutlinedLabel(String text, int alignment) {
        super(text, alignment);
    }

    @Override
    protected void paintComponent(Graphics g) {
        String text = getText();
        if (text == null || text.isEmpty()) {
            super.paintComponent(g);
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Schrift zentrieren
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();

        // 1. Dicker schwarzer Rand
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(4f)); // Dicke des Randes

        // 8-faches Zeichnen für einen perfekten Umriss
        g2d.drawString(text, x - 2, y - 2);
        g2d.drawString(text, x + 2, y - 2);
        g2d.drawString(text, x - 2, y + 2);
        g2d.drawString(text, x + 2, y + 2);
        g2d.drawString(text, x - 2, y);
        g2d.drawString(text, x + 2, y);
        g2d.drawString(text, x, y - 2);
        g2d.drawString(text, x, y + 2);

        // 2. Innere Textfarbe (Rot, wie du es wolltest)
        g2d.setColor(Color.RED);
        g2d.drawString(text, x, y);

        g2d.dispose();
    }
}