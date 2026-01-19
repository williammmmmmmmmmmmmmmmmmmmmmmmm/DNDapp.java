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

public class PlayerPage {
    private final Stage primaryStage;
    private final Scene homeScene;
    // REQUIRED ADDITIONS
    private final WebSocketService webSocketService;
    private final CampaignsPage campaignsPage;
    // END REQUIRED ADDITIONS

    // REQUIRED CHANGE: Updated constructor with 4 arguments
    public PlayerPage(Stage primaryStage, Scene homeScene, WebSocketService webSocketService, CampaignsPage campaignsPage) {
        this.primaryStage = primaryStage;
        this.homeScene = homeScene;
        this.webSocketService = webSocketService;
        this.campaignsPage = campaignsPage;
    }
    // END REQUIRED CHANGE

    public Scene createScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50, 20, 20, 20));
        root.setStyle("-fx-background-color: #000;");

        Label title = new Label("Player Tools");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        GridPane buttonGrid = new GridPane();
        buttonGrid.setAlignment(Pos.CENTER);
        buttonGrid.setHgap(15);
        buttonGrid.setVgap(15);

        Button myCharactersButton = new Button("Characters");
        Button mapsButton = new Button("Maps");
        Button spellGuideButton = new Button("Spell Guide");
        Button backButton = new Button("Go Back");

        String buttonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";
        myCharactersButton.setStyle(buttonStyle);
        mapsButton.setStyle(buttonStyle);
        spellGuideButton.setStyle(buttonStyle);
        backButton.setStyle(buttonStyle);

        mapsButton.setOnAction(e -> {
            // FIX: MapsPage now needs 4 arguments
            MapsPage mapsPage = new MapsPage(primaryStage, createScene(), webSocketService, campaignsPage);
            AppController.goTo(mapsPage.createScene());
            primaryStage.setTitle("Maps");
        });

        myCharactersButton.setOnAction(e -> {
            MyCharactersPage myCharactersPage = new MyCharactersPage(primaryStage, createScene());
            AppController.goTo(myCharactersPage.createScene());
            primaryStage.setTitle("My Characters");
        });
        spellGuideButton.setOnAction(e -> {
            SpellGuidePage spellGuidePage = new SpellGuidePage(primaryStage, createScene());
            AppController.goTo(spellGuidePage.createScene());
            primaryStage.setTitle("Spell Guide");
        });

        backButton.setOnAction(e -> AppController.goTo(homeScene));

        buttonGrid.add(myCharactersButton, 0, 0);
        buttonGrid.add(mapsButton, 1, 0);
        buttonGrid.add(spellGuideButton, 0, 1);
        buttonGrid.add(backButton, 1, 1);

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(title, buttonGrid);

        root.getChildren().add(content);

        //Button BackButton = new Button("Go Back");
        //BackButton.setOnAction(e -> AppController.goTo(homeScene));
        //StackPane.setAlignment(BackButton, Pos.TOP_LEFT);
        //StackPane.setMargin(BackButton, new Insets(15));

        //root.getChildren().add(BackButton);

        return new Scene(root, 1000, 800);
    }
}