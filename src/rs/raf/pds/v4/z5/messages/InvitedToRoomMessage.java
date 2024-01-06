package rs.raf.pds.v4.z5.messages;

public class InvitedToRoomMessage {
    private String message;
    private String room;

    public InvitedToRoomMessage() {
        // Default constructor for Kryo serialization
    }

    public InvitedToRoomMessage(String room, String message) {
        this.room = room;
        this.message = message;
    }

    public String getRoom() {
        return room;
    }
    public String getMessage() {
        return message;
    }
}