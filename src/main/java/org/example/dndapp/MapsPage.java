package org.example.dndapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class MapsPage {

    private final Stage primaryStage;
    private final Scene playerScene;
    // NEW FIELDS FOR MULTIPLAYER INTEGRATION
    private final WebSocketService webSocketService;
    private final CampaignsPage campaignsPage;

    private static final String MAPS_DIRECTORY = "src/main/resources/maps";
    private static final double PREVIEW_SIZE = 150;
    private GridPane mapGrid;
    private boolean isDeleteMode = false;

    // UI elements that need to be updated in the delete mode
    private Button deleteModeButton;
    private Button cancelDeleteButton;

    // UPDATED CONSTRUCTOR
    public MapsPage(Stage primaryStage, Scene playerScene, WebSocketService webSocketService, CampaignsPage campaignsPage) {
        this.primaryStage = primaryStage;
        this.playerScene = playerScene;
        this.webSocketService = webSocketService; // Store WebSocket service
        this.campaignsPage = campaignsPage;       // Store CampaignsPage instance to access room and handler info
    }

    public Scene createScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #000;");

        Label title = new Label("Available Maps");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        Button backButton = new Button("Go Back");
        backButton.setStyle("-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;");
        backButton.setOnAction(e -> primaryStage.setScene(playerScene));

        Button downloadMapButton = new Button("Download Map");
        downloadMapButton.setStyle("-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #28a745; -fx-text-fill: white;");
        downloadMapButton.setOnAction(e -> handleDownloadMap());

        deleteModeButton = new Button("Delete Maps");
        deleteModeButton.setStyle("-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #dc3545; -fx-text-fill: white;");
        deleteModeButton.setOnAction(e -> toggleDeleteMode(true));

        cancelDeleteButton = new Button("Cancel Deletion");
        cancelDeleteButton.setStyle("-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #ffc107; -fx-text-fill: black;");
        cancelDeleteButton.setOnAction(e -> toggleDeleteMode(false));
        cancelDeleteButton.setVisible(false); // Hidden by default

        HBox controlBar = new HBox(20, backButton, downloadMapButton, deleteModeButton, cancelDeleteButton);
        controlBar.setAlignment(Pos.CENTER);

        mapGrid = new GridPane();
        mapGrid.setHgap(20);
        mapGrid.setVgap(20);
        mapGrid.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = new ScrollPane(mapGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        root.getChildren().addAll(controlBar, title, scrollPane);

        loadMaps(mapGrid);

        return new Scene(root);
    }

    /**
     * Toggles the delete mode and reloads the maps to update the buttons.
     */
    private void toggleDeleteMode(boolean activate) {
        isDeleteMode = activate;

        // Update button visibility
        deleteModeButton.setVisible(!activate);
        cancelDeleteButton.setVisible(activate);

        // Also hide download button in delete mode for cleaner UI
        // Assuming downloadMapButton is accessible or you update the controlBar
        // Since we put them all in controlBar, we need to hide it carefully.
        HBox controlBar = (HBox) deleteModeButton.getParent();
        controlBar.getChildren().get(1).setVisible(!activate); // downloadMapButton is index 1

        loadMaps(mapGrid); // Reloads the grid, using the updated isDeleteMode flag
    }

    /**
     * Deletes the specified map file and reloads the map list.
     */
    private void deleteMap(File file) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Map: " + file.getName());
        alert.setContentText("Are you sure you want to permanently delete this map? This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (file.delete()) {
                System.out.println("Map deleted successfully: " + file.getName());
                loadMaps(mapGrid); // Reload after successful deletion
            } else {
                System.err.println("Failed to delete map: " + file.getName());
                // Optional: show a user-friendly error dialog
            }
        }
    }

    private void loadMaps(GridPane mapGrid) {
        mapGrid.getChildren().clear(); // Clear existing maps before loading new ones
        File folder = new File(MAPS_DIRECTORY);
        if (!folder.exists() || !folder.isDirectory()) {
            Label errorLabel = new Label("Error: 'maps' directory not found.");
            errorLabel.setTextFill(Color.RED);
            mapGrid.add(errorLabel, 0, 0);
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            Label noMapsLabel = new Label("No maps found in the 'maps' directory.");
            noMapsLabel.setTextFill(Color.web("#d3d3d3"));
            mapGrid.add(noMapsLabel, 0, 0);
            return;
        }

        int col = 0;
        int row = 0;
        for (File file : files) {
            VBox mapItem = createMapItem(file);
            mapGrid.add(mapItem, col, row);
            col++;
            if (col > 2) { // 3 maps per row
                col = 0;
                row++;
            }
        }
    }

    private VBox createMapItem(File file) {
        VBox mapItem = new VBox(5);
        mapItem.setAlignment(Pos.CENTER);
        mapItem.setPadding(new Insets(10));
        mapItem.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #d3d3d3; -fx-border-width: 1;");

        Label nameLabel = new Label(file.getName());
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Canvas mapCanvas = new Canvas(PREVIEW_SIZE, PREVIEW_SIZE);
        mapCanvas.getGraphicsContext2D().setStroke(Color.web("#ff0000"));
        mapCanvas.getGraphicsContext2D().strokeRect(0, 0, PREVIEW_SIZE, PREVIEW_SIZE);

        Button actionButton;

        if (isDeleteMode) {
            actionButton = new Button("DELETE");
            actionButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            actionButton.setOnAction(e -> deleteMap(file));
        } else {
            actionButton = new Button("View Map");
            actionButton.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white;");
            actionButton.setOnAction(e -> {
                // FIX: Updated constructor call with all 6 required arguments
                PlayerMapViewerPage viewerPage = new PlayerMapViewerPage(
                        primaryStage,
                        createScene(), // The current scene (MapsPage) to return to
                        file.getName(),
                        webSocketService,
                        campaignsPage.getCurrentRoom(),
                        campaignsPage.getMessageHandler()
                );
                primaryStage.setScene(viewerPage.createScene());
                primaryStage.setTitle("Map Viewer");
            });
        }

        mapItem.getChildren().addAll(nameLabel, mapCanvas, actionButton);
        return mapItem;
    }

    /**
     * Handles the button click to open a file chooser and select a map file.
     */
    private void handleDownloadMap() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Map JSON File");

        // Set an extension filter for JSON files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show open file dialog
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            try {
                // Read the file content as a string (JSON data)
                String mapDataJson = Files.readString(selectedFile.toPath());

                // Use the existing receiveMap method to save the file
                receiveMap(selectedFile.getName(), mapDataJson);
            } catch (IOException e) {
                System.err.println("Error reading selected map file: " + e.getMessage());
                // Optionally show an alert to the user
            }
        }
    }

    /**
     * Receives a map file from the server and saves it to the maps directory.
     * This function is for player-side use only.
     * @param fileName The name of the map file.
     * @param mapDataJson The JSON content of the map file as a string.
     */
    public void receiveMap(String fileName, String mapDataJson) {
        try {
            File directory = new File(MAPS_DIRECTORY);
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    System.err.println("Error: Could not create 'maps' directory.");
                    return;
                }
            }

            File file = new File(directory, fileName);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(mapDataJson);
            }
            System.out.println("Map '" + fileName + "' received and saved successfully.");

            // Reload the maps page to display the new map
            if (mapGrid != null) {
                loadMaps(mapGrid);
            }
        } catch (IOException e) {
            System.err.println("Error saving received map: " + e.getMessage());
            e.printStackTrace();
        }
    }
}