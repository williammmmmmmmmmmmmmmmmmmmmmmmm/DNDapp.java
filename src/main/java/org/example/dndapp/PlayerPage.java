package org.example.dndapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class PlayerPage {
    private final Stage primaryStage;
    private final Scene homeScene;

    public PlayerPage(Stage primaryStage, Scene homeScene) {
        this.primaryStage = primaryStage;
        this.homeScene = homeScene;
    }

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
            MapsPage mapsPage = new MapsPage(primaryStage, createScene());
            primaryStage.setScene(mapsPage.createScene());
            primaryStage.setTitle("Maps");
        });

        myCharactersButton.setOnAction(e -> {
            MyCharactersPage myCharactersPage = new MyCharactersPage(primaryStage, createScene());
            primaryStage.setScene(myCharactersPage.createScene());
            primaryStage.setTitle("My Characters");
        });

        backButton.setOnAction(e -> primaryStage.setScene(homeScene));

        buttonGrid.add(myCharactersButton, 0, 0);
        buttonGrid.add(mapsButton, 1, 0);;

        root.getChildren().addAll(backButton, title, buttonGrid);

        return new Scene(root);
    }
}