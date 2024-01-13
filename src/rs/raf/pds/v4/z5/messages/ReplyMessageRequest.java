package rs.raf.pds.v4.z5.messages;

public class ReplyMessageRequest {
    private String replyMessage;
    private String originalMessage;

    public ReplyMessageRequest(String replyMessage, String originalMessage) {
        this.replyMessage = "Replying to: " + originalMessage + ": " + replyMessage;
        this.originalMessage = originalMessage;
    }

    public ReplyMessageRequest() {}

    public String getReplyMessage() {
        return replyMessage;
    }
}
