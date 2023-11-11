package CRProject.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import CRProject.common.Payload;
import CRProject.common.PayloadType;

public class Client {
    Socket server = null;
    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    final String ipAddressPattern = "/connect\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{3,5})";
    final String localhostPattern = "/connect\\s+(localhost:\\d{3,5})";
    boolean isRunning = false;
    private Thread inputThread;
    private Thread fromServerThread;
    private String clientName = "";

    /*
     * Constructs a new client object.
     * 
     * Constructor initializes a new instance of the client class.
     * When instantiated, prints an empty line to the standard output.
     */
    public Client() {
        System.out.println("");
    }

    /**
     * Checks the connection status of the client to the server.
     * 
     * @return {@code true} if the client is connected to the server, {@code false}
     *         otherwise.
     * 
     *         This method verifies the connection status between the client and the
     *         server.
     *         It examines whether the server object is not null and if the server's
     *         connection is active,
     *         not closed, and its input and output streams are not shut down.
     * 
     * @see Server#isConnected()
     * @see Server#isClosed()
     * @see Server#isInputShutdown()
     * @see Server#isOutputShutdown()
     */
    public boolean isConnected() {
        if (server == null) {
            return false;
        }
        return server.isConnected() && !server.isClosed() && !server.isInputShutdown() && !server.isOutputShutdown();

    }

