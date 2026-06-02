package logic;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Card {
    private Suit suit;
    private Rank rank;
    private ImageIcon cardImage; // Das fertige, ausgeschnittene Bild für die GUI

    // STATIC: Das große Sprite Sheet wird nur EINMAL für das gesamte Programm geladen!
    private static BufferedImage spriteSheet;

    static {
        try {
            // Hier den genauen Dateinamen deines Bildes im resources-Ordner eintragen!
            spriteSheet = ImageIO.read(new File("resources/Karten.png"));
        } catch (IOException e) {
            System.out.println("FEHLER: Sprite Sheet konnte nicht geladen werden!");
            e.printStackTrace();
        }
    }

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;

        // 1. Maße deiner Karten festlegen
        int cardWidth = 225;
        int cardHeight = 315;

        // 2. Spalte (X) anhand des Ranks berechnen (Ass = 0, Zwei = 1, ..., König = 12)
        int col = rank.ordinal();

        // 3. Reihe (Y) anhand der Farbe berechnen
        int row = 0;
        switch (suit) {
            case HERZ:  row = 0; break;
            case PIK:   row = 1; break;
            case KARO:  row = 2; break;
            case KREUZ: row = 3; break;
        }

        // 4. Startpunkt (oben links) für das Ausschneiden berechnen
        int x = col * cardWidth;
        int y = row * cardHeight;

        // 5. Das kleine Rechteck aus dem großen Bild ausschneiden!
        if (spriteSheet != null) {
            BufferedImage croppedImage = spriteSheet.getSubimage(x, y, cardWidth, cardHeight);
            this.cardImage = new ImageIcon(croppedImage); // In ein GUI-taugliches Icon umwandeln
        }
    }

    // Getter für die GUI
    public ImageIcon getCardImage() {
        return cardImage;
    }


    // Getter für die Werte
    public int getScoreValue() {
        return rank.getValue();
    }

    //  toString()
    @Override
    public String toString() {
        return rank + " in " + suit; // Gibt z.B. "ACE of SPADES" zurück
    }
    private static ImageIcon cardBackImage;

    static {
        try {
            spriteSheet = ImageIO.read(new File("resources/Karten.png"));
            // Beispiel: Schneide die Rückseite aus (musst die X/Y Pixel an dein Bild anpassen!)
            // Wenn die Rückseite z.B. ganz am Ende ist:
            BufferedImage back = spriteSheet.getSubimage(0, 4 * 315, 225, 315);
            cardBackImage = new ImageIcon(back);
        } catch (IOException e) {
            System.out.println("FEHLER: Sprite Sheet konnte nicht geladen werden!");
            e.printStackTrace();
        }
    }

    // Statischer Getter, den wir überall aufrufen können, ohne eine echte Karte zu brauchen!
    public static ImageIcon getCardBack() {
        return cardBackImage;
    }
}