package logic;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Repräsentiert ein vollständiges Kartendeck mit 52 Spielkarten.
 * <p>
 * Das Deck besteht aus allen Kombinationen der vier {@link Suit Farben}
 * mit den 13 {@link Rank Werten}. Beim Erstellen und nach jedem
 * {@link #refillAndShuffle()} Aufruf ist das Deck vollständig und gemischt.
 * Ist das Deck leer wird es beim nächsten {@link #drawCard()} Aufruf
 * automatisch neu aufgefüllt.
 * </p>
 *
 * @author  Elias
 * @version 1.0
 */
public class Deck {

    /** Die Kartenliste aus der gezogen wird. Oberste Karte ist das letzte Element. */
    private ArrayList<Card> cards;

    /**
     * Erstellt ein neues vollständig befülltes und gemischtes Deck.
     * Entspricht dem Aufruf von {@link #refillAndShuffle()}.
     */
    public Deck() {
        refillAndShuffle();
    }

    /**
     * Füllt das Deck mit allen 52 Karten neu auf und mischt es.
     * <p>
     * Alle bisherigen Karten werden verworfen. Danach enthält das Deck
     * genau eine Karte für jede Kombination aus {@link Suit} und {@link Rank}.
     * </p>
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
     * Zieht die oberste Karte vom Deck und gibt sie zurück.
     * <p>
     * Ist das Deck leer (alle 52 Karten wurden bereits gezogen)
     * wird es automatisch neu aufgefüllt und gemischt bevor eine
     * Karte zurückgegeben wird.
     * </p>
     *
     * @return Die gezogene {@link Card}; nie {@code null}.
     */
    public Card drawCard() {
        if (cards.isEmpty()) {
            refillAndShuffle();
        }
        return cards.remove(cards.size() - 1);
    }

    /**
     * Gibt die Anzahl der noch im Deck verbleibenden Karten zurück.
     *
     * @return Anzahl der verbleibenden Karten (0–52).
     */
    public int getRemainingCards() {
        return cards.size();
    }

    /**
     * Gibt eine kompakte Darstellung des Decks zurück.
     *
     * @return Anzahl der verbleibenden Karten als String.
     */
    @Override
    public String toString() {
        return "Deck{verbleibend=" + cards.size() + "}";
    }
}