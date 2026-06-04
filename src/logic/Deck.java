package logic;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Repräsentiert ein Kartendeck bestehend aus 52 Spielkarten.
 * Kann gemischt und neu generiert werden.
 * * @author  [Dein Name]
 * @version 1.0
 */
public class Deck {

    private ArrayList<Card> cards;

    /**
     * Erstellt ein neues, automatisch gemischtes Standard-Deck.
     */
    public Deck() {
        refillAndShuffle();
    }

    /**
     * Generiert alle 52 Karten neu und mischt den Stapel kräftig durch.
     */
    public void refillAndShuffle() {
        cards = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(cards);
    }

    /**
     * Zieht die oberste Karte vom Stapel.
     * Sollte das Deck leer sein, wird es automatisch neu generiert.
     * * @return Die gezogene {@link Card}.
     */
    public Card drawCard() {
        if (cards.isEmpty()) {
            refillAndShuffle();
        }
        return cards.remove(cards.size() - 1);
    }

    /**
     * Gibt die Anzahl der verbleibenden Karten im Deck zurück.
     * @return Anzahl als int.
     */
    public int getRemainingCards() {
        return cards.size();
    }
}