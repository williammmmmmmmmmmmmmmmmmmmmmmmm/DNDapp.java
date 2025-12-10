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
import java.util.function.Consumer;

public class CampaignsPage {

    private Label statusIndicator;
    private Label statusText;
    private TextField roomNameInput;
    private PasswordField passwordInput;
    private ListView<String> messageLog;
    private ListView<String> roomList;
    private TextField messageBox;

    private String currentRoom = null;

    private Stage primaryStage;
    private Scene homeScene;
    private WebSocketService webSocketService;
    // NEW FIELD: Store the handler reference for external retrieval
    private final Consumer<String> currentMessageHandler;

    public CampaignsPage(Stage primaryStage, Scene homeScene, WebSocketService webSocketService) {
        this.primaryStage = primaryStage;
        this.homeScene = homeScene;
        this.webSocketService = webSocketService;
        // Assign the handler to the field in the constructor
        this.currentMessageHandler = this::handleMessage;
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

        // Bind UI updates to WebSocketService messages using the stored reference
        webSocketService.setOnMessageReceived(currentMessageHandler);

        FloatingDieIcon dieIcon = new FloatingDieIcon();
        StackPane.setAlignment(dieIcon, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(dieIcon, new Insets(20));

        // Add the icon to the root StackPane
        root.getChildren().add(dieIcon);

        return new Scene(root);
    }

    private void handleMessage(String message) {
        // Steps 1 & 2: Receive and read message
        Platform.runLater(() -> {

            // Step 3: check for [NAME:
            if (message.contains("[NAME:")) {

                // --- CUSTOM CHAT PARSING LOGIC (Handles local echo and broadcast) ---

                // Step 4: "delete" (forget) anything before [NAME: in the read message
                int nameStartIndicator = message.indexOf("[NAME:");
                int nameStart = nameStartIndicator + "[NAME:".length(); // Start of name (after :)

                // Step 5: continue reading until you find a ]
                int nameEnd = message.indexOf(']', nameStart);

                if (nameEnd != -1 && nameEnd > nameStart) {
                    // Step 6: remember everything between [NAME: and ]
                    String playerName = message.substring(nameStart, nameEnd).trim();

                    // Step 7: continue reading until message ends (from after ])
                    String actualMessage = message.substring(nameEnd + 1).trim();

                    // Determine color: local echo (usually contains "CHAT:") vs. broadcast (contains "CHAT_MESSAGE:")
                    // Local echo (you) uses blue, Broadcast uses green.
                    Color color = message.contains("CHAT_MESSAGE:") ? Color.web("#00ff00") : Color.web("#4c80ff");

                    // Handle the special name synchronization message
                    if (actualMessage.startsWith("Player joined the room.")) {
                        addMessage(playerName + " has joined the room.", color);
                    } else {
                        // Step 8: turn remembered name into *name*:*message*
                        addMessage(playerName + ": " + actualMessage, color);
                    }

                    return; // Message successfully handled (it was a chat message).
                }
                // If it contains [NAME: but is malformed, we discard it here by falling through.
            }

            // --- NON-CHAT COMMAND HANDLING ---
            // We use simple startsWith checks for known commands. No complex log stripping is necessary.
            String processedMessage = message.trim();

            if (processedMessage.startsWith("ROOMLIST:")) {
                updateRoomList(processedMessage.substring("ROOMLIST:".length()));
            } else if (processedMessage.startsWith("ERROR:")) {
                addMessage(processedMessage.substring("ERROR:".length()), Color.web("#ff4c4c"));
            } else if (processedMessage.startsWith("SUCCESS:")) {
                // Update the currentRoom variable when a successful join message is received
                int lastColonIndex = processedMessage.lastIndexOf(":");
                if (lastColonIndex != -1) {
                    String roomName = processedMessage.substring(lastColonIndex + 1);
                    this.currentRoom = roomName;
                } else {
                    this.currentRoom = "UNKNOWN";
                }
                addMessage(processedMessage.substring("SUCCESS:".length()).trim(), Color.web("#80ff80"));

                // After successful join, send a CHAT message to sync the local player's name
                String playerName = PlayerSession.getPlayerName();
                String syncMessage = "[NAME:" + playerName + "]Player joined the room.";
                webSocketService.sendMessage("CHAT:" + this.currentRoom + ":" + syncMessage);

            } else if (processedMessage.startsWith("MOVE:") || processedMessage.contains("MOVE:")) {
                // Filter out MOVE messages
                return;
            }
            // CRITICAL: NO FINAL 'ELSE' BLOCK. Any other message (including messy log headers
            // that don't contain [NAME:]) is now silently discarded.
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

        // Adjust display message for JOIN/CREATE
        String displayMessage = (command.equals("JOIN") || command.equals("CREATE"))
                ? "Attempting to " + command.toLowerCase() + " room '" + roomName + "' as " + PlayerSession.getPlayerName() + "..."
                : "Sending: " + message;
        addMessage(displayMessage, Color.web("#4c80ff"));
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
            this.currentRoom = null; // Clear room state
        } else {
            addMessage("No room to leave. Please enter a room name.", Color.web("#ff4c4c"));
        }
    }

    private void handleSendMessage() {
        String message = messageBox.getText().trim();
        if (!message.isEmpty() && this.currentRoom != null) {
            String playerName = PlayerSession.getPlayerName();

            // SHADOW TEXT INJECTION: [NAME:PlayerName]Actual Message
            String shadowMessage = "[NAME:" + playerName + "]" + message;

            webSocketService.sendMessage("CHAT:" + this.currentRoom + ":" + shadowMessage);

            // CRITICAL: NO local logging. Rely on the server echo being cleanly parsed by handleMessage.
            messageBox.clear();
        } else if (this.currentRoom == null) {
            addMessage("Please join a room before sending a message.", Color.web("#ff4c4c"));
        }
    }


    // NEW PUBLIC GETTERS to facilitate handing off WebSocket control
    public WebSocketService getWebSocketService() {
        return webSocketService;
    }

    public String getCurrentRoom() {
        return currentRoom;
    }

    public Consumer<String> getMessageHandler() {
        return currentMessageHandler;
    }
}