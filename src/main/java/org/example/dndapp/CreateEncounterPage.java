package org.example.dndapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.List;
import java.util.stream.Collectors;

public class CreateEncounterPage {
    private final Stage stage;
    private final Scene prev;
    private final List<Encounter> savedList;
    private final Encounter currentEncounter = new Encounter("New Encounter");
    private final VBox cartBox = new VBox(5);
    private final BestiaryPage dataLoader;

    public CreateEncounterPage(Stage stage, Scene prev, List<Encounter> savedList) {
        this.stage = stage;
        this.prev = prev;
        this.savedList = savedList;
        this.dataLoader = new BestiaryPage(stage, prev);
    }

    public Scene createScene() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000;");

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(50, 20, 20, 20));
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 10; -fx-border-color: #ff0000; -fx-border-width: 2; -fx-border-radius: 10;");
        mainContent.setMaxWidth(900);
        mainContent.setMaxHeight(700);

        Label title = new Label("CREATE ENCOUNTER");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        // Button style copied exactly from DMPage
        String blueButtonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";

        HBox split = new HBox(30);
        split.setAlignment(Pos.CENTER);

        // Left Side: Search & Results
        VBox left = new VBox(10);
        left.setPrefWidth(400);

        TextField search = new TextField();
        search.setPromptText("Search Monsters...");
        search.setStyle("-fx-background-color: #222; -fx-text-fill: white; -fx-border-color: #444;");

        ListView<Monster> results = new ListView<>();
        results.setPrefHeight(350);
        results.setStyle("-fx-control-inner-background: #111; -fx-text-fill: white;");

        // FIX: This CellFactory ensures the NAME shows up, not the object ID
        results.setCellFactory(lv -> new ListCell<Monster>() {
            @Override
            protected void updateItem(Monster item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (CR " + item.getCr() + ")");
                }
            }
        });

        search.setOnKeyReleased(e -> {
            String f = search.getText().toLowerCase();
            results.getItems().clear();
            results.getItems().addAll(dataLoader.getAllMonsters().stream()
                    .filter(m -> m.getName() != null && m.getName().toLowerCase().contains(f))
                    .limit(20).collect(Collectors.toList()));
        });

        Button addBtn = new Button("Add to Encounter");
        addBtn.setStyle(blueButtonStyle); // Blue style from DMPage
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> {
            Monster m = results.getSelectionModel().getSelectedItem();
            if (m != null) {
                currentEncounter.addMonster(m);
                Label l = new Label("• " + m.getName() + " (CR " + m.getCr() + ")");
                l.setTextFill(Color.WHITE);
                cartBox.getChildren().add(l);
            }
        });
        left.getChildren().addAll(new Label("Search Bestiary:"), search, results, addBtn);

        // Right Side: Cart & Save
        VBox right = new VBox(10);
        right.setPrefWidth(300);

        TextField nameField = new TextField("Untitled Encounter");
        nameField.setStyle("-fx-background-color: #222; -fx-text-fill: white;");

        ScrollPane scroll = new ScrollPane(cartBox);
        scroll.setPrefHeight(350);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #111; -fx-background-color: #111;");
        cartBox.setStyle("-fx-background-color: #111; -fx-padding: 10;");

        Button saveBtn = new Button("SAVE ENCOUNTER");
        saveBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> {
            currentEncounter.setName(nameField.getText());
            savedList.add(currentEncounter);
            stage.setScene(prev);
        });

        right.getChildren().addAll(new Label("Encounter Name:"), nameField, new Label("Added Monsters:"), scroll, saveBtn);

        split.getChildren().addAll(left, right);
        mainContent.getChildren().addAll(title, split);

        // Green Back Button in Top Left (Exact style from DMPage)
        Button backButton = new Button("Go Back");
        backButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 20; -fx-padding: 5 15;");
        backButton.setOnAction(e -> stage.setScene(prev));

        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(15));

        root.getChildren().addAll(mainContent, backButton);
        return new Scene(root, 1000, 800);
    }
}