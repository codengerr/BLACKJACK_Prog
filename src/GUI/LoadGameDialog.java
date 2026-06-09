package GUI;

import Saving.GameState;
import Saving.SaveGameManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Modaler Dialog zum Laden eines gespeicherten Spielstands.
 * <p>
 * Zeigt alle vorhandenen Spielstände als auswählbare Liste an.
 * Bei mehr als 3 Spielern wird nur die Anzahl angezeigt, um
 * die Darstellung übersichtlich zu halten.
 * </p>
 *
 * @author  Elias
 * @version 1.0
 */
public class LoadGameDialog extends JDialog{

    /** Der vom Benutzer gewählte Spielstand, oder {@code null} bei Abbruch. */
    private GameState selectedState = null;

    /**
     * Erstellt und zeigt den Lade-Dialog an.
     *
     * @param parent Das übergeordnete Fenster (für Zentrierung).
     */
    public LoadGameDialog(JFrame parent) {
        super(parent, "Spielstand laden", true); // true = modal
        setSize(700, 450);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(new Color(20, 20, 20));
        setLayout(new BorderLayout(10, 10));

        buildUI();
    }

    /**
     * Gibt den vom Benutzer ausgewählten {@link GameState} zurück.
     *
     * @return Der gewählte Spielstand, oder {@code null} wenn abgebrochen.
     */
    public GameState getSelectedState() {
        return selectedState;
    }

    // -------------------------------------------------------------------------
    // UI-Aufbau
    // -------------------------------------------------------------------------

    /**
     * Baut die gesamte Benutzeroberfläche des Dialogs auf.
     */
    private void buildUI() {
        // --- Titel ---
        JLabel titleLabel = new JLabel("Spielstand laden", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.ORANGE);
        titleLabel.setBorder(new EmptyBorder(15, 0, 5, 0));
        add(titleLabel, BorderLayout.NORTH);

        // --- Spielstandsliste ---
        List<GameState> saves = SaveGameManager.loadAllSaves();

        if (saves.isEmpty()) {
            // Keine Spielstände vorhanden
            JLabel emptyLabel = new JLabel("Keine gespeicherten Spielstände gefunden.", SwingConstants.CENTER);
            emptyLabel.setForeground(Color.LIGHT_GRAY);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            add(emptyLabel, BorderLayout.CENTER);

        } else {
            // Listenmodell befüllen
            DefaultListModel<GameState> listModel = new DefaultListModel<>();
            for (GameState state : saves) {
                listModel.addElement(state);
            }

            JList<GameState> saveList = new JList<>(listModel);
            saveList.setBackground(new Color(30, 30, 30));
            saveList.setForeground(Color.WHITE);
            saveList.setFont(new Font("Monospaced", Font.PLAIN, 15));
            saveList.setSelectionBackground(new Color(60, 120, 60));
            saveList.setSelectionForeground(Color.WHITE);
            saveList.setFixedCellHeight(50);
            saveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // Custom Renderer: zeigt toDisplayString() an
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

            JScrollPane scrollPane = new JScrollPane(saveList);
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
            scrollPane.setBackground(new Color(30, 30, 30));
            add(scrollPane, BorderLayout.CENTER);

            // --- Buttons ---
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            buttonPanel.setBackground(new Color(20, 20, 20));

            JButton loadButton   = buildButton("Laden",   new Color(40, 120, 40));
            JButton deleteButton = buildButton("Löschen", new Color(120, 40, 40));
            JButton cancelButton = buildButton("Abbrechen", new Color(60, 60, 60));

            loadButton.addActionListener(e -> confirmLoad(saveList.getSelectedValue()));

            deleteButton.addActionListener(e -> {
                GameState selected = saveList.getSelectedValue();
                if (selected == null) {
                    JOptionPane.showMessageDialog(this, "Bitte zuerst einen Spielstand auswählen.");
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Spielstand \"" + selected.getId() + "\" wirklich löschen?",
                        "Löschen bestätigen", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        SaveGameManager.deleteSave(selected.getId());
                        listModel.removeElement(selected);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Fehler beim Löschen: " + ex.getMessage());
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

    /**
     * Bestätigt die Auswahl eines Spielstands und schließt den Dialog.
     *
     * @param state Der zu ladende {@link GameState}.
     */
    private void confirmLoad(GameState state) {
        if (state == null) {
            JOptionPane.showMessageDialog(this, "Bitte zuerst einen Spielstand auswählen.");
            return;
        }
        this.selectedState = state;
        dispose();
    }

    /**
     * Erstellt einen styled Button mit der angegebenen Hintergrundfarbe.
     *
     * @param text  Der Button-Text.
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