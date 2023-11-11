package CRProject.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import CRProject.common.Constants;

public class Room implements AutoCloseable {
	private String name;
	private int faceValue; // check
	private List<ServerThread> clients = Collections.synchronizedList(new ArrayList<ServerThread>());
	private boolean isRunning = false;
	// Commands
	private final static String COMMAND_TRIGGER = "/";
	private final static String CREATE_ROOM = "createroom";
	private final static String JOIN_ROOM = "joinroom";
	private final static String DISCONNECT = "disconnect";
	private final static String LOGOUT = "logout";
	private final static String LOGOFF = "logoff";
	private static Logger logger = Logger.getLogger(Room.class.getName());

	/*
	 * mjf8, 11/03/2023, 17:57 || updated mjf8, 11/03/23, 23:41 || updated mjf8,
	 * 11/04/23, 12:21
	 */
	@Deprecated
	private final static String ROLL = "roll";
	private final static String FLIP = "flip";

	private final static String COLOR_REGEX = "#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{3})";

	public Room(String name) {
		this.name = name;
		isRunning = true;
	}

	private void info(String message) {
		logger.log(Level.INFO, String.format("Room[%s]: %s", name, message));
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
					// sendMessage(client, "joined the room " + getName());
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
			// sendMessage(client, "left the room");
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
					case ROLL: // fix issues with roll numbers (sendMessage is wrong).
						if (comm2.length == 2 && !comm2[1].contains("d")) {
							int sides = Integer.parseInt(comm2[1]);
							if (sides > 0) {
								int faceValue = rollDie(sides); // check variable
								sendMessage(client, " rolled a " + comm2[1] + " and got " + faceValue);
							}
						} else if (comm2.length == 2 && comm2[1].matches("\\d+d\\d+")) {
							String[] dice = comm2[1].split("d");
							int numberOfDice = Integer.parseInt(dice[0]);
							int sides = Integer.parseInt(dice[1]);
							if (numberOfDice > 0 && sides > 0) {
								int totalValue = rollDice(numberOfDice, sides); // check variable
								sendMessage(client, " rolled " + numberOfDice + " dice " + " and got " + totalValue); // check
																														// logic
																														// ||
																														// updated
																														// mjf8,
																														// 11/06/23,
																														// 17:28

							} else {
								wasCommand = false;
							}
						}
						break;
					case FLIP:
						if (comm2.length == 2 && comm2[1].equalsIgnoreCase("coin"))
							;
						String result = flipCoin();
						sendMessage(client, " flipped a coin and got " + result);
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

	/*
	 * mjf8, 11/03/23, 21:39 || updated 11/03/23, 23:29 || updated 11/04/23, 11:20
	 * Using Open AI GPT3.5 AI as an outline.
	 */

	private int rollDie(int sides) {
		if (sides <= 0) {
			throw new IllegalArgumentException("Number of sides must be greater than 0");
		}
		return (int) (Math.random() * sides) + 1;
	}

	private int rollDice(int numberOfDice, int sides) {
		if (numberOfDice <= 0 || sides <= 0) {
			throw new IllegalArgumentException("Number of dice must be greater than 0");
		}
		int totalValue = 0;
		for (int i = 0; i < numberOfDice; i++) {
			totalValue += rollDie(sides);
		}
		return totalValue;
	}
	/*
	 * mjf8, 11/06/23, 17:34
	 */

	private String flipCoin() {
		Random r = new Random();
		boolean isHeads = r.nextBoolean();
		return isHeads ? "Heads" : "Tails";

	}

	// Command helper methods

	protected static void getRooms(String query, ServerThread client) {
		String [] rooms = Server.INSTANCE.getRooms(query).toArray(new String[0]);
		client.sendRoomsList(rooms,(rooms!=null&&rooms.length==0)?"No rooms found containing your query string":null);
	}

	protected static void createRoom(String roomName, ServerThread client) {
		if (Server.INSTANCE.createNewRoom(roomName)) {
			Room.joinRoom(roomName, client);
		} else {
			client.sendMessage(Constants.DEFAULT_CLIENT_ID, String.format("Room %s already exists", roomName));
			client.sendRoomsList(null, String.format("Room %s already exists", roomName));
		}
	}

	protected static void joinRoom(String roomName, ServerThread client) {
		if (!Server.INSTANCE.joinRoom(roomName, client)) {
			client.sendMessage(Constants.DEFAULT_CLIENT_ID, String.format("Room %s doesn't exist", roomName));
			client.sendRoomsList(null, String.format("Room %s doesn't exist", roomName));
		}
	}

	protected static void disconnectClient(ServerThread client, Room room) {
		client.setCurrentRoom(null);
		client.disconnect();
		room.removeClient(client);
	}
	// end command helper methods
	/*
	 * mjf8, 11/06/23, 20:31, updated 11/07/23, 09:27
	 * Using GPT 3.5 Open AI for a basic outline and regex
	 */

	protected static String formatMessage(String message) {
		message = message.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
		message = message.replaceAll("\\*(.*?)\\*", "<i>$1</i>");
		message = message.replaceAll("_(.*?)_", "<u>$1</u>");
		message = message.replaceAll(COLOR_REGEX, "<font color=\"$0\">$0</font>");

		// message = message.replaceAll("#r(.*?)#", "<font color=\"red\">$1</font>");
		// message = message.replaceAll("#b(.*?)#", "<font color=\"blue\">$1</font>");
		// message = message.replaceAll("#g(.*?)#", "<font color=\"green\">$1</font>");

		return message;
	}

	/***
	 * Takes a sender and a mjb ent info.
	 * 
	 * @param sender  The client sending the message
	 * @param message The message to broadcast inside the room
	 */

	/*
	 * mjf8, 11/06/23, 22:33
	 */
	protected synchronized void sendMessage(ServerThread sender, String message) {
		if (!isRunning) {
			return;
		}
		info("Sending message to " + clients.size() + " clients");
		if (sender != null && processCommands(message, sender)) {
			return;
		}

		message = formatMessage(message);

		long from = (sender == null) ? Constants.DEFAULT_CLIENT_ID : sender.getClientId();
		Iterator<ServerThread> iter = clients.iterator();
		while (iter.hasNext()) {
			ServerThread client = iter.next();
			boolean messageSent = client.sendMessage(from, message);
			if (!messageSent) {
				handleDisconnect(iter, client);
			}
		}
	}

	protected synchronized void sendConnectionStatus(ServerThread sender, boolean isConnected) {
		Iterator<ServerThread> iter = clients.iterator();
		while (iter.hasNext()) {
			ServerThread client = iter.next();
			boolean messageSent = client.sendConnectionStatus(sender.getClientId(), sender.getClientName(),
					isConnected); // new code
			if (!messageSent) {
				handleDisconnect(iter, client);
			}
		}
	}

	private synchronized void handleDisconnect(Iterator<ServerThread> iter, ServerThread client) {
		if (iter != null) {
			iter.remove();
		}
		info("Removed client " + client.getClientName());
		checkClients();
		sendConnectionStatus(client, false);
		// sendMessage(null, client.getClientName() + " disconnected");
	}

	public void close() {
		Server.INSTANCE.removeRoom(this);
		isRunning = false;
		clients = null;
	}
}