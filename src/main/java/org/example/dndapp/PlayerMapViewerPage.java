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
        }
    }

    /**
     * NEW: Restores the previous WebSocket message handler and navigates back.
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
        // UPDATED: Use the new handler to manage WebSocket delegate
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

        statusLabel = new Label("Loading map...");
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
                // Pass the button type (e.getButton() should work if javafx.scene.input.MouseEvent is used)
                doomEngine.handleMouseClick(e.getButton());
                e.consume(); // Consume the event so map logic is skipped
                return;
            }

            // Click to move functionality (original map logic)
            int[] hex = screenToHex(e.getX(), e.getY());

            if (hex[0] >= 0 && hex[0] < COL_COUNT && hex[1] >= 0 && hex[1] < ROW_COUNT) {
                playerHexQ = hex[0];
                playerHexR = hex[1];
                updateRevealedTiles();
                drawMap();
                sendMoveCommand(); // NEW: Send move to server
            }
        });

        fogTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (isDoomModeActive) return; // Ignore changes in doom mode

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
            if (isDoomModeActive) return; // Ignore changes in doom mode

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
                e.consume(); // Consume the event
                return;
            }

            // --- Key Handling ---
            if (isDoomModeActive) {
                // Route key presses to the external DOOM engine for game control
                doomEngine.handleKeyPress(e.getCode());
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
                sendMoveCommand(); // NEW: Send move to server
            }
        });

        scene.setOnKeyReleased(e -> {
            if (isDoomModeActive) {
                // Route key releases to the external DOOM engine
                doomEngine.handleKeyRelease(e.getCode());
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
        // Call the external DOOM engine's update logic
        doomEngine.update(now);

        // Call the external DOOM engine's rendering logic
        doomEngine.render(
                mapCanvas.getGraphicsContext2D(),
                mapCanvas.getWidth(),
                mapCanvas.getHeight()
        );
    }

    /**
     * NEW: Sends the player's current hex position to the server.
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
     * NEW: Handles WebSocket messages relevant to the map/room.
     * Processes PLAYER_MOVE, PLAYER_LIST, and PLAYER_LEFT messages.
     */
    private void handleRoomMessage(String message) {
        Platform.runLater(() -> {
            if (message.startsWith("PLAYER_MOVE:")) {
                // Format: PLAYER_MOVE:address:q:r
                try {
                    String[] parts = message.substring("PLAYER_MOVE:".length()).split(":", 3);
                    String address = parts[0];
                    int q = Integer.parseInt(parts[1]);
                    int r = Integer.parseInt(parts[2]);

                    // Update the position of the other player
                    otherPlayers.put(address, new int[]{q, r});
                    drawMap();
                } catch (Exception e) {
                    // Ignore malformed message
                }
            } else if (message.startsWith("PLAYER_LIST:")) {
                // Format: PLAYER_LIST:address1:q1,r1|address2:q2,r2|...
                otherPlayers.clear();
                String data = message.substring("PLAYER_LIST:".length());
                if (!data.isEmpty()) {
                    String[] playerEntries = data.split("\\|");
                    for (String entry : playerEntries) {
                        try {
                            String[] parts = entry.split(":");
                            if (parts.length == 2) {
                                String address = parts[0];
                                String[] coords = parts[1].split(",");
                                if (coords.length == 2) {
                                    int q = Integer.parseInt(coords[0]);
                                    int r = Integer.parseInt(coords[1]);

                                    otherPlayers.put(address, new int[]{q, r});
                                }
                            }
                        } catch (Exception e) {
                            // Ignore malformed player entry
                        }
                    }
                    drawMap();
                }
            } else if (message.startsWith("PLAYER_LEFT:")) {
                // Format: PLAYER_LEFT:address
                String address = message.substring("PLAYER_LEFT:".length());
                otherPlayers.remove(address);
                drawMap();
            } else {
                // Forward unknown messages back to the previous handler (CampaignsPage)
                if (previousMessageHandler != null) {
                    previousMessageHandler.accept(message);
                }
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
                statusLabel.setText("Map loaded successfully.");
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
        // ... (existing implementation)
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

    private void drawMap() {
        // Do not draw the map if we are in DOOM mode
        if (isDoomModeActive) {
            // The AnimationTimer handles the drawing, no need to redraw statically here
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
        drawPlayerToken(gc, playerHexQ, playerHexR, Color.web("#ffd700"), Color.web("#8b0000")); // Local Player

        // NEW: Draw all other players
        otherPlayers.forEach((address, pos) -> {
            // Draw other players with a different color (Green)
            drawPlayerToken(gc, pos[0], pos[1], Color.web("#00ff7f"), Color.web("#006400"));
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
        // ... (existing implementation)
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
     * UPDATED: Unified method to draw any player token (local or remote).
     */
    private void drawPlayerToken(GraphicsContext gc, int q, int r, Color fill, Color stroke) {
        double xCenter = HEX_SIZE * 1.5 * q;
        double yCenter = hexHeight * r + hexHeight * (q % 2) / 2;

        // Draw the player token slightly smaller
        double tokenSize = HEX_SIZE * 0.8;
        gc.setFill(fill);
        gc.fillOval(xCenter - tokenSize / 2, yCenter - tokenSize / 2, tokenSize, tokenSize);
        gc.setStroke(stroke);
        gc.setLineWidth(2);
        gc.strokeOval(xCenter - tokenSize / 2, yCenter - tokenSize / 2, tokenSize, tokenSize);
    }
}