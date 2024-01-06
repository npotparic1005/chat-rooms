package rs.raf.pds.v4.z5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import rs.raf.pds.v4.z5.messages.*;

public class ChatServer implements Runnable{

	private volatile Thread thread = null;

	volatile boolean running = false;
	final Server server;
	final int portNumber;
	Map<String, Room> rooms = new HashMap<>();
	List<String> roomNames = new ArrayList<>();
	ConcurrentMap<String, Connection> userConnectionMap = new ConcurrentHashMap<String, Connection>();
	ConcurrentMap<Connection, String> connectionUserMap = new ConcurrentHashMap<Connection, String>();
	Map<Connection, ChatClient> connectionClientMap;
	Map<String, String> userRoomMap = new HashMap<String,String>();

	public ChatServer(int portNumber) {
		this.server = new Server();

		this.portNumber = portNumber;
		KryoUtil.registerKryoClasses(server.getKryo());
		registerListener();
	}
	private void registerListener() {
		server.addListener(new Listener() {
			public void received (Connection connection, Object object) {
				if (object instanceof Login) {
					Login login = (Login)object;
					newUserLogged(login, connection);
					connection.sendTCP(new InfoMessage("Hello "+login.getUserName()));
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return;
				}

				if (object instanceof ChatMessage) {
					ChatMessage chatMessage = (ChatMessage)object;
					System.out.println(chatMessage.getUser() + ": " + chatMessage.getTxt() + ": " + chatMessage.getRoom());
					broadcastChatMessageinRoom(chatMessage, connection);

					Room room = rooms.get(chatMessage.getRoom());
					room.addMessage(chatMessage);

					return;
				}

				if (object instanceof WhoRequest) {
					ListUsers listUsers = new ListUsers(getAllUsers());
					connection.sendTCP(listUsers);
					return;
				}
				/*
				if (object instanceof PrivateMessage) {
	                PrivateMessage privateMessage = (PrivateMessage) object;
	                System.out.println(privateMessage.getSender()+":"+privateMessage.getMessage());
	                handlePrivateMessage(privateMessage, connection);
	                return;
	            }
	            */
				if (object instanceof CreateRoomRequest) {
					CreateRoomRequest createRoomRequest = (CreateRoomRequest) object;
					Room room = new Room(createRoomRequest.getRoomName());
					createRoom(room, connection);
					return;
				}
				if (object instanceof InviteRequest) {
					InviteRequest inviteRequest = (InviteRequest) object;
					String roomName = inviteRequest.getRoomName();
					String invitedUser = inviteRequest.getInvitedUser();
					addUserToRoom(roomName, invitedUser, connection);
					return;
				}
				if (object instanceof JoinRoomRequest) {
					JoinRoomRequest joinRoomRequest = (JoinRoomRequest) object;
					joinRoom(joinRoomRequest.getRoomName(), connection);
					return;
				}
				if (object instanceof GetMoreMessagesRequest) {
					GetMoreMessagesRequest moreMessagesRequest = (GetMoreMessagesRequest) object;
					String roomName = moreMessagesRequest.getRoomName();
					Room room = rooms.get(roomName);
					if (room != null) {
						List<ChatMessage> moreMessages = room.getMoreMessages();
						for ( ChatMessage message: moreMessages){
							connection.sendTCP(message);
							connection.sendTCP(new InfoMessage("\n"));
						}

					}
				}
				
				if(object instanceof ListRoomsRequest) {
					ListRooms listRooms=new ListRooms(listAllRooms());
					connection.sendTCP(listRooms);
					return;
				}
				if (object instanceof ChangeRoomRequest) {
					ChangeRoomRequest changeRoomRequest = (ChangeRoomRequest) object;
					changeUserRoom(connection, changeRoomRequest.getRoomName());
					return;
				}

			}

			public void disconnected(Connection connection) {
				String user = connectionUserMap.get(connection);
				connectionUserMap.remove(connection);
				userConnectionMap.remove(user);
				showTextToAll(user+" has disconnected!", connection);
			}
		});
	}

	String[] getAllUsers() {
		String[] users = new String[userConnectionMap.size()];
		int i=0;
		for (String user: userConnectionMap.keySet()) {
			users[i] = user;
			i++;
		}

		return users;
	}
	void newUserLogged(Login loginMessage, Connection conn) {
		userConnectionMap.put(loginMessage.getUserName(), conn);
		connectionUserMap.put(conn, loginMessage.getUserName());
		userRoomMap.put(loginMessage.getUserName(),null);
		showTextToAll("User "+loginMessage.getUserName()+" has connected!", conn);
	}
	private void broadcastChatMessage(ChatMessage message, Connection exception) {

		for (Connection conn: userConnectionMap.values()) {
			if (conn.isConnected() && conn != exception)
				conn.sendTCP(message);
		}
	}
	private void broadcastChatMessageinRoom(ChatMessage message, Connection connection) {
		String username = connectionUserMap.get(connection);
		String roomName=message.getRoom();
		Room room=rooms.get(roomName);
		List<Connection> users=room.getUsers();
		for (Connection conn: users) {
			conn.sendTCP(message);
		}
	}

