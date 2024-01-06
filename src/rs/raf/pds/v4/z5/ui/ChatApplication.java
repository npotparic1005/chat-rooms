package rs.raf.pds.v4.z5.ui;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import rs.raf.pds.v4.z5.ui.ChatApplicationClient;

import java.io.IOException;

public class ChatApplication extends Application {
    private ChatApplicationClient chatClient;
    private Stage primaryStage;
    private String userName;
    private String joinedRoom;
    private TextArea messageArea;private TextField inputField, joinRoomField, createRoomField;
    private Button sendButton, joinRoomButton, createRoomButton, listRoomsButton;
    private ComboBox<String> roomList;

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
        messageArea = new TextArea();
        messageArea.setEditable(false);

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

        listRoomsButton = new Button("List Room");
        listRoomsButton.setOnAction(event -> listRooms());

        HBox roomControls = new HBox(10, joinRoomField, joinRoomButton, createRoomField, createRoomButton, listRoomsButton);
        VBox chatLayout = new VBox(10, roomControls, messageArea, inputField, sendButton);
        chatLayout.setAlignment(Pos.CENTER);

        Scene chatScene = new Scene(chatLayout, 600, 500);
        primaryStage.setTitle("Chat Application");
        primaryStage.setScene(chatScene);
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
        }
    }

    private void listRooms() {
        chatClient.listRooms();
    }

    private void initializeChatClient() {
        String hostName = "localhost";
        int portNumber = 4555;

        new Thread(() -> {
            try {
                chatClient = new ChatApplicationClient(hostName, portNumber, userName);
                chatClient.connect(message -> Platform.runLater(() -> messageArea.appendText(message + "\n")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
