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
import java.util.*;
import java.util.stream.Collectors;

public class RandomEncounterPage {
    private final Stage stage;
    private final Scene prev;
    private final BestiaryPage data;
    private final List<Encounter> globalList;
    private Encounter lastGenerated;

    public RandomEncounterPage(Stage stage, Scene prev, List<Encounter> globalList) {
        this.stage = stage;
        this.prev = prev;
        this.data = new BestiaryPage(stage, prev);
        this.globalList = globalList;
    }

    public Scene createScene() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000;");

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(50));
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #ff0000; -fx-border-width: 2;");
        mainContent.setMaxWidth(700);

        ComboBox<String> envBox = new ComboBox<>();
        envBox.getItems().addAll("Arctic", "Coastal", "Desert", "Forest", "Grassland", "Hill", "Mountain", "Swamp", "Underdark", "Underwater", "Urban");
        envBox.setPromptText("Select Environment");

        ComboBox<String> diffBox = new ComboBox<>();
        diffBox.getItems().addAll("Easy", "Medium", "Hard", "Deadly");
        diffBox.setPromptText("Select Difficulty");

        TextArea results = new TextArea();
        results.setEditable(false);
        results.setPrefHeight(300);
        results.setStyle("-fx-control-inner-background: #000; -fx-text-fill: #00ff00; -fx-font-family: 'Monospaced';");

        Button genBtn = new Button("GENERATE");
        genBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30;");

        Button saveBtn = new Button("SAVE ENCOUNTER");
        saveBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30;");
        saveBtn.setDisable(true);

        genBtn.setOnAction(e -> {
            String env = envBox.getValue();
            String diff = diffBox.getValue();
            if (env == null || diff == null) return;

            List<Monster> pool = data.getAllMonsters().stream()
                    .filter(m -> m.getFormattedStats().contains(env))
                    .filter(m -> matchesDifficulty(m, diff))
                    .collect(Collectors.toList());

            if (pool.isEmpty()) {
                results.setText("No monsters match " + env + " (" + diff + ")");
                saveBtn.setDisable(true);
            } else {
                Collections.shuffle(pool);
                lastGenerated = new Encounter("Random " + env + " (" + diff + ")");
                StringBuilder sb = new StringBuilder("=== " + env.toUpperCase() + " ===\n\n");
                for (int i = 0; i < new Random().nextInt(3) + 1; i++) {
                    Monster m = pool.get(i % pool.size());
                    lastGenerated.addMonster(m);
                    sb.append("• ").append(m.getName()).append(" (CR ").append(m.getCr()).append(")\n");
                }
                results.setText(sb.toString());
                saveBtn.setDisable(false);
            }
        });

        saveBtn.setOnAction(e -> {
            if (lastGenerated != null) {
                globalList.add(lastGenerated);
                EncountersPage.saveToDisk();
                results.appendText("\n[SAVED]");
                saveBtn.setDisable(true);
            }
        });

        HBox btns = new HBox(15, genBtn, saveBtn);
        btns.setAlignment(Pos.CENTER);

        mainContent.getChildren().addAll(envBox, diffBox, btns, results);

        Button backBtn = new Button("Go Back");
        backBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 20; -fx-padding: 5 15;");
        backBtn.setOnAction(e -> stage.setScene(prev));

        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        StackPane.setMargin(backBtn, new Insets(15));

        root.getChildren().addAll(mainContent, backBtn);
        return new Scene(root, 1000, 800);
    }

    private boolean matchesDifficulty(Monster m, String diff) {
        try {
            double cr = m.getCr().contains("/") ? (1.0 / Double.parseDouble(m.getCr().split("/")[1])) : Double.parseDouble(m.getCr());
            return switch (diff) {
                case "Easy" -> cr <= 1;
                case "Medium" -> cr > 1 && cr <= 5;
                case "Hard" -> cr > 5 && cr <= 12;
                case "Deadly" -> cr > 12;
                default -> true;
            };
        } catch (Exception e) { return false; }
    }
}