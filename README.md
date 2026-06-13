#  Blackjack Casino (Entwickelt als Java-Studienprojekt Wunschnote 1)

Ein vollständig in Java geschriebenes, lokales Multiplayer-Blackjack-Spiel mit einer grafischen Benutzeroberfläche (GUI) und einem eigenen JSON-Speichersystem. 

Dieses Projekt wurde komplett ohne externe Bibliotheken entwickelt. Alle UI-Elemente basieren auf purem `javax.swing` und selbst die JSON-Serialisierung für die Spielstände wurde händisch implementiert.

##  Features

* **Lokaler Multiplayer:** Spielbar für 1 bis 10 Spieler gleichzeitig an einem PC.
* **Grafische Benutzeroberfläche:** Animierte Screens, Custom-Buttons und echte Karten-Sprites.
* **Intelligenter Dealer:** Der Dealer zieht vollautomatisch über einen Swing-Timer basierend auf den echten Blackjack-Regeln (zieht bis 16, steht bei 17).
* **Eigenes Save-System:** Spielerstände und Guthaben können in `.json`-Dateien gespeichert und geladen werden (inkl. Sortierfunktion nach Datum, ID oder Kontostand).


##  Steuerung

Das Spiel kann komplett mit der Maus oder (für den Spielfluss deutlich angenehmer) mit der Tastatur bedient werden:
* **Einsatz bestätigen:** `ENTER`
* **Hit (Karte ziehen):** `LEERTASTE`
* **Stand (Keine Karte mehr):** `ENTER`
* **Nächste Runde (im Ergebnisscreen):** Beliebige Taste

##  Systemvoraussetzungen

* **Java Version:** JDK 14 oder neuer (empfohlen: **Java 17+**), da moderne Switch-Expressions (`switch (suit) { case HERZ -> ... }`) verwendet werden.
* **Keine externen Bibliotheken:** 

##  Installation & Start

1. **Repository klonen:**
   ```bash
   git clone [https://github.com/codengerr/BLACKJACK_Prog.git](https://github.com/codengerr/BLACKJACK_Prog.git)
   ```
   
*   Projekt öffnen:
   Öffne den Ordner in deiner bevorzugten IDE (z. B. IntelliJ IDEA oder Eclipse).
   

*   Ressourcen prüfen:
   Stelle sicher, dass der Ordner resources (mit den Bildern Karten.png, table.png und CardBack.png) im Stammverzeichnis des Projekts liegt.


*   Spiel starten:
   Führe die Main.java aus.

## Dateistruktur

*   src/GUI/ - Beinhaltet alle visuellen Komponenten (Screens, Buttons, ImageLoader).


*   src/logic/ - Das Herzstück des Spiels. Beinhaltet die GameEngine, Deck, Card und Teilnehmer-Logik.


*   src/Saving/ - Handhabt das Lesen, Schreiben und Parsen der JSON-Savegames (SaveGameManager).


*   resources/ - Beinhaltet die grafischen Assets (Karten, Rückseiten, Tisch).


   
