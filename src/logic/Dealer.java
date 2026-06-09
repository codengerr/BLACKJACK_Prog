package logic;

/**
 * Repräsentiert den Dealer (Croupier) im Blackjack-Spiel.
 * <p>
 * Erbt Kartenverwaltung und Punkteberechnung von {@link Participant}.
 * Der automatische Dealer-Zug wird vollständig von {@link GameEngine#startDealerTurn()}
 * über einen Swing-Timer gesteuert – {@link #makeTurn(GameEngine)} bleibt daher leer.
 * </p>
 *
 * @author  [Dein Name]
 * @version 1.0
 */
public class Dealer extends Participant {

    /** Punktwert, ab dem der Dealer laut Regelwerk stehen bleiben muss. */
    public static final int DEALER_STAND_LIMIT = 17;

    /**
     * Erstellt einen neuen Dealer mit dem festen Namen "Dealer".
     */
    public Dealer() {
        super("Dealer");
    }

    /**
     * Beim Dealer wird der Zug automatisch von {@link GameEngine} über einen
     * Timer gesteuert. Diese Methode bleibt daher bewusst leer.
     *
     * @param engine Die aktive {@link GameEngine}.
     */
    @Override
    public void makeTurn(GameEngine engine) {
        // Dealer-Logik läuft in GameEngine.startDealerTurn() via Timer
    }
}