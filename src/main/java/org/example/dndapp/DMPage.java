package org.example.dndapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class DMPage {

    private Stage primaryStage;
    private Scene homeScene;
    private WebSocketService webSocketService; // Add a field for the WebSocket service

    public DMPage(Stage primaryStage, Scene homeScene) { // Add WebSocketService to the constructor
        this.primaryStage = primaryStage;
        this.homeScene = homeScene;
        this.webSocketService = webSocketService; // Initialize the new field
    }

    public Scene createScene() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000;");

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(50, 20, 20, 20));
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 10; -fx-border-color: #ff0000; -fx-border-width: 2; -fx-border-radius: 10;");

        Label title = new Label("Dungeon Master's Tools");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        GridPane buttonGrid = new GridPane();
        buttonGrid.setAlignment(Pos.CENTER);
        buttonGrid.setHgap(15);
        buttonGrid.setVgap(15);

        Button mapCreatorBtn = new Button("Map Creator");
        Button myMapsBtn = new Button("My Maps");
        Button encountersBtn = new Button("Encounters");
        Button playersBtn = new Button("Players");

        String buttonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";
        mapCreatorBtn.setStyle(buttonStyle);
        myMapsBtn.setStyle(buttonStyle);
        encountersBtn.setStyle(buttonStyle);
        playersBtn.setStyle(buttonStyle);

        // Define button actions
        mapCreatorBtn.setOnAction(e -> {
            MapCreatorPage mapCreatorPage = new MapCreatorPage(primaryStage, createScene());
            primaryStage.setScene(mapCreatorPage.createScene());
        });
        playersBtn.setOnAction(e -> System.out.println("Players button clicked!"));

        // Navigation to MyMapsPage and EncountersPage
        myMapsBtn.setOnAction(e -> {
            MyMapsPage myMapsPage = new MyMapsPage(primaryStage, createScene(), webSocketService); // Corrected arguments
            primaryStage.setScene(myMapsPage.createScene());
        });

        encountersBtn.setOnAction(e -> {
            EncountersPage encountersPage = new EncountersPage(primaryStage, this);
            primaryStage.setScene(encountersPage.createScene());
        });

        buttonGrid.add(mapCreatorBtn, 0, 0);
        buttonGrid.add(myMapsBtn, 1, 0);
        buttonGrid.add(encountersBtn, 0, 1);
        buttonGrid.add(playersBtn, 1, 1);

        mainContent.getChildren().addAll(title, buttonGrid);

        Button backButton = new Button("Go Back");
        backButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 20; -fx-padding: 5 15;");
        backButton.setOnAction(e -> primaryStage.setScene(homeScene));
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(15));

        root.getChildren().addAll(mainContent, backButton);

        return new Scene(root);
    }
}