package GUI;

import javax.swing.JLabel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Ein erweitertes {@link JLabel}, das seinen Text mit einem farbigen Umriss rendert.
 * <p>
 * Der Umriss wird durch 8-faches Zeichnen des Textes in der Umrissfarbe erzeugt,
 * bevor der eigentliche Text in der Vordergrundfarbe gezeichnet wird.
 * Umrissfarbe und Textfarbe sind über den Konstruktor konfigurierbar.
 * </p>
 *
 * @author  [Dein Name]
 * @version 1.0
 */
public class OutlinedLabel extends JLabel {

    /** Die Farbe des Umrisses. */
    private Color outlineColor;

    /** Die Breite des Umrisses in Pixeln. */
    private float outlineWidth;

    /**
     * Erstellt ein {@code OutlinedLabel} mit Standard-Styling
     * (schwarzer Umriss, roter Text).
     *
     * @param text      Der anzuzeigende Text.
     * @param alignment Die horizontale Ausrichtung (z.B. {@link javax.swing.SwingConstants#CENTER}).
     */
    public OutlinedLabel(String text, int alignment) {
        this(text, alignment, Color.BLACK, Color.RED, 4f);
    }

    /**
     * Erstellt ein {@code OutlinedLabel} mit vollständig konfigurierbarem Styling.
     *
     * @param text         Der anzuzeigende Text.
     * @param alignment    Die horizontale Ausrichtung.
     * @param outlineColor Die Farbe des Umrisses.
     * @param textColor    Die Farbe des eigentlichen Textes.
     * @param outlineWidth Die Stärke des Umrisses in Pixeln.
     */
    public OutlinedLabel(String text, int alignment,
                         Color outlineColor, Color textColor, float outlineWidth) {
        super(text, alignment);
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;
        setForeground(textColor);
    }

    /**
     * Gibt die aktuelle Umrissfarbe zurück.
     *
     * @return Die Umrissfarbe als {@link Color}.
     */
    public Color getOutlineColor() {
        return outlineColor;
    }

    /**
     * Setzt eine neue Umrissfarbe.
     *
     * @param outlineColor Die neue Umrissfarbe.
     */
    public void setOutlineColor(Color outlineColor) {
        this.outlineColor = outlineColor;
        repaint();
    }

    /**
     * Zeichnet den Text mit Umriss. Wird automatisch von Swing aufgerufen.
     *
     * @param g Das {@link Graphics}-Objekt für das Zeichnen.
     */
    @Override
    protected void paintComponent(Graphics g) {
        String text = getText();
        if (text == null || text.isEmpty()) {
            super.paintComponent(g);
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(getFont());

        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth()  - fm.stringWidth(text)) / 2;
        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();

        int offset = Math.max(1, Math.round(outlineWidth / 2));

        // Umriss: 8-fach versetztes Zeichnen
        g2d.setColor(outlineColor);
        g2d.setStroke(new BasicStroke(outlineWidth));
        for (int dx = -offset; dx <= offset; dx += offset) {
            for (int dy = -offset; dy <= offset; dy += offset) {
                if (dx != 0 || dy != 0) {
                    g2d.drawString(text, x + dx, y + dy);
                }
            }
        }

        // Eigentlicher Text in Vordergrundfarbe
        g2d.setColor(getForeground());
        g2d.drawString(text, x, y);

        g2d.dispose();
    }
}