package logic;

public class Dealer extends Participant {

    public Dealer() {
        super("Der Abzocker");
    }

    @Override
    public void makeTurn(GameEngine engine) {
        // Die KI-Logik für den Dealer (z.B. "Zieh bis 17") kommt hier rein
        System.out.println(getName() + " ist am Zug. Jetzige Hand:" + getHand());
        int score = calculateScore();
        System.out.println("Wert: " + score);
        System.out.println(getName() + "  denkt nach...");
        if (score > 21) {
            System.out.println(getName() + " hat sich überkauft! (Bust)");
            engine.dealerStand(); // Zug beenden -> führt zu evaluateWinner()
        }
        else if (score >= 17){

            System.out.println(getName() + "  hat Angst und bleibt lieber stehen");
            engine.dealerStand();
        }
        else {

            System.out.println(getName() + "  hat keine falsche Scheu und haut drauf");
            engine.dealerHit();
        }

    }
}