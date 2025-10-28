package org.example.dndapp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MapCreatorPage extends Application {
    private Stage primaryStage;
    private Scene previousScene;

    private static final int COL_COUNT = 100;
    private static final int ROW_COUNT = 100;
    private static final int HEX_SIZE = 10;
    // New directory for DM-created maps
    private static final String MY_MAPS_DIRECTORY = "src/main/resources/my-maps";

    private final double hexHeight = HEX_SIZE * Math.sqrt(3);
    private final double hexWidth = HEX_SIZE * 2;

    private Canvas canvas;
    private GraphicsContext gc;
    private String selectedColorHex = "#008000";
    private List<String> currentMapData;
    private Label statusLabel;
    private TextField mapNameInput;
    private Random random = new Random();

    public MapCreatorPage(Stage primaryStage, Scene previousScene) {
        this.primaryStage = primaryStage;
        this.previousScene = previousScene;
        this.currentMapData = new ArrayList<>();
        for (int i = 0; i < COL_COUNT * ROW_COUNT; i++) {
            this.currentMapData.add("#222222");
        }
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        Scene scene = createScene();
        primaryStage.setScene(scene);
        primaryStage.setTitle("Map Creator");
        primaryStage.show();
    }

    public Scene createScene() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000;");

        Button backButton = new Button("Go Back");
        backButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 20; -fx-padding: 5 15;");
        backButton.setOnAction(e -> primaryStage.setScene(previousScene));
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(15));

        VBox mainContent = new VBox(20);
        mainContent.setStyle("-fx-background-color: #000;");
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Map Creator");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        HBox generationControls = new HBox(10);
        generationControls.setAlignment(Pos.CENTER);
        Button generateButton = new Button("Generate");
        generateButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-padding: 5 15;");
        ComboBox<String> mapTypeComboBox = new ComboBox<>();
        mapTypeComboBox.getItems().addAll("Town", "City", "Overworld", "Battle", "Cave", "Dungeon");
        mapTypeComboBox.getSelectionModel().selectFirst();
        mapTypeComboBox.setStyle("-fx-background-color: #333; -fx-text-fill: #fff;");
        generateButton.setOnAction(e -> generateMap(mapTypeComboBox.getValue()));
        generationControls.getChildren().addAll(generateButton, mapTypeComboBox);

        HBox fileControls = new HBox(10);
        fileControls.setAlignment(Pos.CENTER);
        Button saveButton = new Button("Save Map");
        saveButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-padding: 5 15;");
        saveButton.setOnAction(e -> saveMap());
        Button loadButton = new Button("Load Map");
        loadButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-padding: 5 15;");
        loadButton.setOnAction(e -> loadMap());
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-padding: 5 15;");
        deleteButton.setOnAction(e -> clearMap());
        fileControls.getChildren().addAll(saveButton, loadButton, deleteButton);

        HBox colorControls = new HBox(10);
        colorControls.setAlignment(Pos.CENTER);
        Label paintLabel = new Label("Paint with:");
        paintLabel.setTextFill(Color.web("#ff0000"));
        ComboBox<String> colorPickerComboBox = new ComboBox<>();
        colorPickerComboBox.getItems().addAll(
                "Green (grass)",
                "Blue (water)",
                "Black (out of bounds)",
                "Brown (path/dirt)",
                "Red (hazard/house)",
                "Grey (stone)"
        );
        colorPickerComboBox.getSelectionModel().selectFirst();
        colorPickerComboBox.setStyle("-fx-background-color: #333; -fx-text-fill: #fff;");
        colorPickerComboBox.setOnAction(e -> {
            String selected = colorPickerComboBox.getValue();
            switch (selected) {
                case "Green (grass)": selectedColorHex = "#008000"; break;
                case "Blue (water)": selectedColorHex = "#0000FF"; break;
                case "Black (out of bounds)": selectedColorHex = "#000000"; break;
                case "Brown (path/dirt)": selectedColorHex = "#A52A2A"; break;
                case "Red (hazard/house)": selectedColorHex = "#FF0000"; break;
                case "Grey (stone)": selectedColorHex = "#808080"; break;
            }
        });
        colorControls.getChildren().addAll(paintLabel, colorPickerComboBox);

        canvas = new Canvas((COL_COUNT * 1.5 + 0.5) * HEX_SIZE, (ROW_COUNT + 0.5) * hexHeight);
        gc = canvas.getGraphicsContext2D();
        canvas.setOnMouseClicked(this::handleCanvasClick);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(mainContent);
        scrollPane.setStyle("-fx-background-color: #000;");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        mapNameInput = new TextField();
        mapNameInput.setPromptText("Enter map name...");
        mapNameInput.setMaxWidth(200);
        mapNameInput.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: #fff; -fx-prompt-text-fill: #888; -fx-border-color: #ff0000; -fx-border-radius: 5; -fx-padding: 10;");

        statusLabel = new Label("Map Creator Ready.");
        statusLabel.setTextFill(Color.web("#d3d3d3"));

        mainContent.getChildren().addAll(title, generationControls, fileControls, colorControls, mapNameInput, statusLabel, canvas);
        root.getChildren().addAll(scrollPane, backButton);

        drawGrid();

        return new Scene(root);
    }

    private void handleCanvasClick(MouseEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();

        double q = Math.floor((mouseX / (HEX_SIZE * 1.5)));
        double rOffset = (q % 2) * (hexHeight / 2);
        double r = Math.floor((mouseY - rOffset) / hexHeight);

        if (q >= 0 && q < COL_COUNT && r >= 0 && r < ROW_COUNT) {
            int index = (int) (r * COL_COUNT + q);
            if (index < currentMapData.size()) {
                currentMapData.set(index, selectedColorHex);
                drawHex((int) q, (int) r, Color.web(selectedColorHex));
            }
        }
    }

    private void drawHex(int q, int r, Color color) {
        double xOffset = q * HEX_SIZE * 1.5;
        double yOffset = r * hexHeight + (q % 2) * (hexHeight / 2);
        double x = hexWidth / 2 + xOffset;
        double y = hexHeight / 2 + yOffset;

        double[] xPoints = new double[6];
        double[] yPoints = new double[6];
        for (int i = 0; i < 6; i++) {
            xPoints[i] = x + HEX_SIZE * Math.cos(Math.toRadians(60 * i));
            yPoints[i] = y + HEX_SIZE * Math.sin(Math.toRadians(60 * i));
        }

        gc.setFill(color);
        gc.fillPolygon(xPoints, yPoints, 6);
        gc.setStroke(color);
        gc.strokePolygon(xPoints, yPoints, 6);
    }

    private void drawGrid() {
        for (int r = 0; r < ROW_COUNT; r++) {
            for (int q = 0; q < COL_COUNT; q++) {
                int index = r * COL_COUNT + q;
                if (index < currentMapData.size()) {
                    Color color = Color.web(currentMapData.get(index));
                    drawHex(q, r, color);
                }
            }
        }
    }

    private void clearMap() {
        for (int i = 0; i < currentMapData.size(); i++) {
            currentMapData.set(i, "#222222");
        }
        drawGrid();
        statusLabel.setText("Map cleared.");
        statusLabel.setTextFill(Color.web("#d3d3d3"));
    }

    private void generateMap(String mapType) {
        currentMapData.clear();
        for (int i = 0; i < COL_COUNT * ROW_COUNT; i++) {
            currentMapData.add("#222222");
        }

        switch (mapType) {
            case "Town":
                generateTown();
                break;
            case "City":
                generateCity();
                break;
            case "Overworld":
                generateOverworld();
                break;
            case "Battle":
                generateBattle();
                break;
            case "Cave":
                generateCave();
                break;
            case "Dungeon":
                generateDungeon();
                break;
            default:
                break;
        }

        drawGrid();
        statusLabel.setText(mapType + " map generated.");
        statusLabel.setTextFill(Color.web("#d3d3d3"));
    }

    private void generateTown() {
        for (int i = 0; i < COL_COUNT * ROW_COUNT; i++) {
            currentMapData.set(i, "#008000"); // Green base
        }

        int houseCount = random.nextInt(6) + 5; // 5 to 10 houses
        List<int[]> housePositions = new ArrayList<>();
        int attempts = 0;

        int townSizeX = 40;
        int townSizeY = 40;
        int townStartX = COL_COUNT / 2 - townSizeX / 2;
        int townStartY = ROW_COUNT / 2 - townSizeY / 2;

        while (housePositions.size() < houseCount && attempts < 100) {
            int houseWidth = random.nextInt(7) + 4;
            int houseHeight = random.nextInt(7) + 4;
            int startX = townStartX + random.nextInt(townSizeX - houseWidth);
            int startY = townStartY + random.nextInt(townSizeY - houseHeight);

            boolean overlaps = false;
            for (int[] pos : housePositions) {
                if (startX < pos[0] + pos[2] && startX + houseWidth > pos[0] &&
                        startY < pos[1] + pos[3] && startY + houseHeight > pos[1]) {
                    overlaps = true;
                    break;
                }
            }

            if (!overlaps) {
                housePositions.add(new int[]{startX, startY, houseWidth, houseHeight});
                for (int y = startY; y < startY + houseHeight; y++) {
                    for (int x = startX; x < startX + houseWidth; x++) {
                        currentMapData.set(y * COL_COUNT + x, "#FF0000"); // Red house
                    }
                }
            }
            attempts++;
        }

        // Generate stone paths
        for (int y = townStartY; y < townStartY + townSizeY; y++) {
            for (int x = townStartX; x < townStartX + townSizeX; x++) {
                if (!currentMapData.get(y * COL_COUNT + x).equals("#FF0000")) {
                    currentMapData.set(y * COL_COUNT + x, "#808080");
                }
            }
        }
    }

    private void generateCity() {
        for (int i = 0; i < COL_COUNT * ROW_COUNT; i++) {
            currentMapData.set(i, "#008000"); // Green base
        }

        // Castle
        int castleSize = 15;
        int castleStartX = COL_COUNT / 2 - castleSize / 2;
        int castleStartY = ROW_COUNT / 2 - castleSize / 2;
        for (int y = castleStartY; y < castleStartY + castleSize; y++) {
            for (int x = castleStartX; x < castleStartX + castleSize; x++) {
                currentMapData.set(y * COL_COUNT + x, "#808080"); // Grey castle
            }
        }

        // Moat
        for (int y = castleStartY - 1; y <= castleStartY + castleSize; y++) {
            for (int x = castleStartX - 1; x <= castleStartX + castleSize; x++) {
                if ((x == castleStartX - 1 || x == castleStartX + castleSize ||
                        y == castleStartY - 1 || y == castleStartY + castleSize) &&
                        (y * COL_COUNT + x) >= 0 && (y * COL_COUNT + x) < COL_COUNT*ROW_COUNT) {
                    currentMapData.set(y * COL_COUNT + x, "#0000FF"); // Blue
                }
            }
        }

        // Moat gap (2 tiles wide, facing south)
        currentMapData.set((castleStartY + castleSize) * COL_COUNT + (castleStartX + castleSize / 2 -1), "#808080");
        currentMapData.set((castleStartY + castleSize) * COL_COUNT + (castleStartX + castleSize / 2), "#808080");
    }

    private void generateOverworld() {
        for (int i = 0; i < COL_COUNT * ROW_COUNT; i++) {
            currentMapData.set(i, "#008000"); // Green
        }
        // Rivers
        int riverCount = random.nextInt(3) + 2;
        for (int i = 0; i < riverCount; i++) {
            int startX = random.nextInt(COL_COUNT);
            int startY = 0;
            int endX = random.nextInt(COL_COUNT);
            int endY = ROW_COUNT - 1;
            drawPath(startX, startY, endX, endY, random.nextInt(3) + 2, "#0000FF");
        }
        // Lakes
        int lakeCount = random.nextInt(4) + 3;
        for (int i = 0; i < lakeCount; i++) {
            int startX = random.nextInt(COL_COUNT);
            int startY = random.nextInt(ROW_COUNT);
            int radius = random.nextInt(4) + 3;
            drawCircle(startX, startY, radius, "#0000FF");
        }
        // Towns and Cities
        int townCount = random.nextInt(6) + 10;
        int cityCount = random.nextInt(3) + 3;
        List<int[]> settlements = new ArrayList<>();
        for (int i = 0; i < townCount; i++) {
            int x = random.nextInt(COL_COUNT);
            int y = random.nextInt(ROW_COUNT);
            currentMapData.set(y * COL_COUNT + x, "#FF0000");
            settlements.add(new int[]{x, y});
        }
        for (int i = 0; i < cityCount; i++) {
            int x = random.nextInt(COL_COUNT);
            int y = random.nextInt(ROW_COUNT);
            currentMapData.set(y * COL_COUNT + x, "#808080");
            settlements.add(new int[]{x, y});
        }
        // Paths
        for (int[] start : settlements) {
            int[] nearest = null;
            double minDist = Double.MAX_VALUE;
            for (int[] end : settlements) {
                if (start != end) {
                    double dist = Math.sqrt(Math.pow(start[0] - end[0], 2) + Math.pow(start[1] - end[1], 2));
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = end;
                    }
                }
            }
            if (nearest != null) {
                drawPath(start[0], start[1], nearest[0], nearest[1], 1, "#A52A2A");
            }
        }
    }

    private void generateBattle() {
        for (int i = 0; i < COL_COUNT * ROW_COUNT; i++) {
            currentMapData.set(i, "#008000"); // Green
        }
        // River
        if (random.nextDouble() < 0.1) {
            int startY = ROW_COUNT / 2 - 5;
            for (int y = startY; y < startY + 10; y++) {
                for (int x = 0; x < COL_COUNT; x++) {
                    currentMapData.set(y * COL_COUNT + x, "#0000FF");
                }
            }
            // Path through river
            int startX = COL_COUNT / 2 - 2;
            for (int x = startX; x < startX + 4; x++) {
                for (int y = 0; y < ROW_COUNT; y++) {
                    currentMapData.set(y * COL_COUNT + x, "#A52A2A");
                }
            }
            // Hazards
            int hazardCount = random.nextInt(7) + 4;
            for (int i = 0; i < hazardCount; i++) {
                int x = random.nextInt(COL_COUNT);
                int y = random.nextInt(startY);
                currentMapData.set(y * COL_COUNT + x, "#FF0000");
            }
        } else {
            // Hazards
            int hazardCount = random.nextInt(7) + 4;
            for (int i = 0; i < hazardCount; i++) {
                int x = random.nextInt(COL_COUNT);
                int y = random.nextInt(ROW_COUNT);
                currentMapData.set(y * COL_COUNT + x, "#FF0000");
            }
        }
    }

    private void generateCave() {
        for (int i = 0; i < COL_COUNT * ROW_COUNT; i++) {
            currentMapData.set(i, "#444444"); // Dark grey
        }

        int chamberCount = random.nextInt(10) + 5;
        List<int[]> chamberCenters = new ArrayList<>();
        int startX = COL_COUNT / 2;
        int startY = ROW_COUNT - 10;
        chamberCenters.add(new int[]{startX, startY});
        drawCircle(startX, startY, random.nextInt(5) + 5, "#888888");

        for (int i = 1; i < chamberCount; i++) {
            int[] previousCenter = chamberCenters.get(i - 1);
            int nextX = previousCenter[0] + random.nextInt(40) - 20;
            int nextY = previousCenter[1] - random.nextInt(20) - 10;
            drawCircle(nextX, nextY, random.nextInt(5) + 5, "#888888");
            drawPath(previousCenter[0], previousCenter[1], nextX, nextY, random.nextInt(4) + 2, "#888888");
            chamberCenters.add(new int[]{nextX, nextY});
        }
    }

    private void generateDungeon() {
        for (int i = 0; i < COL_COUNT * ROW_COUNT; i++) {
            currentMapData.set(i, "#333333"); // Darker grey
        }
        int centerX = COL_COUNT / 2;
        int centerY = ROW_COUNT / 2;

        List<int[]> roomsToGenerate = new ArrayList<>();
        roomsToGenerate.add(new int[]{centerX, centerY});

        while (!roomsToGenerate.isEmpty()) {
            int[] currentRoom = roomsToGenerate.remove(0);
            int roomX = currentRoom[0];
            int roomY = currentRoom[1];
            int roomWidth = random.nextInt(10) + 5;
            int roomHeight = random.nextInt(10) + 5;
            int startX = roomX - roomWidth / 2;
            int startY = roomY - roomHeight / 2;

            for (int j = startY; j < startY + roomHeight; j++) {
                for (int i = startX; i < startX + roomWidth; i++) {
                    if (i >= 0 && i < COL_COUNT && j >= 0 && j < ROW_COUNT) {
                        currentMapData.set(j * COL_COUNT + i, "#666666"); // Lighter grey
                    }
                }
            }

            for (int i = 0; i < 4; i++) {
                if (random.nextDouble() < 0.5) {
                    int nextX = roomX;
                    int nextY = roomY;
                    switch (i) {
                        case 0: nextX = startX - 5; break;
                        case 1: nextX = startX + roomWidth + 5; break;
                        case 2: nextY = startY - 5; break;
                        case 3: nextY = startY + roomHeight + 5; break;
                    }
                    if (nextX >= 0 && nextX < COL_COUNT && nextY >= 0 && nextY < ROW_COUNT &&
                            currentMapData.get(nextY * COL_COUNT + nextX).equals("#333333")) {
                        drawPath(roomX, roomY, nextX, nextY, 1, "#666666");
                        roomsToGenerate.add(new int[]{nextX, nextY});
                    }
                }
            }
        }
    }

    private void drawPath(int startX, int startY, int endX, int endY, int thickness, String color) {
        double dx = endX - startX;
        double dy = endY - startY;
        double steps = Math.max(Math.abs(dx), Math.abs(dy));
        double xIncrement = dx / steps;
        double yIncrement = dy / steps;
        for (int i = 0; i <= steps; i++) {
            int x = (int) (startX + i * xIncrement);
            int y = (int) (startY + i * yIncrement);
            drawCircle(x, y, thickness / 2, color);
        }
    }

    private void drawCircle(int centerX, int centerY, int radius, String color) {
        for (int y = centerY - radius; y <= centerY + radius; y++) {
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
                if (distance <= radius && x >= 0 && x < COL_COUNT && y >= 0 && y < ROW_COUNT) {
                    currentMapData.set(y * COL_COUNT + x, color);
                }
            }
        }
    }

    private void saveMap() {
        String mapName = mapNameInput.getText().trim();
        if (mapName.isEmpty()) {
            statusLabel.setText("Please enter a map name.");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        File directory = new File(MY_MAPS_DIRECTORY);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                statusLabel.setText("Error: Could not create 'my-maps' directory.");
                statusLabel.setTextFill(Color.RED);
                return;
            }
        }

        File file = new File(directory, mapName + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            MapData mapData = new MapData(ROW_COUNT, COL_COUNT, currentMapData);
            gson.toJson(mapData, writer);
            statusLabel.setText("Map saved successfully to " + file.getName());
            statusLabel.setTextFill(Color.GREEN);
        } catch (IOException ex) {
            statusLabel.setText("Error saving file: " + ex.getMessage());
            statusLabel.setTextFill(Color.RED);
        }
    }

    private void loadMap() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Map File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File myMapsDir = new File(MY_MAPS_DIRECTORY);
        if (myMapsDir.exists() && myMapsDir.isDirectory()) {
            fileChooser.setInitialDirectory(myMapsDir);
        }
        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            try (FileReader reader = new FileReader(file)) {
                Gson gson = new Gson();
                MapData mapData = gson.fromJson(reader, MapData.class);

                if (mapData != null && mapData.getGrid() != null && mapData.getGrid().size() == COL_COUNT * ROW_COUNT) {
                    currentMapData = mapData.getGrid();
                    drawGrid();
                    statusLabel.setText("Map loaded successfully from " + file.getName());
                    statusLabel.setTextFill(Color.GREEN);
                } else {
                    statusLabel.setText("Invalid map file format or size.");
                    statusLabel.setTextFill(Color.RED);
                }

            } catch (IOException ex) {
                statusLabel.setText("Error loading file: " + ex.getMessage());
                statusLabel.setTextFill(Color.RED);
            }
        }
    }
}