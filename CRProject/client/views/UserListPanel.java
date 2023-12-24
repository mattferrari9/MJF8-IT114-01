package CRProject.client.views;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import CRProject.client.ClientUtils;
import CRProject.client.ICardControls;

public class UserListPanel extends JPanel {
    private JPanel userListArea;
    private static Logger logger = Logger.getLogger(UserListPanel.class.getName());
    private Map<Long, JEditorPane> userItems;

    public UserListPanel(ICardControls controls) {
        super(new BorderLayout(10, 10));
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        userListArea = content;
        userItems = new HashMap<>();

        wrapper.add(scroll);
        this.add(wrapper, BorderLayout.CENTER);

        userListArea.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
                if (userListArea.isVisible()) {
                    userListArea.revalidate();
                    userListArea.repaint();
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                if (userListArea.isVisible()) {
                    userListArea.revalidate();
                    userListArea.repaint();
                }
            }
        });
    }

    protected void addUserListItem(long clientId, String clientName) {
        logger.log(Level.INFO, "Adding user to list: " + clientName);

        JEditorPane textContainer = new JEditorPane("text/plain", clientName);
        textContainer.setName(clientId + "");
        textContainer.setPreferredSize(new Dimension(userListArea.getWidth(),
                ClientUtils.calcHeightForText(this, clientName, userListArea.getWidth())));
        textContainer.setMaximumSize(textContainer.getPreferredSize());
        textContainer.setEditable(false);

        ClientUtils.clearBackground(textContainer);
        userListArea.add(textContainer);

        userItems.put(clientId, textContainer);
    }

    protected void removeUserListItem(long clientId) {
        logger.log(Level.INFO, "Removing user list item for id " + clientId);
        JEditorPane userItem = userItems.remove(clientId);
        if (userItem != null) {
            userListArea.remove(userItem);
        }
    }

    protected void updateUserListItem(long clientId, boolean isMuted, boolean isHighlighted, Color newColor) {
        logger.log(Level.INFO, "Updating user list item for id " + clientId);
        JEditorPane userItem = userItems.get(clientId);

        if (userItem != null) {
            userItem.setForeground(isMuted ? Color.GRAY : newColor);
            userItem.setBackground(isHighlighted ? Color.YELLOW : null);
        }
    }

    protected void resetUserHighlights() {
        for (JEditorPane userItem : userItems.values()) {
            userItem.setBackground(null);
        }
    }

    protected void highlightUsers(boolean isLastPersonSpeaking) {
        if (isLastPersonSpeaking) {
            for (JEditorPane userItem : userItems.values()) {
                if (!userItem.getForeground().equals(Color.GRAY)) {
                    userItem.setBackground(Color.YELLOW);
                }
            }
        }
    }

    protected void clearUserList() {
        userListArea.removeAll();
        userItems.clear();
    }

    // New method to highlight a specific user
    public void highlightUser(String username) {
        for (JEditorPane userItem : userItems.values()) {
            if (userItem.getName().equals(username)) {
                userItem.setBackground(Color.YELLOW);
            } else {
                userItem.setBackground(null);
            }
        }
    }
}
