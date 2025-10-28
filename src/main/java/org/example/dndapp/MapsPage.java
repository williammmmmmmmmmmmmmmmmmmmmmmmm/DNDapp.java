package org.example.dndapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
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
import java.io.FileWriter;
import java.io.IOException;

public class MapsPage {

    private final Stage primaryStage;
    private final Scene playerScene;
    private static final String MAPS_DIRECTORY = "src/main/resources/maps";
    private static final double PREVIEW_SIZE = 150;
    private GridPane mapGrid;

    public MapsPage(Stage primaryStage, Scene playerScene) {
        this.primaryStage = primaryStage;
        this.playerScene = playerScene;
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

        mapGrid = new GridPane();
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

        Button viewButton = new Button("View Map");
        viewButton.setOnAction(e -> {
            PlayerMapViewerPage viewerPage = new PlayerMapViewerPage(primaryStage, createScene(), file.getName());
            primaryStage.setScene(viewerPage.createScene());
            primaryStage.setTitle("Map Viewer");
        });

        mapItem.getChildren().addAll(nameLabel, mapCanvas, viewButton);
        return mapItem;
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