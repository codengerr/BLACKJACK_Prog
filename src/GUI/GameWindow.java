package GUI;

import logic.Card;
import logic.GameEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Das Hauptfenster des Blackjack-Spiels.
 * <p>
 * Verwaltet alle Screens (Setup, Spiel, Ergebnis, Bankrott) über ein
 * {@link CardLayout} und kommuniziert bidirektional mit der {@link GameEngine}.
 * </p>
 *
 * @author  [Dein Name]
 * @version 1.0
 */
public class GameWindow extends JFrame {

    // -------------------------------------------------------------------------
    // Konstanten
    // -------------------------------------------------------------------------

    /** Verzögerung in ms, bevor der GameScreen nach einem Screen-Wechsel Tastatureingaben akzeptiert. */
    private static final int INPUT_DELAY_MS = 200;

    /** Blink-Intervall in ms für den Hinweistext auf dem Ergebnis-Screen. */
    private static final int BLINK_INTERVAL_MS = 600;

    // -------------------------------------------------------------------------
    // Screen-Panels (CardLayout)
    // -------------------------------------------------------------------------

    private JPanel setupScreen;
    private JPanel gameScreen;
    private JPanel resultScreen;
    private JPanel bankruptScreen;

    // -------------------------------------------------------------------------
    // Wiederverwendete UI-Elemente
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

    /**
     * Gibt an, ob der Game-Screen Tastatureingaben verarbeiten darf.
     * Wird beim Screen-Wechsel kurz gesperrt, um versehentliche Doppel-Eingaben zu verhindern.
     */
    private boolean gameScreenReady = false;

    // -------------------------------------------------------------------------
    // Konstruktor
    // -------------------------------------------------------------------------

    /**
     * Erstellt und zeigt das Hauptfenster des Spiels.
     * Alle Screens werden beim Start einmalig aufgebaut.
     */
    public GameWindow() {
        setTitle("Blackjack Casino");
        setSize(1980, 1020);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout     = new CardLayout();
        mainContainer  = new JPanel(cardLayout);

        buildSetupScreen();
        buildGameScreen();
        buildResultScreen();
        buildBankruptScreen();

        mainContainer.add(setupScreen,   "SETUP");
        mainContainer.add(gameScreen,    "GAME");
        mainContainer.add(resultScreen,  "RESULTS");
        mainContainer.add(bankruptScreen,"BANKRUPT");

        add(mainContainer);
        setVisible(true);
        cardLayout.show(mainContainer, "SETUP");
    }

    // =========================================================================
    // INNERE KLASSE: Hintergrund-Panel
    // =========================================================================

    /**
     * Ein {@link JPanel}, das ein skalierbares Hintergrundbild zeichnet.
     * Das Bild wächst automatisch mit der Fenstergröße mit.
     */
    static class BackgroundPanel extends JPanel {

        /** Das geladene Hintergrundbild. */
        private final Image bgImage;

