package logic;

/**
 * Repräsentiert einen menschlichen Spieler im Blackjack-Spiel.
 * <p>
 * Erbt Kartenverwaltung und Punkteberechnung von {@link Participant}.
 * Verwaltet zusätzlich Guthaben und Einsatz des Spielers.
 * Der eigentliche Spielzug wird über die GUI gesteuert – {@link #makeTurn(GameEngine)}
 * dient hier nur als Auslöser, damit die Engine den richtigen Spieler aktiviert.
 * </p>
 *
 * @author  [Dein Name]
 * @version 1.0
 */
public class Player extends Participant {

    /** Startkapital jedes neuen Spielers in Euro. */
    public static final int STARTING_BALANCE = 100;

    /** Mindest-Einsatz pro Runde in Euro. */
    public static final int MIN_BET = 5;

    /** Das aktuelle Guthaben des Spielers. */
    private int balance;

    /** Der Einsatz des Spielers für die aktuelle Runde. */
    private int currentBet;

    /**
     * Erstellt einen neuen Spieler mit dem angegebenen Namen,
     * Startkapital und Standard-Einsatz.
     *
     * @param name Der Anzeigename des Spielers.
     */
    public Player(String name) {
        super(name);
        this.balance    = STARTING_BALANCE;
        this.currentBet = MIN_BET;
    }

    /**
     * Wird von der {@link GameEngine} aufgerufen, wenn dieser Spieler am Zug ist.
     * Da der Zug über GUI-Buttons gesteuert wird, muss hier nichts weiter passieren –
     * die Engine wartet auf {@code playerHit()} oder {@code playerStand()}.
     *
     * @param engine Die aktive {@link GameEngine}.
     */
    @Override
    public void makeTurn(GameEngine engine) {
        // Spielerzug läuft über GUI-Events (Hit/Stand-Buttons).
        // Kein automatischer Zug nötig.
    }

    // -------------------------------------------------------------------------
    // Getter & Setter
    // -------------------------------------------------------------------------

    /**
     * Gibt das aktuelle Guthaben des Spielers zurück.
     *
     * @return Guthaben in Euro.
     */
    public int getBalance() {
        return balance;
    }

    /**
     * Gibt den aktuellen Einsatz des Spielers zurück.
     *
     * @return Einsatz in Euro.
     */
    public int getCurrentBet() {
        return currentBet;
    }

    /**
     * Setzt den Einsatz auf einen bestimmten Betrag.
     * Der Betrag muss zwischen {@link #MIN_BET} und dem verfügbaren Guthaben liegen.
     *
     * @param amount Der gewünschte Einsatz in Euro.
     * @throws IllegalArgumentException wenn der Betrag ungültig ist.
     */
    public void setCurrentBet(int amount) {
        if (amount < MIN_BET) {
            throw new IllegalArgumentException(
                    "Einsatz muss mindestens " + MIN_BET + "€ betragen.");
        }
        if (amount > balance) {
            throw new IllegalArgumentException(
                    "Einsatz darf das Guthaben (" + balance + "€) nicht überschreiten.");
        }
        this.currentBet = amount;
    }

    // -------------------------------------------------------------------------
    // Einsatz-Anpassung
    // -------------------------------------------------------------------------

    /**
     * Erhöht den aktuellen Einsatz um den angegebenen Betrag,
     * sofern das Guthaben ausreicht.
     *
     * @param amount Der Erhöhungsbetrag in Euro.
     */
    public void increaseBet(int amount) {
        if (currentBet + amount <= balance) {
            currentBet += amount;
        }
    }

    /**
     * Verringert den aktuellen Einsatz um den angegebenen Betrag,
     * sofern der Mindesteinsatz nicht unterschritten wird.
     *
     * @param amount Der Verringerungsbetrag in Euro.
     */
    public void decreaseBet(int amount) {
        if (currentBet - amount >= MIN_BET) {
            currentBet -= amount;
        }
    }

    // -------------------------------------------------------------------------
    // Rundenabrechnung
    // -------------------------------------------------------------------------

    /**
     * Verarbeitet einen Gewinn: Guthaben wird um den Einsatz erhöht,
     * Einsatz wird auf den Mindestwert zurückgesetzt.
     */
    public void winBet() {
        balance    += currentBet;
        currentBet  = MIN_BET;
    }

    /**
     * Verarbeitet einen Verlust: Guthaben wird um den Einsatz verringert,
     * Einsatz wird auf den Mindestwert zurückgesetzt.
     */
    public void loseBet() {
        balance    -= currentBet;
        currentBet  = MIN_BET;
    }

    /**
     * Verarbeitet ein Unentschieden (Push): Guthaben bleibt unverändert,
     * Einsatz wird auf den Mindestwert zurückgesetzt.
     */
    public void pushBet() {
        currentBet = MIN_BET;
    }

    /**
     * Gibt eine lesbare Zusammenfassung des Spielers zurück.
     *
     * @return Name, Guthaben, Einsatz und aktuelle Hand als String.
     */
    @Override
    public String toString() {
        return getName()
                + " | Guthaben: " + balance + "€"
                + " | Einsatz: "  + currentBet + "€"
                + " | Hand: "     + getHand()
                + " | Wert: "     + calculateScore();
    }
}