package GUI;

import logic.Card;
import logic.Suit;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Zuständig für das Laden und Bereitstellen aller Kartenbilder.
 * <p>
 * Diese Klasse gehört zur <em>GUISchicht</em> und kapselt sämtliche
 * Bildlogik die zuvor fälschlicherweise in {@code logic.Card} lag.
 * Das Spritesheet und das Rückseitenbild werden beim ersten Zugriff
 * einmalig geladen (Singleton Pattern).
 * </p>
 *
 * @author  Elias
 * @version 1.0
 */
public class CardImageLoader {

    /** Breite einer einzelnen Karte im Spritesheet in Pixeln. */
    private static final int CARD_WIDTH  = 225;

    /** Höhe einer einzelnen Karte im Spritesheet in Pixeln. */
    private static final int CARD_HEIGHT = 315;

    /** Pfad zum Spritesheet relativ zum Projektordner. */
    private static final String SPRITE_PATH   = "resources/Karten.png";

    /** Pfad zum Rückseitenbild relativ zum Projektordner. */
    private static final String BACK_PATH     = "resources/CardBack.png";

    /** Das einmal geladene Spritesheet. */
    private static BufferedImage spriteSheet;

    /** Das einmal geladene und skalierte Rückseitenicon. */
    private static ImageIcon cardBackImage;

    /** Statischer Initialisierer – lädt beide Ressourcen einmalig beim Klassenstart. */
    static {
        loadSpriteSheet();
        loadCardBack();
    }

    /** Privater Konstruktor – diese Klasse soll nicht instanziiert werden. */
    private CardImageLoader() {}

    // -------------------------------------------------------------------------
    // Öffentliche Methoden
    // -------------------------------------------------------------------------

    /**
     * Gibt das {@link ImageIcon} für die angegebene Karte zurück
     * ausgeschnitten aus dem Spritesheet.
     *
     * @param card Die logische {@link Card} deren Bild benötigt wird.
     * @return Das passende {@link ImageIcon} oder {@code null} bei Ladefehler.
     */
    public static ImageIcon getCardImage(Card card) {
        if (spriteSheet == null) {
            return null;
        }

        int col = card.getRank().ordinal();
        int row = getSuitRow(card.getSuit());

        int x = col * CARD_WIDTH;
        int y = row * CARD_HEIGHT;

        try {
            BufferedImage cropped = spriteSheet.getSubimage(x, y, CARD_WIDTH, CARD_HEIGHT);
            return new ImageIcon(cropped);
        } catch (Exception e) {
            System.err.println("Fehler beim Ausschneiden der Karte: " + card);
            return null;
        }
    }

    /**
     * Gibt das {@link ImageIcon} für die Kartenrückseite zurück.
     *
     * @return Das Rückseitenicon oder {@code null} bei Ladefehler.
     */
    public static ImageIcon getCardBack() {
        return cardBackImage;
    }

    // -------------------------------------------------------------------------
    // Private Hilfsmethoden
    // -------------------------------------------------------------------------

    /** Lädt das Spritesheet einmalig aus dem Dateisystem. */
    private static void loadSpriteSheet() {
        try {
            spriteSheet = ImageIO.read(new File(SPRITE_PATH));
        } catch (IOException e) {
            System.err.println("FEHLER: Spritesheet konnte nicht geladen werden: " + SPRITE_PATH);
        }
    }

    /** Lädt und skaliert das Rückseitenbild einmalig. */
    private static void loadCardBack() {
        try {
            BufferedImage raw = ImageIO.read(new File(BACK_PATH));
            Image scaled = raw.getScaledInstance(CARD_WIDTH, CARD_HEIGHT, Image.SCALE_SMOOTH);
            cardBackImage = new ImageIcon(scaled);
        } catch (IOException e) {
            System.err.println("FEHLER: Rückseitenbild konnte nicht geladen werden: " + BACK_PATH);
        }
    }

    /**
     * Gibt die Zeilennummer im Spritesheet für eine gegebene Kartenfarbe zurück.
     *
     * @param suit Die {@link Suit} der Karte.
     * @return Die Zeilennummer (0-basiert).
     */
    private static int getSuitRow(Suit suit) {
        return switch (suit) {
            case HERZ  -> 0;
            case PIK   -> 1;
            case KARO  -> 2;
            case KREUZ -> 3;
        };
    }
}