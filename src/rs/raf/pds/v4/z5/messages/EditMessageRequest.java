package rs.raf.pds.v4.z5.messages;

import java.util.UUID;

public class EditMessageRequest {
    private UUID messageId;
    private String newContent;

    public EditMessageRequest(UUID messageId, String newContent) {
        this.messageId = messageId;
        this.newContent = "Edited: " + newContent;
    }

    public EditMessageRequest() {
    }

    public UUID getMessageId() {
        return messageId;
    }

    public String getNewContent() {
        return newContent;
    }
}
