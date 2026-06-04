package logic;

/**
 * Definiert die Kartenwerte (von Zwei bis Ass) und deren Standard-Blackjack-Punktwert.
 * * @author  [Dein Name]
 * @version 2.0
 */
public enum Rank {
    ZWEI(2), DREI(3), VIER(4), FÜNF(5), SECHS(6), SIEBEN(7),
    ACHT(8), NEUN(9), ZEHN(10), BUBE(10), DAME(10), KÖNIG(10), ASS(11);

    private final int value;

    Rank(int value) {
        this.value = value;
    }

    /**
     * Gibt den numerischen Blackjack-Wert der Karte zurück.
     * @return Punktwert als int.
     */
    public int getValue() {
        return this.value;
    }
}