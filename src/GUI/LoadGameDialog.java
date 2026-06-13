package GUI;

import Saving.GameState;
import Saving.SaveGameManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Modaler Dialog zum Laden eines gespeicherten Spielstands.
 * <p>
 * Zeigt alle vorhandenen Spielstände als auswählbare Liste an.
 * Die Liste kann nach Datum Gesamtguthaben oder ID sortiert werden.
 * Bei mehr als 3 Spielern wird nur die Anzahl angezeigt.
 * </p>
 *
 * @author  Elias
 * @version 1.1
 */
public class LoadGameDialog extends JDialog {

    /** Der vom Benutzer gewählte Spielstand oder {@code null} bei Abbruch. */
    private GameState selectedState = null;

    /**
     * Erstellt und zeigt den Ladedialog an.
     *
     * @param parent Das übergeordnete Fenster (für Zentrierung).
     */
    public LoadGameDialog(JFrame parent) {
        super(parent, "Spielstand laden", true);
        setSize(700, 500);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(new Color(20, 20, 20));
        setLayout(new BorderLayout(10, 10));

        buildUI();
    }

    /**
     * Gibt den vom Benutzer ausgewählten {@link GameState} zurück.
     *
     * @return Der gewählte Spielstand oder {@code null} wenn abgebrochen.
     */
    public GameState getSelectedState() {
        return selectedState;
    }

    // -------------------------------------------------------------------------
    // UIAufbau
    // -------------------------------------------------------------------------

