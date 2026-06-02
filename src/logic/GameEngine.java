package logic;

import java.util.ArrayList;
import javax.swing.Timer;

public class GameEngine {
    private Deck deck;
    private ArrayList<Player> players;
    private Dealer dealer;
    private int currentPlayerIndex;
    private GUI.GameWindow window;

    public void setWindow(GUI.GameWindow window) {
        this.window = window;
    }

    // VORHER: public GameEngine(int playerCount) { ... }

    // NACHHER:
    public GameEngine(ArrayList<String> playerNames) {
        this.players = new ArrayList<>();
        this.dealer = new Dealer();
        this.deck = new Deck();

        // Dynamische Erstellung der Spieler anhand der Namensliste
        for (String name : playerNames) {
            this.players.add(new Player(name));
        }
    }

    private boolean betPlaced = false;

    public void startNewRound() {
        currentPlayerIndex = 0;
        deck = new Deck();
        deck.shuffle();
        dealer.clearHand();
        betPlaced = false;

        for (Player p : players) {
            p.clearHand();
        }

        // Karten heimlich ziehen
        for (int i = 0; i < 2; i++) {
            dealer.addCard(deck.drawCard());
            for (Player p : players) {
                p.addCard(deck.drawCard());
            }
        }

        // In der GUI zeigen wir NUR Rückseiten! (z.B. 2 Karten)
        if (window != null) {
            window.updateCardImagesHidden(2);
            window.updateGameView("Bitte setze deinen Einsatz, um die Karten umzudrehen!");
        }
    }

    // Wird aufgerufen, wenn der Button geklickt wird!
    public void startAfterBet() {
        betPlaced = true;
        currentPlayerIndex = 0;
        nextPlayerTurn(); // Startet das normale Spiel und deckt die Karten auf!
    }

    public void nextPlayerTurn() {
        if (currentPlayerIndex < players.size()) {
            Player activePlayer = players.get(currentPlayerIndex);

            // Text für die GUI zusammenbauen
            String uiText = activePlayer.getName() + " ist am Zug.\n\n" +
                    "Aktueller Wert: " + activePlayer.calculateScore() + "\n\n" +
                    "Was möchtest du tun?";

            if (window != null) {
                window.updateGameView(uiText);

                window.updateCardImages(activePlayer.getHand());
                window.updateBalanceView(activePlayer.getBalance(), activePlayer.getCurrentBet());
            }
            activePlayer.makeTurn(this);
        } else {
            boolean allBust = true;
            for (Player p : players) {
                if (p.calculateScore() <= 21) {
                    allBust = false;
                    break;
                }
            }

            if (allBust) {
                System.out.println("Alle Spieler haben sich überkauft! Der Dealer gewinnt sofort.");
                handleAllPlayersBust();
            } else {
                // Wenn alle Spieler fertig sind, fängt der Dealer an zu ziehen
                if (window != null) {
                    window.updateGameView("Der Dealer deckt auf...");
                }

                // Ein Timer, der alle 800 Millisekunden eine Karte zieht/zeigt
                Timer dealerTimer = new Timer(800, null);
                dealerTimer.addActionListener(e -> {
                    int dealerScore = dealer.calculateScore();

                    if (dealerScore < 17) {
                        dealerHit(); // Zieht eine Karte im Hintergrund
                        if (window != null) {
                            window.updateCardImages(dealer.getHand());
                            window.updateGameView("Der Dealer zieht... \nAktueller Wert: " + dealer.calculateScore());
                        }
                    } else {
                        // Dealer ist fertig (17 oder mehr)
                        dealerTimer.stop(); // Timer anhalten!
                        dealerStand(); // Gewinner auswerten
                    }
                });
                dealerTimer.start();
            }
        }
    }

    public void playerHit() {
        Player activePlayer = players.get(currentPlayerIndex);


        activePlayer.addCard(deck.drawCard());
        int newScore = activePlayer.calculateScore();
        if (window != null) {
            window.updateCardImages(activePlayer.getHand());
        }
        // 3. Fall: Spieler hat sich überkauft (> 21)
        if (newScore > 21) {
            System.out.println("gg");
            playerStand();
        }
        // 4. Fall: Spieler hat die perfekte 21 getroffen!
        else if (newScore == 21) {
            String uiText = activePlayer.getName() + " hat die perfekte 21 getroffen!\n" +
                    "Deine Hand: " + activePlayer.getHand() + "\n\n" +
                    "Der Zug wird automatisch beendet.";
            if (window != null) window.updateGameView(uiText);

            playerStand(); // Automatisch weiter zum nächsten, da 21 nicht schlagbar ist
        }
        // 5. Fall: Spieler hat unter 21 und darf normal weiterentscheiden
        else {
            String uiText = activePlayer.getName() + " zieht eine Karte.\n\n" +
                    "Neue Hand: " + activePlayer.getHand() + "\n" +
                    "Aktueller Wert: " + newScore + "\n\n" +
                    "Was möchtest du tun?";
            if (window != null) window.updateGameView(uiText);

            activePlayer.makeTurn(this);
        }
    }

