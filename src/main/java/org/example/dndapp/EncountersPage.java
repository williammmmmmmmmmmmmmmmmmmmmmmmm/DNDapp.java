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

public class EncountersPage {

    private Stage primaryStage;
    private DMPage dmPage; // Reference to the Dungeon Master page

    public EncountersPage(Stage primaryStage, DMPage dmPage) {
        this.primaryStage = primaryStage;
        this.dmPage = dmPage;
    }

    public Scene createScene() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000;");

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(50, 20, 20, 20));
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 10; -fx-border-color: #ff0000; -fx-border-width: 2; -fx-border-radius: 10;");

        Label title = new Label("Encounters");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        GridPane buttonGrid = new GridPane();
        buttonGrid.setAlignment(Pos.CENTER);
        buttonGrid.setHgap(15);
        buttonGrid.setVgap(15);

        Button createBtn = new Button("Create New Encounter");
        Button myEncountersBtn = new Button("My Encounters");
        Button randomEncounterBtn = new Button("Random Encounter");
        Button bestiaryBtn = new Button("Bestiary");

        String buttonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";
        createBtn.setStyle(buttonStyle);
        myEncountersBtn.setStyle(buttonStyle);
        randomEncounterBtn.setStyle(buttonStyle);
        bestiaryBtn.setStyle(buttonStyle);

        createBtn.setOnAction(e -> System.out.println("Create New Encounter button clicked!"));
        myEncountersBtn.setOnAction(e -> System.out.println("My Encounters button clicked!"));
        randomEncounterBtn.setOnAction(e -> System.out.println("Random Encounter button clicked!"));

        // Navigation to BestiaryPage
        bestiaryBtn.setOnAction(e -> {
            BestiaryPage bestiaryPage = new BestiaryPage(primaryStage, primaryStage.getScene());
            primaryStage.setScene(bestiaryPage.createScene());
        });

        buttonGrid.add(createBtn, 0, 0);
        buttonGrid.add(myEncountersBtn, 1, 0);
        buttonGrid.add(randomEncounterBtn, 0, 1);
        buttonGrid.add(bestiaryBtn, 1, 1);

        mainContent.getChildren().addAll(title, buttonGrid);

        Button backButton = new Button("Go Back");
        backButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 20; -fx-padding: 5 15;");
        backButton.setOnAction(e -> primaryStage.setScene(dmPage.createScene()));
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(15));

        root.getChildren().addAll(mainContent, backButton);

        return new Scene(root);
    }
}