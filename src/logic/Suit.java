package logic;

public enum Suit {
    HERZ("Herz"),
    PIK("Pik"),
    KREUZ("Kreuz"),
    KARO("Karo");

    private final String name;

    // Konstruktor
    Suit(String name) {
        this.name = name;
    }

    // Getter für den schönen Text
    public String getName() {
        return name;
    }
}