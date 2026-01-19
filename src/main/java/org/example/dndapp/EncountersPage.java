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
import java.util.List;

public class EncountersPage {
    private final Stage primaryStage;
    private final DMPage dmPage;

    // Load saved encounters from the file immediately
    private static final List<Encounter> savedEncounters = EncounterStorage.loadEncounters();

    public EncountersPage(Stage primaryStage, DMPage dmPage) {
        this.primaryStage = primaryStage;
        this.dmPage = dmPage;
    }

    // Call this whenever the list is changed (Add or Delete)
    public static void saveToDisk() {
        EncounterStorage.saveEncounters(savedEncounters);
    }

    public Scene createScene() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000;");

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(50, 20, 20, 20));
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 10; -fx-border-color: #ff0000; -fx-border-width: 2; -fx-border-radius: 10;");
        mainContent.setMaxWidth(800);

        Label title = new Label("ENCOUNTERS");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 48));
        title.setTextFill(Color.web("#ff0000"));

        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(25);
        buttonGrid.setVgap(25);
        buttonGrid.setAlignment(Pos.CENTER);

        String blueStyle = "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 18px; " +
                "-fx-font-weight: bold; -fx-min-width: 250px; -fx-min-height: 120px; " +
                "-fx-background-radius: 10; -fx-cursor: hand;";

        Button createBtn = new Button("Create New Encounter");
        Button myEncountersBtn = new Button("My Encounters");
        Button randomEncounterBtn = new Button("Random Generator");
        Button bestiaryBtn = new Button("Monster Bestiary");

        createBtn.setStyle(blueStyle);
        myEncountersBtn.setStyle(blueStyle);
        randomEncounterBtn.setStyle(blueStyle);
        bestiaryBtn.setStyle(blueStyle);

        createBtn.setOnAction(e -> AppController.goTo(new CreateEncounterPage(primaryStage, createScene(), savedEncounters).createScene()));
        myEncountersBtn.setOnAction(e -> AppController.goTo(new MyEncountersPage(primaryStage, createScene(), savedEncounters).createScene()));
        randomEncounterBtn.setOnAction(e -> AppController.goTo(new RandomEncounterPage(primaryStage, createScene(), savedEncounters).createScene()));
        bestiaryBtn.setOnAction(e -> AppController.goTo(new BestiaryPage(primaryStage, createScene()).createScene()));

        buttonGrid.add(createBtn, 0, 0);
        buttonGrid.add(myEncountersBtn, 1, 0);
        buttonGrid.add(randomEncounterBtn, 0, 1);
        buttonGrid.add(bestiaryBtn, 1, 1);

        mainContent.getChildren().addAll(title, buttonGrid);

        Button backBtn = new Button("Go Back");
        backBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 20; -fx-padding: 5 15;");
        backBtn.setOnAction(e -> AppController.goTo(dmPage.createScene()));

        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        StackPane.setMargin(backBtn, new Insets(15));

        root.getChildren().addAll(mainContent, backBtn);
        return new Scene(root, 1000, 800);
    }
}