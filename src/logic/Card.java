package logic;

/**
 * Repräsentiert eine einzelne Spielkarte mit Farbe ({@link Suit}) und
 * Wert ({@link Rank}).
 * <p>
 * Diese Klasse gehört zur <em>Logik-Schicht</em> und enthält bewusst * <strong>keine</strong> GUI-Abhängigkeiten. Das Laden von Bildern
 * übernimmt die GUI-Schicht (z.B. ein {@code CardImageLoader}). * </p>
 *
 * @author  [Dein Name]
 * @version 1.0
 */
public class Card {

    /** Die Farbe der Karte (Herz, Pik, Karo, Kreuz). */
    private final Suit suit;

    /** Der Wert der Karte (Ass, Zwei, ..., König). */
    private final Rank rank;

    /**
     * Erstellt eine neue Karte mit der angegebenen Farbe und dem angegebenen Wert.
     *
     * @param suit Die Kartenfarbe ({@link Suit}).
     * @param rank Der Kartenwert ({@link Rank}).
     */
    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    /**
     * Gibt die Farbe der Karte zurück.
     *
     * @return Die {@link Suit} dieser Karte.
     */
    public Suit getSuit() {
        return suit;
    }

    /**
     * Gibt den Wert (Rank) der Karte zurück.
     *
     * @return Das {@link Rank}-Enum dieser Karte.
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * Gibt den Punktwert dieser Karte für die Blackjack-Wertung zurück.
     * Asse zählen initial als 11 – die Anpassung auf 1 erfolgt in
     * {@link Participant#calculateScore()}.
     *
     * @return Der Punktwert als int (1–11).
     */
    public int getScoreValue() {
        return rank.getValue();
    }

    /**
     * Gibt eine lesbare Darstellung der Karte zurück.
     * Beispiel: {@code "ASS in HERZ"}
     *
     * @return Rank und Suit als formatierter String.
     */
    @Override
    public String toString() {
        return rank + " in " + suit;
    }
}