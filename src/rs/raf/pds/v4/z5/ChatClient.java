package rs.raf.pds.v4.z5;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import rs.raf.pds.v4.z5.messages.*;


public class ChatClient implements Runnable{

	public static int DEFAULT_CLIENT_READ_BUFFER_SIZE = 1000000;
	public static int DEFAULT_CLIENT_WRITE_BUFFER_SIZE = 1000000;

	private volatile Thread thread = null;

	volatile boolean running = false;

	final Client client;
	final String hostName;
	final int portNumber;
	final String userName;


	public ChatClient(String hostName, int portNumber, String userName) {
		this.client = new Client(DEFAULT_CLIENT_WRITE_BUFFER_SIZE, DEFAULT_CLIENT_READ_BUFFER_SIZE);

		this.hostName = hostName;
		this.portNumber = portNumber;
		this.userName = userName;
		KryoUtil.registerKryoClasses(client.getKryo());
		registerListener();
	}
	private void registerListener() {
		client.addListener(new Listener() {
			public void connected (Connection connection) {
				Login loginMessage = new Login(userName);
				client.sendTCP(loginMessage);
			}

			public void received (Connection connection, Object object) {
				if (object instanceof ChatMessage) {
					ChatMessage chatMessage = (ChatMessage)object;
					showMessage(chatMessage.getUser()+":"+chatMessage.getTxt());
					return;


				}

				if (object instanceof ListUsers) {
					ListUsers listUsers = (ListUsers)object;
					showOnlineUsers(listUsers.getUsers());
					return;
				}

				if (object instanceof InfoMessage) {
					InfoMessage message = (InfoMessage)object;
					showMessage("Server:"+message.getTxt());
					return;
				}

				/*if (object instanceof ChatMessage) {
					ChatMessage message = (ChatMessage)object;
					showMessage(message.getUser()+"r:"+message.getTxt());
					return;
				}
				*/
				/*
				if (object instanceof PrivateMessage) {
	                PrivateMessage privateMessage = (PrivateMessage) object;
	               if(privateMessage.getRecipient()==userName) {
	                showPrivateMessage(privateMessage);
	                }
	                return;
	            }
				*/
				if (object instanceof CreateRoomRequest) {
					// Obrada zahteva za kreiranje sobe
					CreateRoomRequest createRoomRequest = (CreateRoomRequest) object;
					createRoom(createRoomRequest.getRoomName());
					return;
				}
				if (object instanceof JoinRoomRequest) {
					// Obrada zahteva za pridruživanje sobi
					JoinRoomRequest joinRequest = (JoinRoomRequest) object;
					joinRoom(joinRequest.getRoomName());
					return;
				}

				if (object instanceof InviteRequest) {
					// Obrada zahteva za pozivnicu u sobu
					InviteRequest inviteRequest = (InviteRequest) object;
					inviteUserToRoom(inviteRequest.getRoomName(), inviteRequest.getInvitedUser());
					return;
				}

				if (object instanceof GetMoreMessagesRequest) {
					// Obrada zahteva za dobijanje dodatnih poruka
					GetMoreMessagesRequest getMoreMessagesRequest = (GetMoreMessagesRequest) object;
					getMoreMessages(getMoreMessagesRequest.getRoomName());
					return;
				}

				if(object instanceof ListRooms) {
					ListRooms listRooms=(ListRooms)object;
					listAllRooms(listRooms.getRooms());
					return;
				}
				if (object instanceof ChangeRoomRequest) {
					ChangeRoomRequest changeRoomRequest = (ChangeRoomRequest) object;
					changeRoom(changeRoomRequest.getRoomName());
					return;
				}

			}

			public void disconnected(Connection connection) {

			}
		});
	}
	private void showPrivateMessage(PrivateMessage privateMessage) {
		// Implementirajte logiku za prikazivanje privatnih poruka
		System.out.println("Private message from " + privateMessage.getSender() + ": " + privateMessage.getMessage());
	}
	private void showChatMessage(ChatMessage chatMessage) {
		System.out.println(chatMessage.getUser()+":"+chatMessage.getTxt());
	}
	private void listAllRooms(ArrayList<String> rooms) {
		System.out.print("Server:");
		for(String room:rooms) {
			System.out.println(room);
		}
	}
	private void showMessage(String txt) {
		System.out.println(txt);
	}
	private void showOnlineUsers(String[] users) {
		System.out.print("Server:");
		for (int i=0; i<users.length; i++) {
			String user = users[i];
			System.out.print(user);
			System.out.printf((i==users.length-1?"\n":", "));
		}
	}
	public void start() throws IOException {
		client.start();
		connect();

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

	public void connect() throws IOException {
		client.connect(1000, hostName, portNumber);
	}

	//novo
	private void sendPrivateMessage(String recipient, String txt) {
		PrivateMessage privateMessage = new PrivateMessage(userName, recipient, txt);
		client.sendTCP(privateMessage);
	}


	private void createRoom(String roomName) {
		CreateRoomRequest createRoomRequest = new CreateRoomRequest(roomName);
		client.sendTCP(createRoomRequest);
	}
	private void inviteUserToRoom(String roomName, String invitedUser) {
		InviteRequest inviteRequest = new InviteRequest(roomName, invitedUser);
		client.sendTCP(inviteRequest);
	}

	private void joinRoom(String roomName) {
		JoinRoomRequest joinRoomRequest = new JoinRoomRequest(roomName);
		client.sendTCP(joinRoomRequest);
	}
	private void getMoreMessages(String roomName) {
		GetMoreMessagesRequest getMoreMessagesRequest = new GetMoreMessagesRequest(roomName);
		client.sendTCP(getMoreMessagesRequest);
	}

	private void listRooms(List<String> list ) {
		System.out.print("Server:");
		for (String room : list) {
			System.out.print(room);
		}

	}
	public void changeRoom(String roomName) {
		client.sendTCP(new ChangeRoomRequest(roomName));
	}

	public void sendMessageToRoom(String roomName, String messageText) {
		ChatMessage chatMessage = new ChatMessage(userName, messageText, roomName);
		client.sendTCP(chatMessage);
	}

	public void run() {

		try (
				BufferedReader stdIn = new BufferedReader(
						new InputStreamReader(System.in))	// Za citanje sa standardnog ulaza - tastature!
		) {

			String userInput;
			running = true;

			while (running) {
				userInput = stdIn.readLine();
				if (userInput == null || "BYE".equalsIgnoreCase(userInput)) // userInput - tekst koji je unet sa tastature!
				{
					running = false;
				}
				else if ("WHO".equalsIgnoreCase(userInput)){
					client.sendTCP(new WhoRequest());
				}//nov
	            	/*
	            	else if (userInput.startsWith("PRIVATE")) {
	                    // Format: /PRIVATE @recipient_username @message
	                    String[] parts = userInput.split(" ", 3);
	                    if (parts.length == 3) {
	                        sendPrivateMessage(parts[1], parts[2]);
	                }*/
				else if (userInput.startsWith("CREATE")) {
					// Format: /CREATE @room_name
					String[] parts2 = userInput.split(" ", 2);
					if (parts2.length == 2) {
						createRoom(parts2[1]);
					}
				}
				else if (userInput.startsWith("INVITE")) {
					// Format: /INVITE @room_name @invited_user
					String[] parts3 = userInput.split(" ", 3);
					if (parts3.length == 3) {
						inviteUserToRoom(parts3[1], parts3[2]);
					}
				}
				else if (userInput.startsWith("JOIN")) {
					// Format: /JOIN @room_name
					String[] parts4 = userInput.split(" ", 2);
					if (parts4.length == 2) {
						joinRoom(parts4[1]);
					}
				}
				else if (userInput.startsWith("GETMOREMESSAGES")) {
					// Format: /GETMOREMESSAGES @room_name
					client.sendTCP(new GetMoreMessagesRequest());
				}
				else if (userInput.startsWith("LISTROOMS")) {
					// Format: /LISTROOMS
					client.sendTCP(new ListRoomsRequest());
				}
				else if (userInput.startsWith("CHANGE_ROOM")) {
					// Format: /CHANGE_ROOM @room_name
					String[] parts5 = userInput.split(" ", 2);
					if (parts5.length == 2) {
						changeRoom(parts5[1]);
					}
				}else if (userInput.startsWith("SEND")) {
					// Format: /SEND @room_name @message_text
					String[] parts = userInput.split(" ", 3);
					if (parts.length == 3) {
						ChatMessage chatmessage = new ChatMessage(userName,parts[2],parts[1]);
						client.sendTCP(chatmessage);
					}
				}
				else {
					ChatMessage chatmessage = new ChatMessage(userName,userInput);
					client.sendTCP(chatmessage);

				}
				if (!client.isConnected() && running)
					connect();

			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			running = false;
			System.out.println("CLIENT SE DISCONNECTUJE");
			client.close();;
		}
	}
	public static void main(String[] args) {
		if (args.length != 3) {

			System.err.println(
					"Usage: java -jar chatClient.jar <host name> <port number> <username>");
			System.out.println("Recommended port number is 54555");
			System.exit(1);
		}

		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);
		String userName = args[2];

		try{
			ChatClient chatClient = new ChatClient(hostName, portNumber, userName);
			chatClient.start();
		}catch(IOException e) {
			e.printStackTrace();
			System.err.println("Error:"+e.getMessage());
			System.exit(-1);
		}
	}
}
