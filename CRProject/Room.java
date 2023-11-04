package CRProject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Room implements AutoCloseable{
	protected static Server server; // used to refer to accessible server functions
	private String name;
	private int faceValue;
	private List<ServerThread> clients = new ArrayList<ServerThread>();
	private boolean isRunning = false;
	// Commands
	private final static String COMMAND_TRIGGER = "/";
	private final static String CREATE_ROOM = "createroom";
	private final static String JOIN_ROOM = "joinroom";
	private final static String DISCONNECT = "disconnect";
	private final static String LOGOUT = "logout";
	private final static String LOGOFF = "logoff";
	
	/*
	 * mjf8, 11/03/2023, 17:57 || updated mjf8, 11/3/23, 23:41
	 */
	//@Deprecated
	private final static String ROLL = "roll";


	public Room(String name) {
		this.name = name;
		isRunning = true;
	}

	private void info(String message) {
		System.out.println(String.format("Room[%s]: %s", name, message));
	}

	public String getName() {
		return name;
	}

	protected synchronized void addClient(ServerThread client) {
		if (!isRunning) {
			return;
		}
		client.setCurrentRoom(this);
		if (clients.indexOf(client) > -1) {
			info("Attempting to add a client that already exists");
		} else {
			clients.add(client);
			new Thread() {
				@Override
				public void run() {
					// slight delay to let potentially new client to finish
					// binding input/output streams
					// comment out the Thread.sleep to see what happens
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//sendMessage(client, "joined the room " + getName());
					sendConnectionStatus(client, true);
				}
			}.start();

		}
	}

	protected synchronized void removeClient(ServerThread client) {
		if (!isRunning) {
			return;
		}
		clients.remove(client);
		// we don't need to broadcast it to the server
		// only to our own Room
		if (clients.size() > 0) {
			//sendMessage(client, "left the room");
			sendConnectionStatus(client, false);
		}
		checkClients();
	}

	/***
	 * Checks the number of clients.
	 * If zero, begins the cleanup process to dispose of the room
	 */
	private void checkClients() {
		// Cleanup if room is empty and not lobby
		if (!name.equalsIgnoreCase("lobby") && clients.size() == 0) {
			close();
		}
	}

	/***
	 * Helper function to process messages to trigger different functionality.
	 * 
	 * @param message The original message being sent
	 * @param client  The sender of the message (since they'll be the ones
	 *                triggering the actions)
	 */
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
	
				if (command.equalsIgnoreCase(ROLL)) {
					// Check for /roll # or /roll #d#
					if (comm2.length == 2 && comm2[1].matches("\\d+")) {
						int sides = Integer.parseInt(comm2[1]);
						if (sides > 0) {
							int faceValue = rollDie(sides); //check variable
							//client.sendMessage(name + " rolled a " + faceValue);
						} else {
							//client.sendMessage("Number of sides must be greater than 0.");
						}
					} else if (comm2.length == 2 && comm2[1].matches("\\d+d\\d+")) {
						String[] dice = comm2[1].split("d");
						int numberOfDice = Integer.parseInt(dice[0]);
						int sides = Integer.parseInt(dice[1]);
						if (numberOfDice > 0 && sides > 0) {
							int totalValue = rollDice(numberOfDice, sides); //check variable
							//client.sendMessage(name + " rolled " + numberOfDice + "d" + sides + " and got a total of " + totalValue);
						} else {
							//client.sendMessage("Number of dice and sides must be greater than 0.");
						}
					} else {
						wasCommand = false;
					}
				} else {
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
						default:
							wasCommand = false;
							break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wasCommand;
	}

	/*
	 * mjf8, 11/03/23, 21:39 || updated 11/03/23, 23:29
	 * Using Open AI GPT3.5 AI as an outline.
	 */
	//Later, change syntax of the 'throw new' blocks so they are the same

	private int rollDie(int sides) {
		if (sides <= 0) {
			throw new IllegalArgumentException("Number of sides must be greater than 0");
		}
		return (int) (Math.random() * sides) + 1;
	}

	private int rollDice(int numberOfDice, int sides) {
		if (numberOfDice <= 0 || sides <= 0) {
			throw new IllegalArgumentException("Number of dice should be greater than 0");
		}
		int totalValue = 0;
		for (int i = 0; i < numberOfDice; i++) {
			totalValue += rollDie(sides);
		}
		return totalValue;
	}

	// Command helper methods
	protected static void createRoom(String roomName, ServerThread client) {
		if (server.createNewRoom(roomName)) {
			server.joinRoom(roomName, client);
		} else {
			client.sendMessage("Server", String.format("Room %s already exists", roomName));
		}
	}

	protected static void joinRoom(String roomName, ServerThread client) {
		if (!server.joinRoom(roomName, client)) {
			client.sendMessage("Server", String.format("Room %s doesn't exist", roomName));
		}
	}

	protected static void disconnectClient(ServerThread client, Room room) {
		client.setCurrentRoom(null);
		client.disconnect();
		room.removeClient(client);
	}
	// end command helper methods

	/***
	 * Takes a sender and a message and broadcasts the message to all clients in
	 * this room. Client is mostly passed for command purposes but we can also use
	 * it to extract other client info.
	 * 
	 * @param sender  The client sending the message
	 * @param message The message to broadcast inside the room
	 */
	protected synchronized void sendMessage(ServerThread sender, String message) {
		if (!isRunning) {
			return;
		}
		info("Sending message to " + clients.size() + " clients");
		if (sender != null && processCommands(message, sender)) {
			// it was a command, don't broadcast
			return;
		}
		
		String from = (sender == null ? "Room" : sender.getClientName());
		Iterator<ServerThread> iter = clients.iterator();
		while (iter.hasNext()) {
			ServerThread client = iter.next();
			boolean messageSent = client.sendMessage(from, message);
			if (!messageSent) {
				handleDisconnect(iter, client);
			}
		}
	}
	protected synchronized void sendConnectionStatus(ServerThread sender, boolean isConnected){
		Iterator<ServerThread> iter = clients.iterator();
		while (iter.hasNext()) {
			ServerThread client = iter.next();
			boolean messageSent = client.sendConnectionStatus(sender.getClientName(), isConnected);
			if (!messageSent) {
				handleDisconnect(iter, client);
			}
		}
	}
	private void handleDisconnect(Iterator<ServerThread> iter, ServerThread client){
		iter.remove();
		info("Removed client " + client.getId());
		checkClients();
		sendMessage(null, client.getId() + " disconnected");
	}
	public void close() {
		server.removeRoom(this);
		//NOTE: This will break all rooms
		//be sure to remove/comment out server = null;
		server = null;
		isRunning = false;
		clients = null;
	}
}