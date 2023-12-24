package CRProject.client;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import CRProject.client.views.ChatPanel;
import CRProject.client.views.ConnectionPanel;
import CRProject.client.views.UserInputPanel;
import CRProject.client.views.RoomsPanel;
import CRProject.client.views.Menu;
import CRProject.common.Constants;

public class ClientUI extends JFrame implements IClientEvents, ICardControls {
    private CardLayout card;
    private Container container;
    private String originalTitle;
    private static Logger logger = Logger.getLogger(ClientUI.class.getName());
    private JPanel currentCardPanel;
    private Card currentCard;

    private Hashtable<Long, String> userList;

    private long myId = Constants.DEFAULT_CLIENT_ID;
    private JMenuBar menu;
    private ConnectionPanel csPanel;
    private UserInputPanel inputPanel;
    private RoomsPanel roomsPanel;
    private ChatPanel chatPanel;

    public ClientUI(String title) {
        super(title);
        originalTitle = title;
        container = getContentPane();
        container.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                container.setPreferredSize(e.getComponent().getSize());
                container.revalidate();
                container.repaint();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }
        });

        setMinimumSize(new Dimension(400, 400));
        setLocationRelativeTo(null);
        card = new CardLayout();
        setLayout(card);

        menu = new Menu(this);
        this.setJMenuBar(menu);

        csPanel = new ConnectionPanel(this);
        inputPanel = new UserInputPanel(this);
        chatPanel = new ChatPanel(this);
        roomsPanel = new RoomsPanel(this);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                int response = JOptionPane.showConfirmDialog(container,
                        "Are you sure you want to close this window?", "Close Window?",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    try {
                        Client.INSTANCE.sendDisconnect();
                    } catch (NullPointerException | IOException e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                }
            }
        });

        pack();
        setVisible(true);
    }

    private void highlightLastPersonSpeaking() {
        if (currentCard == Card.CHAT) {
            chatPanel.highlightLastPersonSpeaking();
        }
    }

    private void updatePersonColor(long clientId, Color newColor) {
        if (currentCard == Card.CHAT) {
            chatPanel.updatePersonColor(clientId, newColor);
        }
    }

    void findAndSetCurrentPanel() {
        Component[] components = container.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component c = components[i];
            if (c.isVisible()) {
                currentCardPanel = (JPanel) c;
                currentCard = Enum.valueOf(Card.class, currentCardPanel.getName());

                if (myId == Constants.DEFAULT_CLIENT_ID && currentCard.ordinal() >= Card.CHAT.ordinal()) {
                    show(Card.CONNECT.name());
                }
                break;
            }
        }
        logger.log(Level.INFO, currentCardPanel.getName());
    }

    @Override
    public void next() {
        card.next(container);
        findAndSetCurrentPanel();
        highlightLastPersonSpeaking();
    }

    @Override
    public void previous() {
        card.previous(container);
    }

    @Override
    public void show(String cardName) {
        card.show(container, cardName);
        findAndSetCurrentPanel();
        highlightLastPersonSpeaking();
    }

    @Override
    public void addPanel(String cardName, JPanel panel) {
        this.add(cardName, panel);
    }

    @Override
    public void connect() {
        String username = inputPanel.getUsername();
        String host = csPanel.getHost();
        int port = csPanel.getPort();
        setTitle(originalTitle + " - " + username);
        Client.INSTANCE.connect(host, port, username, this);
    }

    public static void main(String[] args) {
        new ClientUI("Client");
    }

    private String mapClientId(long clientId) {
        String clientName = userList.get(clientId);
        if (clientName == null) {
            clientName = "Server";
        }
        return clientName;
    }

    private synchronized void processClientConnectionStatus(long clientId, String clientName, boolean isConnect) {
        if (isConnect) {
            if (!userList.containsKey(clientId)) {
                logger.log(Level.INFO, String.format("Adding %s[%s]", clientName, clientId));
                userList.put(clientId, clientName);
                chatPanel.addUserListItem(clientId, String.format("%s (%s)", clientName, clientId));
            }
        } else {
            if (userList.containsKey(clientId)) {
                logger.log(Level.INFO, String.format("Removing %s[%s]", clientName, clientId));
                userList.remove(clientId);
                chatPanel.removeUserListItem(clientId);
            }
            if (clientId == myId) {
                logger.log(Level.INFO, "I disconnected");
                myId = Constants.DEFAULT_CLIENT_ID;
                previous();
            }
        }
    }

    @Override
    public void onClientConnect(long clientId, String clientName, String message) {
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            processClientConnectionStatus(clientId, clientName, true);
            chatPanel.addText(String.format("*%s %s*", clientName, message));
        }
    }

    @Override
    public void onClientDisconnect(long clientId, String clientName, String message) {
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            processClientConnectionStatus(clientId, clientName, false);
            chatPanel.addText(String.format("*%s %s*", clientName, message));
        }
    }

    @Override
    public void onMessageReceive(long clientId, String message) {
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            String clientName = mapClientId(clientId);
            chatPanel.addText(String.format("%s: %s", clientName, message));

            // Example: Determine if the current client is the last person who spoke
            boolean isLastPersonSpeaking = (clientName.equals(chatPanel.getLastPersonSpeaking()));

            // Call methods in ChatPanel
            chatPanel.highlightUsers(isLastPersonSpeaking);
        }
    }

    @Override
    public void onReceiveClientId(long id) {
        if (myId == Constants.DEFAULT_CLIENT_ID) {
            myId = id;
            show(Card.CHAT.name());
        } else {
            logger.log(Level.WARNING, "Received client id after already being set, this shouldn't happen");
        }
    }

    @Override
    public void onResetUserList() {
        userList.clear();
        chatPanel.clearUserList();
    }

    @Override
    public void onSyncClient(long clientId, String clientName) {
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            processClientConnectionStatus(clientId, clientName, true);
        }
    }

    @Override
    public void onReceiveRoomList(String[] rooms, String message) {
        roomsPanel.removeAllRooms();
        if (message != null && message.length() > 0) {
            roomsPanel.setMessage(message);
        }
        if (rooms != null) {
            for (String room : rooms) {
                roomsPanel.addRoom(room);
            }
        }
    }

    @Override
    public void onRoomJoin(String roomName) {
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            chatPanel.addText("Joined room " + roomName);
        }
    }
}
