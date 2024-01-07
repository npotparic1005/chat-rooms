package rs.raf.pds.v4.z5.messages;

import java.util.UUID;

public class ChatMessage {
	private UUID messageId;
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
		this.messageId = UUID.randomUUID();
	}

	public ChatMessage(String user, String txt) {
		this.user = user;
		this.messageId = UUID.randomUUID();
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

	public UUID getMessageId() {
		return messageId;
	}

	public void editMessage(String newTxt) {
		this.txt = newTxt;
	}

	public void setTxt(String txt) {
		this.txt = txt;
	}
}
