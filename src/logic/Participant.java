package logic;


import java.util.ArrayList;

public abstract class Participant {
    // Die Liste der Karten. 'protected' bedeutet, dass die Unterklassen
    // (Player und Dealer) darauf zugreifen dürfen.
    protected ArrayList<Card> hand;
    private String name;

    // Der Konstruktor
    public Participant(String name) {
        this.hand = new ArrayList<>();
        this.name = name;
    }
    // Der Getter gibt die gesamte ArrayList mit den Karten zurück
    public ArrayList<Card> getHand() {
        return this.hand;
    }
    public String getName() {
        return this.name;
    }
    // Die Methode wird direkt HIER fertig gecodet!
    public void clearHand() {
        this.hand.clear();
    }

    // Hier kommt z.B. noch addCard() rein, denn auch das ist für beide gleich:
    public void addCard(Card card) {
        this.hand.add(card);
    }

    // Eine abstrakte Methode wäre z.B. sowas hier, weil der Dealer automatisch
    // zieht, der Spieler aber per Knopfdruck:
    public abstract void makeTurn(GameEngine engine);



    public int calculateScore() {
        int value = 0;
        int aces = 0;

        for (Card c : hand) {
            // Wert genau dieser einer Karte BUGFIX
            int cardValue = c.getScoreValue();
            value += cardValue;

            if (cardValue == 11) {
                aces++;
            }
        }

        while (value > 21 && aces > 0) {
            value -= 10;
            aces--;
        }

        return value;
    }
}