        /**
         * Erstellt ein BackgroundPanel mit dem angegebenen Layout.
         * Das Bild wird aus {@code resources/table.png} geladen.
         *
         * @param layout Das zu verwendende {@link LayoutManager}-Objekt.
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

    // =========================================================================
    // HILFSMETHODEN
    // =========================================================================

    /**
     * Wendet ein einheitliches dunkles Styling auf einen {@link JButton} an.
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
     * Wechselt nach kurzer Verzögerung zum Game-Screen und gibt die Tastatur frei.
     * Verhindert, dass ein Tastendruck vom vorherigen Screen durchdringt.
     */
    private void switchToGameWithDelay() {
        this.gameScreenReady = false;
        cardLayout.show(mainContainer, "GAME");

        Timer delayTimer = new Timer(INPUT_DELAY_MS, e -> {
            this.gameScreenReady = true;
            gameScreen.requestFocusInWindow();
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    // =========================================================================
    // SCREEN-AUFBAU
    // =========================================================================

    /**
     * Erstellt den Setup-Screen mit Spieleranzahl-Auswahl und Start-Button.
     */
    private void buildSetupScreen() {
        setupScreen = new BackgroundPanel(new GridBagLayout());

        JPanel innerPanel = new JPanel(new GridLayout(3, 1, 20, 20));
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

        playerSelect.addActionListener(e -> {
            if (playerSelect.getSelectedIndex() == 4) {
                startButton.setText("Spielerzahl wählen & Starten");
            } else {
                startButton.setText("Spiel Starten");
            }
        });

        startButton.addActionListener(e -> handleStartButton(playerSelect));

        innerPanel.add(titleLabel);
        innerPanel.add(playerSelect);
        innerPanel.add(startButton);
        setupScreen.add(innerPanel);
    }

    /**
     * Verarbeitet den Klick auf "Spiel Starten": fragt Spielerzahl und Namen ab
     * und startet die {@link GameEngine}.
     *
     * @param playerSelect Das Dropdown mit der Spieleranzahl-Auswahl.
     */
    private void handleStartButton(JComboBox<String> playerSelect) {
        // 1. Spielerzahl bestimmen
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
                JOptionPane.showMessageDialog(this, "Ungültige Eingabe! Bitte eine Zahl eintippen.");
                return;
            }
        }

        // 2. Namen abfragen
        ArrayList<String> playerNames = new ArrayList<>();
        int dialogResult = JOptionPane.showConfirmDialog(
                this, "Möchtest du den Spielern eigene Namen geben?",
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

        // 3. Engine starten
        engine = new GameEngine(playerNames);
        engine.setWindow(this);

        prepareNewRoundUI();
        engine.startNewRound();
        switchToGameWithDelay();
    }

    /**
     * Erstellt den Spiel-Screen mit Karten-Anzeige, Punktestand und Aktions-Buttons.
     */
    private void buildGameScreen() {
        gameScreen = new BackgroundPanel(new BorderLayout());

        // --- TOP: Info-Text + Guthaben ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        gameTextArea = new JTextArea("Bitte setze deinen Einsatz, um die Runde zu starten!");
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

        topPanel.add(scrollPane, BorderLayout.CENTER);
        topPanel.add(balanceLabel, BorderLayout.EAST);

        // --- CENTER: Score + Karten ---
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);

        scoreLabel = new JLabel("Wert: 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 32));
        scoreLabel.setForeground(Color.YELLOW);
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        cardPanel = new JPanel(new FlowLayout());
        cardPanel.setOpaque(false);

        centerWrapper.add(scoreLabel, BorderLayout.NORTH);
        centerWrapper.add(cardPanel, BorderLayout.CENTER);

        // --- BOTTOM: Buttons ---
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);

        // Einsatz (links)
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        JLabel betLabel = new JLabel("Dein Einsatz: ");
        betLabel.setForeground(Color.WHITE);

        betInputField = new JTextField(4);
        betInputField.setFont(new Font("Arial", Font.BOLD, 16));
        setBetButton = new JButton("Einsatz bestätigen");
        styleButton(setBetButton);

        leftPanel.add(betLabel);
        leftPanel.add(betInputField);
        leftPanel.add(setBetButton);

        // Hit / Stand (mitte)
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);

        hitButton   = new JButton("Hit (Ziehen)");
        standButton = new JButton("Stand (Bleiben)");
        styleButton(hitButton);
        styleButton(standButton);
        hitButton.setEnabled(false);
        standButton.setEnabled(false);

        setBetButton.addActionListener(e -> handleSetBet());
        hitButton.addActionListener(e   -> { if (engine != null && gameScreenReady) engine.playerHit(); });
        standButton.addActionListener(e -> { if (engine != null && gameScreenReady) engine.playerStand(); });

        centerPanel.add(hitButton);
        centerPanel.add(standButton);

        // Menü / Beenden (rechts)
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        JButton menuButton = new JButton("Hauptmenü");
        JButton exitButton = new JButton("Beenden");
        styleButton(menuButton);
        styleButton(exitButton);
        menuButton.setForeground(Color.ORANGE);
        exitButton.setForeground(Color.RED);

        menuButton.addActionListener(e -> { engine = null; cardLayout.show(mainContainer, "SETUP"); });
        exitButton.addActionListener(e -> System.exit(0));

        rightPanel.add(menuButton);
        rightPanel.add(exitButton);

        buttonPanel.add(leftPanel,   BorderLayout.WEST);
        buttonPanel.add(centerPanel, BorderLayout.CENTER);
        buttonPanel.add(rightPanel,  BorderLayout.EAST);

        gameScreen.add(topPanel,      BorderLayout.NORTH);
        gameScreen.add(centerWrapper, BorderLayout.CENTER);
        gameScreen.add(buttonPanel,   BorderLayout.SOUTH);

        // Key Bindings
        InputMap  im = gameScreen.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = gameScreen.getActionMap();

        im.put(KeyStroke.getKeyStroke("SPACE"), "hitAction");
        am.put("hitAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (engine != null && gameScreenReady && hitButton.isEnabled()) engine.playerHit();
            }
        });

