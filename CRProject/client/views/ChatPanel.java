import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import CRProject.client.Card;
import CRProject.client.ClientUtils;
import CRProject.client.ICardControls;
import CRProject.client.Client;

public class ChatPanel extends JPanel {
    private static Logger logger = Logger.getLogger(ChatPanel.class.getName());
    private JPanel chatArea = null;
    private UserListPanel userListPanel;

    public ChatPanel(ICardControls controls) {
        super(new BorderLayout(10, 10));
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        wrapper.add(scroll);
        this.add(wrapper, BorderLayout.CENTER);

        JPanel input = new JPanel();
        input.setLayout(new BoxLayout(input, BoxLayout.X_AXIS));
        JTextField textValue = new JTextField();
        input.add(textValue);
        JButton button = new JButton("Send");
        textValue.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    button.doClick();
                }
            }
        });

        button.addActionListener((event) -> {
            try {
                String text = textValue.getText().trim();
                if (text.length() > 0) {
                    Client.INSTANCE.sendMessage(text);
                    textValue.setText("");
                }
            } catch (NullPointerException e) {
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        chatArea = content;
        input.add(button);
        userListPanel = new UserListPanel(controls);
        this.add(userListPanel, BorderLayout.EAST);
        this.add(input, BorderLayout.SOUTH);
        this.setName(Card.CHAT.name());
        controls.addPanel(Card.CHAT.name(), this);

        chatArea.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
                if (chatArea.isVisible()) {
                    chatArea.revalidate();
                    chatArea.repaint();
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                if (chatArea.isVisible()) {
                    chatArea.revalidate();
                    chatArea.repaint();
                }
            }
        });

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension frameSize = wrapper.getParent().getParent().getSize();
                int w = (int) Math.ceil(frameSize.getWidth() * .3f);

                userListPanel.setPreferredSize(new Dimension(w, (int) frameSize.getHeight()));
                userListPanel.revalidate();
                userListPanel.repaint();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }
        });
    }

    public void highlightLastPersonSpeaking() {
        String username = Client.INSTANCE.getUsername();
        if (username != null) {
            userListPanel.highlightUser(username);
        }
    }

    public String getLastPersonSpeaking() {
        return Client.INSTANCE.getUsername();
    }

    public void updatePersonColor(long clientId, Color newColor) {
        userListPanel.updateUserListItem(clientId, false, false, newColor);
    }

    public void highlightUsers(boolean isLastPersonSpeaking) {
        userListPanel.highlightUsers(isLastPersonSpeaking);
    }

    public void addUserListItem(long clientId, String clientName) {
        userListPanel.addUserListItem(clientId, clientName);
    }

    public void removeUserListItem(long clientId) {
        userListPanel.removeUserListItem(clientId);
    }

    public void clearUserList() {
        userListPanel.clearUserList();
    }

    public void addText(String text) {
        JEditorPane textContainer = new JEditorPane("text/html", text);
        textContainer.setLayout(null);
        textContainer.setPreferredSize(new Dimension(chatArea.getWidth(),
                ClientUtils.calcHeightForText(this, text, chatArea.getWidth())));
        textContainer.setMaximumSize(textContainer.getPreferredSize());
        textContainer.setEditable(false);
        ClientUtils.clearBackground(textContainer);
        chatArea.add(textContainer);

        JScrollBar vertical = ((JScrollPane) chatArea.getParent().getParent()).getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }
}
