package logic;

/**
 * Repräsentiert die vier Farben einer Spielkarte im Blackjackspiel.
 * <p>
 * Jede Farbe besitzt einen lesbaren deutschen Namen der zum Beispiel für
 * die Ausgabe in der Konsole oder in {@link Card#toString()} verwendet wird.
 * </p>
 *
 * @author  Elias
 * @version 1.0
 */
public enum Suit {

    /** Herzfarbe (rot Herz-Symbol). */
    HERZ("Herz"),

    /** Pikfarbe (schwarz Pik-Symbol). */
    PIK("Pik"),

    /** Kreuzfarbe (schwarz Kreuz-Symbol). */
    KREUZ("Kreuz"),

    /** Karofarbe (rot Karo-Symbol). */
    KARO("Karo");

    /** Der lesbare deutsche Name der Kartenfarbe. */
    private final String name;

    /**
     * Erstellt eine Kartenfarbe mit dem angegebenen Anzeigenamen.
     *
     * @param name Der deutsche Name der Farbe wie etwa {@code "Herz"}.
     */
    Suit(String name) {
        this.name = name;
    }

    /**
     * Gibt den lesbaren deutschen Namen der Kartenfarbe zurück.
     *
     * @return Der Anzeigename als String wie etwa {@code "Herz"}.
     */
    public String getName() {
        return name;
    }

    /**
     * Gibt den Namen der Kartenfarbe zurück.
     *
     * @return Derselbe Wert wie {@link #getName()}.
     */
    @Override
    public String toString() {
        return name;
    }
}