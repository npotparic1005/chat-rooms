package rs.raf.pds.v4.z5.messages;

import java.util.*;

import com.esotericsoftware.kryonet.Connection;

public class Room {
    private String roomName;
    private List<Connection> users;
    private List<ChatMessage> messages;

    public Room(String roomName) {
        this.roomName = roomName;
        this.users = new ArrayList<>();
        this.messages = new LinkedList<>();
    }

    public String getName() {
        return roomName;
    }

    public List<Connection> getUsers() {
        return users;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

	public void addUser(Connection userName) {
		users.add(userName);
		
	}
	public List<ChatMessage> getLastMessages(int count) {
        // Konvertujemo Deque u List kako bismo dobili jednostavan pristup elementima
        List<ChatMessage> allMessages = new ArrayList<>(messages);
        
        // Ako je broj poruka manji od traženog broja, vraćamo sve poruke
        if (allMessages.size() <= count) {
            return allMessages;
        }

        // Ako je broj poruka veći od traženog broja, vraćamo poslednjih 'count' poruka
        return allMessages.subList(allMessages.size() - count, allMessages.size());
    }

    public List<ChatMessage> getLastFiveMessages() {
        return getLastMessages(5);
    }

    public List<ChatMessage> getMoreMessages() {
        return getLastMessages(20);
    }

	public void addMessage(ChatMessage message) {
		messages.add(message);
	}

    public ChatMessage getMessageById(UUID messageId) {
        for (ChatMessage message : messages) {
            if (message.getMessageId().equals(messageId)) {
                return message;
            }
        }
        return null;
    }
    // Dodajte metode za dodavanje korisnika i poruka po potrebi
}