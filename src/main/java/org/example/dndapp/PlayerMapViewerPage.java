package org.example.dndapp;


import com.google.gson.Gson;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ScrollPane;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PlayerMapViewerPage {

    private final Stage primaryStage;
    private final Scene mapsScene;
    private final String mapFileName;
    private Canvas mapCanvas;
    private Label statusLabel;
    private MapData mapData;

    // Hex grid parameters, aligned with MapCreatorPage
    private static final int COL_COUNT = 100;
    private static final int ROW_COUNT = 100;
    private static final int HEX_SIZE = 10;
    private final double hexHeight = HEX_SIZE * Math.sqrt(3);
    private final double hexWidth = HEX_SIZE * 2;
    private static final String MAPS_DIRECTORY = "src/main/resources/maps";

    // Player token properties
    private int playerHexQ = 0; // Column coordinate (q)
    private int playerHexR = 0; // Row coordinate (r)

    // Fog of War controls
    private ToggleGroup fogTypeGroup;
    private Slider fogStrengthSlider;
    private boolean[][] revealedTiles = new boolean[COL_COUNT][ROW_COUNT];

    // Added fields for canvas resizing and mode switching
    private double originalCanvasWidth;
    private double originalCanvasHeight;
    private boolean isDoomModeActive = false; // Flag to track the current state

    // Core DOOM Integration
    private final DoomEngine doomEngine; // Instance of the external DOOM code
    private final AnimationTimer doomLoop;
    private long lastFrameTime = 0;

    // NEW: Multiplayer fields
    private final WebSocketService webSocketService;
    private final String currentRoom;
    // Map of other players: WebSocket Address -> [q, r]
    private final Map<String, int[]> otherPlayers = new ConcurrentHashMap<>();
    // Reference to the handler of the previous scene (CampaignsPage)
    private final Consumer<String> previousMessageHandler;

    // --- NEW: ENEMY TOKEN FIELDS ---
    // Map of enemy tokens: Token Name -> [q, r]
    private final Map<String, int[]> enemyTokens = new ConcurrentHashMap<>();
    private String nextEnemyName = "Goblin 1";
    private ContextMenu enemyContextMenu;


    public PlayerMapViewerPage(Stage primaryStage, Scene mapsScene, String mapFileName,
                               WebSocketService webSocketService, String currentRoom,
                               Consumer<String> previousMessageHandler) { // UPDATED CONSTRUCTOR
        this.primaryStage = primaryStage;
        this.mapsScene = mapsScene;
        this.mapFileName = mapFileName;
        this.webSocketService = webSocketService; // NEW
        this.currentRoom = currentRoom; // NEW
        this.previousMessageHandler = previousMessageHandler; // NEW

        // Initialize the external DOOM engine
        this.doomEngine = new DoomEngine();

        // Initialize the game loop
        this.doomLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isDoomModeActive) {
                    runDoomFrame(now); // Call the wrapper method
                }
            }
        };
        // Start the timer immediately, it will only draw when isDoomModeActive is true
        this.doomLoop.start();

        // NEW: Set the current page as the WebSocket message handler
        if (webSocketService != null) {
            webSocketService.setOnMessageReceived(this::handleRoomMessage);
            // NEW: Send initial move command to announce player presence
            if (currentRoom != null) {
                sendMoveCommand();
            }
        }
    }

    /**
     * Restores the previous WebSocket message handler and navigates back.
     */
    private void handleGoBack() {
        if (webSocketService != null && previousMessageHandler != null) {
            // Restore the previous handler (CampaignsPage::handleMessage)
            webSocketService.setOnMessageReceived(previousMessageHandler);
        }
        primaryStage.setScene(mapsScene);
    }

    public Scene createScene() {
        VBox mainContent = new VBox(20);
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: #1a1a1a; -fx-font-family: \"Inter\", sans-serif;");

        Label title = new Label("Viewing Map: " + mapFileName);
        title.setFont(Font.font("Inter", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff6347"));

        // Controls at the top
        HBox topControls = new HBox(15);
        topControls.setAlignment(Pos.CENTER_LEFT);
        topControls.setPadding(new Insets(0, 0, 20, 0));

        Button backButton = new Button("Go Back");
        backButton.setStyle("-fx-padding: 12 24; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 8px; -fx-background-color: #007bff; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);");
        backButton.setOnAction(e -> handleGoBack());

        VBox fogControls = new VBox(5);
        fogControls.setAlignment(Pos.CENTER_LEFT);
        Label fogLabel = new Label("Fog of War:");
        fogLabel.setTextFill(Color.web("#f0f0f0"));
        fogLabel.setStyle("-fx-font-size: 14px;");
        fogTypeGroup = new ToggleGroup();
        RadioButton noFogRadio = new RadioButton("None");
        noFogRadio.setToggleGroup(fogTypeGroup);
        noFogRadio.setSelected(true);
        RadioButton fogRadio = new RadioButton("Fog");
        fogRadio.setToggleGroup(fogTypeGroup);
        RadioButton fogOfWarRadio = new RadioButton("Fog of War");
        fogOfWarRadio.setToggleGroup(fogTypeGroup);
        noFogRadio.setTextFill(Color.web("#d3d3d3"));
        fogRadio.setTextFill(Color.web("#d3d3d3"));
        fogOfWarRadio.setTextFill(Color.web("#d3d3d3"));
        HBox fogRadios = new HBox(10, noFogRadio, fogRadio, fogOfWarRadio);
        fogRadios.setAlignment(Pos.CENTER);
        fogControls.getChildren().addAll(fogLabel, fogRadios);

        VBox strengthControls = new VBox(5);
        strengthControls.setAlignment(Pos.CENTER_LEFT);
        Label strengthLabel = new Label("Fog Strength:");
        strengthLabel.setTextFill(Color.web("#f0f0f0"));
        strengthLabel.setStyle("-fx-font-size: 14px;");
        fogStrengthSlider = new Slider(0, 10, 5);
        strengthControls.getChildren().addAll(strengthLabel, fogStrengthSlider);

        statusLabel = new Label("Loading map... (Press Ctrl+E to add an Enemy Token)");
        statusLabel.setTextFill(Color.web("#d3d3d3"));
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-style: italic; -fx-padding: 10px 0;");

        topControls.getChildren().addAll(backButton, fogControls, strengthControls, statusLabel);

        // Initialize canvas and save original dimensions
        originalCanvasWidth = (COL_COUNT * 1.5 + 0.5) * HEX_SIZE;
        originalCanvasHeight = (ROW_COUNT + 0.5) * hexHeight;
        mapCanvas = new Canvas(originalCanvasWidth, originalCanvasHeight);

        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());

        mainContent.getChildren().addAll(topControls, title, mapCanvas);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(mainContent);
        scrollPane.setStyle("-fx-background-color: #1a1a1a;");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // --- Floating Die Icon Setup ---
        FloatingDieIcon dieIcon = new FloatingDieIcon();
        StackPane.setAlignment(dieIcon, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(dieIcon, new Insets(20));

        StackPane finalRoot = new StackPane(scrollPane, dieIcon);

        // Create the Scene using the StackPane
        Scene scene = new Scene(finalRoot, 900, 800);
        setupMouseEvents();
        setupKeyEvents(scene);
        loadMap();

        return scene;
    }

    private void setupMouseEvents() {
        // Ensure you are using the correct JavaFX MouseEvent
        mapCanvas.setOnMousePressed(e -> {
            // Check if DOOM mode is active before handling the click
            if (isDoomModeActive) {
                doomEngine.handleMouseClick(e.getButton());
                e.consume();
                return;
            }

            int[] hex = screenToHex(e.getX(), e.getY());
            boolean isHexValid = hex[0] >= 0 && hex[0] < COL_COUNT && hex[1] >= 0 && hex[1] < ROW_COUNT;

            if (e.getButton() == MouseButton.PRIMARY) {
                // Primary click for local player movement
                if (isHexValid) {
                    playerHexQ = hex[0];
                    playerHexR = hex[1];
                    updateRevealedTiles();
                    drawMap();
                    sendMoveCommand(); // Send move to server
                }
            } else if (e.getButton() == MouseButton.SECONDARY) {
                // Secondary click for enemy token movement/management
                if (isHexValid) {
                    // Show the context menu to select which enemy to move
                    if (enemyContextMenu == null) {
                        enemyContextMenu = new ContextMenu();
                    }
                    enemyContextMenu.getItems().clear();

                    if (!enemyTokens.isEmpty()) {
                        // Create menu items for each enemy token
                        enemyTokens.forEach((name, pos) -> {
                            // Option to move this enemy to the clicked hex
                            MenuItem moveItem = new MenuItem("Move " + name + " to (" + hex[0] + ", " + hex[1] + ")");
                            moveItem.setOnAction(event -> {
                                // Move the selected enemy
                                enemyTokens.put(name, new int[]{hex[0], hex[1]});
                                sendEnemyUpdateCommand("MOVE", name, hex[0], hex[1]);
                                drawMap();
                            });
                            enemyContextMenu.getItems().add(moveItem);

                            // Option to remove this enemy
                            MenuItem removeItem = new MenuItem("Remove " + name);
                            removeItem.setOnAction(event -> {
                                enemyTokens.remove(name);
                                sendEnemyUpdateCommand("REMOVE", name, 0, 0); // Coords ignored for remove
                                drawMap();
                            });
                            enemyContextMenu.getItems().add(removeItem);
                            enemyContextMenu.getItems().add(new SeparatorMenuItem()); // Separator
                        });

                        // Remove the last separator if it exists
                        if (!enemyContextMenu.getItems().isEmpty() && enemyContextMenu.getItems().get(enemyContextMenu.getItems().size() - 1) instanceof SeparatorMenuItem) {
                            enemyContextMenu.getItems().remove(enemyContextMenu.getItems().size() - 1);
                        }
                    } else {
                        MenuItem infoItem = new MenuItem("No enemy tokens to move (Press Ctrl+E to add one)");
                        infoItem.setDisable(true);
                        enemyContextMenu.getItems().add(infoItem);
                    }


                    if (!enemyContextMenu.getItems().isEmpty()) {
                        enemyContextMenu.show(mapCanvas, e.getScreenX(), e.getScreenY());
                    }
                }
            }
        });

        fogTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (isDoomModeActive) return;

            if (newValue != null) {
                if ("Fog".equals(((RadioButton) newValue).getText())) {
                    for (int q = 0; q < COL_COUNT; q++) {
                        for (int r = 0; r < ROW_COUNT; r++) {
                            revealedTiles[q][r] = false;
                        }
                    }
                }
                updateRevealedTiles();
                drawMap();
            }
        });
        fogStrengthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isDoomModeActive) return;

            updateRevealedTiles();
            drawMap();
        });
    }

    private void setupKeyEvents(Scene scene) {
        // Define the key combination: Ctrl + Alt + D
        final KeyCombination doomKeyCombo = new KeyCodeCombination(KeyCode.D,
                KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN);

        scene.setOnKeyPressed(e -> {
            // Check for the DOOM key combination and toggle the mode
            if (doomKeyCombo.match(e)) {
                toggleDoomMode();
                e.consume();
                return;
            }

            // --- Key Handling ---
            if (isDoomModeActive) {
                doomEngine.handleKeyPress(e.getCode());
                return;
            }

            // NEW: Add Enemy Token on Ctrl + E
            if (e.getCode() == KeyCode.E && e.isControlDown()) {
                showAddEnemyDialog();
                e.consume();
                return;
            }

            // Map Movement Logic (only runs if not in DOOM mode)
            int newQ = playerHexQ;
            int newR = playerHexR;

            if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP) {
                newR = playerHexR - 1;
            } else if (e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN) {
                newR = playerHexR + 1;
            } else if (e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT) {
                newQ = playerHexQ - 1;
            } else if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) {
                newQ = playerHexQ + 1;
            }

            if (newQ >= 0 && newQ < COL_COUNT && newR >= 0 && newR < ROW_COUNT) {
                playerHexQ = newQ;
                playerHexR = newR;
                updateRevealedTiles();
                drawMap();
                sendMoveCommand(); // Send move to server
            }
        });

        scene.setOnKeyReleased(e -> {
            if (isDoomModeActive) {
                doomEngine.handleKeyRelease(e.getCode());
            }
        });
    }

    /**
     * Prompts the user to name a new enemy token and adds it to the map at the player's current location.
     */
    private void showAddEnemyDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add New Enemy Token (Ctrl+E)");
        dialog.setHeaderText("Enter a name for the new enemy token. It will be placed at your current location.");

        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create the enemy name field
        TextField nameField = new TextField(nextEnemyName);
        nameField.setPromptText("Enemy Name");

        VBox content = new VBox(10, nameField);
        dialog.getDialogPane().setContent(content);

        // Convert the result to a name when the Add button is clicked
        Platform.runLater(nameField::requestFocus);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return nameField.getText().trim();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(name -> {
            if (!name.isEmpty()) {
                // Ensure the name is unique (simple uniqueness check)
                String uniqueName = name;
                int suffix = 1;
                while (enemyTokens.containsKey(uniqueName)) {
                    // Try to make the name unique by appending a number
                    uniqueName = name + " " + suffix++;
                }

                // 1. Add the new enemy at the local player's current position (local update)
                int q = playerHexQ;
                int r = playerHexR;
                enemyTokens.put(uniqueName, new int[]{q, r});

                // 2. Send update to other players (and self, as the command will bounce)
                sendEnemyUpdateCommand("ADD", uniqueName, q, r);

                // Update the suggestion for the next enemy
                try {
                    // Try to increment the number in the name for the next suggestion
                    String[] parts = uniqueName.split(" ");
                    int lastIndex = parts.length - 1;
                    int lastNumber = Integer.parseInt(parts[lastIndex]);
                    parts[lastIndex] = String.valueOf(lastNumber + 1);
                    nextEnemyName = String.join(" ", parts);
                } catch (NumberFormatException ignored) {
                    // If no number, just suggest a base name + 1
                    nextEnemyName = uniqueName + " 1";
                }

                drawMap();
                statusLabel.setText("Added enemy: " + uniqueName + " at (" + q + ", " + r + ")");
            }
        });
    }


    private void toggleDoomMode() {
        isDoomModeActive = !isDoomModeActive;

        if (isDoomModeActive) {
            // Switch to DOOM Mode
            double doomWidth = 640;
            double doomHeight = 480;

            mapCanvas.setWidth(doomWidth);
            mapCanvas.setHeight(doomHeight);

            statusLabel.setText("!!! Ripping and Tearing on the Map Canvas !!!");
            statusLabel.setTextFill(Color.web("#ff0000"));

        } else {
            // Switch back to Map Mode

            // Restore original status label
            statusLabel.setText("Map mode restored.");
            statusLabel.setTextFill(Color.web("#d3d3d3"));

            // Redraw the map, which will handle size restoration
            drawMap();
        }
    }

    /**
     * The game loop wrapper. Calls the external DoomEngine for logic and rendering.
     */
    private void runDoomFrame(long now) {
        if (lastFrameTime == 0) {
            lastFrameTime = now;
            return;
        }

        double delta = (now - lastFrameTime) / 1_000_000_000.0;
        lastFrameTime = now;

        GraphicsContext gc = mapCanvas.getGraphicsContext2D();

        // 1. Update the engine state
        doomEngine.update((long) delta);

        // 2. Render the new frame to the canvas
        // This method assumes DoomEngine has a method that draws its current state
        doomEngine.render(gc, mapCanvas.getWidth(), mapCanvas.getHeight());
    }

    /**
     * Sends the player's current hex position to the server.
     * Message format: MOVE:roomName:q,r
     */
    private void sendMoveCommand() {
        if (webSocketService != null && currentRoom != null) {
            // Sends the command that the GameServer needs to handle: MOVE:roomName:q,r
            String message = "MOVE:" + currentRoom + ":" + playerHexQ + "," + playerHexR;
            webSocketService.sendMessage(message);
        }
    }

    /**
     * Sends an enemy token update to the server via the CHAT command, which gets broadcast.
     * The payload format is: ENEMY_UPDATE|ACTION|Name|q,r
     * ACTION is ADD, MOVE, or REMOVE.
     */
    private void sendEnemyUpdateCommand(String action, String name, int q, int r) {
        if (webSocketService != null && currentRoom != null) {
            // Encode the name in case it contains pipes, though simple names are expected
            String encodedName = name.replace("|", "/");
            String payload = String.format("ENEMY_UPDATE|%s|%s|%d,%d", action, encodedName, q, r);
            // Send as a CHAT message to be broadcast by GameServer
            String message = "CHAT:" + currentRoom + ":" + payload;
            webSocketService.sendMessage(message);
        }
    }

    /**
     * Handles WebSocket messages relevant to the map/room by parsing the required server log format.
     */
    private void handleRoomMessage(String message) {
        Platform.runLater(() -> {
            boolean mapUpdateNeeded = false;
            String playerAddress = null;

            // 1. Parse the MOVE message based on the required server log format.
            // Expected format (as client message payload): INFO: Message received from /[address]:port: MOVE:roomName:q,r
            if (message.startsWith("INFO: Message received from /")) {
                try {
                    String[] parts = message.split(": ");

                    // We expect at least 3 parts: [0]INFO [1]Message received from /[addr] [2]MOVE:room:q,r
                    if (parts.length >= 3 && parts[1].startsWith("Message received from /")) {

                        // Extract the full address (e.g., /[0:0:0:0:0:0:0:1]:52674)
                        String fullAddressPart = parts[1].substring("Message received from ".length());
                        playerAddress = fullAddressPart;

                        // Extract the move command part (e.g., MOVE:test:51,28)
                        String moveCommand = parts[2];

                        // Ensure the message is a MOVE command for the current room
                        if (currentRoom != null && moveCommand.startsWith("MOVE:" + currentRoom + ":")) {

                            // Extract coordinates (e.g., 51,28)
                            String coordsStr = moveCommand.substring(("MOVE:" + currentRoom + ":").length());
                            String[] coords = coordsStr.split(",");

                            if (coords.length == 2) {
                                int q = Integer.parseInt(coords[0]);
                                int r = Integer.parseInt(coords[1]);

                                // Store or update the player's position
                                otherPlayers.put(playerAddress, new int[]{q, r});
                                mapUpdateNeeded = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing MOVE message from server log format: " + message + " - " + e.getMessage());
                }
            } else if (message.startsWith("CHAT_MESSAGE:")) { // --- NEW CHAT/ENEMY LOGIC ---
                try {
                    // Message format: CHAT_MESSAGE:[sender_address]:[payload]
                    // We use index search because sender_address may contain colons (e.g., IPv6)
                    String content = message.substring("CHAT_MESSAGE:".length());
                    int senderEnd = content.indexOf(':'); // Find the colon separating sender address and payload

                    if (senderEnd != -1) {
                        String payload = content.substring(senderEnd + 1);

                        if (payload.startsWith("ENEMY_UPDATE|")) {
                            // Payload format: ENEMY_UPDATE|ACTION|Name|q,r
                            String enemyUpdateData = payload.substring("ENEMY_UPDATE|".length());
                            String[] enemyParts = enemyUpdateData.split("\\|", 3); // ACTION|Name|q,r

                            if (enemyParts.length == 3) {
                                String action = enemyParts[0];
                                String encodedName = enemyParts[1];
                                String name = encodedName.replace("/", "|"); // Decode the name
                                String coordsStr = enemyParts[2];
                                String[] coords = coordsStr.split(",");
                                int q = Integer.parseInt(coords[0]);
                                int r = Integer.parseInt(coords[1]);

                                switch (action) {
                                    case "ADD":
                                    case "MOVE":
                                        // Update the map regardless of sender (allows bounce)
                                        enemyTokens.put(name, new int[]{q, r});
                                        statusLabel.setText("Enemy token processed: " + action + " " + name);
                                        mapUpdateNeeded = true;
                                        break;
                                    case "REMOVE":
                                        // Update the map regardless of sender (allows bounce)
                                        enemyTokens.remove(name);
                                        statusLabel.setText("Enemy token processed: Removed " + name);
                                        mapUpdateNeeded = true;
                                        break;
                                }
                            }
                            // Consume the message here, do not forward to previousMessageHandler
                            // if it was an enemy update
                            if (mapUpdateNeeded) return;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing CHAT message: " + message + " - " + e.getMessage());
                }
                // Fall through to forward if it's a regular chat message
            }

            // 2. Forward other known server messages (like CHAT_MESSAGE or ROOMLIST)
            // This is needed to maintain functionality with CampaignsPage
            if (previousMessageHandler != null) {
                previousMessageHandler.accept(message);
            }

            if (mapUpdateNeeded) {
                // Update status and draw map
                int playerCount = otherPlayers.size();
                String enemyCount = enemyTokens.isEmpty() ? "" : ". Tracking " + enemyTokens.size() + " enemies.";
                statusLabel.setText((currentRoom != null ? "Room: " + currentRoom : "Map loaded.") + ". Tracking " + playerCount + " remote players" + enemyCount + ".");
                drawMap();
            }
        });
    }

    private int[] screenToHex(double x, double y) {
        // Step 1: Invert the drawing logic to get approximate odd-q coordinates.
        double approxQ = x / (HEX_SIZE * 1.5);
        double approxR = (y - (approxQ % 2) * hexHeight / 2) / hexHeight;

        // Step 2: Round to the nearest integer coordinates.
        int finalQ = (int) Math.round(approxQ);
        int finalR = (int) Math.round(approxR);

        return new int[]{finalQ, finalR};
    }

    private void loadMap() {
        File file = new File(MAPS_DIRECTORY, mapFileName);

        if (!file.exists()) {
            statusLabel.setText("Error: Map file not found.");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            mapData = gson.fromJson(reader, MapData.class);

            if (mapData != null && mapData.getGrid() != null &&
                    mapData.getGrid().size() == COL_COUNT * ROW_COUNT) {
                updateRevealedTiles(); // Initial reveal
                drawMap();
                String status = currentRoom != null ? "Room: " + currentRoom + ". Map loaded successfully. Press Ctrl+E to add an Enemy Token." : "Map loaded successfully. Press Ctrl+E to add an Enemy Token.";
                statusLabel.setText(status);
                statusLabel.setTextFill(Color.web("#d3d3d3"));
            } else {
                statusLabel.setText("Invalid map file format or size.");
                statusLabel.setTextFill(Color.RED);
            }
        } catch (IOException ex) {
            statusLabel.setText("Error loading file: " + ex.getMessage());
            statusLabel.setTextFill(Color.RED);
        }
    }

    private void updateRevealedTiles() {
        String selectedFog = ((RadioButton) fogTypeGroup.getSelectedToggle()).getText();
        int revealRadius = (int) (10 - fogStrengthSlider.getValue());
        if (revealRadius < 0) revealRadius = 0;

        if ("Fog".equals(selectedFog)) {
            for (int q = 0; q < COL_COUNT; q++) {
                for (int r = 0; r < ROW_COUNT; r++) {
                    revealedTiles[q][r] = false;
                }
            }
        }

        // Only reveal for Fog of War and initial setup
        if ("Fog of War".equals(selectedFog) || "None".equals(selectedFog)) {
            // Reset for calculation
            if ("Fog of War".equals(selectedFog)) {
                for (int q = 0; q < COL_COUNT; q++) {
                    for (int r = 0; r < ROW_COUNT; r++) {
                        revealedTiles[q][r] = false;
                    }
                }
            }

            for (int q = -revealRadius; q <= revealRadius; q++) {
                for (int r = -revealRadius; r <= revealRadius; r++) {
                    int hexQ = playerHexQ + q;
                    int hexR = playerHexR + r;
                    if (hexQ >= 0 && hexQ < COL_COUNT && hexR >= 0 && hexR < ROW_COUNT) {
                        revealedTiles[hexQ][hexR] = true;
                    }
                }
            }
        }
    }

    private void drawMap() {
        // Do not draw the map if we are in DOOM mode
        if (isDoomModeActive) {
            return;
        }

        // Restore original size
        mapCanvas.setWidth(originalCanvasWidth);
        mapCanvas.setHeight(originalCanvasHeight);

        if (mapData == null) {
            return;
        }

        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());

        drawGridAndFog();

        // --- NEW: Draw all enemy tokens ---
        enemyTokens.forEach((name, pos) -> {
            drawEnemyToken(gc, pos[0], pos[1], name);
        });

        // Local Player - Pass "You" as the label
        drawPlayerToken(gc, playerHexQ, playerHexR, Color.web("#ffd700"), Color.web("#8b0000"), "You");

        // NEW: Draw all other players
        otherPlayers.forEach((address, pos) -> {
            // Get the short ID from the address (e.g., "52674" from "...:52674")
            String shortId = address.substring(address.lastIndexOf(':') + 1);
            // Draw other players with a different color (Green)
            drawPlayerToken(gc, pos[0], pos[1], Color.web("#00ff7f"), Color.web("#006400"), shortId);
        });
    }

    private void drawGridAndFog() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        RadioButton selectedRadio = (RadioButton) fogTypeGroup.getSelectedToggle();
        String selectedFog = (selectedRadio != null) ? selectedRadio.getText() : "None";

        for (int q = 0; q < COL_COUNT; q++) {
            for (int r = 0; r < ROW_COUNT; r++) {
                int index = r * COL_COUNT + q;
                if (index < mapData.getGrid().size()) {
                    Color color = Color.web(mapData.getGrid().get(index));
                    drawHex(gc, q, r, color);

                    if (!"None".equals(selectedFog) && !revealedTiles[q][r]) {
                        drawHex(gc, q, r, Color.web("black"));
                    }
                }
            }
        }
    }

    private void drawHex(GraphicsContext gc, int q, int r, Color fill) {
        double xCenter = HEX_SIZE * 1.5 * q;
        double yCenter = hexHeight * r + hexHeight * (q % 2) / 2;

        double[] xPoints = new double[6];
        double[] yPoints = new double[6];
        for (int i = 0; i < 6; i++) {
            double angleDeg = 60 * i;
            double angleRad = Math.toRadians(angleDeg);
            xPoints[i] = xCenter + HEX_SIZE * Math.cos(angleRad);
            yPoints[i] = yCenter + HEX_SIZE * Math.sin(angleRad);
        }

        gc.setFill(fill);
        gc.setStroke(Color.web("#555"));
        gc.setLineWidth(1);
        gc.fillPolygon(xPoints, yPoints, 6);
        gc.strokePolygon(xPoints, yPoints, 6);
    }

    /**
     * Draws an enemy token (a dark red circle with a label) on the map.
     */
    private void drawEnemyToken(GraphicsContext gc, int q, int r, String label) {
        double xCenter = HEX_SIZE * 1.5 * q;
        double yCenter = hexHeight * r + hexHeight * (q % 2) / 2;

        // Enemy fill color: Dark Red
        Color fillColor = Color.web("#8B0000");
        // Enemy border color: Black
        Color strokeColor = Color.BLACK;

        // Draw the enemy token slightly smaller than player token for distinction
        double tokenSize = HEX_SIZE * 0.7;

        gc.setFill(fillColor);
        // Draw a circle for simplicity
        gc.fillOval(xCenter - tokenSize / 2, yCenter - tokenSize / 2, tokenSize, tokenSize);

        gc.setStroke(strokeColor);
        gc.setLineWidth(1.5);
        gc.strokeOval(xCenter - tokenSize / 2, yCenter - tokenSize / 2, tokenSize, tokenSize);

        // Draw the enemy label
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));

        // Simple text centering logic
        double textWidth = label.length() * 4.5;
        gc.fillText(label, xCenter - textWidth / 2, yCenter + tokenSize / 2 + 5);
    }

    /**
     * UPDATED: Unified method to draw any player token (local or remote), including a label.
     */
    private void drawPlayerToken(GraphicsContext gc, int q, int r, Color fill, Color stroke, String label) {
        double xCenter = HEX_SIZE * 1.5 * q;
        double yCenter = hexHeight * r + hexHeight * (q % 2) / 2;

        // Draw the player token slightly smaller
        double tokenSize = HEX_SIZE * 0.8;
        gc.setFill(fill);
        gc.fillOval(xCenter - tokenSize / 2, yCenter - tokenSize / 2, tokenSize, tokenSize);
        gc.setStroke(stroke);
        gc.setLineWidth(2);
        gc.strokeOval(xCenter - tokenSize / 2, yCenter - tokenSize / 2, tokenSize, tokenSize);

        // Draw the player label (short identifier)
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));

        // Simple text centering logic
        double textWidth = label.length() * 4.5;
        gc.fillText(label, xCenter - textWidth / 2, yCenter + tokenSize / 2 + 5);
    }
}