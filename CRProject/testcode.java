package CRProject;
import java.util.Random;

public class testcode {

    private static final String ROLL_DICE = "ROLL";
    private Random random = new Random();

    private void rollDice(ServerThread client) {
        int result = random.nextInt(6) + 1;
        String message = "You rolled a " + result;
        client.sendMessage(message);
    }
    
}