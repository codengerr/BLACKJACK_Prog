package logic;

/**
 * Definiert alle 13 Kartenwerte im Blackjack-Spiel, von Zwei bis Ass.
 * <p>
 * Jeder Wert ist mit seinem Standard-Blackjack-Punktwert verknüpft.
 * Bildkarten (Bube, Dame, König) zählen je 10 Punkte.
 * Das Ass zählt initial 11 Punkte; die Reduzierung auf 1 Punkt
 * bei Überkauf erfolgt in {@link Participant#calculateScore()}.
 * </p>
 *
 * @author  Elias
 * @version 2.0
 */
public enum Rank {


    ZWEI(2),


    DREI(3),


    VIER(4),


    FÜNF(5),


    SECHS(6),


    SIEBEN(7),


    ACHT(8),


    NEUN(9),


    ZEHN(10),


    BUBE(10),


    DAME(10),


    KÖNIG(10),

    /**
     * Ass – Punktwert initial 11.
     * Wird in {@link Participant#calculateScore()} bei Bedarf auf 1 reduziert.
     */
    ASS(11);

    /** Der Blackjack-Punktwert dieses Kartenwerts. */
    private final int value;

    /**
     * Erstellt einen Kartenwert mit dem angegebenen Blackjack-Punktwert.
     *
     * @param value Der Punktwert (1–11).
     */
    Rank(int value) {
        this.value = value;
    }

    /**
     * Gibt den numerischen Blackjack-Punktwert zurück.
     * <p>
     * Bildkarten (Bube, Dame, König) liefern 10,
     * das Ass liefert 11 (vor möglicher Reduzierung durch
     * {@link Participant#calculateScore()}).
     * </p>
     *
     * @return Punktwert als int.
     */
    public int getValue() {
        return value;
    }

    /**
     * Gibt den Namen des Kartenwerts zurück.
     *
     * @return Den Enum-Namen (z.B. {@code "ASS"}, {@code "KÖNIG"}).
     */
    @Override
    public String toString() {
        return name();
    }
}