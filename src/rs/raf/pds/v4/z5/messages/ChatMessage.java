package rs.raf.pds.v4.z5.messages;

public class ChatMessage {
	String user;
	String txt;
	String recipient=null;
	String room=null;
	protected ChatMessage() {
		
	}
	public ChatMessage(String user, String txt,String room) {
		this.user = user;
		this.txt = txt;
		this.room=room;
	}
	public ChatMessage(String user, String txt) {
		this.user = user;
		this.txt = txt;
	}

	public String getUser() {
		return user;
	}

	public String getTxt() {
		return txt;
	}
	public String getRoom() {
		return room;
	}
	// nov konstruktor
   /* public ChatMessage(String user, String recipient, String txt) {
        this.user = user;
        this.recipient = recipient;
        this.txt = txt;
    }
    */
	
}
