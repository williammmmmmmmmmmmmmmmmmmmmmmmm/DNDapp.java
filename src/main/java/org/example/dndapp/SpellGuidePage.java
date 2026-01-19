package org.example.dndapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SpellGuidePage {
    private final Stage primaryStage;
    private final Scene previousScene;
    private final VBox root;

    // Consistency with CharacterSheetPage theme
    private static final String BG_BLACK = "#000000";
    private static final String SECTION_BG_DARK = "#1a1a1a";
    private static final String ACCENT_RED = "#FF3333";
    private static final String ACCENT_BLUE = "#3399FF";
    private static final String ACCENT_ORANGE = "#FF8C00";
    private static final String TEXT_LIGHT = "#f0f0f0";

    private ComboBox<String> classComboBox;
    private ComboBox<String> levelComboBox;
    private Accordion spellAccordion;

    public SpellGuidePage(Stage primaryStage, Scene previousScene) {
        this.primaryStage = primaryStage;
        this.previousScene = previousScene;
        this.root = new VBox(20);
        this.root.setPadding(new Insets(30));
        this.root.setAlignment(Pos.TOP_CENTER);
        this.root.setStyle("-fx-background-color: " + BG_BLACK + ";");
    }

    public Scene createScene() {
        Label title = new Label("SPELL GUIDE");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web(ACCENT_RED));

        // --- Class Selection ---
        VBox classBox = new VBox(5);
        Label classLabel = new Label("Select Class:");
        classLabel.setTextFill(Color.web(ACCENT_ORANGE));
        classComboBox = new ComboBox<>();
        classComboBox.setPromptText("Choose Class...");
        classComboBox.setPrefWidth(300);
        classComboBox.setStyle("-fx-background-color: #333; -fx-text-fill: white;");

        loadClasses();

        // --- Level Selection ---
        VBox levelBox = new VBox(5);
        Label levelLabel = new Label("Select Level:");
        levelLabel.setTextFill(Color.web(ACCENT_ORANGE));
        levelComboBox = new ComboBox<>();
        levelComboBox.setPromptText("Choose Level...");
        levelComboBox.setPrefWidth(300);
        levelComboBox.setDisable(true);
        levelComboBox.setStyle("-fx-background-color: #333; -fx-text-fill: white;");

        // --- Spell List ---
        spellAccordion = new Accordion();
        ScrollPane scrollPane = new ScrollPane(spellAccordion);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        scrollPane.setStyle("-fx-background: " + BG_BLACK + "; -fx-background-color: transparent;");

        // Event: When Class changes, reset levels and clear spells
        classComboBox.setOnAction(e -> {
            String selectedClass = classComboBox.getValue();
            if (selectedClass != null) {
                levelComboBox.getItems().clear(); // Fix for duplicates/ghost levels
                spellAccordion.getPanes().clear(); // Fix for old spells showing
                levelComboBox.setDisable(false);
                loadLevels(selectedClass);
            }
        });

        // Event: When Level changes, clear and load new spells
        levelComboBox.setOnAction(e -> {
            String selectedClass = classComboBox.getValue();
            String selectedLevel = levelComboBox.getValue();
            if (selectedClass != null && selectedLevel != null) {
                loadSpells(selectedClass, selectedLevel);
            }
        });

        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: " + ACCENT_BLUE + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30; -fx-cursor: hand;");
        backButton.setOnAction(e -> AppController.goTo(previousScene));

        root.getChildren().addAll(title, classLabel, classComboBox, levelLabel, levelComboBox, scrollPane, backButton);

        return new Scene(root, 1000, 800);
    }

    private void loadClasses() {
        File classesDir = new File("src/main/resources/Classes");
        if (classesDir.exists() && classesDir.isDirectory()) {
            String[] classes = classesDir.list((dir, name) -> new File(dir, name).isDirectory());
            if (classes != null) {
                Arrays.sort(classes);
                classComboBox.getItems().addAll(classes);
            }
        }
    }

    private void loadLevels(String className) {
        File classFolder = new File("src/main/resources/Classes/" + className);
        if (classFolder.exists() && classFolder.isDirectory()) {
            String[] levelDirs = classFolder.list((dir, name) -> new File(dir, name).isDirectory());
            if (levelDirs != null) {
                List<String> levels = new ArrayList<>(Arrays.asList(levelDirs));
                // Sort numerically so Cantrips (0) comes first
                levels.sort((a, b) -> {
                    try {
                        return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
                    } catch (NumberFormatException e) {
                        return a.compareTo(b);
                    }
                });

                for (String lvl : levels) {
                    levelComboBox.getItems().add(lvl.equals("0") ? "Cantrips" : "Level " + lvl);
                }
            }
        }
    }

    private void loadSpells(String className, String levelDisplay) {
        spellAccordion.getPanes().clear(); // Ensure no duplicates from previous selection

        // Map "Cantrips" back to folder "0"
        String folderName = levelDisplay.equals("Cantrips") ? "0" : levelDisplay.replace("Level ", "");
        File spellDir = new File("src/main/resources/Classes/" + className + "/" + folderName);

        if (spellDir.exists() && spellDir.isDirectory()) {
            File[] files = spellDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
            if (files != null) {
                List<File> spellFiles = Arrays.asList(files);
                Collections.sort(spellFiles); // Sort alphabetically

                for (File file : spellFiles) {
                    String spellName = file.getName().replace(".png", "");
                    spellAccordion.getPanes().add(createSpellPane(spellName, file));
                }
            }
        }
    }

    private TitledPane createSpellPane(String spellName, File imageFile) {
        TitledPane pane = new TitledPane();
        pane.setText(spellName);
        // Style to match CharacterSheetPage's Accordion/Tab look
        pane.setStyle("-fx-text-fill: " + TEXT_LIGHT + "; -fx-body-color: " + SECTION_BG_DARK + ";");

        try {
            Image image = new Image(imageFile.toURI().toString());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(450); // Larger view for the guide
            imageView.setPreserveRatio(true);

            VBox content = new VBox(imageView);
            content.setAlignment(Pos.CENTER);
            content.setPadding(new Insets(10));
            content.setStyle("-fx-background-color: " + SECTION_BG_DARK + ";");

            pane.setContent(content);
        } catch (Exception e) {
            pane.setContent(new Label("Could not load spell image."));
        }

        return pane;
    }
}