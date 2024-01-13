package rs.raf.pds.v4.z5.messages;

import java.util.UUID;

public class EditedMessage {
    private UUID messageId;
    private String newContent;

    public EditedMessage(UUID messageId, String newContent) {
        this.messageId = messageId;
        this.newContent = newContent;
    }

    public EditedMessage() {
    }

    public UUID getMessageId() {
        return messageId;
    }

    public String getNewContent() {
        return newContent;
    }
}