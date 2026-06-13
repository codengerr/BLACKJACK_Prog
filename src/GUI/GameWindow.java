package GUI;

import Saving.GameState;
import Saving.SaveGameManager;
import logic.Card;
import logic.GameEngine;
import logic.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Das Hauptfenster des Blackjack Spiels.
 * <p>
 * Verwaltet alle Screens (Setup Spiel Ergebnis Bankrott) über ein
 * {@link CardLayout} und kommuniziert bidirektional mit der {@link GameEngine}.
 * </p>
 *
 * @author  Elias
 * @version 1.0
 */
public class GameWindow extends JFrame {

    // -------------------------------------------------------------------------
    // Konstanten
    // -------------------------------------------------------------------------

    /** Verzögerung in ms nach Screenwechsel bevor Tastatur wieder aktiv ist. */
    private static final int INPUT_DELAY_MS   = 200;

    /** Blinkintervall in ms für den Hinweistext auf dem Ergebnisscreen. */
    private static final int BLINK_INTERVAL_MS = 600;

    // -------------------------------------------------------------------------
    // Screens
    // -------------------------------------------------------------------------

    private JPanel setupScreen;
    private JPanel gameScreen;
    private JPanel resultScreen;
    private JPanel bankruptScreen;

    // -------------------------------------------------------------------------
    // UI Elemente
    // -------------------------------------------------------------------------

    private JTextArea  gameTextArea;
    private JTextArea  resultTextArea;
    private JTextArea  dramaTextArea;
    private JPanel     cardPanel;
    private JLabel     balanceLabel;
    private JLabel     scoreLabel;
    private JTextField betInputField;
    private JButton    setBetButton;
    private JButton    hitButton;
    private JButton    standButton;

    // -------------------------------------------------------------------------
    // Zustand
    // -------------------------------------------------------------------------

    private CardLayout        cardLayout;
    private JPanel            mainContainer;
    private GameEngine        engine;
    private ArrayList<String> currentBankrupts = new ArrayList<>();
    private boolean           isGameOver       = false;
    private boolean           gameScreenReady  = false;

    // -------------------------------------------------------------------------
    // Konstruktor
    // -------------------------------------------------------------------------

    /**
     * Erstellt und zeigt das Hauptfenster.
     */
    public GameWindow() {
        setTitle("Blackjack Casino");
        setSize(1980, 1020);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout    = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        buildSetupScreen();
        buildGameScreen();
        buildResultScreen();
        buildBankruptScreen();

        mainContainer.add(setupScreen,    "SETUP");
        mainContainer.add(gameScreen,     "GAME");
        mainContainer.add(resultScreen,   "RESULTS");
        mainContainer.add(bankruptScreen, "BANKRUPT");

        add(mainContainer);
        setVisible(true);
        cardLayout.show(mainContainer, "SETUP");
    }

    // -------------------------------------------------------------------------
    // Innere Klasse: Hintergrundpanel
    // -------------------------------------------------------------------------

    /**
     * {@link JPanel} das ein skalierbares Hintergrundbild zeichnet.
     */
    static class BackgroundPanel extends JPanel {
        private final Image bgImage;

