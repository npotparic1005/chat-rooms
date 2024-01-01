package rs.raf.pds.v4.z5.messages;


public class CreateRoomRequest {
    private String roomName;

    public CreateRoomRequest() {
    }
    public CreateRoomRequest(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomName() {
        return roomName;
    }
}