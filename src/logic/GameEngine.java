package logic;

import GUI.GameWindow;


import javax.swing.Timer;
import java.util.ArrayList;

/**
 * Das Herzstück des Spiels – die Logik-Schicht.
 * <p>
 * Verwaltet Deck, Spielerliste und Dealer, steuert den Rundenablauf
 * und kommuniziert Ergebnisse an die {@link GameWindow GUI-Schicht}.
 * </p>
 *
 * @author  [Dein Name]
 * @version 1.0
 */
public class GameEngine {

    // -------------------------------------------------------------------------
    // Konstanten
    // -------------------------------------------------------------------------

    /** Punktwert, ab dem der Dealer stehen bleiben muss. */
    private static final int DEALER_STAND_VALUE = 17;

    /** Intervall in ms, in dem der Dealer eine Karte nach der anderen aufdeckt. */
    private static final int DEALER_TIMER_MS = 800;

    // -------------------------------------------------------------------------
    // Felder
    // -------------------------------------------------------------------------

    /** Das Kartendeck für die aktuelle Runde. */
    private Deck deck;

    /** Liste aller aktiven Spieler. */
    private ArrayList<Player> players;

    /** Der Dealer (Computer-Gegner). */
    private Dealer dealer;

    /** Index des Spielers, der gerade am Zug ist. */
    private int currentPlayerIndex;

    /** Referenz auf das GUI-Fenster (darf {@code null} sein). */
    private GameWindow window;

    /** Gibt an, ob der Einsatz für diese Runde bereits gesetzt wurde. */
    private boolean betPlaced;

    // -------------------------------------------------------------------------
    // Konstruktor
    // -------------------------------------------------------------------------

    /**
     * Erstellt eine neue GameEngine mit einer dynamischen Spielerliste.
     *
     * @param playerNames Liste der Spielernamen; für jeden Namen wird ein {@link Player} erstellt.
     */
    public GameEngine(ArrayList<String> playerNames) {
        this.players = new ArrayList<>();
        this.dealer  = new Dealer();
        this.deck    = new Deck();

        for (String name : playerNames) {
            this.players.add(new Player(name));
        }
    }

    // -------------------------------------------------------------------------
    // Setter
    // -------------------------------------------------------------------------

    /**
     * Setzt die Referenz auf das GUI-Fenster.
     * Muss nach der Konstruktion aufgerufen werden, bevor die Runde startet.
     *
     * @param window Das aktive {@link GameWindow}.
     */
    public void setWindow(GameWindow window) {
        this.window = window;
    }

    // -------------------------------------------------------------------------
    // Rundensteuerung
    // -------------------------------------------------------------------------

    /**
     * Startet eine neue Runde: Deck wird neu gemischt, Hände geleert,
     * Karten verteilt. Die GUI zeigt zunächst nur Rückseiten an,
     * bis der Spieler seinen Einsatz gesetzt hat.
     */
    public void startNewRound() {
        currentPlayerIndex = 0;
        betPlaced          = false;

        deck = new Deck();
        deck.refillAndShuffle();

        dealer.clearHand();
        for (Player p : players) {
            p.clearHand();
        }

        // Zwei Karten für alle austeilen
        for (int i = 0; i < 2; i++) {
            dealer.addCard(deck.drawCard());
            for (Player p : players) {
                p.addCard(deck.drawCard());
            }
        }

        if (window != null) {
            window.updateCardImagesHidden(2);
            window.updateGameView("Bitte setze deinen Einsatz, um die Karten umzudrehen!");
        }
    }

    /**
     * Wird aufgerufen, sobald der Spieler seinen Einsatz bestätigt hat.
     * Deckt die Karten auf und startet den ersten Spielerzug.
     */
    public void startAfterBet() {
        betPlaced          = true;
        currentPlayerIndex = 0;
        nextPlayerTurn();
    }

