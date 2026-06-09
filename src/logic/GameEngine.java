package logic;

import GUI.GameWindow;
import Saving.SaveGameManager;

import javax.swing.Timer;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Das Herzstück des Spiels – die Logik-Schicht.
 * <p>
 * Verwaltet Deck, Spielerliste und Dealer, steuert den Rundenablauf
 * und kommuniziert Ergebnisse an die {@link GameWindow GUI-Schicht}.
 * </p>
 * <p>
 * Rundenablauf:
 * <ol>
 *   <li>Karten werden verdeckt ausgeteilt.</li>
 *   <li>Jeder Spieler setzt seinen Einsatz ({@link #setcurrentBet()}).</li>
 *   <li>Alle Einsätze gesetzt → Karten aufgedeckt, Spielzüge beginnen.</li>
 *   <li>Dealer zieht automatisch, danach Auswertung.</li>
 * </ol>
 * </p>
 *
 * @author  Elias
 * @version 1.0
 */
public class GameEngine {

    // -------------------------------------------------------------------------
    // Konstanten
    // -------------------------------------------------------------------------

    /** Punktwert, ab dem der Dealer stehen bleiben muss. */
    private static final int DEALER_STAND_VALUE = 17;

    /** Intervall in ms zwischen den Dealer-Karten. */
    private static final int DEALER_TIMER_MS = 800;

    // -------------------------------------------------------------------------
    // Felder
    // -------------------------------------------------------------------------

    private Deck              deck;
    private ArrayList<Player> players;
    private Dealer            dealer;
    private GameWindow        window;
    private String            loadedSaveId = null;

    /**
     * Index des Spielers, der gerade seinen Einsatz setzt (Bet-Phase).
     * Läuft von 0 bis players.size()-1, dann startet die Spielphase.
     */
    private int betPlayerIndex;

    /**
     * Index des Spielers, der gerade am Zug ist (Spielphase).
     */
    private int currentPlayerIndex;

    // -------------------------------------------------------------------------
    // Konstruktor
    // -------------------------------------------------------------------------

    /**
     * Erstellt eine neue GameEngine mit einer dynamischen Spielerliste.
     *
     * @param playerNames Namen der Spieler; für jeden wird ein {@link Player} erstellt.
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

    /** @param window Das aktive {@link GameWindow}. */
    public void setWindow(GameWindow window) {
        this.window = window;
    }

    /**
     * Setzt die ID des geladenen Spielstands.
     * Bei Game-Over wird dieser Spielstand automatisch gelöscht.
     *
     * @param id Spielstand-ID oder {@code null} für ein neues Spiel.
     */
    public void setLoadedSaveId(String id) {
        this.loadedSaveId = id;
    }

    // -------------------------------------------------------------------------
    // Rundensteuerung
    // -------------------------------------------------------------------------

    /**
     * Startet eine neue Runde: Karten werden verdeckt ausgeteilt,
     * dann beginnt die Einsatz-Phase beim ersten Spieler.
     */
    public void startNewRound() {
        betPlayerIndex     = 0;
        currentPlayerIndex = 0;

        deck = new Deck();
        deck.refillAndShuffle();

        dealer.clearHand();
        for (Player p : players) {
            p.clearHand();
        }

        for (int i = 0; i < 2; i++) {
            dealer.addCard(deck.drawCard());
            for (Player p : players) {
                p.addCard(deck.drawCard());
            }
        }

        // Einsatz-Phase starten: Karten bleiben verdeckt
        if (window != null) {
            window.updateCardImagesHidden(2);
            startBetPhase();
        }
    }

    /**
     * Startet die Einsatz-Phase: zeigt dem aktuellen Spieler das Einsatz-UI
     * und wartet auf seine Eingabe.
     */
    private void startBetPhase() {
        if (betPlayerIndex < players.size()) {
            Player betPlayer = players.get(betPlayerIndex);
            if (window != null) {
                window.updateBalanceView(betPlayer.getBalance(), betPlayer.getCurrentBet());
                window.updateGameView(betPlayer.getName() + ", setze deinen Einsatz!\n\n"
                        + "Guthaben: " + betPlayer.getBalance() + "€\n"
                        + "Mindest-Einsatz: " + Player.MIN_BET + "€");
                window.showBetUI(true);  // Einsatz-Felder AN, Hit/Stand AUS
            }
        }
    }

    /**
     * Bestätigt den Einsatz des aktuellen Bet-Spielers und geht zum nächsten.
     * Haben alle Spieler gesetzt, beginnt die Spielphase.
     *
     * @param amount Der Einsatz in Euro.
     * @throws IllegalArgumentException wenn der Betrag ungültig ist.
     */
    public void confirmBet(int amount) {
        Player betPlayer = players.get(betPlayerIndex);
        betPlayer.setCurrentBet(amount); // wirft IllegalArgumentException bei ungültigem Wert

        if (window != null) {
            window.updateBalanceView(betPlayer.getBalance(), betPlayer.getCurrentBet());
        }

        betPlayerIndex++;

        if (betPlayerIndex < players.size()) {
            // Nächster Spieler setzt seinen Einsatz
            startBetPhase();
        } else {
            // Alle haben gesetzt → Karten aufdecken, Spielphase starten
            currentPlayerIndex = 0;
            if (window != null) {
                window.showBetUI(false); // Einsatz-Felder AUS, Hit/Stand AN
            }
            nextPlayerTurn();
        }
    }

    // -------------------------------------------------------------------------
    // Spielphase
    // -------------------------------------------------------------------------

    /**
     * Aktiviert den nächsten Spieler.
     * Sind alle Spieler fertig, beginnt der Dealer-Zug.
     */
    public void nextPlayerTurn() {
        if (currentPlayerIndex < players.size()) {
            Player active = players.get(currentPlayerIndex);

            if (window != null) {
                window.updateGameView(active.getName() + " ist am Zug.\n\n"
                        + "Aktueller Wert: " + active.calculateScore() + "\n\n"
                        + "Was möchtest du tun?");
                window.updateCardImages(active.getHand());
                window.updateBalanceView(active.getBalance(), active.getCurrentBet());
            }

            active.makeTurn(this);

        } else {
            boolean allBust = players.stream()
                    .allMatch(p -> p.calculateScore() > Participant.BLACKJACK_VALUE);
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
     * Der aktive Spieler zieht eine Karte ("Hit").
     */
    public void playerHit() {
        Player active = players.get(currentPlayerIndex);
        active.addCard(deck.drawCard());
        int newScore = active.calculateScore();

        if (window != null) {
            window.updateCardImages(active.getHand());
        }

        if (newScore > Participant.BLACKJACK_VALUE) {
            if (window != null) window.updateGameView(
                    active.getName() + " hat sich überkauft! (" + newScore + " Punkte)");
            playerStand();

        } else if (newScore == Participant.BLACKJACK_VALUE) {
            if (window != null) window.updateGameView(
                    active.getName() + " hat die perfekte 21!\nZug wird automatisch beendet.");
            playerStand();

        } else {
            if (window != null) window.updateGameView(
                    active.getName() + " zieht eine Karte.\n\n"
                            + "Neue Hand: " + active.getHand() + "\n"
                            + "Aktueller Wert: " + newScore + "\n\n"
                            + "Was möchtest du tun?");
            active.makeTurn(this);
        }
    }

    /**
     * Der aktive Spieler bleibt stehen ("Stand").
     */
    public void playerStand() {
        currentPlayerIndex++;
        nextPlayerTurn();
    }

    // -------------------------------------------------------------------------
    // Dealer-Zug
    // -------------------------------------------------------------------------

    /**
     * Startet den automatischen Dealer-Zug mit Swing-Timer.
     */
    private void startDealerTurn() {
        if (window != null) window.updateGameView("Der Dealer deckt auf...");

        Timer dealerTimer = new Timer(DEALER_TIMER_MS, null);
        dealerTimer.addActionListener(e -> {
            if (dealer.calculateScore() < DEALER_STAND_VALUE) {
                dealer.addCard(deck.drawCard());
                if (window != null) {
                    window.updateCardImages(dealer.getHand());
                    window.updateGameView("Der Dealer zieht...\nAktueller Wert: "
                            + dealer.calculateScore());
                }
            } else {
                dealerTimer.stop();
                evaluateWinner();
            }
        });
        dealerTimer.start();
    }

    // -------------------------------------------------------------------------
    // Auswertung
    // -------------------------------------------------------------------------

    /**
     * Sonderfall: alle Spieler überkauft, Dealer gewinnt sofort.
     */
    private void handleAllPlayersBust() {
        StringBuilder results = new StringBuilder();
        results.append("Alle Spieler haben sich überkauft! Der Dealer gewinnt.\n");
        results.append("--------------------------------------------------\n\n");
        for (Player p : players) {
            p.loseBet();
            results.append(p.getName())
                    .append(" (Rest: ").append(p.getBalance()).append("€) → überkauft\n");
        }
        checkBankruptciesAndShowResults(results.toString());
    }

    /**
     * Vergleicht jeden Spieler mit dem Dealer und passt Guthaben an.
     */
    private void evaluateWinner() {
        int dealerScore = dealer.calculateScore();
        StringBuilder results = new StringBuilder();
        results.append("Der Dealer hat: ").append(dealerScore).append(" Punkte.\n");
        results.append("--------------------------------------------------\n\n");

        for (Player p : players) {
            int ps = p.calculateScore();
            results.append(p.getName()).append(" (").append(ps).append(" Punkte) → ");

            if (ps > Participant.BLACKJACK_VALUE) {
                p.loseBet();
                results.append("Überkauft! (Rest: ").append(p.getBalance()).append("€)\n");
            } else if (dealerScore > Participant.BLACKJACK_VALUE) {
                p.winBet();
                results.append("Gewonnen! Dealer überkauft. (Neu: ").append(p.getBalance()).append("€)\n");
            } else if (ps == dealerScore) {
                p.pushBet();
                results.append("Unentschieden! Push. (Rest: ").append(p.getBalance()).append("€)\n");
            } else if (ps > dealerScore) {
                p.winBet();
                results.append("Gewonnen! (Neu: ").append(p.getBalance()).append("€)\n");
            } else {
                p.loseBet();
                results.append("Verloren! (Rest: ").append(p.getBalance()).append("€)\n");
            }
        }
        checkBankruptciesAndShowResults(results.toString());
    }

    /**
     * Prüft Bankrotte, entfernt Pleite-Spieler, löscht ggf. den Spielstand
     * und übergibt das Ergebnis an die GUI.
     *
     * @param resultText Der Ergebnistext für die GUI.
     */
    private void checkBankruptciesAndShowResults(String resultText) {
        ArrayList<String> bankruptNames = new ArrayList<>();
        ArrayList<Player> toRemove      = new ArrayList<>();

        for (Player p : players) {
            if (p.getBalance() <= 0) {
                bankruptNames.add(p.getName());
                toRemove.add(p);
            }
        }

        players.removeAll(toRemove);
        boolean isGameOver = players.isEmpty();

        if (isGameOver && loadedSaveId != null) {
            try {
                SaveGameManager.deleteSave(loadedSaveId);
            } catch (IOException e) {
                System.err.println("Fehler beim Löschen des Spielstands: " + e.getMessage());
            }
            loadedSaveId = null;
        }

        if (window != null) {
            window.showResults(resultText, bankruptNames, isGameOver);
        }
    }

    // -------------------------------------------------------------------------
    // Getter
    // -------------------------------------------------------------------------

    /** @return Alle aktiven Spieler. */
    public ArrayList<Player> getPlayers() { return players; }

    /** @return Der Dealer. */
    public Dealer getDealer() { return dealer; }

    /** @return Das aktuelle Deck. */
    public Deck getDeck() { return deck; }
}