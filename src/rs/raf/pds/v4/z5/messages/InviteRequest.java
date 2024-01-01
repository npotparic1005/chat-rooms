package rs.raf.pds.v4.z5.messages;

public class InviteRequest {
    private String roomName;
    private String invitedUser;

    public InviteRequest() {
    }

    public InviteRequest(String roomName, String invitedUser) {
        this.roomName = roomName;
        this.invitedUser = invitedUser;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getInvitedUser() {
        return invitedUser;
    }

    public void setInvitedUser(String invitedUser) {
        this.invitedUser = invitedUser;
    }
}
