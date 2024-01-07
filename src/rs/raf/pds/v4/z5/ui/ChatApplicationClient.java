package rs.raf.pds.v4.z5.ui;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.UUID;
import java.util.function.Consumer;
import java.io.IOException;
import rs.raf.pds.v4.z5.messages.*;

public class ChatApplicationClient {
    private Client client;
    private String userName;

    public ChatApplicationClient(String host, int port, String userName) throws IOException {
        this.userName = userName;
        client = new Client();
        client.start();
        KryoUtil.registerKryoClasses(client.getKryo());

        client.connect(5000, host, port);
    }

    public void connect(Consumer<Object> onMessageReceived) throws IOException {
        client.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                onMessageReceived.accept(object);
            }
        });

        sendLogin();
    }

    private void sendLogin() {
        Login login = new Login(this.userName);
        client.sendTCP(login);
    }

    public void sendMessageToRoom(String roomName, String messageText) {
        ChatMessage chatMessage = new ChatMessage(userName, messageText, roomName);
        client.sendTCP(chatMessage);
    }

    public void joinRoom(String roomName) {
        JoinRoomRequest joinRoom = new JoinRoomRequest(roomName);
        client.sendTCP(joinRoom);
    }

    public void createRoom(String roomName) {
        CreateRoomRequest createRoomRequest = new CreateRoomRequest(roomName);
        client.sendTCP(createRoomRequest);
    }

    public void listRooms() {
        ListRoomsRequest listRoomsRequest = new ListRoomsRequest();
        client.sendTCP(listRoomsRequest);
    }

    public void inviteToRoom(String roomName, String userToInvite) {
        InviteRequest inviteRequest = new InviteRequest(roomName, userToInvite);
        client.sendTCP(inviteRequest);
    }

    public void getMoreMessages(String roomName) {
        GetMoreMessagesRequest getMoreMessagesRequest = new GetMoreMessagesRequest(roomName);
        client.sendTCP(getMoreMessagesRequest);
    }

    public void sendEditMessageRequest(UUID messageId, String newMessage) {
        EditMessageRequest editMessageRequest = new EditMessageRequest(messageId, newMessage);
        client.sendTCP(editMessageRequest);
    }

    public void sendReplyMessageRequest(String replyMessage, String originalMessage) {
        ReplyMessageRequest replyMessageRequest = new ReplyMessageRequest(replyMessage, originalMessage);
        client.sendTCP(replyMessageRequest);
    }

    public void disconnect() {
        client.stop();
    }
}