    public void playerStand() {
        currentPlayerIndex++;
        nextPlayerTurn();
    }

    public void dealerHit() {
        Card drawnCard = deck.drawCard();
        dealer.addCard(drawnCard);
        dealer.makeTurn(this);
    }

    public void dealerStand() {
        evaluateWinner();
    }

    private void handleAllPlayersBust() {
        StringBuilder results = new StringBuilder();
        System.out.println("\n--- RUNDENENDE: AUTOMATISCHER VERLUST ---");
        results.append("Alle Spieler haben sich überkauft! Der Dealer gewinnt automatisch.\n");
        results.append("--------------------------------------------------\n\n");

        for (Player p : players) {
            p.loseBet();
            String line = p.getName() + " (Rest: " + p.getBalance() + "€) -> gg (Einsatz verloren!)";
            System.out.println(line);
            results.append(line).append("\n");
        }

        // NEU: Übergabe an die Auswertung
        checkBankruptciesAndShowResults(results.toString());
    }

    private void evaluateWinner() {
        int dealerScore = dealer.calculateScore();
        StringBuilder results = new StringBuilder();

        System.out.println("\n--- RUNDENENDE: ERGEBNISSE ---");
        results.append("Der Dealer hat: ").append(dealerScore).append(" Punkte.\n");
        results.append("--------------------------------------------------\n\n");

        for (Player p : players) {
            int playerScore = p.calculateScore();
            String startLine = p.getName() + "'s Hand ist Wert: " + playerScore + " -> ";
            System.out.print(startLine);
            results.append(startLine);

            if (playerScore > 21) {
                p.loseBet();
                results.append("gg (Überkauft! Rest: ").append(p.getBalance()).append("€)\n");
            } else if (dealerScore > 21) {
                p.winBet();
                results.append("Gewonnen! (Dealer überkauft. Neu: ").append(p.getBalance()).append("€)\n");
            } else if (playerScore == dealerScore) {
                p.pushBet();
                results.append("Unentschieden! (Push. Rest: ").append(p.getBalance()).append("€)\n");
            } else if (playerScore > dealerScore) {
                p.winBet();
                results.append("Gewonnen! (Neu: ").append(p.getBalance()).append("€)\n");
            } else {
                p.loseBet();
                results.append("Verloren! (Dealer hat mehr. Rest: ").append(p.getBalance()).append("€)\n");
            }
        }

        // NEU: Übergabe an die Auswertung
        checkBankruptciesAndShowResults(results.toString());
    }

    // NEU: Diese Methode sucht alle Verlierer und wirft sie aus dem Spiel!
    private void checkBankruptciesAndShowResults(String resultText) {
        ArrayList<String> pleiteNamen = new ArrayList<>();
        ArrayList<Player> toRemove = new ArrayList<>();

        // Alle Spieler prüfen
        for (Player p : players) {
            if (p.getBalance() <= 0) {
                pleiteNamen.add(p.getName()); // Namen für das Drama-Fenster merken
                toRemove.add(p); // Spieler zum Löschen markieren
            }
        }

        // Pleite-Spieler aus der Engine löschen
        players.removeAll(toRemove);

        // Prüfen, ob überhaupt noch jemand lebt
        boolean isGameOver = players.isEmpty();

        if (window != null) {
            // Wir schicken den Text, die Liste der Verlierer und den Game-Over-Status an die GUI
            window.showResults(resultText, pleiteNamen, isGameOver);
        }
    }

    public void setBet(int amount) {
        Player activePlayer = players.get(currentPlayerIndex);

        if (amount > 0 && amount <= activePlayer.getBalance()) {
            activePlayer.setCurrentBet(amount); // Vergiss nicht, diesen Setter in Player.java zu machen!
            if (window != null) {
                window.updateBalanceView(activePlayer.getBalance(), activePlayer.getCurrentBet());
            }
        } else {
            System.out.println("Ungültiger Einsatz!"); // Oder eine JOptionPane Nachricht
        }
    }


}