	ArrayList<String> listAllRooms() {
		ArrayList<String> roomList=new ArrayList<>();
		for(Room chatRoom: rooms.values()) {
			roomList.add(chatRoom.getName());
		}
		return roomList;

	}
	private void addUserToRoom(String roomName, String userName, Connection inviterConnection) {
		Room room = rooms.get(roomName);
		Connection invitedConnection = userConnectionMap.get(userName);

		if (room != null) {
			room.addUser(invitedConnection);
			invitedConnection.sendTCP(new InfoMessage("dodati ste u sobu "+ roomName));
			inviterConnection.sendTCP(new InfoMessage("User " + userName + " invited to room " + roomName));
		} else {
			inviterConnection.sendTCP(new InfoMessage("Room " + roomName + " not found."));
		}
	}

	private void joinRoom(String roomName, Connection connection) {
		if (rooms.containsKey(roomName)) {
			Room room = rooms.get(roomName);
			List<Connection> users=room.getUsers();

			if (!(users.contains(connection))) {
				users.add(connection);
				connection.sendTCP(new InfoMessage("You joined room '" + roomName + "'."));
				List<ChatMessage> lastMessages = getLastMessagesForRoom(roomName);
				if(!(lastMessages.size()==0)) {
					for (ChatMessage message : lastMessages) {
						connection.sendTCP(message);
					}
				}
				else {
					connection.sendTCP(new InfoMessage("There are less than 5 messages in a room '" + roomName + "'."));
				}
				for (Connection conn: users) {
					if (conn.isConnected() && conn != connection)
						conn.sendTCP(new InfoMessage(connectionUserMap.get(connection) + " joined the room."));
				}
			} else {
				connection.sendTCP(new InfoMessage("You are already in room '" + roomName + "'."));
			}
		} else {
			connection.sendTCP(new InfoMessage("Room '" + roomName + "' does not exist."));
		}
	}
	private void changeUserRoom(Connection connection, String roomName) {
		String username = connectionUserMap.get(connection);
		Room room=rooms.get(roomName);
		List<Connection> users=room.getUsers();

		if (users.contains(connection)) {
			userRoomMap.put(username,roomName);
			connection.sendTCP(new InfoMessage("Uspesno ste promenili sobu u '" + roomName + "'."));
		}else {
			connection.sendTCP(new InfoMessage("Niste clan ove sobe, molim vas pridruzite se pre nego sto pokusate ponovo!" ));
		}
	}
	private void createRoom(Room room, Connection creatorConnection) {
		if (rooms.containsKey(room.getName())) {
			creatorConnection.sendTCP(new InfoMessage("Room '" + room.getName() + "' already exists."));
			return;
		}

		rooms.put(room.getName(), room);
		roomNames.add(room.getName());

		creatorConnection.sendTCP(new InfoMessage("Room '" + room.getName() + "' created successfully."));
		room.addUser(creatorConnection);

	}
	private List<ChatMessage> getLastMessagesForRoom(String roomName) {

		Room room=rooms.get(roomName);
		return room.getLastFiveMessages();

	}
	private void handlePrivateMessage(PrivateMessage privateMessage, Connection senderConnection) {
		String senderUsername = connectionUserMap.get(senderConnection);

		String recipientUsername = privateMessage.getRecipient();
		String messageText = privateMessage.getMessage();

		Connection recipientConnection = userConnectionMap.get(recipientUsername);

		if (recipientConnection != null && recipientConnection.isConnected()) {
			// Pošalji privatnu poruku primaocu
			recipientConnection.sendTCP(new InfoMessage("dobili ste poruku "));
			recipientConnection.sendTCP(new PrivateMessage(senderUsername, recipientUsername, messageText));
			recipientConnection.sendTCP(new InfoMessage("dobili ste poruku od"+senderUsername +"koja glasi"+" " +messageText));

			// Potvrdi pošiljaocu da je poruka uspešno poslata
			senderConnection.sendTCP(new InfoMessage("Your private message to " + recipientUsername + " has been sent."));
		} else {
			// Obavesti pošiljaoca da primalac nije dostupan
			senderConnection.sendTCP(new InfoMessage("User " + recipientUsername + " is not currently online."));
		}
	}

	private void showTextToAll(String txt, Connection exception) {
		System.out.println(txt);
		for (Connection conn: userConnectionMap.values()) {
			if (conn.isConnected() && conn != exception)
				conn.sendTCP(new InfoMessage(txt));
		}
	}
	public void start() throws IOException {
		server.start();
		server.bind(portNumber);

		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}
	public void stop() {
		Thread stopThread = thread;
		thread = null;
		running = false;
		if (stopThread != null)
			stopThread.interrupt();
	}
	@Override
	public void run() {
		running = true;

		while(running) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	public static void main(String[] args) {

		if (args.length != 1) {
			System.err.println("Usage: java -jar chatServer.jar <port number>");
			System.out.println("Recommended port number is 54555");
			System.exit(1);
		}

		int portNumber = Integer.parseInt(args[0]);
		try {
			ChatServer chatServer = new ChatServer(portNumber);
			chatServer.start();

			chatServer.thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



}