        /**
         * @param layout Das zu verwendende Layout.
         */
        public BackgroundPanel(LayoutManager layout) {
            super(layout);
            Image loaded;
            try {
                loaded = new ImageIcon("resources/table.png").getImage();
            } catch (Exception e) {
                System.err.println("Hintergrundbild konnte nicht geladen werden.");
                loaded = null;
            }
            this.bgImage = loaded;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) {
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    /**
     * Wendet einheitliches dunkles Styling auf einen Button an.
     *
     * @param btn Der zu stylende Button.
     */
    private void styleButton(JButton btn) {
        btn.setBackground(new Color(40, 40, 40));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Wechselt zum Gamescreen und gibt nach kurzer Verzögerung die Tastatur frei.
     */
    private void switchToGameWithDelay() {
        this.gameScreenReady = false;
        cardLayout.show(mainContainer, "GAME");
        Timer t = new Timer(INPUT_DELAY_MS, e -> {
            this.gameScreenReady = true;
            gameScreen.requestFocusInWindow();
        });
        t.setRepeats(false);
        t.start();
    }

    // -------------------------------------------------------------------------
    // Screenaufbau
    // -------------------------------------------------------------------------

    /**
     * Erstellt den Setupscreen mit Spieleranzahlauswahl Start Lade und Beendenbutton.
     */
    private void buildSetupScreen() {
        setupScreen = new BackgroundPanel(new BorderLayout());

        // Mittleres Panel für die Hauptelemente im GridBagLayout zentriert
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setOpaque(false);

        JPanel innerPanel = new JPanel(new GridLayout(4, 1, 20, 20));
        innerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Willkommen im Blackjack Casino", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);

        String[] options = {"1 Spieler (Solo)", "2 Spieler", "3 Spieler", "4 Spieler", "Benutzerdefiniert..."};
        JComboBox<String> playerSelect = new JComboBox<>(options);
        playerSelect.setFont(new Font("Arial", Font.BOLD, 18));

        JButton startButton = new JButton("Spiel Starten");
        styleButton(startButton);
        startButton.setFont(new Font("Arial", Font.BOLD, 22));

        JButton loadButton = new JButton("Spielstand laden");
        styleButton(loadButton);
        loadButton.setFont(new Font("Arial", Font.BOLD, 18));
        loadButton.setForeground(Color.ORANGE);

        playerSelect.addActionListener(e ->
                startButton.setText(playerSelect.getSelectedIndex() == 4
                        ? "Spielerzahl wählen & Starten" : "Spiel Starten"));

        startButton.addActionListener(e -> handleStartButton(playerSelect));
        loadButton.addActionListener(e  -> handleLoadButton());

        innerPanel.add(titleLabel);
        innerPanel.add(playerSelect);
        innerPanel.add(startButton);
        innerPanel.add(loadButton);
        centerContainer.add(innerPanel);

        // Unteres Panel für den Beendenknopf unten rechts
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        bottomPanel.setOpaque(false);

        JButton setupExitButton = new JButton("Beenden");
        styleButton(setupExitButton);
        setupExitButton.setFocusable(false);
        setupExitButton.setForeground(Color.RED);
        setupExitButton.addActionListener(e -> System.exit(0));
        bottomPanel.add(setupExitButton);

        setupScreen.add(centerContainer, BorderLayout.CENTER);
        setupScreen.add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Verarbeitet "Spiel Starten": fragt Spielerzahl und Namen ab startet die Engine.
     *
     * @param playerSelect Dropdown mit der Spieleranzahl.
     */
    private void handleStartButton(JComboBox<String> playerSelect) {
        int playerCount;
        int selectedIndex = playerSelect.getSelectedIndex();

        if (selectedIndex < 4) {
            playerCount = selectedIndex + 1;
        } else {
            String input = JOptionPane.showInputDialog(
                    this, "Wie viele Spieler? (Maximal 10):", "Custom Lobby", JOptionPane.QUESTION_MESSAGE);
            if (input == null) return;
            try {
                playerCount = Integer.parseInt(input.trim());
                if (playerCount < 1 || playerCount > 10) {
                    JOptionPane.showMessageDialog(this, "Bitte eine Zahl zwischen 1 und 10 eingeben!");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ungültige Eingabe!");
                return;
            }
        }

        ArrayList<String> playerNames = new ArrayList<>();
        int dialogResult = JOptionPane.showConfirmDialog(this,
                "Möchtest du den Spielern eigene Namen geben?",
                "Namen eingeben?", JOptionPane.YES_NO_OPTION);

        for (int i = 1; i <= playerCount; i++) {
            if (dialogResult == JOptionPane.YES_OPTION) {
                String name = JOptionPane.showInputDialog(
                        this, "Name für Spieler " + i + ":", "Spieler " + i);
                playerNames.add((name == null || name.trim().isEmpty()) ? "Spieler " + i : name.trim());
            } else {
                playerNames.add("Spieler " + i);
            }
        }

        engine = new GameEngine(playerNames);
        engine.setWindow(this);
        prepareNewRoundUI();
        engine.startNewRound();
        switchToGameWithDelay();
    }

    /**
     * Verarbeitet "Spielstand laden": öffnet den Ladedialog und stellt
     * Namen und Guthaben aus dem gespeicherten Zustand wieder her.
     */
    private void handleLoadButton() {
        LoadGameDialog dialog = new LoadGameDialog(this);
        dialog.setVisible(true);

        GameState loaded = dialog.getSelectedState();
        if (loaded == null) return;

        ArrayList<String> names = new ArrayList<>();
        for (GameState.PlayerEntry entry : loaded.getPlayers()) {
            names.add(entry.getName());
        }

        engine = new GameEngine(names);

        for (int i = 0; i < engine.getPlayers().size(); i++) {
            engine.getPlayers().get(i).setBalance(loaded.getPlayers().get(i).getBalance());
        }

        engine.setWindow(this);
        engine.setLoadedSaveId(loaded.getId()); // damit Engine den Save bei Game Over löscht
        prepareNewRoundUI();
        engine.startNewRound();
        switchToGameWithDelay();
    }

    /**
     * Erstellt den Spielscreen.
     */
    private void buildGameScreen() {
        gameScreen = new BackgroundPanel(new BorderLayout());

        // Oben
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        gameTextArea = new JTextArea("Bitte setze deinen Einsatz!");
        gameTextArea.setEditable(false);
        gameTextArea.setOpaque(false);
        gameTextArea.setForeground(Color.WHITE);
        gameTextArea.setFont(new Font("Arial", Font.BOLD, 25));
        gameTextArea.setMargin(new Insets(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(gameTextArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        balanceLabel = new JLabel("Bank: 100 € (Einsatz: 5 €)");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 22));
        balanceLabel.setForeground(new Color(50, 255, 50));
        balanceLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 20));
        balanceLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        topPanel.add(scrollPane,   BorderLayout.CENTER);
        topPanel.add(balanceLabel, BorderLayout.EAST);

        // Mitte
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);

        scoreLabel = new JLabel("Wert: 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 32));
        scoreLabel.setForeground(Color.YELLOW);
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        cardPanel = new JPanel(new FlowLayout());
        cardPanel.setOpaque(false);

        centerWrapper.add(scoreLabel, BorderLayout.NORTH);
        centerWrapper.add(cardPanel,  BorderLayout.CENTER);

        // Unten
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);

        // Links: Einsatz
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);

        JLabel betLabel = new JLabel("Dein Einsatz: ");
        betLabel.setForeground(Color.WHITE);

        betInputField = new JTextField(4);
        betInputField.setFont(new Font("Arial", Font.BOLD, 16));

        setBetButton = new JButton("Einsatz bestätigen");
        styleButton(setBetButton);
        setBetButton.addActionListener(e -> handleConfirmBet());

        leftPanel.add(betLabel);
        leftPanel.add(betInputField);
        leftPanel.add(setBetButton);

        // Mitte: Hit / Stand
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);

        hitButton   = new JButton("Hit (Ziehen)");
        standButton = new JButton("Stand (Bleiben)");
        styleButton(hitButton);
        styleButton(standButton);
        hitButton.setEnabled(false);
        standButton.setEnabled(false);

        hitButton.addActionListener(e   -> { if (engine != null && gameScreenReady) engine.playerHit(); });
        standButton.addActionListener(e -> { if (engine != null && gameScreenReady) engine.playerStand(); });

        centerPanel.add(hitButton);
        centerPanel.add(standButton);

        // Rechts: Speichern / Menü / Beenden
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);

