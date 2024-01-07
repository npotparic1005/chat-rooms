package rs.raf.pds.v4.z5.ui;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import rs.raf.pds.v4.z5.messages.*;

import java.io.IOException;
import java.util.Optional;

public class ChatApplication extends Application {
    private ChatApplicationClient chatClient;
    private Stage primaryStage;
    private String userName;
    private String joinedRoom;
    private TextArea infoMessageArea;
    private TextField inputField, joinRoomField, createRoomField, inviteToRoomField;
    private Button sendButton, joinRoomButton, createRoomButton, listRoomsButton, getMoreMessagesButton, inviteToRoomButton;
    private ListView<ChatMessageItem> messageListView;

    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(false);
        this.primaryStage = primaryStage;
        showLoginScreen();
    }

    private void showLoginScreen() {
        Label usernameLabel = new Label("Enter Username:");
        TextField usernameField = new TextField();
        Button loginButton = new Button("Login");
        loginButton.setOnAction(event -> setUser(usernameField.getText()));

        VBox loginLayout = new VBox(10, usernameLabel, usernameField, loginButton);
        loginLayout.setAlignment(Pos.CENTER);
        Scene loginScene = new Scene(loginLayout, 300, 200);

        primaryStage.setTitle("Chat Application - Login");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private void setUser(String username) {
        if (username != null && !username.trim().isEmpty()) {
            this.userName = username.trim();

            initializeChatClient();
            showChatScreen();
        }
    }

    private void showChatScreen() {
        infoMessageArea = new TextArea();
        infoMessageArea.setEditable(false);

        messageListView = new ListView<>();
        messageListView.setCellFactory(param -> new ListCell<ChatMessageItem>() {
            @Override
            protected void updateItem(ChatMessageItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getUser() + ": " + item.getContent());
                    if (item.isOwnMessage()) {
                        ContextMenu contextMenu = new ContextMenu();
                        MenuItem editItem = new MenuItem("Edit");
                        editItem.setOnAction(event -> editMessage(item));
                        contextMenu.getItems().add(editItem);
                        setContextMenu(contextMenu);
                    }
                }
            }
        });

        inputField = new TextField();
        sendButton = new Button("Send");
        sendButton.setOnAction(event -> sendMessage());

        joinRoomField = new TextField();
        joinRoomField.setPromptText("Enter Room Name to Join");
        joinRoomButton = new Button("Join Room");
        joinRoomButton.setOnAction(event -> joinRoom(joinRoomField.getText()));

        createRoomField = new TextField();
        createRoomField.setPromptText("Enter Room Name to Create");
        createRoomButton = new Button("Create Room");
        createRoomButton.setOnAction(event -> createRoom(createRoomField.getText()));

        inviteToRoomField = new TextField();
        inviteToRoomField.setPromptText("Enter User to Invite");
        inviteToRoomButton = new Button("Invite User to Current Room");
        inviteToRoomButton.setOnAction(event -> inviteToRoom(inviteToRoomField.getText()));

        listRoomsButton = new Button("List Room");
        listRoomsButton.setOnAction(event -> listRooms());

        getMoreMessagesButton = new Button("Get More Messages");
        getMoreMessagesButton.setOnAction(event -> getMoreMessages());

        HBox roomControls = new HBox(10, joinRoomField, joinRoomButton, createRoomField, createRoomButton, listRoomsButton);
        HBox invitationControls = new HBox(10, inviteToRoomField, inviteToRoomButton);
        HBox messagesControls = new HBox(10, getMoreMessagesButton);
        VBox chatLayout = new VBox(10, roomControls, invitationControls, messagesControls, infoMessageArea ,messageListView, inputField, sendButton);
        chatLayout.setAlignment(Pos.CENTER);

        Scene chatScene = new Scene(chatLayout, 600, 500);
        primaryStage.setTitle("Chat Application");
        primaryStage.setScene(chatScene);
    }

    private void editMessage(ChatMessageItem messageItem) {
        TextInputDialog dialog = new TextInputDialog(messageItem.getContent());
        dialog.setTitle("Edit Message");
        dialog.setHeaderText("Enter new message");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newMessage -> {
            chatClient.sendEditMessageRequest(messageItem.getMessageId(), newMessage);
        });
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty() && joinedRoom != null && !joinedRoom.isEmpty()) {
            chatClient.sendMessageToRoom(joinedRoom, message);
            inputField.clear();
        }
    }

    private void joinRoom(String roomName) {
        if (roomName != null && !roomName.trim().isEmpty()) {
            chatClient.joinRoom(roomName);
            joinedRoom = roomName;
        }
    }

    private void createRoom(String roomName) {
        if (roomName != null && !roomName.trim().isEmpty()) {
            chatClient.createRoom(roomName);
            joinedRoom = roomName;
        }
    }

    private void inviteToRoom(String userToInvite) {
        if (userToInvite != null && !userToInvite.trim().isEmpty() && joinedRoom != null) {
            chatClient.inviteToRoom(joinedRoom, userToInvite);
        }
    }

    private void listRooms() {
        chatClient.listRooms();
    }

    private void getMoreMessages() {
        if (joinedRoom != null) {
            chatClient.getMoreMessages(joinedRoom);
        }
    }

    private void updateMessageUI(EditedMessage editedMessage) {
        Platform.runLater(() -> {
            ObservableList<ChatMessageItem> items = messageListView.getItems();
            for (ChatMessageItem item : items) {
                if (item.getMessageId().equals(editedMessage.getMessageId())) {
                    item.setContent(editedMessage.getNewContent());
                    break;
                }
            }
            messageListView.refresh();
        });
    }

    private void displayInfoMessage(String message) {
        infoMessageArea.appendText(message + "\n");
    }

    private void displayChatMessage(ChatMessage chatMessage) {
        boolean isOwnMessage = userName.equals(chatMessage.getUser());
        ChatMessageItem messageItem = new ChatMessageItem(chatMessage, isOwnMessage);
        messageListView.getItems().add(messageItem);
    }

    private void initializeChatClient() {
        String hostName = "localhost";
        int portNumber = 4555;

        new Thread(() -> {
            try {
                chatClient = new ChatApplicationClient(hostName, portNumber, userName);
                chatClient.connect(object -> Platform.runLater(() -> {
                    System.out.println(object);
                    if (object instanceof InfoMessage) {
                        System.out.println(((InfoMessage) object).getTxt());
                        InfoMessage message = (InfoMessage) object;
                        displayInfoMessage(message.getTxt());
                        return;
                    }

                    if (object instanceof ChatMessage) {
                        ChatMessage chatMessage = (ChatMessage) object;
                        displayChatMessage(chatMessage);
                        return;
                    }

                    if (object instanceof ListRooms) {
                        ListRooms listRoomsMessage = (ListRooms) object;
                        displayInfoMessage(listRoomsMessage.getRooms().toString());
                        return;
                    }

                    if (object instanceof InvitedToRoomMessage) {
                        InvitedToRoomMessage invitedToRoomMessage = (InvitedToRoomMessage) object;
                        displayInfoMessage(invitedToRoomMessage.getMessage());
                        joinedRoom = invitedToRoomMessage.getRoom();
                    }

                    if (object instanceof EditedMessage) {
                        EditedMessage editedMessage = (EditedMessage) object;
                        updateMessageUI(editedMessage);
                    }
                }));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
