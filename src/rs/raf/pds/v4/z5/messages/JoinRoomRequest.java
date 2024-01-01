package rs.raf.pds.v4.z5.messages;

public class JoinRoomRequest {
    private String roomName;

    public JoinRoomRequest() {
        // Prazan konstruktor za KryoNet
    }

    public JoinRoomRequest(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
