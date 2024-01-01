package rs.raf.pds.v4.z5.messages;

import java.util.List;

import com.esotericsoftware.kryonet.Connection;

public class ListRoomsRequest {
	private List<String> rooms;
	
	public ListRoomsRequest() {
		
	}
	public ListRoomsRequest(List<String> rooms) {
		this.rooms = rooms;
	}

	public List<String> getRooms() {
		return rooms;
	}
}