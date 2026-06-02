package logic;


import java.util.ArrayList;
import java.util.Collections;

public class Deck {

    private ArrayList<Card> cards;

    public Deck(){
        cards = new ArrayList<>();

        for (Suit suit : Suit.values()){
            for (Rank rank : Rank.values()){
                cards.add(new Card(suit, rank));
            }
        }
    }

    public void shuffle(){
        Collections.shuffle(cards);
    }

    public Card drawCard(){
        return cards.remove(0);
    }
}
