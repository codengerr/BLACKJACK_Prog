package Saving;

import Saving.SaveGameManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Datenklasse die den vollständigen Zustand eines gespeicherten Spiels enthält.
 * <p>
 * Wird von {@link SaveGameManager} in JSON serialisiert und deserialisiert.
 * Enthält keine Spiellogik sondern nur die reinen Daten.
 * </p>
 *
 * @author  Elias
 * @version 1.0
 */
public class GameState {

    /** Eindeutige ID des Spielstands wie etwa "save_1". */
    private String id;

    /** Anzeigename oder Beschriftung des Spielstands wie etwa "Runde 5 – 3 Spieler". */
    private String label;

    /** Zeitstempel des Speichervorgangs in lesbarer Form wie etwa "04.06.2026 14:32". */
    private String savedAt;

    /** Liste der gespeicherten Spieler Einträge. */
    private List<PlayerEntry> players;

    // -------------------------------------------------------------------------
    // Konstruktoren
    // -------------------------------------------------------------------------

    /**
     * Leerer Konstruktor – wird für die JSON Deserialisierung benötigt.
     */
    public GameState() {
        this.players = new ArrayList<>();
    }

    /**
     * Erstellt einen vollständigen Spielstand.
     *
     * @param id      Eindeutige ID.
     * @param label   Anzeigename.
     * @param savedAt Zeitstempel als String.
     * @param players Liste der {@link PlayerEntry} Objekte.
     */
    public GameState(String id, String label, String savedAt, List<PlayerEntry> players) {
        this.id      = id;
        this.label   = label;
        this.savedAt = savedAt;
        this.players = players != null ? players : new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Getter & Setter
    // -------------------------------------------------------------------------

    /** @return Die eindeutige ID dieses Spielstands. */
    public String getId()      { return id; }

    /** @param id Die neue ID. */
    public void setId(String id) { this.id = id; }

    /** @return Der Anzeigename dieses Spielstands. */
    public String getLabel()   { return label; }

    /** @param label Der neue Anzeigename. */
    public void setLabel(String label) { this.label = label; }

    /** @return Der Zeitstempel des Speichervorgangs. */
    public String getSavedAt() { return savedAt; }

    /** @param savedAt Der neue Zeitstempel. */
    public void setSavedAt(String savedAt) { this.savedAt = savedAt; }

    /** @return Liste aller gespeicherten Spieler. */
    public List<PlayerEntry> getPlayers() { return players; }

    /** @param players Die neue Spielerliste. */
    public void setPlayers(List<PlayerEntry> players) { this.players = players; }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    /**
     * Erstellt einen lesbaren Anzeige String für die Ladeauswahl.
     * Bei mehr als 3 Spielern wird nur die Anzahl angezeigt.
     *
     * @return Formatierter String wie etwa "Save #1 | Anna Ben Clara | 04.06.2026"
     * oder "Save #1 | 5 Spieler | 04.06.2026"
     */
    public String toDisplayString() {
        String playerInfo;
        if (players.size() <= 3) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < players.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(players.get(i).getName())
                        .append(" (")
                        .append(players.get(i).getBalance())
                        .append("€)");
            }
            playerInfo = sb.toString();
        } else {
            playerInfo = players.size() + " Spieler";
        }
        return id + " | " + playerInfo + " | " + savedAt;
    }

    /**
     * Gibt eine kompakte Darstellung des Spielstands zurück.
     *
     * @return ID Label Zeitstempel und Spieleranzahl als String.
     */
    @Override
    public String toString() {
        return "GameState{id='" + id + "', label='" + label
                + "', savedAt='" + savedAt + "', players=" + players.size() + "}";
    }

    // -------------------------------------------------------------------------
    // Innere Klasse: PlayerEntry
    // -------------------------------------------------------------------------

    /**
     * Repräsentiert die gespeicherten Daten eines einzelnen Spielers.
     * Wird innerhalb von {@link GameState} als Liste gespeichert.
     */
    public static class PlayerEntry {

        /** Name des Spielers. */
        private String name;

        /** Aktuelles Guthaben in Euro. */
        private int balance;

        /**
         * Leerer Konstruktor – für die JSON Deserialisierung.
         */
        public PlayerEntry() {}

        /**
         * Erstellt einen neuen Spieler Eintrag.
         *
         * @param name    Name des Spielers.
         * @param balance Guthaben in Euro.
         */
        public PlayerEntry(String name, int balance) {
            this.name    = name;
            this.balance = balance;
        }

        /** @return Name des Spielers. */
        public String getName()    { return name; }

        /** @param name Neuer Name. */
        public void setName(String name) { this.name = name; }

        /** @return Guthaben in Euro. */
        public int getBalance()    { return balance; }

        /** @param balance Neues Guthaben. */
        public void setBalance(int balance) { this.balance = balance; }

        @Override
        public String toString() {
            return name + " (" + balance + "€)";
        }
    }
}