    /**
     * Takes an ip address and a port to attempt a socket connection to a server.
     * 
     * @param address
     * @param port
     * @return true if connection was successful
     */
    private boolean connect(String address, int port) {
        try {
            server = new Socket(address, port);
            // channel to send to server
            out = new ObjectOutputStream(server.getOutputStream());
            // channel to listen to server
            in = new ObjectInputStream(server.getInputStream());
            System.out.println("Client connected");
            listenForServerMessage();
            sendConnect();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isConnected();
    }

    /**
     * <p>
     * Check if the string contains the <i>connect</i> command
     * followed by an ip address and port or localhost and port.
     * </p>
     * <p>
     * Example format: 123.123.123:3000
     * </p>
     * <p>
     * Example format: localhost:3000
     * </p>
     * https://www.w3schools.com/java/java_regex.asp
     * 
     * @param text
     * @return
     */
    private boolean isConnection(String text) {
        // https://www.w3schools.com/java/java_regex.asp
        return text.matches(ipAddressPattern)
                || text.matches(localhostPattern);
    }

    private boolean isQuit(String text) {
        return text.equalsIgnoreCase("/quit");
    }

    /**
     * Checks if the provided text signifies a quit command.
     *
     * @param text The text to be evaluated for a quit command.
     * @return {@code true} if the text represents a quit command ("/quit"
     *         case-insensitive), {@code false} otherwise.
     *
     *         This method examines whether the provided text matches the quit
     *         command "/quit" (case-insensitive).
     *         It returns true if the text indicates the intention to quit or exit a
     *         command loop,
     *         and false if it does not match the quit command.
     *
     * @param text The text to be evaluated for a quit command.
     * @return {@code true} if the text represents a quit command ("/quit"
     *         case-insensitive), {@code false} otherwise.
     */
    private boolean isName(String text) {
        if (text.startsWith("/name")) {
            String[] parts = text.split(" ");
            if (parts.length >= 2) {
                clientName = parts[1].trim();
                System.out.println("Name set to " + clientName);
            }
            return true;
        }
        return false;
    }

    /**
     * Controller for handling various text commands.
     * <p>
     * Add more here as needed
     * </p>
     * 
     * @param text
     * @return true if a text was a command or triggered a command
     */
    private boolean processCommand(String text) {
        if (isConnection(text)) {
            if (clientName.isBlank()) {
                System.out.println("You must set your name before you can connect via: /name your_name");
                return true;
            }
            String[] parts = text.trim().replaceAll(" +", " ").split(" ")[1].split(":");
            connect(parts[0].trim(), Integer.parseInt(parts[1].trim()));
            return true;
        } else if (isQuit(text)) {
            isRunning = false;
            return true;
        } else if (isName(text)) {
            return true;
        }
        return false;
    }

    /**
     * Sends a connection request to the server.
     *
     * @throws IOException if an I/O error occurs while sending the connection
     *                     request.
     *
     *                     This method constructs a Payload object representing a
     *                     connection request.
     *                     It sets the payload type as a connection request and
     *                     includes the client's name.
     *                     The constructed Payload object is sent to the output
     *                     stream.
     *
     * @throws IOException if an I/O error occurs during the object transmission.
     */
    private void sendConnect() throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CONNECT);
        p.setClientName(clientName);
        out.writeObject(p);
    }

    /**
     * Sends a message to the server.
     *
     * @param message The message to be sent to the server.
     * @throws IOException if an I/O error occurs while sending the message.
     *
     *                     This method constructs a Payload object representing a
     *                     message to be transmitted.
     *                     It sets the payload type as a message and includes the
     *                     provided message and the client's name.
     *                     The constructed Payload object is sent to the output
     *                     stream.
     *
     * @param message The message to be sent to the server.
     * @throws IOException if an I/O error occurs during the object transmission.
     */
    private void sendMessage(String message) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.MESSAGE);
        p.setMessage(message);
        p.setClientName(clientName);
        out.writeObject(p);
    }

    /**
     * Initiates a thread to listen for input from the keyboard.
     * 
     * This method initializes a thread to monitor and process user input from the
     * keyboard.
     * It sets up a scanner to read input from the standard input (keyboard),
     * processes commands,
     * sends messages to the server if connected, and handles exceptions or
     * disconnections.
     * It continuously listens for input until the loop is exited or an error
     * occurs.
     * 
     * When an input is received, it checks if it's a command or a message. If it's
     * a message and the client
     * is connected to the server, it sends the message. If not connected, it
     * displays a message indicating
     * the lack of a connection to the server.
     * 
     * If an exception occurs during the input processing or if the connection is
     * dropped, it breaks the loop
     * and triggers the closing of resources. The method is wrapped to catch and
     * print any exceptions.
     * The thread is started to begin monitoring input from the keyboard.
     */
    private void listenForKeyboard() {
        inputThread = new Thread() {
            @Override
            public void run() {
                System.out.println("Listening for input");
                try (Scanner si = new Scanner(System.in);) {
                    String line = "";
                    isRunning = true;
                    while (isRunning) {
                        try {
                            System.out.println("Waiting for input");
                            line = si.nextLine();
                            if (!processCommand(line)) {
                                if (isConnected()) {
                                    if (line != null && line.trim().length() > 0) {
                                        sendMessage(line);
                                    }

                                } else {
                                    System.out.println("Not connected to server");
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Connection dropped");
                            break;
                        }
                    }
                    System.out.println("Exited loop");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    close();
                }
            }
        };
        inputThread.start();
    }

    /**
     * Initiates a thread to listen for incoming messages from the server.
     * This method continuously listens for and processes payloads/messages received
     * from the connected server.
     * 
     * The method operates within a separate thread, constantly checking the
     * server's input stream for incoming payloads.
     * It iterates through the loop while the server connection remains open and
     * data is successfully received.
     * 
     * During the loop, it reads incoming Payload objects from the server's input
     * stream, prints debug information
     * related to the received payload, and processes the message through the
     * 'processMessage' method.
     * 
     * Upon encountering exceptions during message reception or if the server
     * connection is closed,
     * the method handles the exception, prints relevant messages based on the error
     * type, and performs cleanup.
     * 
     * After exiting the listening loop or encountering errors, it ensures proper
     * closure of resources
     * and finalizes the listening process for server input.
     */
    private void listenForServerMessage() {
        fromServerThread = new Thread() {
            @Override
            public void run() {
                try {
                    Payload fromServer;

                    // while we're connected, listen for payload from server
                    while (!server.isClosed() && !server.isInputShutdown()
                            && (fromServer = (Payload) in.readObject()) != null) {

                        System.out.println("Debug Info: " + fromServer);
                        processMessage(fromServer);

                    }
                    System.out.println("Loop exited");
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!server.isClosed()) {
                        System.out.println("Server closed connection");
                    } else {
                        System.out.println("Connection closed");
                    }
                } finally {
                    close();
                    System.out.println("Stopped listening to server input");
                }
            }
        };
        fromServerThread.start();// start the thread
    }

    /**
     * Initiates a thread to listen for incoming messages from the server.
     *
     * This method sets up a thread to continuously listen for messages from the
     * connected server.
     * It reads incoming payloads from the server's input stream and processes the
     * received messages.
     * If the server connection is active and payloads are being received, they are
     * printed for debugging
     * and then passed for further processing via the 'processMessage' method.
     * 
     * If an exception occurs during the reception of server messages, it handles
     * the exception appropriately,
     * prints relevant information based on the type of error, and triggers the
     * closing of resources.
     * After exiting the listening loop or encountering an error, it ensures a
     * graceful closure of resources.
     * The method is wrapped to catch and print any exceptions.
     * The thread is started to commence listening for incoming messages from the
     * server.
     */
    private void processMessage(Payload p) {
        switch (p.getPayloadType()) {
            case CONNECT:
            case DISCONNECT:
                System.out.println(String.format("*%s %s*",
                        p.getClientName(),
                        p.getMessage()));
                break;
            case MESSAGE:
                System.out.println(String.format("%s: %s",
                        p.getClientName(),
                        p.getMessage()));
                break;
            default:
                break;

        }
    }

    /**
     * Starts the client application by initiating keyboard input monitoring.
     *
     * @throws IOException if an I/O error occurs during the keyboard input
     *                     monitoring setup.
     *
     *                     This method serves as the entry point to start the client
     *                     application. It initiates the process by invoking
     *                     the 'listenForKeyboard()' method, which sets up a thread
     *                     to monitor user input from the keyboard.
     *                     Any I/O-related errors encountered during the setup of
     *                     keyboard input monitoring are handled by this method.
     *                     Ensure the 'listenForKeyboard()' method is appropriately
     *                     configured to manage user input for the application.
     *
     * @throws IOException if an I/O error occurs during the keyboard input
     *                     monitoring setup.
     */
    public void start() throws IOException {
        listenForKeyboard();
    }

    private void close() {
        try {
            inputThread.interrupt();
        } catch (Exception e) {
            System.out.println("Error interrupting input");
            e.printStackTrace();
        }
        try {
            fromServerThread.interrupt();
        } catch (Exception e) {
            System.out.println("Error interrupting listener");
            e.printStackTrace();
        }
        try {
            System.out.println("Closing output stream");
            out.close();
        } catch (NullPointerException ne) {
            System.out.println("Server was never opened so this exception is ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            System.out.println("Closing input stream");
            in.close();
        } catch (NullPointerException ne) {
            System.out.println("Server was never opened so this exception is ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            System.out.println("Closing connection");
            server.close();
            System.out.println("Closed socket");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException ne) {
            System.out.println("Server was never opened so this exception is ok");
        }
    }

    /**
     * Closes all resources associated with the client application.
     * This method handles the graceful closure of various resources utilized by the
     * client application.
     * It interrupts the input and server message listening threads, closes the
     * output and input streams,
     * and finally, closes the server connection.
     * Any exceptions encountered during the closing process are caught and
     * appropriately handled.
     * Ensure that this method is called when the client application needs to be
     * shut down or resources released.
     */
    public static void main(String[] args) {
        Client client = new Client();

        try {
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}