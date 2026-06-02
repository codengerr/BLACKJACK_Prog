package logic;



public class Player extends Participant {

    private int balance;
    private int currentBet;

    public Player(String name) {
        super(name);
        this.balance = 100; // Startkapital: 100€
        this.currentBet = 5; // Standard-Einsatz: 5€
    }

    @Override
    public void makeTurn(GameEngine engine) {
        System.out.println(getName() + " ist am Zug. Jetzige Hand:" + getHand());
        int score = calculateScore();
        System.out.println("Wert: " + score);


    }
    public void setCurrentBet(int amount) { this.currentBet = amount; }
    public int getBalance() { return balance; }
    public int getCurrentBet() { return currentBet; }

    public void increaseBet(int amount) {
        if (currentBet + amount <= balance) {
            currentBet += amount;
        }
    }

    public void decreaseBet(int amount) {
        if (currentBet - amount >= 5) { // Minimum 5€ Einsatz
            currentBet -= amount;
        }
    }

    // Auszahlungen am Ende der Runde
    public void winBet() {
        balance += currentBet; // Gewinn draufrechnen
        currentBet = 5; // Reset für nächste Runde
    }

    public void loseBet() {
        balance -= currentBet; // Verlust abziehen
        currentBet = 5; // Reset für nächste Runde
    }

    public void pushBet() {
        // Bei Unentschieden passiert mit der Balance nichts, nur Reset
        currentBet = 5;
    }

}
