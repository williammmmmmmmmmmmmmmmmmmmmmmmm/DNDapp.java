package org.example.dndapp;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class CampaignsPage {

    private Label statusIndicator;
    private Label statusText;
    private TextField roomNameInput;
    private PasswordField passwordInput;
    private ListView<String> messageLog;
    private ListView<String> roomList;
    private TextField messageBox;

    private String currentRoom = null; // New variable to track the current room name

    private Stage primaryStage;
    private Scene homeScene;
    private WebSocketService webSocketService;

    public CampaignsPage(Stage primaryStage, Scene homeScene, WebSocketService webSocketService) {
        this.primaryStage = primaryStage;
        this.homeScene = homeScene;
        this.webSocketService = webSocketService;
    }

    public Scene createScene() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000;");

        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(50, 20, 20, 20));
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 10; -fx-border-color: #ff0000; -fx-border-width: 2; -fx-border-radius: 10;");

        Label title = new Label("Game Room Client");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        Label subtitle = new Label("Connect to your WebSocket server and manage game rooms.");
        subtitle.setFont(Font.font("Inter", FontWeight.NORMAL, 12));
        subtitle.setTextFill(Color.web("#d3d3d3"));

        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPadding(new Insets(10));
        statusBox.setStyle("-fx-background-color: #333; -fx-background-radius: 5;");
        statusIndicator = new Label("●");
        statusIndicator.setFont(Font.font(20));
        statusIndicator.setTextFill(Color.GRAY);
        statusText = new Label("Connecting...");
        statusText.setTextFill(Color.web("#d3d3d3"));
        statusBox.getChildren().addAll(statusIndicator, statusText);

        GridPane roomManagementGrid = new GridPane();
        roomManagementGrid.setVgap(10);
        roomManagementGrid.setHgap(10);
        roomManagementGrid.setPadding(new Insets(15));
        roomManagementGrid.setStyle("-fx-background-color: #333; -fx-background-radius: 5;");

        Label roomNameLabel = new Label("Room Name");
        roomNameLabel.setTextFill(Color.web("#d3d3d3"));
        roomNameInput = new TextField();
        roomNameInput.setPromptText("Enter room name");
        roomNameInput.setStyle("-fx-background-color: #444; -fx-text-fill: white;");

        Label passwordLabel = new Label("Password (Optional)");
        passwordLabel.setTextFill(Color.web("#d3d3d3"));
        passwordInput = new PasswordField();
        passwordInput.setPromptText("Enter password");
        passwordInput.setStyle("-fx-background-color: #444; -fx-text-fill: white;");

        Button createBtn = new Button("Create Room");
        createBtn.setStyle("-fx-background-color: #ff0000; -fx-text-fill: white;");
        createBtn.setOnAction(e -> handleCreateRoom());

        Button joinBtn = new Button("Join Room");
        joinBtn.setStyle("-fx-background-color: #ff0000; -fx-text-fill: white;");
        joinBtn.setOnAction(e -> handleJoinRoom());

        Button leaveBtn = new Button("Leave Room");
        leaveBtn.setStyle("-fx-background-color: #ff0000; -fx-text-fill: white;");
        leaveBtn.setOnAction(e -> handleLeaveRoom());

        roomManagementGrid.add(roomNameLabel, 0, 0);
        roomManagementGrid.add(roomNameInput, 1, 0);
        roomManagementGrid.add(passwordLabel, 0, 1);
        roomManagementGrid.add(passwordInput, 1, 1);
        HBox buttonBox = new HBox(10, createBtn, joinBtn, leaveBtn);
        roomManagementGrid.add(buttonBox, 0, 2, 2, 1);

        HBox listLogsBox = new HBox(15);
        listLogsBox.setAlignment(Pos.TOP_CENTER);

        VBox roomListBox = new VBox(5);
        Label roomListLabel = new Label("Available Rooms");
        roomListLabel.setTextFill(Color.web("#ff0000"));
        roomList = new ListView<>();
        roomList.setStyle("-fx-background-color: #333; -fx-control-inner-background: #333; -fx-text-fill: #d3d3d3;");
        roomListBox.getChildren().addAll(roomListLabel, roomList);

        VBox messageLogBox = new VBox(5);
        Label messageLogLabel = new Label("Server Messages");
        messageLogLabel.setTextFill(Color.web("#ff0000"));
        messageLog = new ListView<>();
        messageLog.setStyle("-fx-background-color: #333; -fx-control-inner-background: #333; -fx-text-fill: #d3d3d3;");

        // Add message box and send button
        messageBox = new TextField();
        messageBox.setPromptText("Type your message here...");
        messageBox.setStyle("-fx-background-color: #444; -fx-text-fill: white;");
        Button sendButton = new Button("Send");
        sendButton.setStyle("-fx-background-color: #ff0000; -fx-text-fill: white;");
        sendButton.setOnAction(e -> handleSendMessage());

        HBox messageInputBox = new HBox(5, messageBox, sendButton);
        HBox.setHgrow(messageBox, Priority.ALWAYS);

        messageLogBox.getChildren().addAll(messageLogLabel, messageLog, messageInputBox);

        listLogsBox.getChildren().addAll(roomListBox, messageLogBox);
        HBox.setHgrow(roomListBox, Priority.ALWAYS);
        HBox.setHgrow(messageLogBox, Priority.ALWAYS);

        mainContent.getChildren().addAll(title, subtitle, statusBox, roomManagementGrid, listLogsBox);

        Button backButton = new Button("Go Back");
        backButton.setStyle("-fx-background-color: #ff0000; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 20; -fx-padding: 5 15;");
        backButton.setOnAction(e -> primaryStage.setScene(homeScene));
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(15));

        root.getChildren().addAll(mainContent, backButton);

        // Bind UI updates to WebSocketService messages
        webSocketService.setOnMessageReceived(this::handleMessage);

        return new Scene(root);
    }

    private void handleMessage(String message) {
        Platform.runLater(() -> {
            if (message.startsWith("ROOMLIST:")) {
                updateRoomList(message.substring("ROOMLIST:".length()));
            } else if (message.startsWith("ERROR:")) {
                addMessage(message.substring("ERROR:".length()), Color.web("#ff4c4c"));
            } else if (message.startsWith("SUCCESS:")) {
                // Update the currentRoom variable when a successful join message is received
                this.currentRoom = message.substring(message.lastIndexOf(":") + 1);
                addMessage(message.substring("SUCCESS:".length()), Color.web("#80ff80"));
            } else {
                addMessage(message, Color.web("#d3d3d3"));
            }
        });
    }

    private void updateRoomList(String roomListString) {
        roomList.getItems().clear();
        String[] rooms = roomListString.split(",");
        if (rooms.length == 0 || (rooms.length == 1 && rooms[0].isEmpty())) {
            roomList.getItems().add("No rooms available.");
        } else {
            for (String room : rooms) {
                roomList.getItems().add(room);
            }
        }
    }

    private void addMessage(String text, Color color) {
        messageLog.getItems().add(text);
        messageLog.scrollTo(messageLog.getItems().size() - 1);
    }

    private void sendMessage(String command, String roomName, String password) {
        String message = command + ":" + roomName + ":" + password;
        webSocketService.sendMessage(message);
        addMessage("Sending: " + message, Color.web("#4c80ff"));
    }

    private void handleCreateRoom() {
        String roomName = roomNameInput.getText().trim();
        String password = passwordInput.getText().trim();
        if (!roomName.isEmpty()) {
            sendMessage("CREATE", roomName, password);
        } else {
            addMessage("Please enter a room name.", Color.web("#ff4c4c"));
        }
    }

    private void handleJoinRoom() {
        String roomName = roomNameInput.getText().trim();
        String password = passwordInput.getText().trim();
        if (!roomName.isEmpty()) {
            sendMessage("JOIN", roomName, password);
        } else {
            addMessage("Please enter a room name.", Color.web("#ff4c4c"));
        }
    }

    private void handleLeaveRoom() {
        String roomName = roomNameInput.getText().trim();
        if (!roomName.isEmpty()) {
            sendMessage("LEAVE", roomName, "");
            addMessage("You have left the room.", Color.web("#ffb366"));
        } else {
            addMessage("No room to leave. Please enter a room name.", Color.web("#ff4c4c"));
        }
    }

    private void handleSendMessage() {
        String message = messageBox.getText().trim();
        if (!message.isEmpty() && this.currentRoom != null) {
            // The message is now correctly formatted for the server: CHAT:roomName:message
            webSocketService.sendMessage("CHAT:" + this.currentRoom + ":" + message);
            addMessage("You: " + message, Color.web("#4c80ff"));
            messageBox.clear();
        } else if (this.currentRoom == null) {
            addMessage("Please join a room before sending a message.", Color.web("#ff4c4c"));
        }
    }
}