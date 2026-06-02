package GUI;

import logic.Card;
import logic.GameEngine;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GameWindow extends JFrame {

    private GameEngine engine;
    private CardLayout cardLayout;
    private JPanel mainContainer;

    private JPanel setupScreen;
    private JPanel gameScreen;
    private JPanel resultScreen;
    private JPanel bankruptScreen; // Der Drama-Screen

    private JTextArea gameTextArea;
    private JTextArea resultTextArea;
    private JTextArea dramaTextArea; // Textfeld für das Drama
    private JPanel cardPanel;
    private JLabel balanceLabel;

    // NEU: Logik-Variablen für das Menü
    private ArrayList<String> currentBankrupts = new ArrayList<>();
    private boolean isGameOver = false;
    private OutlinedLabel actionLabel;
    private Timer actionTextTimer;
    private JLabel scoreLabel;
    private boolean gameScreenReady = false;

    // NEU: Damit wir die Knöpfe in jeder Runde zurücksetzen können!
    private JTextField betInputField;
    private JButton setBetButton;
    private JButton hitButton;
    private JButton standButton;

    public GameWindow() {
        setTitle("Blackjack Casino");
        setSize(1980, 1020);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        buildSetupScreen();
        buildGameScreen();
        buildResultScreen();
        buildBankruptScreen();

        mainContainer.add(setupScreen, "SETUP");
        mainContainer.add(gameScreen, "GAME");
        mainContainer.add(resultScreen, "RESULTS");
        mainContainer.add(bankruptScreen, "BANKRUPT");

        add(mainContainer);
        setVisible(true);

        cardLayout.show(mainContainer, "SETUP");
    }


    // ==========================================
    // NEU: Custom Panel für das Hintergrundbild
    // ==========================================
    static class BackgroundPanel extends JPanel {
        private Image bgImage;

        public BackgroundPanel(LayoutManager layout) {
            super(layout);
            try {
                // Wir nutzen exakt denselben Pfad-Stil wie bei den Karten!
                // Ohne src/, da dein resources-Ordner direkt im Projekt liegt.
                bgImage = new ImageIcon("resources/table.png").getImage();

            } catch (Exception e) {
                System.out.println("Hintergrundbild konnte nicht geladen werden.");
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) {
                // Zeichnet das Bild und passt es IMMER genau an die Fenstergröße an (mitwachsen)
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    // ==========================================
    // STYLING HILFSMETHODE FÜR DIE BUTTONS
    // ==========================================
    private void styleButton(JButton btn) {
        btn.setBackground(new Color(40, 40, 40));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // ==========================================
    // SCREENS BAUEN
    // ==========================================
    private void buildSetupScreen() {
        // GridBagLayout hält alles perfekt zentriert, egal wie groß du das Fenster ziehst!
        setupScreen = new BackgroundPanel(new GridBagLayout());

        // Ein Panel für die Elemente in der Mitte
        JPanel innerPanel = new JPanel(new GridLayout(3, 1, 20, 20));
        innerPanel.setOpaque(false); // Durchsichtig, damit wir den Tisch sehen!

        JLabel titleLabel = new JLabel("Willkommen im Blackjack Casino", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36)); // Schön groß für Vollbild
        titleLabel.setForeground(Color.WHITE);

        String[] options = {"1 Spieler (Solo)", "2 Spieler", "3 Spieler", "4 Spieler", "Benutzerdefiniert..."};
        JComboBox<String> playerSelect = new JComboBox<>(options);
        playerSelect.setFont(new Font("Arial", Font.BOLD, 18)); // Dropdown auch vergrößert

        JButton startButton = new JButton("Spiel Starten");
        styleButton(startButton); // Unseren modernen Style anwenden!
        startButton.setFont(new Font("Arial", Font.BOLD, 22)); // Extra fetter Startknopf

        playerSelect.addActionListener(e -> {
            if (playerSelect.getSelectedIndex() == 4) {
                startButton.setText("Spielerzahl wählen & Starten");
            } else {
                startButton.setText("Spiel Starten");
            }
        });

        startButton.addActionListener(e -> {
            int playerCount = 1;
            int selectedIndex = playerSelect.getSelectedIndex();

            // 1. Spielerzahl herausfinden
            if (selectedIndex < 4) {
                playerCount = selectedIndex + 1;
            } else {
                String input = JOptionPane.showInputDialog(this, "Wie viele Spieler? (Maximal 10):", "Custom Lobby", JOptionPane.QUESTION_MESSAGE);
                if (input == null) return;
                try {
                    playerCount = Integer.parseInt(input);
                    if (playerCount < 1 || playerCount > 10) {
                        JOptionPane.showMessageDialog(this, "Bitte eine Zahl zwischen 1 und 10 eingeben!");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Ungültige Eingabe! Bitte eine Zahl eintippen.");
                    return;
                }
            }

            // 2. Optionale Namenseingabe
            ArrayList<String> playerNames = new ArrayList<>();
            int dialogResult = JOptionPane.showConfirmDialog(this,
                    "Möchtest du den Spielern eigene Namen geben?",
                    "Namen eingeben?",
                    JOptionPane.YES_NO_OPTION);

            if (dialogResult == JOptionPane.YES_OPTION) {
                for (int i = 1; i <= playerCount; i++) {
                    String name = JOptionPane.showInputDialog(this, "Name für Spieler " + i + ":", "Spieler " + i);
                    if (name == null || name.trim().isEmpty()) {
                        playerNames.add("Spieler " + i);
                    } else {
                        playerNames.add(name.trim());
                    }
                }
            } else {
                for (int i = 1; i <= playerCount; i++) {
                    playerNames.add("Spieler " + i);
                }
            }

            // 3. Engine mit Namen starten (in buildSetupScreen)
            engine = new GameEngine(playerNames);
            engine.setWindow(this);

            this.gameScreenReady = false; // Tastatur blockieren
            prepareNewRoundUI();
            engine.startNewRound();
            cardLayout.show(mainContainer, "GAME");

            Timer startTimer = new Timer(200, ex -> {
                this.gameScreenReady = true; // Tastatur freigeben
                gameScreen.requestFocusInWindow();
            });
            startTimer.setRepeats(false);
            startTimer.start();
        });

        innerPanel.add(titleLabel);
        innerPanel.add(playerSelect);
        innerPanel.add(startButton);

        // Das fertige innere Panel in den zentrierten Setup-Screen packen!
        setupScreen.add(innerPanel);
    }

    private void buildGameScreen() {
        gameScreen = new BackgroundPanel(new BorderLayout());

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

        // --- ZENTRIERTER BEREICH FÜR SCORE + KARTEN ---
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
        // ----------------------------------------------

        // Bottom Panel
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBackground(new Color(0, 0, 0, 150));

        // --- LINKER BEREICH: EINSATZ ---
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        JLabel betLabel = new JLabel("Dein Einsatz: ");
        betLabel.setForeground(Color.WHITE);

        // FEHLER 1 BEHOBEN: Kein "JTextField" oder "JButton" mehr davor!
        betInputField = new JTextField(4);
        betInputField.setFont(new Font("Arial", Font.BOLD, 16));
        setBetButton = new JButton("Einsatz bestätigen");
        styleButton(setBetButton);

        // FEHLER 2 BEHOBEN: Hier sind die fehlenden Zeilen von mir!
        leftPanel.add(betLabel);
        leftPanel.add(betInputField);
        leftPanel.add(setBetButton);

        // --- MITTLERER BEREICH: SPIEL-AKTIONEN ---
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);

        // HIER AUCH: Kein "JButton" mehr davor!
        hitButton = new JButton("Hit (Ziehen)");
        standButton = new JButton("Stand (Bleiben)");
        styleButton(hitButton);
        styleButton(standButton);

        // Am Anfang deaktiviert, bis der Einsatz steht!
        hitButton.setEnabled(false);
        standButton.setEnabled(false);

        setBetButton.addActionListener(e -> {
            try {
                int neuerEinsatz = Integer.parseInt(betInputField.getText());
                if (engine != null && neuerEinsatz > 0) {
                    engine.setBet(neuerEinsatz);
                    betInputField.setText("");

                    // Schaltet das Spiel frei!
                    hitButton.setEnabled(true);
                    standButton.setEnabled(true);
                    setBetButton.setEnabled(false); // Nicht doppelt setzen

                    // Hier sagen wir der Engine: "Einsatz steht, Karten umdrehen!"
                    engine.startAfterBet();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Bitte nur ganze Zahlen eingeben!");
            }
        });

        hitButton.addActionListener(e -> { if (engine != null && gameScreenReady) engine.playerHit(); });
        standButton.addActionListener(e -> { if (engine != null && gameScreenReady) engine.playerStand(); });
        centerPanel.add(hitButton);
        centerPanel.add(standButton);

        // --- RECHTER BEREICH: SYSTEM ---
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        JButton menuButton = new JButton("Hauptmenü");
        JButton exitButton = new JButton("Beenden");
        styleButton(menuButton);
        styleButton(exitButton);

        menuButton.setForeground(Color.ORANGE);
        exitButton.setForeground(Color.RED);

        menuButton.addActionListener(e -> {
            engine = null;
            cardLayout.show(mainContainer, "SETUP");
        });
        exitButton.addActionListener(e -> System.exit(0));
        rightPanel.add(menuButton);
        rightPanel.add(exitButton);

        buttonPanel.add(leftPanel, BorderLayout.WEST);
        buttonPanel.add(centerPanel, BorderLayout.CENTER);
        buttonPanel.add(rightPanel, BorderLayout.EAST);

        // HIER WAR DER FEHLER: Wir fügen jetzt den centerWrapper hinzu!
        gameScreen.add(topPanel, BorderLayout.NORTH);
        gameScreen.add(centerWrapper, BorderLayout.CENTER); // <-- FIX!
        gameScreen.add(buttonPanel, BorderLayout.SOUTH);

        // KEY BINDINGS
        InputMap im = gameScreen.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = gameScreen.getActionMap();

        im.put(KeyStroke.getKeyStroke("SPACE"), "hitAction");
        am.put("hitAction", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (engine != null && gameScreenReady && hitButton.isEnabled()) engine.playerHit();
            }
        });

        im.put(KeyStroke.getKeyStroke("ENTER"), "standAction");
        am.put("standAction", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (engine != null && gameScreenReady && standButton.isEnabled()) engine.playerStand();
            }
        });
    }

    private void buildResultScreen() {
        resultScreen = new JPanel(new BorderLayout());
        resultScreen.setBackground(new Color(20, 20, 20)); // Dunkler Hintergrund


        JLabel gameOverLabel = new JLabel("Runde Beendet!", SwingConstants.CENTER);
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 32));
        gameOverLabel.setForeground(Color.GREEN);
        gameOverLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        // 2. Die Text Area für die Spieler-Ergebnisse
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font("Monospaced", Font.PLAIN, 25));
        resultTextArea.setBackground(new Color(30, 30, 30));
        resultTextArea.setForeground(Color.LIGHT_GRAY);
        resultTextArea.setMargin(new Insets(20, 20, 20, 20));
        resultTextArea.setFocusable(false);

        // Wir packen die Textarea in ein ScrollPane, falls es mal viele Spieler werden
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50), 2));

        // 3. NEU: Ein eigenes Panel für den "Weiter"-Bereich ganz unten
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // HIER IST DEIN STATISCHER TEXT! Schön groß, Orange und zentriert
        JLabel instructionLabel = new JLabel(" DRÜCKE EINE BELIEBIGE TASTE UM FORTZUFAHREN ", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 22));
        instructionLabel.setForeground(Color.ORANGE); // Schickes Casino-Gold/Orange

        // Für einen subtilen "Glow"-Effekt im Text (falls du es noch auffälliger willst)
        // Kannst du dem Label z.B. einen coolen Rahmen oder Tooltip geben, aber die Farbe reicht meistens schon!

        JButton playAgainButton = new JButton("Nächste Runde / Weiter (Mausklick)");
        styleButton(playAgainButton);
        playAgainButton.addActionListener(e -> checkNextScreen());

        // Den Text und den Button unten hinzufügen
        bottomPanel.add(instructionLabel);
        bottomPanel.add(playAgainButton);

        // Alles auf dem ResultScreen verteilen
        resultScreen.add(gameOverLabel, BorderLayout.NORTH);
        resultScreen.add(scrollPane, BorderLayout.CENTER);
        resultScreen.add(bottomPanel, BorderLayout.SOUTH);

        // ==========================================
        // JEDE BELIEBIGE TASTE FÜR DIE NÄCHSTE RUNDE
        // ==========================================
        resultScreen.setFocusable(true);
        resultScreen.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                checkNextScreen(); // Reagiert beim Loslassen JEDER Taste
            }
        });

        Timer blinkTimer = new Timer(600, e -> {
            instructionLabel.setVisible(!instructionLabel.isVisible());
        });
        blinkTimer.start();
    }

    // ==========================================
    // NEU: Der dynamische Bankrott-Screen
    // ==========================================
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
        weiterButton.addActionListener(e -> checkNextScreen()); // Leitet zum nächsten Screen weiter

        bankruptScreen.add(dramaTextArea, BorderLayout.CENTER);
        bankruptScreen.add(weiterButton, BorderLayout.SOUTH);
    }

    // ==========================================
    // NEUE METHODEN FÜR DIE LOGIK
    // ==========================================

    public void updateBalanceView(int balance, int currentBet) {
        balanceLabel.setText("Bank: " + balance + " € (Einsatz: " + currentBet + " €)");
    }

    public void updateGameView(String text) {
        gameTextArea.setText(text);
    }

    public void updateCardImages(ArrayList<Card> hand) {
        cardPanel.removeAll();
        for (Card karte : hand) {
            if (karte.getCardImage() != null) {
                cardPanel.add(new JLabel(karte.getCardImage()));
            } else {
                cardPanel.add(new JLabel("[BILD FEHLT]"));
            }
        }
        cardPanel.revalidate();
        cardPanel.repaint();
        this.revalidate();
        this.repaint();
    }

    // Die Engine ruft das auf und übergibt die Liste der Pleite-Spieler
    public void showResults(String resultText, ArrayList<String> bankrupts, boolean gameOver) {
        this.currentBankrupts = bankrupts;
        this.isGameOver = gameOver;

        this.gameScreenReady = false; // <-- NEU: GameScreen ist jetzt gesperrt!

        resultTextArea.setText(resultText);
        cardLayout.show(mainContainer, "RESULTS");
        resultScreen.requestFocusInWindow();
    }

    // ==========================================
    // NEU: Der Dirigent, der weiß, welcher Screen als Nächstes kommt!
    // ==========================================
    private void checkNextScreen() {
        // 1. Gibt es noch Spieler, die den Drama-Screen sehen müssen?
        if (!currentBankrupts.isEmpty()) {
            String opferName = currentBankrupts.remove(0); // Ersten Namen nehmen und aus Liste löschen

            dramaTextArea.setText(
                    "  ACHTUNG: " + opferName.toUpperCase() + "\n\n" +
                            "  GUTHABEN: 0 €\n\n" +
                            "  Du hast alles verspielt.\n" +
                            "  Das Casino wirft dich hochkant vor die Tür.\n" +
                            "  Du bist aus dem Spiel ausgeschieden."
            );
            cardLayout.show(mainContainer, "BANKRUPT");
        }
        // 2. Keine Drama-Screens mehr übrig. Sind ALLE tot?
        else if (isGameOver) {
            JOptionPane.showMessageDialog(this, "Alle Spieler sind pleite! Das Spiel ist vorbei.");
            engine = null;
            cardLayout.show(mainContainer, "SETUP");
        }
        else {
            if (engine != null) {
                this.gameScreenReady = false; // 1. Tastatur im Spiel SPERREN
                prepareNewRoundUI();
                engine.startNewRound();
                cardLayout.show(mainContainer, "GAME");

                // 2. Nach 200 Millisekunden (wenn die Taste sicher losgelassen wurde) wieder ENTSPERREN
                Timer delayTimer = new Timer(200, event -> {
                    this.gameScreenReady = true;
                    gameScreen.requestFocusInWindow();
                });
                delayTimer.setRepeats(false);
                delayTimer.start();
            }
        }
    }
    public void showActionText(String text) {
        // Falls das Label noch nicht existiert, stürzt es hier nicht ab
        if (actionLabel == null) return;

        actionLabel.setText(text);

        // Falls schon ein Timer läuft, stoppen wir ihn
        if (actionTextTimer != null && actionTextTimer.isRunning()) {
            actionTextTimer.stop();
        }

        // Timer starten: Nach 3 Sekunden wird der Text gelöscht
        actionTextTimer = new Timer(3000, e -> actionLabel.setText(""));
        actionTextTimer.setRepeats(false);
        actionTextTimer.start();
    }

    public void updateScore(int score) {
        // Gleicher Schutz hier
        if (scoreLabel != null) {
            scoreLabel.setText("Wert: " + score);
        }
    }
    public void updateCardImagesHidden(int count) {
        cardPanel.removeAll();
        for (int i = 0; i < count; i++) {
            if (Card.getCardBack() != null) {
                cardPanel.add(new JLabel(Card.getCardBack()));
            } else {
                cardPanel.add(new JLabel("[RÜCKSEITE]"));
            }
        }
        cardPanel.revalidate();
        cardPanel.repaint();
    }
    // NEU: Setzt das UI für eine komplett neue Runde zurück
    public void prepareNewRoundUI() {
        if (setBetButton != null) setBetButton.setEnabled(true);   // Einsatz-Knopf AN
        if (hitButton != null) hitButton.setEnabled(false);        // Hit-Knopf AUS
        if (standButton != null) standButton.setEnabled(false);    // Stand-Knopf AUS
        if (betInputField != null) betInputField.setText("");      // Textfeld leeren
    }
}