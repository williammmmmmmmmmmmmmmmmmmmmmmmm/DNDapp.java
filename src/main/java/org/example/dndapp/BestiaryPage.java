package org.example.dndapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BestiaryPage {
    private final Stage primaryStage;
    private final Scene previousScene;
    private final List<Monster> allMonsters = new ArrayList<>();
    private Accordion monsterAccordion;

    private static final String BG_BLACK = "#000000";
    private static final String SECTION_BG = "#1a1a1a";
    private static final String ACCENT_RED = "#FF3333";
    private static final String TEXT_GOLD = "#FFD700";

    public BestiaryPage(Stage primaryStage, Scene previousScene) {
        this.primaryStage = primaryStage;
        this.previousScene = previousScene;
        loadMonstersFromCSV();
    }

    public Scene createScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + BG_BLACK + ";");
        root.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("D&D BESTIARY");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.web(ACCENT_RED));

        TextField searchField = new TextField();
        searchField.setPromptText("Type and press ENTER to search...");
        searchField.setStyle(
                "-fx-background-color: #222; " +
                        "-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: #777; " +
                        "-fx-border-color: " + ACCENT_RED + "; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5;"
        );
        searchField.setMaxWidth(500);

        // This makes it work when you press ENTER
        searchField.setOnAction(e -> updateList(searchField.getText()));

        // This keeps the "as you type" functionality
        searchField.setOnKeyReleased(e -> {
            if (searchField.getText().length() > 2 || searchField.getText().isEmpty()) {
                updateList(searchField.getText());
            }
        });

        monsterAccordion = new Accordion();

        ScrollPane scrollPane = new ScrollPane(monsterAccordion);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);
        // Force scrollpane to stay black
        scrollPane.setStyle("-fx-background: black; -fx-background-color: black; -fx-viewport-background-color: black; -fx-border-color: #333;");

        Button backBtn = new Button("Back to Encounters");
        backBtn.setStyle("-fx-background-color: " + ACCENT_RED + "; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 10 30; -fx-font-weight: bold; -fx-background-radius: 20;");
        backBtn.setOnAction(e -> primaryStage.setScene(previousScene));

        root.getChildren().addAll(title, searchField, scrollPane, backBtn);
        updateList("");

        return new Scene(root, 1000, 850);
    }

    private void loadMonstersFromCSV() {
        try (InputStream is = getClass().getResourceAsStream("/Beastiary - Monsters.csv")) {
            if (is == null) return;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                br.readLine(); br.readLine(); br.readLine();
                String line;
                while ((line = br.readLine()) != null) {
                    String[] d = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    if (d.length > 5) allMonsters.add(new Monster(d));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateList(String filter) {
        monsterAccordion.getPanes().clear();
        String f = filter.toLowerCase().trim();

        List<Monster> filtered = allMonsters.stream()
                .filter(m -> m.getName().toLowerCase().contains(f) ||
                        m.getType().toLowerCase().contains(f) ||
                        m.getFormattedStats().toLowerCase().contains(f))
                .limit(60)
                .collect(Collectors.toList());

        for (Monster m : filtered) {
            TitledPane pane = new TitledPane();
            pane.setText(String.format("%s (CR %s)", m.getName(), m.getCr()));

            // CSS to force the TitledPane to stay DARK and avoid the white default look
            pane.setStyle(
                    "-fx-base: black; " +
                            "-fx-text-fill: " + (m.isLegendary() ? TEXT_GOLD : "white") + "; " +
                            "-fx-focus-color: transparent; " +
                            "-fx-faint-focus-color: transparent;"
            );

            Label stats = new Label(m.getFormattedStats());
            stats.setTextFill(Color.WHITE);
            stats.setPadding(new Insets(15));
            stats.setFont(Font.font("Monospaced", 14));
            stats.setWrapText(true);

            VBox content = new VBox(stats);
            // Black background for the interior of the drop-box
            content.setStyle("-fx-background-color: black; -fx-border-color: " + ACCENT_RED + "; -fx-border-width: 1 0 0 0;");
            pane.setContent(content);

            monsterAccordion.getPanes().add(pane);
        }
    }
    public List<Monster> getAllMonsters() {
        return allMonsters;
    }
}