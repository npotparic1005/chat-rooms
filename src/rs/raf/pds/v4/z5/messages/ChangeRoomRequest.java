package rs.raf.pds.v4.z5.messages;

public class ChangeRoomRequest {
    private String roomName;
    
    public ChangeRoomRequest() {
    }


    public ChangeRoomRequest(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomName() {
        return roomName;
    }
}