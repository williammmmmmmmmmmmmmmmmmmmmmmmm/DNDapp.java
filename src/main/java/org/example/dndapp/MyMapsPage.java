package org.example.dndapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class MyMapsPage {

    private final Stage primaryStage;
    private final Scene previousScene;
    private final WebSocketService client;
    private static final String MY_MAPS_DIRECTORY = "src/main/resources/my-maps";
    private static final double PREVIEW_SIZE = 150;

    // --- Path Resolution to Downloads/To Send folder ---
    private static final String TO_SEND_FOLDER_NAME = "To Send";
    // Find the base Downloads path (e.g., C:\Users\alain\Downloads)
    private static final Path DOWNLOADS_PATH = Path.of(System.getProperty("user.home"), "Downloads");
    // Define the full path (e.g., C:\Users\alain\Downloads\To Send)
    private static final Path TO_SEND_PATH = DOWNLOADS_PATH.resolve(TO_SEND_FOLDER_NAME);
    // ---------------------------------------------------

    public MyMapsPage(Stage primaryStage, Scene previousScene, WebSocketService client) {
        this.primaryStage = primaryStage;
        this.previousScene = previousScene;
        this.client = client;
    }

    public Scene createScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #000;");

        Label title = new Label("My Maps");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        Button backButton = new Button("Go Back");
        backButton.setStyle("-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;");
        backButton.setOnAction(e -> primaryStage.setScene(previousScene));

        GridPane mapGrid = new GridPane();
        mapGrid.setHgap(20);
        mapGrid.setVgap(20);
        mapGrid.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = new ScrollPane(mapGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        root.getChildren().addAll(backButton, title, scrollPane);

        loadMaps(mapGrid);

        return new Scene(root);
    }

    private void loadMaps(GridPane mapGrid) {
        mapGrid.getChildren().clear(); // Clear existing content
        File folder = new File(MY_MAPS_DIRECTORY);
        if (!folder.exists() || !folder.isDirectory()) {
            Label errorLabel = new Label("Error: 'my-maps' directory not found.");
            errorLabel.setTextFill(Color.RED);
            mapGrid.add(errorLabel, 0, 0);
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            Label noMapsLabel = new Label("No maps found in the 'my-maps' directory.");
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

    /**
     * Cleans up the "To Send" folder in Downloads by deleting it if it is empty.
     */
    private void cleanupToSendFolder() {
        File folder = TO_SEND_PATH.toFile();
        if (folder.exists() && folder.isDirectory()) {
            String[] contents = folder.list();
            // Check if the directory is empty
            if (contents == null || contents.length == 0) {
                if (folder.delete()) {
                    System.out.println("Cleaned up empty 'To Send' folder in Downloads.");
                } else {
                    System.err.println("Failed to delete empty 'To Send' folder in Downloads.");
                }
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

        // Updated button text
        Button sendButton = new Button("Export to Downloads/To Send");
        sendButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");

        sendButton.setOnAction(e -> {
            try {
                // 1. Create the target subdirectory in Downloads
                Files.createDirectories(TO_SEND_PATH);

                // 2. Define the destination file path
                Path destinationPath = TO_SEND_PATH.resolve(file.getName());

                // 3. Copy the file, replacing an existing file if present
                Files.copy(file.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Map '" + file.getName() + "' copied successfully to Downloads/To Send at: " + TO_SEND_PATH.toAbsolutePath());

                // Show success alert
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Map Exported");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Map '" + file.getName() + "' has been saved to your Downloads/To Send folder.");
                successAlert.showAndWait();

            } catch (IOException ex) {
                System.err.println("Error copying map to Downloads/To Send folder: " + ex.getMessage());
                ex.printStackTrace();

                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Could Not Export Map");
                errorAlert.setContentText("Failed to copy map to the Downloads/To Send folder. Check permissions.");
                errorAlert.showAndWait();

            } finally {
                // Always check for cleanup after the operation (successful or failed)
                // Deletes the "To Send" folder if it is left empty
                cleanupToSendFolder();
            }
        });

        mapItem.getChildren().addAll(nameLabel, mapCanvas, sendButton);
        return mapItem;
    }
}