        JButton saveButton = new JButton("Speichern");
        JButton menuButton = new JButton("Hauptmenü");
        JButton exitButton = new JButton("Beenden");
        styleButton(saveButton);
        styleButton(menuButton);
        styleButton(exitButton);
        saveButton.setFocusable(false); // Damit man mit der Leertaste spielen kann
        menuButton.setFocusable(false);
        exitButton.setFocusable(false);
        saveButton.setForeground(new Color(100, 200, 255));
        menuButton.setForeground(Color.ORANGE);
        exitButton.setForeground(Color.RED);

        saveButton.addActionListener(e -> handleSave());
        menuButton.addActionListener(e -> { engine = null; cardLayout.show(mainContainer, "SETUP"); });
        exitButton.addActionListener(e -> System.exit(0));

        rightPanel.add(saveButton);
        rightPanel.add(menuButton);
        rightPanel.add(exitButton);

        buttonPanel.add(leftPanel,   BorderLayout.WEST);
        buttonPanel.add(centerPanel, BorderLayout.CENTER);
        buttonPanel.add(rightPanel,  BorderLayout.EAST);

        gameScreen.add(topPanel,      BorderLayout.NORTH);
        gameScreen.add(centerWrapper, BorderLayout.CENTER);
        gameScreen.add(buttonPanel,   BorderLayout.SOUTH);

