package tests; // Passe das an deinen neuen Ordnernamen an

import Saving.GameState;
import Saving.SaveGameManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für den SaveGameManager.
 * <p>
 * Prüft ob das Speichern Laden und Löschen von Spielständen
 * im JSONFormat korrekt funktioniert.
 * </p>
 * * @author Elias
 */
public class SaveGameManagerTest {

    /** Speichert die ID des Testspielstands um ihn am Ende wieder zu löschen. */
    private String testSaveId;

    /**
     * Wird vor jedem einzelnen Test ausgeführt.
     * Setzt die ID sicherheitshalber zurück.
     */
    @BeforeEach
    void setUp() {
        testSaveId = null;
    }

    /**
     * Wird nach jedem einzelnen Test ausgeführt.
     * Räumt auf damit die echte Speicherdatei nicht zugemüllt wird.
     */
    @AfterEach
    void tearDown() {
        if (testSaveId != null) {
            try {
                SaveGameManager.deleteSave(testSaveId);
            } catch (IOException e) {
                // Fehler beim Aufräumen ignorieren
            }
        }
    }

    /**
     * Testet den kompletten Kreislauf: Erstellen Speichern und wieder Laden.
     */
    @Test
    void testSaveAndLoadGame() throws IOException {

        List<GameState.PlayerEntry> players = new ArrayList<>();
        players.add(new GameState.PlayerEntry("TestSpieler1", 150));
        players.add(new GameState.PlayerEntry("TestSpieler2", 50));


        testSaveId = SaveGameManager.saveGame(players, "Unit Test Runde");

        // Prüfung 1: Wurde eine gültige ID generiert?
        assertNotNull(testSaveId);
        assertTrue(testSaveId.startsWith("save_"));

        GameState loadedState = SaveGameManager.loadById(testSaveId);

        // Prüfung 2: Sind die Daten korrekt aus der JSON gelesen worden?
        assertNotNull(loadedState);
        assertEquals("Unit Test Runde", loadedState.getLabel());
        assertEquals(2, loadedState.getPlayers().size());

        assertEquals("TestSpieler1", loadedState.getPlayers().get(0).getName());
        assertEquals(150, loadedState.getPlayers().get(0).getBalance());

        assertEquals("TestSpieler2", loadedState.getPlayers().get(1).getName());
        assertEquals(50, loadedState.getPlayers().get(1).getBalance());
    }

    /**
     * Testet ob die Löschfunktion einen Spielstand restlos entfernt.
     */
    @Test
    void testDeleteSave() throws IOException {

        List<GameState.PlayerEntry> players = new ArrayList<>();
        players.add(new GameState.PlayerEntry("Opfer", 0));
        testSaveId = SaveGameManager.saveGame(players, "Zu Löschen");

        // Sicherstellen dass er wirklich da ist
        assertNotNull(SaveGameManager.loadById(testSaveId));

        // Ausführung: Löschen
        boolean wasDeleted = SaveGameManager.deleteSave(testSaveId);

        // Prüfung: Methode muss true zurückgeben und Laden muss null ergeben
        assertTrue(wasDeleted);
        assertNull(SaveGameManager.loadById(testSaveId));

        testSaveId = null;
    }
}