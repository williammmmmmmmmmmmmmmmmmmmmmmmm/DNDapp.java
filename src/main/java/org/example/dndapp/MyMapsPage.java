package org.example.dndapp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class MyMapsPage {

    private final Stage primaryStage;
    private final Scene previousScene;
    private final WebSocketService client; // Corrected class name
    private static final String MY_MAPS_DIRECTORY = "src/main/resources/my-maps";
    private static final double PREVIEW_SIZE = 150;

    public MyMapsPage(Stage primaryStage, Scene previousScene, WebSocketService client) { // Corrected constructor parameter
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

        Button sendButton = new Button("Send Map");
        sendButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        sendButton.setOnAction(e -> {
            try {
                // Read the map file content
                Gson gson = new Gson();
                FileReader reader = new FileReader(file);
                MapData mapData = gson.fromJson(reader, MapData.class);
                reader.close();

                // Convert map data to a JSON string and send it
                String mapJson = new Gson().toJson(mapData);
                client.sendMessage("MAP:" + file.getName() + ":" + mapJson);

                System.out.println("Map '" + file.getName() + "' sent to players.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        mapItem.getChildren().addAll(nameLabel, mapCanvas, sendButton);
        return mapItem;
    }

    private static class MapData {
        private int rowCount;
        private int colCount;
        private List<String> grid;

        public MapData(int rowCount, int colCount, List<String> grid) {
            this.rowCount = rowCount;
            this.colCount = colCount;
            this.grid = grid;
        }
    }
}