    /**
     * Baut die gesamte Benutzeroberfläche des Dialogs auf.
     */
    private void buildUI() {
        // --- Titelzeile mit Sortierdropdown ---
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.setBorder(new EmptyBorder(10, 15, 5, 15));

        JLabel titleLabel = new JLabel("Spielstand laden", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.ORANGE);

        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        sortPanel.setOpaque(false);

        JLabel sortLabel = new JLabel("Sortieren: ");
        sortLabel.setForeground(Color.LIGHT_GRAY);
        sortLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        String[] sortOptions = {
                "Datum (neu \u2192 alt)",
                "Datum (alt \u2192 neu)",
                "Geld (\u2193 reich \u2192 arm)",
                "Geld (\u2191 arm \u2192 reich)",
                "ID (A \u2192 Z)"
        };
        JComboBox<String> sortBox = new JComboBox<>(sortOptions);
        sortBox.setBackground(new Color(40, 40, 40));
        sortBox.setForeground(Color.WHITE);
        sortBox.setFont(new Font("Arial", Font.PLAIN, 13));
        sortBox.setFocusable(false);

        sortPanel.add(sortLabel);
        sortPanel.add(sortBox);

        northPanel.add(titleLabel, BorderLayout.WEST);
        northPanel.add(sortPanel,  BorderLayout.EAST);
        add(northPanel, BorderLayout.NORTH);

        // --- Spielstandsliste ---
        List<GameState> saves = SaveGameManager.loadAllSaves();

        if (saves.isEmpty()) {
            JLabel emptyLabel = new JLabel("Keine gespeicherten Spielst\u00e4nde gefunden.", SwingConstants.CENTER);
            emptyLabel.setForeground(Color.LIGHT_GRAY);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            add(emptyLabel, BorderLayout.CENTER);

        } else {
            DefaultListModel<GameState> listModel = new DefaultListModel<>();
            fillModel(listModel, saves, 0); // initial: Datum neu -> alt

            JList<GameState> saveList = new JList<>(listModel);
            saveList.setBackground(new Color(30, 30, 30));
            saveList.setForeground(Color.WHITE);
            saveList.setFont(new Font("Monospaced", Font.PLAIN, 15));
            saveList.setSelectionBackground(new Color(60, 120, 60));
            saveList.setSelectionForeground(Color.WHITE);
            saveList.setFixedCellHeight(50);
            saveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            saveList.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                        JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof GameState) {
                        setText("  " + ((GameState) value).toDisplayString());
                    }
                    setBackground(isSelected ? new Color(60, 120, 60) : new Color(30, 30, 30));
                    setForeground(Color.WHITE);
                    setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 50)));
                    return this;
                }
            });

            // Doppelklick lädt sofort
            saveList.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        confirmLoad(saveList.getSelectedValue());
                    }
                }
            });

            // Sortierung neu anwenden wenn Dropdown geändert wird
            sortBox.addActionListener(e -> {
                GameState selected = saveList.getSelectedValue();
                fillModel(listModel, saves, sortBox.getSelectedIndex());
                // Selektion beibehalten falls möglich
                if (selected != null) {
                    for (int i = 0; i < listModel.size(); i++) {
                        if (listModel.get(i).getId().equals(selected.getId())) {
                            saveList.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(saveList);
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
            scrollPane.setBackground(new Color(30, 30, 30));
            add(scrollPane, BorderLayout.CENTER);

            // --- Buttons ---
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            buttonPanel.setBackground(new Color(20, 20, 20));

            JButton loadButton   = buildButton("Laden",     new Color(40, 120, 40));
            JButton deleteButton = buildButton("L\u00f6schen",   new Color(120, 40, 40));
            JButton cancelButton = buildButton("Abbrechen", new Color(60, 60, 60));

            loadButton.addActionListener(e -> confirmLoad(saveList.getSelectedValue()));

            deleteButton.addActionListener(e -> {
                GameState sel = saveList.getSelectedValue();
                if (sel == null) {
                    JOptionPane.showMessageDialog(this, "Bitte zuerst einen Spielstand ausw\u00e4hlen.");
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Spielstand \"" + sel.getId() + "\" wirklich l\u00f6schen?",
                        "L\u00f6schen best\u00e4tigen", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        SaveGameManager.deleteSave(sel.getId());
                        saves.removeIf(s -> s.getId().equals(sel.getId()));
                        fillModel(listModel, saves, sortBox.getSelectedIndex());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Fehler beim L\u00f6schen: " + ex.getMessage());
                    }
                }
            });

            cancelButton.addActionListener(e -> dispose());

            buttonPanel.add(loadButton);
            buttonPanel.add(deleteButton);
            buttonPanel.add(cancelButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
    }

    // -------------------------------------------------------------------------
    // Sortierung
    // -------------------------------------------------------------------------

    /**
     * Bef\u00fcllt das ListModel mit den Spielst\u00e4nden in der gew\u00e4hlten Sortierreihenfolge.
     *
     * @param model      Das zu befüllende {@link DefaultListModel}.
     * @param saves      Die Quellliste der Spielstände.
     * @param sortIndex  Index der gewählten Sortieroption (0-4).
     */
    private void fillModel(DefaultListModel<GameState> model, List<GameState> saves, int sortIndex) {
        List<GameState> sorted = new ArrayList<>(saves);

        switch (sortIndex) {
            case 0 -> java.util.Collections.reverse(sorted); // Datum neu -> alt
            case 1 -> {}                                      // Datum alt -> neu: Originalreihenfolge
            case 2 -> sorted.sort(Comparator.comparingInt(LoadGameDialog::totalBalance).reversed()); // Geld hoch -> tief
            case 3 -> sorted.sort(Comparator.comparingInt(LoadGameDialog::totalBalance));            // Geld tief -> hoch
            case 4 -> sorted.sort(Comparator.comparing(GameState::getId));                           // ID A->Z
        }

        model.clear();
        for (GameState s : sorted) {
            model.addElement(s);
        }
    }

    /**
     * Berechnet das Gesamtguthaben aller Spieler eines Spielstands.
     *
     * @param state Der Spielstand.
     * @return Summe aller Spieler Guthaben in Euro.
     */
    private static int totalBalance(GameState state) {
        return state.getPlayers().stream()
                .mapToInt(GameState.PlayerEntry::getBalance)
                .sum();
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    /**
     * Bestätigt die Auswahl eines Spielstands und schließt den Dialog.
     *
     * @param state Der zu ladende {@link GameState}.
     */
    private void confirmLoad(GameState state) {
        if (state == null) {
            JOptionPane.showMessageDialog(this, "Bitte zuerst einen Spielstand ausw\u00e4hlen.");
            return;
        }
        this.selectedState = state;
        dispose();
    }

    /**
     * Erstellt einen styled Button mit der angegebenen Hintergrundfarbe.
     *
     * @param text  Der Button Text.
     * @param color Die Hintergrundfarbe.
     * @return Den fertigen {@link JButton}.
     */
    private JButton buildButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}