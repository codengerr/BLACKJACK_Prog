package JUnitTest;

import logic.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Tests für die Kernlogik des Blackjack-Spiels.
 */
public class GameLogicTest {

    private Player testPlayer;
    private Deck testDeck;

    // Wird vor jedem einzelnen Test ausgeführt, um einen sauberen Zustand zu haben
    @BeforeEach
    public void setUp() {
        testPlayer = new Player("Testo");
        testDeck = new Deck();
    }

    // -------------------------------------------------------------------------
    // Tests für die Punkteberechnung (Participant / Player)
    // -------------------------------------------------------------------------

    @Test
    public void testScoreWithoutAces() {
        testPlayer.addCard(new Card(Suit.HERZ, Rank.ZEHN));
        testPlayer.addCard(new Card(Suit.PIK, Rank.SIEBEN));

        // 10 + 7 = 17
        assertEquals(17, testPlayer.calculateScore(), "Punkte sollten 17 sein.");
    }

    @Test
    public void testScoreWithOneAce_Under21() {
        testPlayer.addCard(new Card(Suit.KARO, Rank.ASS));
        testPlayer.addCard(new Card(Suit.KREUZ, Rank.NEUN));

        // 11 + 9 = 20
        assertEquals(20, testPlayer.calculateScore(), "Das Ass sollte als 11 zählen.");
    }

    @Test
    public void testScoreWithOneAce_Over21() {
        testPlayer.addCard(new Card(Suit.HERZ, Rank.ZEHN));
        testPlayer.addCard(new Card(Suit.PIK, Rank.ACHT));
        testPlayer.addCard(new Card(Suit.KARO, Rank.ASS));

        // 10 + 8 + 11 = 29 -> Ass wird zu 1 -> 19
        assertEquals(19, testPlayer.calculateScore(), "Das Ass sollte auf 1 reduziert werden um einen Überkauf zu verhindern.");
    }

    @Test
    public void testScoreWithMultipleAces() {
        testPlayer.addCard(new Card(Suit.HERZ, Rank.ASS));
        testPlayer.addCard(new Card(Suit.PIK, Rank.ASS));

        // 11 + 11 = 22 -> Ein Ass wird zu 1 -> 12
        assertEquals(12, testPlayer.calculateScore(), "Zwei Asse sollten zusammen 12 Punkte ergeben.");

        testPlayer.addCard(new Card(Suit.KARO, Rank.ZEHN));
        // 12 + 10 = 22 -> Das zweite Ass wird auch zu 1 -> 12
        assertEquals(12, testPlayer.calculateScore(), "Beide Asse sollten auf 1 reduziert sein.");
    }

    // -------------------------------------------------------------------------
    // Tests für das Kartendeck
    // -------------------------------------------------------------------------

    @Test
    public void testDeckInitialSize() {
        assertEquals(52, testDeck.getRemainingCards(), "Ein neues Deck muss genau 52 Karten haben.");
    }

    @Test
    public void testDeckDrawCardReducesSize() {
        assertNotNull(testDeck.drawCard(), "Die gezogene Karte darf nicht null sein.");
        assertEquals(51, testDeck.getRemainingCards(), "Nach dem Ziehen einer Karte müssen es 51 sein.");
    }

    @Test
    public void testDeckAutoRefill() {
        // Ziehe alle 52 Karten
        for (int i = 0; i < 52; i++) {
            testDeck.drawCard();
        }
        assertEquals(0, testDeck.getRemainingCards(), "Das Deck sollte jetzt leer sein.");

        // Ziehe Karte 53 -> Deck muss sich automatisch auffüllen (52) und dann eine ausgeben (51)
        Card extraCard = testDeck.drawCard();

        assertNotNull(extraCard, "Auch nach 52 Zügen muss eine Karte kommen.");
        assertEquals(51, testDeck.getRemainingCards(), "Das Deck muss sich neu gemischt und eine Karte ausgegeben haben.");
    }
}