    /**
     * Aktiviert den nächsten Spieler in der Liste.
     * Sind alle Spieler fertig, beginnt der Dealer-Zug.
     */
    public void nextPlayerTurn() {
        if (currentPlayerIndex < players.size()) {
            Player active = players.get(currentPlayerIndex);

            String uiText = active.getName() + " ist am Zug.\n\n"
                    + "Aktueller Wert: " + active.calculateScore() + "\n\n"
                    + "Was möchtest du tun?";

            if (window != null) {
                window.updateGameView(uiText);
                window.updateCardImages(active.getHand());
                window.updateBalanceView(active.getBalance(), active.getCurrentBet());
            }

            active.makeTurn(this);

        } else {
            // Alle Spieler fertig → prüfen ob alle überkauft sind
            boolean allBust = players.stream().allMatch(p -> p.calculateScore() > Participant.BLACKJACK_VALUE);

            if (allBust) {
                handleAllPlayersBust();
            } else {
                startDealerTurn();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Spieler-Aktionen
    // -------------------------------------------------------------------------

    /**
     * Der aktive Spieler zieht eine weitere Karte ("Hit").
     * Bei Überkauf oder Blackjack wird der Zug automatisch beendet.
     */
    public void playerHit() {
        Player active = players.get(currentPlayerIndex);
        active.addCard(deck.drawCard());
        int newScore = active.calculateScore();

        if (window != null) {
            window.updateCardImages(active.getHand());
        }

        if (newScore > Participant.BLACKJACK_VALUE) {
            // Überkauft
            if (window != null) {
                window.updateGameView(active.getName() + " hat sich überkauft! (" + newScore + " Punkte)");
            }
            playerStand();

        } else if (newScore == Participant.BLACKJACK_VALUE) {
            // Perfekte 21
            if (window != null) {
                window.updateGameView(active.getName() + " hat die perfekte 21 getroffen!\n"
                        + "Der Zug wird automatisch beendet.");
            }
            playerStand();

        } else {
            // Weiterspielen
            if (window != null) {
                window.updateGameView(active.getName() + " zieht eine Karte.\n\n"
                        + "Neue Hand: " + active.getHand() + "\n"
                        + "Aktueller Wert: " + newScore + "\n\n"
                        + "Was möchtest du tun?");
            }
            active.makeTurn(this);
        }
    }

    /**
     * Der aktive Spieler bleibt stehen ("Stand").
     * Der nächste Spieler oder der Dealer ist an der Reihe.
     */
    public void playerStand() {
        currentPlayerIndex++;
        nextPlayerTurn();
    }

    // -------------------------------------------------------------------------
    // Dealer-Zug
    // -------------------------------------------------------------------------

    /**
     * Startet den automatischen Dealer-Zug mit einem Swing-Timer.
     * Der Dealer zieht Karten in Abständen von {@value #DEALER_TIMER_MS} ms,
     * bis er {@value #DEALER_STAND_VALUE} oder mehr Punkte hat.
     */
    private void startDealerTurn() {
        if (window != null) {
            window.updateGameView("Der Dealer deckt auf...");
        }

        Timer dealerTimer = new Timer(DEALER_TIMER_MS, null);
        dealerTimer.addActionListener(e -> {
            int dealerScore = dealer.calculateScore();

            if (dealerScore < DEALER_STAND_VALUE) {
                dealerHit();
                if (window != null) {
                    window.updateCardImages(dealer.getHand());
                    window.updateGameView("Der Dealer zieht...\nAktueller Wert: " + dealer.calculateScore());
                }
            } else {
                dealerTimer.stop();
                evaluateWinner();
            }
        });
        dealerTimer.start();
    }

    /**
     * Der Dealer zieht eine Karte vom Deck.
     * Wird vom Timer in {@link #startDealerTurn()} aufgerufen.
     */
    public void dealerHit() {
        dealer.addCard(deck.drawCard());
    }

    // -------------------------------------------------------------------------
    // Einsatz
    // -------------------------------------------------------------------------

    /**
     * Setzt den Einsatz des aktuell aktiven Spielers.
     * Der Betrag wird gegen das Guthaben des Spielers validiert.
     *
     * @param amount Der gewünschte Einsatz in Euro (muss &gt; 0 und ≤ Guthaben sein).
     */
    public void setBet(int amount) {
        Player active = players.get(currentPlayerIndex);
        try {
            active.setCurrentBet(amount);
            if (window != null) {
                window.updateBalanceView(active.getBalance(), active.getCurrentBet());
            }
        } catch (IllegalArgumentException e) {
            // Fehlermeldung wird von der GUI (handleSetBet) angezeigt
            throw e;
        }
    }

    // -------------------------------------------------------------------------
    // Auswertung
    // -------------------------------------------------------------------------

    /**
     * Sonderfall: Alle Spieler haben sich überkauft.
     * Der Dealer gewinnt automatisch, ohne Karten aufzudecken.
     */
    private void handleAllPlayersBust() {
        StringBuilder results = new StringBuilder();
        results.append("Alle Spieler haben sich überkauft! Der Dealer gewinnt automatisch.\n");
        results.append("--------------------------------------------------\n\n");

        for (Player p : players) {
            p.loseBet();
            results.append(p.getName())
                    .append(" (Rest: ").append(p.getBalance()).append("€) → überkauft\n");
        }

        checkBankruptciesAndShowResults(results.toString());
    }

    /**
     * Wertet die Runde aus: vergleicht jeden Spieler mit dem Dealer
     * und passt die Guthaben entsprechend an.
     */
    private void evaluateWinner() {
        int dealerScore = dealer.calculateScore();
        StringBuilder results = new StringBuilder();

        results.append("Der Dealer hat: ").append(dealerScore).append(" Punkte.\n");
        results.append("--------------------------------------------------\n\n");

        for (Player p : players) {
            int playerScore = p.calculateScore();
            results.append(p.getName()).append("'s Hand ist wert: ").append(playerScore).append(" → ");

            if (playerScore > Participant.BLACKJACK_VALUE) {
                p.loseBet();
                results.append("Überkauft! (Rest: ").append(p.getBalance()).append("€)\n");
            } else if (dealerScore > Participant.BLACKJACK_VALUE) {
                p.winBet();
                results.append("Gewonnen! Dealer überkauft. (Neu: ").append(p.getBalance()).append("€)\n");
            } else if (playerScore == dealerScore) {
                p.pushBet();
                results.append("Unentschieden! Push. (Rest: ").append(p.getBalance()).append("€)\n");
            } else if (playerScore > dealerScore) {
                p.winBet();
                results.append("Gewonnen! (Neu: ").append(p.getBalance()).append("€)\n");
            } else {
                p.loseBet();
                results.append("Verloren! Dealer hat mehr. (Rest: ").append(p.getBalance()).append("€)\n");
            }
        }

        checkBankruptciesAndShowResults(results.toString());
    }

    /**
     * Prüft nach der Rundenauswertung, welche Spieler pleite sind,
     * entfernt sie aus der Spielerliste und übergibt das Ergebnis an die GUI.
     *
     * @param resultText Der fertig formatierte Ergebnistext für die GUI.
     */
    private void checkBankruptciesAndShowResults(String resultText) {
        ArrayList<String>  bankruptNames = new ArrayList<>();
        ArrayList<Player>  toRemove      = new ArrayList<>();

        for (Player p : players) {
            if (p.getBalance() <= 0) {
                bankruptNames.add(p.getName());
                toRemove.add(p);
            }
        }

        players.removeAll(toRemove);
        boolean isGameOver = players.isEmpty();

        if (window != null) {
            window.showResults(resultText, bankruptNames, isGameOver);
        }
    }

    // -------------------------------------------------------------------------
    // Getter
    // -------------------------------------------------------------------------

    /**
     * Gibt die Liste der noch aktiven Spieler zurück.
     *
     * @return {@link ArrayList} mit allen verbleibenden {@link Player}-Objekten.
     */
    public ArrayList<Player> getPlayers() {
        return players;
    }

    /**
     * Gibt den Dealer zurück.
     *
     * @return Der {@link Dealer} dieser Runde.
     */
    public Dealer getDealer() {
        return dealer;
    }

    /**
     * Gibt das aktuelle Deck zurück.
     *
     * @return Das aktive {@link Deck}.
     */
    public Deck getDeck() {
        return deck;
    }
}