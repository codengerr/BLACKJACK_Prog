package logic;

public enum Rank {
    ASS(11),ZWEI(2), DREI(3), VIER(4), FÜNF(5), SECHS(6), SIEBEN(7), ACHT(8), NEUN(9), ZEHN(10),
    BUBE(10),
    DAME(10),
    KOENIG(10);
    // Ass erstmal 11 Punkte später wird nochmal geprüft ob Bust -> dann ein Punkt

    // Das Attribut, das den Punktewert speichert
    private final int value;

    // Der Konstruktor für das Enum (wird intern von Java aufgerufen)
    Rank(int value) {
        this.value = value;
    }

    // Eine Getter-Methode, um den Wert später abzufragen
    public int getValue() {
        return value;
    }
}