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
	private List<ServerThread> clients = Collections.synchronizedList(new ArrayList<ServerThread>());
	private boolean isRunning = false;

	/**
	 * Commands
	 */
	private final static String COMMAND_TRIGGER = "/";
	private final static String CREATE_ROOM = "createroom";
	private final static String JOIN_ROOM = "joinroom";
	private final static String DISCONNECT = "disconnect";
	private final static String LOGOUT = "logout";
	private final static String LOGOFF = "logoff";
	private final static String ROLL = "roll";
	private final static String FLIP = "flip";
	private final static String MUTE = "mute";
	private final static String UNMUTE = "unmute";

	private static Logger logger = Logger.getLogger(Room.class.getName());

	/**
	 * 
	 * @param name
	 */
	public Room(String name) {
		this.name = name;
		isRunning = true;
	}

	/**
	 * 
	 * @param message
	 */
	private void info(String message) {
		logger.log(Level.INFO, String.format("Room[%s]: %s", name, message));
	}

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * 
	 * @param client
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
			sendConnectionStatus(client, true);
			sendRoomJoined(client);
			sendUserListToClient(client);
		}
	}

	/**
	 * 
	 * @param client
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
	 * If zero, begins the cleanup process to dispose of the room
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
								sendMessage(client, " rolled 1 die with " + comm2[1] + "sides and got " + faceValue);
							}
						} else if (comm2.length == 2 && comm2[1].matches("\\d+d\\d+")) {
							String[] dice = comm2[1].split("d");
							int numberOfDice = Integer.parseInt(dice[0]);
							int sides = Integer.parseInt(dice[1]);
							if (numberOfDice > 0 && sides > 0) {
								int totalValue = rollDice(numberOfDice, sides); // check variable
								sendMessage(client, "<b>" + " rolled " + numberOfDice + " dice " + " and got "
										+ totalValue + "</b>");

							} else {
								wasCommand = false;
							}
						}
						break;
					case FLIP:
						if (comm2.length == 2 && comm2[1].equalsIgnoreCase("coin"))
							;
						String result = flipCoin();
						sendMessage(client, "<b>" + " flipped a coin and got " + result + " </b>");
						break;
						case MUTE:
						if (comm2.length == 2) {
							String targetUsername = comm2[1];
							ServerThread targetUser = findClientByUsername(targetUsername);
							if (targetUser != null) {
								targetUser.addMute();
								client.sendMessage(Constants.DEFAULT_CLIENT_ID, "You have muted " + targetUsername);
								sendMessage(client, targetUsername + " has been muted in the room.");
							} else {
								client.sendMessage(Constants.DEFAULT_CLIENT_ID, "User " + targetUsername + " not found in the room.");
							}
						}
						break;
					case UNMUTE:
						if (comm2.length == 2) {
							String targetUsername = comm2[1];
							ServerThread targetUser = findClientByUsername(targetUsername);
							if (targetUser != null) {
								targetUser.removeMute();
								client.sendMessage(Constants.DEFAULT_CLIENT_ID, "You have unmuted " + targetUsername);
								sendMessage(client, targetUsername + " has been unmuted in the room.");
							} else {
								client.sendMessage(Constants.DEFAULT_CLIENT_ID, "User " + targetUsername + " not found in the room.");
							}
						}
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

	/**
	 * Simulates rolling a single die with a specificed number of sides.
	 *
	 * @param sides the number of sides on the die that will be "rolled".
	 * @return an integer representing the result of the rolling die.
	 * @throws IllegalArgumentException if the number of sides is less than or equal
	 *                                  to 0;
	 */
	private int rollDie(int sides) {
		if (sides <= 0) {
			throw new IllegalArgumentException("Number of sides must be greater than 0");
		}
		return (int) (Math.random() * sides) + 1;
	}

	/**
	 * Simulates rolling multiple dice with a specified number of sides each.
	 * 
	 * @param numberOfDice the number of dice to roll.
	 * @param sides        the number of sides on each die.
	 * @return an integer representing the total value obtained by rolling the
	 *         specified number of dice.
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
	 * Simulates flipping of a coin and returns the result (heads or tails)
	 * 
	 * @return a string representing the result of the coin flip, either heads or
	 *         tails
	 */
	private String flipCoin() {
		Random r = new Random();
		boolean isHeads = r.nextBoolean();
		return isHeads ? "Heads" : "Tails";

	}

	// Command helper methods

	protected static void getRooms(String query, ServerThread client) {
		String[] rooms = Server.INSTANCE.getRooms(query).toArray(new String[0]);
		client.sendRoomsList(rooms,
				(rooms != null && rooms.length == 0) ? "No rooms found containing your query string" : null);
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

	/**
	 * Formats the input message by applying basic text formatting.
	 *
	 * @param message The message to be formatted, containing specified markers for
	 *                bold, italic, underline, and color.
	 * @return A string with applied HTML-like formatting for bold, italic,
	 *         underline, and color based on specified markers.
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

	/**
	 * 
	 * @param sender
	 * @param message
	 */
	protected synchronized void sendMessage(ServerThread sender, String message) {
		if (!isRunning) {
			return;
		}

		info("Sending message to " + clients.size() + " clients");

		if (sender != null && processCommands(message, sender)) {
			return;
		}

		long from = (sender == null) ? Constants.DEFAULT_CLIENT_ID : sender.getClientId();

		message = formatMessage(message);

		if (message.startsWith("@")) {
			String[] parts = message.split(" ", 2);
			String username = parts[0].substring(1);
			String privateMessage = parts[1];

			username = username.trim();

			ServerThread receiver = findClientByUsername(username);

			if (sender != null && receiver != null) {
				sender.sendMessage(from, message);
				receiver.sendMessage(from, message);
				return;
			}
		}

		synchronized (clients) {
			Iterator<ServerThread> iter = clients.iterator();
			while (iter.hasNext()) {
				ServerThread client = iter.next();
				boolean messageSent = client.sendMessage(from, message);
				if (!messageSent) {
					handleDisconnect(iter, client);
				}
			}
		}
	}

	protected synchronized void sendUserListToClient(ServerThread receiver) {
		logger.log(Level.INFO, String.format("Room[%s] Syncing client list of %s to %s", getName(), clients.size(),
				receiver.getClientName()));
		synchronized (clients) {
			Iterator<ServerThread> iter = clients.iterator();
			while (iter.hasNext()) {
				ServerThread clientInRoom = iter.next();
				if (clientInRoom.getClientId() != receiver.getClientId()) {
					boolean messageSent = receiver.sendExistingClient(clientInRoom.getClientId(),
							clientInRoom.getClientName());
					if (!messageSent) {
						handleDisconnect(null, receiver);
						break;
					}
				}
			}
		}
	}

	private ServerThread findClientByUsername(String username) {
		synchronized (clients) {
			for (ServerThread client : clients) {
				if (client.getClientName().trim().equalsIgnoreCase(username.trim())) {
					return client;
				}
			}
		}
		return null;
	}

	protected synchronized void sendRoomJoined(ServerThread receiver) {
		boolean messageSent = receiver.sendRoomName(getName());
		if (!messageSent) {
			handleDisconnect(null, receiver);
		}
	}

	protected synchronized void sendConnectionStatus(ServerThread sender, boolean isConnected) {
		if (clients == null) {
			return;
		}
		synchronized (clients) {
			for (int i = clients.size() - 1; i >= 0; i--) {
				ServerThread client = clients.get(i);
				boolean messageSent = client.sendConnectionStatus(sender.getClientId(), sender.getClientName(),
						isConnected);
				if (!messageSent) {
					clients.remove(i);
					info("Removed client " + client.getClientName());
					checkClients();
					sendConnectionStatus(client, false);
				}
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