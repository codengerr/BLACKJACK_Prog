package Saving;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Verwaltet das Speichern und Laden von Spielständen im JSON-Format.
 * <p>
 * Die Spielstände werden in der Datei {@value #SAVE_FILE} abgelegt.
 * Da das Projekt keine externen Bibliotheken nutzt, wird JSON manuell
 * geparst und generiert (kein Gson / Jackson).
 * </p>
 * <p>
 * Format der JSON-Datei:
 * <pre>
 * [
 *   {
 *     "id": "save_1",
 *     "label": "Runde 5",
 *     "savedAt": "04.06.2026 14:32",
 *     "players": [
 *       { "name": "Anna", "balance": 120 },
 *       { "name": "Ben",  "balance": 80  }
 *     ]
 *   }
 * ]
 * </pre>
 * </p>
 *
 * @author  [Dein Name]
 * @version 1.0
 */
public class SaveGameManager {

    /** Pfad zur JSON-Speicherdatei. */
    private static final String SAVE_FILE = "saves/savegames.json";

    /** Maximale Anzahl gleichzeitig gespeicherter Spielstände. */
    public static final int MAX_SAVES = 10;

    /** Datumsformat für den Zeitstempel. */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // -------------------------------------------------------------------------
    // Öffentliche Methoden
    // -------------------------------------------------------------------------

    /**
     * Speichert einen neuen Spielstand. Die Liste der vorhandenen Spielstände
     * wird geladen, der neue Eintrag hinzugefügt und alles zurückgeschrieben.
     * Gibt es bereits {@value #MAX_SAVES} Einträge, wird der älteste überschrieben.
     *
     * @param players Liste von {@link GameState.PlayerEntry}-Objekten mit Namen und Guthaben.
     * @param label   Optionaler Anzeigename (darf leer sein).
     * @return Die ID des neu erstellten Spielstands.
     * @throws IOException bei Schreib-/Lesefehlern.
     */
    public static String saveGame(List<GameState.PlayerEntry> players, String label) throws IOException {
        List<GameState> allSaves = loadAllSaves();

        // Neue ID generieren
        String newId = "save_" + (System.currentTimeMillis() % 100000);

        // Label aufbauen, falls leer
        if (label == null || label.trim().isEmpty()) {
            label = buildAutoLabel(players);
        }

        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        GameState newState = new GameState(newId, label, timestamp, players);

        // Ältesten entfernen wenn voll
        if (allSaves.size() >= MAX_SAVES) {
            allSaves.remove(0);
        }

        allSaves.add(newState);
        writeToFile(allSaves);
        return newId;
    }

    /**
     * Lädt alle gespeicherten Spielstände aus der JSON-Datei.
     * Existiert die Datei nicht, wird eine leere Liste zurückgegeben.
     *
     * @return Liste aller {@link GameState}-Objekte, nie {@code null}.
     */
    public static List<GameState> loadAllSaves() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
            return parseJsonArray(content);
        } catch (IOException e) {
            System.err.println("Fehler beim Laden der Spielstände: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Lädt einen einzelnen Spielstand anhand seiner ID.
     *
     * @param id Die gesuchte Spielstand-ID.
     * @return Das passende {@link GameState}-Objekt, oder {@code null} wenn nicht gefunden.
     */
    public static GameState loadById(String id) {
        for (GameState state : loadAllSaves()) {
            if (state.getId().equals(id)) {
                return state;
            }
        }
        return null;
    }

    /**
     * Löscht einen Spielstand anhand seiner ID.
     *
     * @param id Die ID des zu löschenden Spielstands.
     * @return {@code true} wenn erfolgreich gelöscht, {@code false} wenn nicht gefunden.
     * @throws IOException bei Schreibfehlern.
     */
    public static boolean deleteSave(String id) throws IOException {
        List<GameState> allSaves = loadAllSaves();
        boolean removed = allSaves.removeIf(s -> s.getId().equals(id));
        if (removed) {
            writeToFile(allSaves);
        }
        return removed;
    }

    // -------------------------------------------------------------------------
    // JSON schreiben (manuell)
    // -------------------------------------------------------------------------

    /**
     * Schreibt die komplette Liste der Spielstände als JSON in die Datei.
     *
     * @param saves Die zu schreibende Liste.
     * @throws IOException bei Schreibfehlern.
     */
    private static void writeToFile(List<GameState> saves) throws IOException {
        File file = new File(SAVE_FILE);
        file.getParentFile().mkdirs(); // Ordner anlegen falls nötig

        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        for (int i = 0; i < saves.size(); i++) {
            sb.append(toJson(saves.get(i)));
            if (i < saves.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("]");

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(sb.toString());
        }
    }

    /**
     * Serialisiert ein einzelnes {@link GameState}-Objekt als JSON-String.
     *
     * @param state Das zu serialisierende Objekt.
     * @return JSON-Darstellung als String.
     */
    private static String toJson(GameState state) {
        StringBuilder sb = new StringBuilder();
        sb.append("  {\n");
        sb.append("    \"id\": \"").append(escape(state.getId())).append("\",\n");
        sb.append("    \"label\": \"").append(escape(state.getLabel())).append("\",\n");
        sb.append("    \"savedAt\": \"").append(escape(state.getSavedAt())).append("\",\n");
        sb.append("    \"players\": [\n");

        List<GameState.PlayerEntry> players = state.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            GameState.PlayerEntry p = players.get(i);
            sb.append("      { \"name\": \"").append(escape(p.getName()))
                    .append("\", \"balance\": ").append(p.getBalance()).append(" }");
            if (i < players.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("    ]\n");
        sb.append("  }");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // JSON parsen (manuell)
    // -------------------------------------------------------------------------

    /**
     * Parst eine JSON-Array-Zeichenkette in eine Liste von {@link GameState}-Objekten.
     *
     * @param json Der vollständige JSON-String (Array).
     * @return Die geparste Liste.
     */
    private static List<GameState> parseJsonArray(String json) {
        List<GameState> result = new ArrayList<>();
        // Äußere eckige Klammern entfernen
        json = json.trim();
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]"))   json = json.substring(0, json.length() - 1);

        // Jedes Objekt { ... } einzeln extrahieren
        List<String> objects = splitJsonObjects(json);
        for (String obj : objects) {
            try {
                result.add(parseGameState(obj.trim()));
            } catch (Exception e) {
                System.err.println("Warnung: Spielstand konnte nicht geparst werden: " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * Teilt einen JSON-String in einzelne Objekt-Blöcke auf (auf Basis von { }).
     *
     * @param json Der zu teilende String.
     * @return Liste der Objekt-Strings.
     */
    private static List<String> splitJsonObjects(String json) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start != -1) {
                    objects.add(json.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return objects;
    }

    /**
     * Parst einen einzelnen JSON-Objekt-String in ein {@link GameState}-Objekt.
     *
     * @param json Der JSON-String des Objekts.
     * @return Das erstellte {@link GameState}.
     */
    private static GameState parseGameState(String json) {
        GameState state = new GameState();
        state.setId(extractString(json, "id"));
        state.setLabel(extractString(json, "label"));
        state.setSavedAt(extractString(json, "savedAt"));

        // Spieler-Array extrahieren
        int playersStart = json.indexOf("\"players\"");
        if (playersStart != -1) {
            int arrStart = json.indexOf('[', playersStart);
            int arrEnd   = json.indexOf(']', arrStart);
            if (arrStart != -1 && arrEnd != -1) {
                String playersJson = json.substring(arrStart, arrEnd + 1);
                state.setPlayers(parsePlayers(playersJson));
            }
        }
        return state;
    }

    /**
     * Parst das Spieler-Array aus einem JSON-String.
     *
     * @param json Der JSON-Array-String der Spieler.
     * @return Liste der {@link GameState.PlayerEntry}-Objekte.
     */
    private static List<GameState.PlayerEntry> parsePlayers(String json) {
        List<GameState.PlayerEntry> players = new ArrayList<>();
        List<String> objects = splitJsonObjects(json);

        for (String obj : objects) {
            String name    = extractString(obj, "name");
            String balStr  = extractValue(obj, "balance");
            int balance    = 0;
            try {
                balance = Integer.parseInt(balStr.trim());
            } catch (NumberFormatException ignored) {}
            players.add(new GameState.PlayerEntry(name, balance));
        }
        return players;
    }

    /**
     * Extrahiert den String-Wert eines JSON-Felds.
     * Beispiel: {@code "name": "Anna"} → {@code "Anna"}
     *
     * @param json  Der JSON-String.
     * @param field Der Feldname.
     * @return Den extrahierten Wert oder einen leeren String.
     */
    private static String extractString(String json, String field) {
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return "";
        int colon    = json.indexOf(':', idx);
        int q1       = json.indexOf('"', colon + 1);
        int q2       = json.indexOf('"', q1 + 1);
        if (q1 == -1 || q2 == -1) return "";
        return json.substring(q1 + 1, q2);
    }

    /**
     * Extrahiert den Rohwert (ohne Anführungszeichen) eines JSON-Felds.
     * Nützlich für Zahlen-Felder wie {@code "balance": 120}.
     *
     * @param json  Der JSON-String.
     * @param field Der Feldname.
     * @return Den extrahierten Rohwert oder "0".
     */
    private static String extractValue(String json, String field) {
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return "0";
        int colon = json.indexOf(':', idx);
        int end   = colon + 1;
        while (end < json.length() && json.charAt(end) == ' ') end++;
        int valStart = end;
        while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
        return json.substring(valStart, end).trim();
    }

    /**
     * Escaped Sonderzeichen in einem String für die JSON-Ausgabe.
     *
     * @param s Der zu escapende String.
     * @return Den escapten String.
     */
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    /**
     * Generiert automatisch ein Label aus der Spielerliste.
     * Bei ≤ 3 Spielern werden alle Namen aufgelistet, sonst nur die Anzahl.
     *
     * @param players Die Spielerliste.
     * @return Ein lesbares Label.
     */
    private static String buildAutoLabel(List<GameState.PlayerEntry> players) {
        if (players.size() <= 3) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < players.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(players.get(i).getName());
            }
            return sb.toString();
        }
        return players.size() + " Spieler";
    }
}