        im.put(KeyStroke.getKeyStroke("ENTER"), "standAction");
        am.put("standAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (engine != null && gameScreenReady && standButton.isEnabled()) engine.playerStand();
            }
        });
    }

    /**
     * Verarbeitet die Bestätigung des Einsatzes durch den Spieler.
     * Aktiviert Hit/Stand-Buttons und informiert die Engine.
     */
    private void handleSetBet() {
        try {
            int neuerEinsatz = Integer.parseInt(betInputField.getText().trim());
            if (engine == null || neuerEinsatz <= 0) return;

            engine.setBet(neuerEinsatz);
            betInputField.setText("");

            hitButton.setEnabled(true);
            standButton.setEnabled(true);
            setBetButton.setEnabled(false);

            engine.startAfterBet();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Bitte nur ganze Zahlen eingeben!");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    /**
     * Erstellt den Ergebnis-Screen mit Rundenergebnis und "Weiter"-Optionen.
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
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                checkNextScreen();
            }
        });

        // Blink-Timer für den Hinweistext
        new Timer(BLINK_INTERVAL_MS, e ->
                instructionLabel.setVisible(!instructionLabel.isVisible())
        ).start();
    }

    /**
     * Erstellt den Bankrott-Screen, der einem ausgeschiedenen Spieler angezeigt wird.
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

    // =========================================================================
    // ÖFFENTLICHE UPDATE-METHODEN (werden von GameEngine aufgerufen)
    // =========================================================================

    /**
     * Aktualisiert die Guthaben- und Einsatz-Anzeige.
     *
     * @param balance    Das aktuelle Guthaben in Euro.
     * @param currentBet Der aktuelle Einsatz in Euro.
     */
    public void updateBalanceView(int balance, int currentBet) {
        balanceLabel.setText("Bank: " + balance + " € (Einsatz: " + currentBet + " €)");
    }

    /**
     * Aktualisiert den Informationstext im Spiel-Screen.
     *
     * @param text Der anzuzeigende Text.
     */
    public void updateGameView(String text) {
        gameTextArea.setText(text);
    }

    /**
     * Aktualisiert den angezeigten Punktestand.
     *
     * @param score Der neue Punktestand.
     */
    public void updateScore(int score) {
        if (scoreLabel != null) {
            scoreLabel.setText("Wert: " + score);
        }
    }

    /**
     * Zeigt die Karten einer Hand als Bilder an.
     * Bilder werden über {@link CardImageLoader} geladen – nicht aus {@code Card} direkt.
     *
     * @param hand Die anzuzeigende Hand als {@link ArrayList} von {@link Card}-Objekten.
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
     * Zeigt eine bestimmte Anzahl Karten-Rückseiten an (vor dem Aufdecken).
     *
     * @param count Anzahl der anzuzeigenden Rückseiten.
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
     * Wechselt zum Ergebnis-Screen und zeigt die Rundenauswertung an.
     *
     * @param resultText  Der anzuzeigende Ergebnistext.
     * @param bankrupts   Liste der Spielernamen, die pleite sind.
     * @param gameOver    {@code true}, wenn alle Spieler ausgeschieden sind.
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
     * Setzt alle UI-Elemente für eine neue Runde zurück.
     * Deaktiviert Hit/Stand, aktiviert den Einsatz-Button.
     */
    public void prepareNewRoundUI() {
        if (setBetButton  != null) setBetButton.setEnabled(true);
        if (hitButton     != null) hitButton.setEnabled(false);
        if (standButton   != null) standButton.setEnabled(false);
        if (betInputField != null) betInputField.setText("");
    }

    // =========================================================================
    // PRIVATE NAVIGATIONS-LOGIK
    // =========================================================================

    /**
     * Entscheidet, welcher Screen als Nächstes angezeigt wird:
     * <ol>
     *   <li>Bankrott-Screen, falls noch Spieler ausgeschieden sind.</li>
     *   <li>Setup-Screen mit Meldung, falls alle Spieler pleite sind.</li>
     *   <li>Neues Spiel, falls noch Spieler übrig sind.</li>
     * </ol>
     */
    private void checkNextScreen() {
        if (!currentBankrupts.isEmpty()) {
            String name = currentBankrupts.remove(0);
            dramaTextArea.setText(
                    "  ACHTUNG: " + name.toUpperCase() + "\n\n" +
                            "  GUTHABEN: 0 €\n\n" +
                            "  Du hast alles verspielt.\n" +
                            "  Das Casino wirft dich hochkant vor die Tür.\n" +
                            "  Du bist aus dem Spiel ausgeschieden."
            );
            cardLayout.show(mainContainer, "BANKRUPT");

        } else if (isGameOver) {
            JOptionPane.showMessageDialog(this, "Alle Spieler sind pleite! Das Spiel ist vorbei.");
            engine = null;
            cardLayout.show(mainContainer, "SETUP");

        } else if (engine != null) {
            prepareNewRoundUI();
            engine.startNewRound();
            switchToGameWithDelay();
        }
    }
}