        // Tastenbelegungen
        InputMap  im = gameScreen.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = gameScreen.getActionMap();

        im.put(KeyStroke.getKeyStroke("SPACE"), "hitAction");
        am.put("hitAction", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (engine != null && gameScreenReady && hitButton.isEnabled()) engine.playerHit();
            }
        });

        im.put(KeyStroke.getKeyStroke("ENTER"), "standAction");
        am.put("standAction", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                // ENTER nur als Stand werten wenn das Einsatztextfeld NICHT fokussiert ist
                if (engine != null && gameScreenReady && standButton.isEnabled()
                        && !betInputField.isFocusOwner()) {
                    engine.playerStand();
                }
            }
        });
    }

    /**
     * Verarbeitet die Einsatzbestätigung.
     * Delegiert an {@link GameEngine#confirmBet(int)} die dann automatisch
     * den nächsten Spieler oder die Spielphase startet.
     */
    private void handleConfirmBet() {
        try {
            int betrag = Integer.parseInt(betInputField.getText().trim());
            if (engine == null || betrag <= 0) return;

            engine.confirmBet(betrag);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Bitte nur ganze Zahlen eingeben!");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    /**
     * Speichert den aktuellen Spielstand.
     */
    private void handleSave() {
        if (engine == null) return;
        ArrayList<GameState.PlayerEntry> entries = new ArrayList<>();
        for (Player p : engine.getPlayers()) {
            entries.add(new GameState.PlayerEntry(p.getName(), p.getBalance()));
        }
        try {
            String id = SaveGameManager.saveGame(entries, null);
            JOptionPane.showMessageDialog(this,
                    "Spielstand gespeichert!\nID: " + id, "Gespeichert",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Fehler beim Speichern: " + ex.getMessage(), "Fehler",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Erstellt den Ergebnisscreen.
     */
    private void buildResultScreen() {
        resultScreen = new JPanel(new BorderLayout());
        resultScreen.setBackground(new Color(20, 20, 20));

        JLabel headerLabel = new JLabel("Runde Beendet!", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 32));
        headerLabel.setForeground(Color.GREEN);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font("Monospaced", Font.PLAIN, 25));
        resultTextArea.setBackground(new Color(30, 30, 30));
        resultTextArea.setForeground(Color.LIGHT_GRAY);
        resultTextArea.setMargin(new Insets(20, 20, 20, 20));
        resultTextArea.setFocusable(false);

        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50), 2));

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel instructionLabel = new JLabel(
                " DRÜCKE EINE BELIEBIGE TASTE UM FORTZUFAHREN ", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 22));
        instructionLabel.setForeground(Color.ORANGE);

        JButton playAgainButton = new JButton("Nächste Runde / Weiter (Mausklick)");
        styleButton(playAgainButton);
        playAgainButton.addActionListener(e -> checkNextScreen());

        bottomPanel.add(instructionLabel);
        bottomPanel.add(playAgainButton);

        resultScreen.add(headerLabel,  BorderLayout.NORTH);
        resultScreen.add(scrollPane,   BorderLayout.CENTER);
        resultScreen.add(bottomPanel,  BorderLayout.SOUTH);

        resultScreen.setFocusable(true);
        resultScreen.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent e) { checkNextScreen(); }
        });

        new Timer(BLINK_INTERVAL_MS, e ->
                instructionLabel.setVisible(!instructionLabel.isVisible())
        ).start();
    }

    /**
     * Erstellt den Bankrottscreen.
     */
    private void buildBankruptScreen() {
        bankruptScreen = new JPanel(new BorderLayout());
        bankruptScreen.setBackground(Color.BLACK);

        dramaTextArea = new JTextArea();
        dramaTextArea.setEditable(false);
        dramaTextArea.setBackground(Color.BLACK);
        dramaTextArea.setForeground(Color.RED);
        dramaTextArea.setFont(new Font("Monospaced", Font.BOLD, 24));
        dramaTextArea.setMargin(new Insets(50, 50, 50, 50));

        JButton weiterButton = new JButton("Traurig akzeptieren...");
        styleButton(weiterButton);
        weiterButton.addActionListener(e -> checkNextScreen());

        bankruptScreen.add(dramaTextArea, BorderLayout.CENTER);
        bankruptScreen.add(weiterButton,  BorderLayout.SOUTH);
    }

    // -------------------------------------------------------------------------
    // Updatemethoden (von GameEngine aufgerufen)
    // -------------------------------------------------------------------------

    /**
     * Aktualisiert die Guthabenanzeige oben rechts.
     *
     * @param balance    Aktuelles Guthaben in Euro.
     * @param currentBet Aktueller Einsatz in Euro.
     */
    public void updateBalanceView(int balance, int currentBet) {
        balanceLabel.setText("Bank: " + balance + " € (Einsatz: " + currentBet + " €)");
    }

    /**
     * Aktualisiert den Infotext im Spielscreen.
     * Bei leerem Text wird die TextArea ausgeblendet.
     *
     * @param text Der anzuzeigende Text.
     */
    public void updateGameView(String text) {
        gameTextArea.setText(text);
        gameTextArea.setVisible(text != null && !text.isEmpty());
    }

    /**
     * Aktualisiert den Punktestand.
     *
     * @param score Der neue Punktwert.
     */
    public void updateScore(int score) {
        if (scoreLabel != null) scoreLabel.setText("Wert: " + score);
    }

    /**
     * Zeigt Karten einer Hand als Bilder an.
     *
     * @param hand Die anzuzeigende Hand.
     */
    public void updateCardImages(ArrayList<Card> hand) {
        cardPanel.removeAll();
        for (Card karte : hand) {
            ImageIcon icon = CardImageLoader.getCardImage(karte);
            cardPanel.add(icon != null ? new JLabel(icon) : new JLabel("[BILD FEHLT]"));
        }
        cardPanel.revalidate();
        cardPanel.repaint();
        revalidate();
        repaint();
    }

    /**
     * Zeigt {@code count} Kartenrückseiten an.
     *
     * @param count Anzahl der Rückseiten.
     */
    public void updateCardImagesHidden(int count) {
        cardPanel.removeAll();
        ImageIcon back = CardImageLoader.getCardBack();
        for (int i = 0; i < count; i++) {
            cardPanel.add(back != null ? new JLabel(back) : new JLabel("[RÜCKSEITE]"));
        }
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    /**
     * Schaltet zwischen Einsatzphase und Spielphase um.
     * <p>
     * Wird von {@link GameEngine} aufgerufen:
     * {@code showBetUI(true)} → Einsatzfeld aktiv Hit/Stand deaktiviert.<br>
     * {@code showBetUI(false)} → Einsatzfeld deaktiviert Hit/Stand aktiv.
     * </p>
     *
     * @param betPhase {@code true} während der Einsatzphase {@code false} während der Spielphase.
     */
    public void showBetUI(boolean betPhase) {
        setBetButton.setEnabled(betPhase);
        betInputField.setEnabled(betPhase);
        betInputField.setText("");
        hitButton.setEnabled(!betPhase);
        standButton.setEnabled(!betPhase);

        if (betPhase) {
            betInputField.requestFocusInWindow();
        }
    }

    /**
     * Trägt einen Vorschlagswert ins Einsatztextfeld ein und markiert ihn
     * sodass der Spieler ihn direkt überschreiben oder mit Enter bestätigen kann.
     *
     * @param amount Der voreinzutragende Betrag.
     */
    public void prefillBetInput(int amount) {
        if (betInputField != null) {
            betInputField.setText(String.valueOf(amount));
            betInputField.selectAll(); // direkt überschreibbar
            betInputField.requestFocusInWindow();
        }
    }

    /**
     * Wechselt zum Ergebnisscreen.
     *
     * @param resultText  Ergebnistext der Runde.
     * @param bankrupts   Namen der pleite gegangenen Spieler.
     * @param gameOver    {@code true} wenn alle Spieler ausgeschieden sind.
     */
    public void showResults(String resultText, ArrayList<String> bankrupts, boolean gameOver) {
        this.currentBankrupts = new ArrayList<>(bankrupts);
        this.isGameOver       = gameOver;
        this.gameScreenReady  = false;

        resultTextArea.setText(resultText);
        cardLayout.show(mainContainer, "RESULTS");
        resultScreen.requestFocusInWindow();
    }

    /**
     * Setzt alle UI Elemente für den Start einer neuen Runde zurück.
     * Einsatzphase aktiv Spielphase deaktiviert.
     */
    public void prepareNewRoundUI() {
        if (setBetButton  != null) setBetButton.setEnabled(true);
        if (betInputField != null) { betInputField.setEnabled(true); betInputField.setText(""); }
        if (hitButton     != null) hitButton.setEnabled(false);
        if (standButton   != null) standButton.setEnabled(false);
        if (scoreLabel    != null) scoreLabel.setText("Wert: –");
    }

    /**
     * Aktiviert oder deaktiviert Hit und Stand Button.
     * Wird von {@link GameEngine} genutzt um während des Dealerzugs
     * Spielereingaben zu sperren.
     *
     * @param enabled {@code true} = Buttons aktiv {@code false} = gesperrt.
     */
    public void setActionButtonsEnabled(boolean enabled) {
        if (hitButton   != null) hitButton.setEnabled(enabled);
        if (standButton != null) standButton.setEnabled(enabled);
    }

    // -------------------------------------------------------------------------
    // Navigationslogik
    // -------------------------------------------------------------------------

    /**
     * Entscheidet welcher Screen als Nächstes gezeigt wird:
     * <ol>
     * <li>Einzelne Bankrottscreens abarbeiten.</li>
     * <li>Nach dem letzten Bankrottscreen bei Game Over: finalen Casino Screen zeigen.</li>
     * <li>Noch Spieler übrig → neue Runde starten.</li>
     * <li>engine == null (nach finalem Screen) → Setup.</li>
     * </ol>
     */
    private void checkNextScreen() {

        // 1. Noch Bankrottscreens ausstehend
        if (!currentBankrupts.isEmpty()) {
            String name = currentBankrupts.remove(0);

            // Balance sofort auf nächsten lebenden Spieler umschalten
            if (engine != null && !engine.getPlayers().isEmpty()) {
                Player next = engine.getPlayers().get(0);
                updateBalanceView(next.getBalance(), next.getCurrentBet());
            } else {
                updateBalanceView(0, 0);
            }

            dramaTextArea.setText(
                    "  ACHTUNG: " + name.toUpperCase() + "\n\n" +
                            "  GUTHABEN: 0 €\n\n" +
                            "  Du hast alles verspielt.\n" +
                            "  Das Casino wirft dich hochkant vor die Tür.\n" +
                            "  Du bist aus dem Spiel ausgeschieden."
            );
            cardLayout.show(mainContainer, "BANKRUPT");
            return;
        }

        // 2. Alle Bankrottscreens weg + Game Over → finaler Screen
        if (isGameOver) {
            isGameOver = false; // Reset damit nächster Klick in Zweig 4 landet
            engine     = null;  // Spielstand wurde bereits in GameEngine gelöscht

            dramaTextArea.setText(
                    "  S P I E L E N D E\n\n" +
                            "  Das Casino hat gewonnen.\n\n" +
                            "  Alle Spieler sind pleite.\n" +
                            "  Euer Spielstand wurde gelöscht.\n\n" +
                            "  Das Casino wirft euch alle raus.\n" +
                            "  Kommt nicht wieder."
            );
            cardLayout.show(mainContainer, "BANKRUPT");
            return;
        }

        // 3. Noch Spieler übrig → neue Runde
        if (engine != null) {
            prepareNewRoundUI();
            engine.startNewRound();
            switchToGameWithDelay();
            return;
        }

        // 4. engine == null → nach finalem Screen zurück zum Setup
        cardLayout.show(mainContainer, "SETUP");
    }
}