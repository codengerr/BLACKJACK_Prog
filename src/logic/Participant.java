package logic;

import java.util.ArrayList;

/**
 * Abstrakte Basisklasse für alle Teilnehmer am Blackjack-Spiel.
 * <p>
 * Sowohl {@link Player} als auch {@link Dealer} erben von dieser Klasse
 * und teilen sich grundlegende Funktionen wie Kartenverwaltung und
 * Punkteberechnung.
 * </p>
 *
 * @author  Elias
 * @version 1.0
 */
public abstract class Participant {

    /** Konstante für den Blackjack-Wert, bei dem automatisch gestanden wird. */
    public static final int BLACKJACK_VALUE = 21;

    /** Die Hand (Kartenliste) des Teilnehmers. Privat – Zugriff nur über Methoden. */
    private ArrayList<Card> hand;

    /** Der Name des Teilnehmers. */
    private String name;

    /**
     * Erstellt einen neuen Teilnehmer mit dem angegebenen Namen
     * und einer leeren Hand.
     *
     * @param name Der Name des Teilnehmers.
     */
    public Participant(String name) {
        this.hand = new ArrayList<>();
        this.name = name;
    }

    /**
     * Gibt die aktuelle Hand (Kartenliste) des Teilnehmers zurück.
     *
     * @return Eine {@link ArrayList} mit den Karten auf der Hand.
     */
    public ArrayList<Card> getHand() {
        return this.hand;
    }

    /**
     * Gibt den Namen des Teilnehmers zurück.
     *
     * @return Der Name als String.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Leert die Hand des Teilnehmers (z.B. zu Beginn einer neuen Runde).
     */
    public void clearHand() {
        this.hand.clear();
    }

    /**
     * Fügt eine Karte zur Hand des Teilnehmers hinzu.
     *
     * @param card Die hinzuzufügende {@link Card}.
     */
    public void addCard(Card card) {
        this.hand.add(card);
    }

    /**
     * Berechnet den aktuellen Punktwert der Hand.
     * <p>
     * Asse zählen zunächst als 11. Würde der Gesamtwert 21 überschreiten,
     * wird jedes Ass auf 1 reduziert, bis der Wert wieder ≤ 21 ist
     * oder keine Asse mehr übrig sind.
     * </p>
     *
     * @return Der Punktwert der Hand als int.
     */
    public int calculateScore() {
        int value = 0;
        int aces  = 0;

        for (Card c : hand) {
            int cardValue = c.getScoreValue();
            value += cardValue;
            if (cardValue == 11) {
                aces++;
            }
        }

        while (value > BLACKJACK_VALUE && aces > 0) {
            value -= 10;
            aces--;
        }

        return value;
    }

    /**
     * Führt den Zug des Teilnehmers aus. Muss von Unterklassen implementiert werden,
     * da Spieler und Dealer unterschiedlich reagieren (Tastendruck vs. Automatik).
     *
     * @param engine Die aktive {@link GameEngine}, über die Aktionen ausgelöst werden.
     */
    public abstract void makeTurn(GameEngine engine);

    /**
     * Gibt eine lesbare Darstellung des Teilnehmers zurück.
     *
     * @return Name und aktuelle Hand als formatierter String.
     */
    @Override
    public String toString() {
        return name + " | Hand: " + hand + " | Wert: " + calculateScore();
    }
}