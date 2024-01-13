package rs.raf.pds.v4.z5.ui;

import rs.raf.pds.v4.z5.messages.ChatMessage;

import java.util.UUID;

public class ChatMessageItem {
    private UUID messageId;
    private String content;

    private String user;
    private boolean isOwnMessage;

    public ChatMessageItem(ChatMessage chatMessage, boolean isOwnMessage) {
        this.messageId = chatMessage.getMessageId();
        this.content = chatMessage.getTxt();
        this.user = chatMessage.getUser();
        this.isOwnMessage = isOwnMessage;
    }

    public UUID getMessageId() { return messageId; }
    public String getContent() { return content; }
    public String getUser() { return user; }
    public boolean isOwnMessage() { return isOwnMessage; }
    public void setContent(String newMessage) { this.content = newMessage; }
}
