package logic;

/**
 * Repräsentiert den Dealer (Croupier) im Blackjack-Spiel.
 * Erbt von {@link Participant} und steuert seine Züge vollautomatisch.
 * * @author  [Dein Name]
 * @version 1.0
 */
public class Dealer extends Participant {

    /** Der Punktwert, ab dem der Dealer laut Regelwerk stehen bleiben MUSS. */
    public static final int DEALER_STAND_LIMIT = 17;

    public Dealer() {
        super("Dealer");
    }

    /**
     * Führt den automatischen Zug des Dealers aus.
     * Der Dealer zieht solange Karten, bis er mindestens 17 Punkte erreicht hat.
     * * @param engine Die aktive {@link GameEngine}, von der Karten gezogen werden.
     */
    @Override
    public void makeTurn(GameEngine engine) {
        while (calculateScore() < DEALER_STAND_LIMIT) {
            engine.dealerHit();
        }
    }
}