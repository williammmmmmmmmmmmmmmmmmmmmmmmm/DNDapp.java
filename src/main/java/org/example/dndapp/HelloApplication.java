package org.example.dndapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    private final WebSocketService webSocketService = new WebSocketService();
    private Scene campaignsScene;
    private Scene homeScene;
    private Scene dmScene;
    private Scene accountScene;
    private Scene playerScene;

    @Override
    public void start(Stage stage) {
        // Set the window to be maximized instead of fullscreen
        stage.setMaximized(true);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50, 20, 20, 20));
        root.setStyle("-fx-background-color: #000;");

        Label title = new Label("Welcome to Williams D&D app!");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        GridPane buttonGrid = new GridPane();
        buttonGrid.setAlignment(Pos.CENTER);
        buttonGrid.setHgap(15);
        buttonGrid.setVgap(15);

        Button campaignsButton = new Button("Campaigns");
        Button dmButton = new Button("Dungeon Master");
        Button accountButton = new Button("Account");
        Button playerButton = new Button("Player Tools");

        String buttonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";
        campaignsButton.setStyle(buttonStyle);
        dmButton.setStyle(buttonStyle);
        accountButton.setStyle(buttonStyle);
        playerButton.setStyle(buttonStyle);

        homeScene = new Scene(root, 1000, 800);

        // --- DEPENDENCY INITIALIZATION AND FIXES ---
        // CampaignsPage and its Scene must be initialized first because it is a dependency for PlayerPage and MapsPage
        CampaignsPage campaignsPage = new CampaignsPage(stage, homeScene, webSocketService);
        campaignsScene = campaignsPage.createScene();

        // Uncommented and re-instantiated
        // DMPage and AccountPage instantiation assumed to be present for compilation
        DMPage dmPage = new DMPage(stage, homeScene);
        dmScene = dmPage.createScene();

        AccountPage accountPage = new AccountPage(stage, homeScene);
        accountScene = accountPage.createScene();

        // FIX: PlayerPage now requires WebSocketService and CampaignsPage
        PlayerPage playerPage = new PlayerPage(stage, homeScene, webSocketService, campaignsPage);
        playerScene = playerPage.createScene();
        // --- END DEPENDENCY INITIALIZATION AND FIXES ---

        campaignsButton.setOnAction(e -> {
            stage.setScene(campaignsScene);
            stage.setTitle("Campaigns");
        });

        dmButton.setOnAction(e -> {
            stage.setScene(dmScene);
            stage.setTitle("Dungeon Master");
        });

        accountButton.setOnAction(e -> {
            stage.setScene(accountScene);
            stage.setTitle("Account Page");
        });

        playerButton.setOnAction(e -> {
            stage.setScene(playerScene);
            stage.setTitle("Player Tools");
        });

        buttonGrid.add(accountButton, 0, 0);
        buttonGrid.add(campaignsButton, 1, 0);
        buttonGrid.add(dmButton, 0, 1);
        buttonGrid.add(playerButton, 1, 1);

        root.getChildren().addAll(title, buttonGrid);

        // Global Key Bindings for easy navigation
        // F1: Home (escape all views)
        homeScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F1) {
                stage.setScene(homeScene);
                stage.setTitle("Welcome to Williams D&D app!");
            }
        });

        stage.setScene(homeScene);
        stage.setTitle("Welcome to Williams D&D app!");
        stage.show();

        // Connect the WebSocket service on application start
        webSocketService.connect();

        // Clean up WebSocket connection when the stage is closed
        stage.setOnCloseRequest(event -> webSocketService.disconnect());
    }

    public static void main(String[] args) {
        launch();
    }
}