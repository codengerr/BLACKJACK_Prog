package logic;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
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
            // 1. Das komplette Bild laden (kein Subimage mehr)
            BufferedImage fullImage = ImageIO.read(new File("resources/CardBack.png"));

            // 2. Das Bild auf 225x315 Pixel skalieren
            // SCALE_SMOOTH sorgt dafür, dass das Bild beim Skalieren scharf bleibt
            Image scaledImage = fullImage.getScaledInstance(225, 315, Image.SCALE_SMOOTH);

            // 3. Das skaliere Image an das ImageIcon übergeben
            cardBackImage = new ImageIcon(scaledImage);

        } catch (IOException e) {
            System.out.println("FEHLER: Bild konnte nicht geladen werden!");
            e.printStackTrace();
        }
    }

    // Statischer Getter, den wir überall aufrufen können, ohne eine echte Karte zu brauchen!
    public static ImageIcon getCardBack() {
        return cardBackImage;
    }
}