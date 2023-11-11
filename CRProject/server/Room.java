package CRProject.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Room implements AutoCloseable {
	protected static Server server;
	private String name;
	private int faceValue; // check
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
	 * mjf8, 11/03/2023, 17:57 || updated mjf8, 11/03/23, 23:41 || updated mjf8,
	 * 11/04/23, 12:21 || Deprecated 11/10/23, 23:34
	 */
	@Deprecated
	private final static String ROLL = "roll";
	private final static String FLIP = "flip";

	@Deprecated
	private final static String COLOR_REGEX = "#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{3})";

	/**
	 * Default constructor for Room
	 * 
	 * @param name
	 */
	public Room(String name) {
		this.name = name;
		isRunning = true;
	}

	/**
	 * Logs one (1) informational message related to the specific Room.
	 * 
	 * @param message The information to be logged.
	 */
	private void info(String message) {
		System.out.println(String.format("Room[%s]: %s", name, message));
	}

	/**
	 * Getter method for variable name.
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Adds a client to the current room.
	 *
	 * @param client The ServerThread representing the client to be added.
	 */
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
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					sendConnectionStatus(client, true);
				}
			}.start();

		}
	}

	/**
	 * Removes a client from the room.
	 *
	 * @param client The ServerThread representing the client to be removed.
	 */
	protected synchronized void removeClient(ServerThread client) {
		if (!isRunning) {
			return;
		}
		clients.remove(client);
		if (clients.size() > 0) {
			sendConnectionStatus(client, false);
		}
		checkClients();
	}

	/***
	 * Checks the number of clients.
	 * If zero, begins the cleanup process to dispose of the room.
	 * If the room is not lobby only.
	 */
	private void checkClients() {
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
	/**
	 * Simulates the roll of a die with a specified number of sides.
	 *
	 * @param sides The number of sides on the die.
	 * @return The result of the die roll.
	 * @throws IllegalArgumentException if the number of sides is less than or equal
	 *                                  to 0.
	 */
	private int rollDie(int sides) {
		if (sides <= 0) {
			throw new IllegalArgumentException("Number of sides must be greater than 0");
		}
		return (int) (Math.random() * sides) + 1;
	}

	/**
	 * Simulates rolling multiple dice with a specified number of sides.
	 *
	 * @param numberOfDice The number of dice to roll.
	 * @param sides        The number of sides on each die.
	 * @return The total value obtained by rolling the specified number of dice with
	 *         the given number of sides.
	 * @throws IllegalArgumentException if the number of dice or sides is less than
	 *                                  or equal to 0.
	 */
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
	/**
	 * Simulates flipping a coin and returns the result.
	 *
	 * @return The result of the coin flip, either "Heads" or "Tails".
	 */
	private String flipCoin() {
		Random r = new Random();
		boolean isHeads = r.nextBoolean();
		return isHeads ? "Heads" : "Tails";

	}

	/**
	 * Creates a new room with the specified name and adds a client to it if the
	 * room doesn't exist.
	 * Notifies the client if the room already exists.
	 *
	 * @param roomName The name of the room to be created.
	 * @param client   The ServerThread representing the client to add to the room.
	 */
	protected static void createRoom(String roomName, ServerThread client) {
		if (server.createNewRoom(roomName)) {
			server.joinRoom(roomName, client);
		} else {
			client.sendMessage("Server", String.format("Room %s already exists", roomName));
		}
	}

	/**
	 * Adds a client to the specified room if the room exists. Notifies the client
	 * if the room doesn't exist.
	 *
	 * @param roomName The name of the room to join.
	 * @param client   The ServerThread representing the client to be added to the
	 *                 room.
	 */
	protected static void joinRoom(String roomName, ServerThread client) {
		if (!server.joinRoom(roomName, client)) {
			client.sendMessage("Server", String.format("Room %s doesn't exist", roomName));
		}
	}

	/**
	 * Disconnects a client from a room, setting their current room to null and
	 * disconnecting the client.
	 *
	 * @param client The ServerThread representing the client to be disconnected.
	 * @param room   The Room from which the client is to be disconnected.
	 */
	protected static void disconnectClient(ServerThread client, Room room) {
		client.setCurrentRoom(null);
		client.disconnect();
		room.removeClient(client);
	}

	/*
	 * mjf8, 11/06/23, 20:31, updated 11/07/23, 09:27
	 * Using GPT 3.5 Open AI for a basic outline and regex
	 */
	/**
	 * Formats a message based on certain patterns to apply HTML formatting.
	 *
	 * @param message The message to be formatted.
	 * @return The formatted message with HTML tags.
	 */
	protected static String formatMessage(String message) {
		message = message.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
		message = message.replaceAll("\\*(.*?)\\*", "<i>$1</i>");
		message = message.replaceAll("_(.*?)_", "<u>$1</u>");
		message = message.replaceAll("#r(.*?)#", "<font color=\"red\">$1</font>");
		message = message.replaceAll("#b(.*?)#", "<font color=\"blue\">$1</font>");
		message = message.replaceAll("#g(.*?)#", "<font color=\"green\">$1</font>");

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

	/**
	 * Sends a message to all clients in the room, processing commands and
	 * formatting the message.
	 *
	 * @param sender  The ServerThread of the message sender.
	 * @param message The message to be sent.
	 */
	protected synchronized void sendConnectionStatus(ServerThread sender, boolean isConnected) {
		Iterator<ServerThread> iter = clients.iterator();
		while (iter.hasNext()) {
			ServerThread client = iter.next();
			boolean messageSent = client.sendConnectionStatus(sender.getClientName(), isConnected);
			if (!messageSent) {
				handleDisconnect(iter, client);
			}
		}
	}

	/**
	 * Handles the disconnection of a client from the room, removing the client and
	 * informing other clients about the disconnection.
	 *
	 * @param iter   Iterator of clients in the room.
	 * @param client The ServerThread representing the client to be disconnected.
	 */
	private void handleDisconnect(Iterator<ServerThread> iter, ServerThread client) {
		iter.remove();
		info("Removed client " + client.getId());
		checkClients();
		sendMessage(null, client.getId() + " disconnected");
	}
/**
 * Closes the room, removing it from the server and marking it as inactive.
 * Removes references to the server and clients, terminating the room.
 */
	public void close() {
		server.removeRoom(this);
		// NOTE: This will break all rooms
		// be sure to remove/comment out server = null;
		server = null;
		isRunning = false;
		clients = null;
	}
}