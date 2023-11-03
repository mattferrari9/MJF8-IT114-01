package CRProject.testcode;

public class DieClass {
    
    
@Deprecated
private boolean processCommands(String message, ServerThread client) {
    boolean wasCommand = false;
    try {
        if (message.startsWith(COMMAND_TRIGGER)) {
            String[] comm = message.split(COMMAND_TRIGGER);
            String part1 = comm[1];
            String[] comm2 = part1.split(" ");
            String command = comm2[0];
            String roomName;
            wasCommand = true;
            switch (command) {
                case CREATE_ROOM:
                    roomName = comm2[1];
                    Room.createRoom(roomName, client);
                    break;
                case JOIN_ROOM:
                    roomName = comm2[1];
                    Room.joinRoom(roomName, client);
                    break;
                case DISCONNECT:
                case LOGOUT:
                case LOGOFF:
                    Room.disconnectClient(client, this);
                    break;
                case ROLL:
                    int faceValue = rollDie();
                    client.sendMessage(name + "rolled a " + faceValue);
                    break;
                default:
                    wasCommand = false;
                    break;
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return wasCommand;
}

private int rollDie() {
    return (int) (Math.random() * 6) + 1;
}