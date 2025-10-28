package org.example.dndapp;

import javafx.scene.control.Alert;
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

import java.io.File;
import java.util.List;

public class MyCharactersPage {
    private final Stage primaryStage;
    private final Scene previousScene;

    public MyCharactersPage(Stage primaryStage, Scene previousScene) {
        this.primaryStage = primaryStage;
        this.previousScene = previousScene;
    }

    public Scene createScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(50, 20, 20, 20));
        root.setStyle("-fx-background-color: #000;");

        Label title = new Label("My Characters");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        VBox characterListVBox = new VBox(10);
        characterListVBox.setAlignment(Pos.CENTER);
        characterListVBox.setPadding(new Insets(10));

        Button backButton = new Button("Go Back");
        String backButtonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";
        backButton.setStyle(backButtonStyle);
        backButton.setOnAction(e -> primaryStage.setScene(previousScene));

        List<String> characterNames = CharacterFileManager.getSavedCharacters();
        if (characterNames.isEmpty()) {
            Label noCharactersLabel = new Label("No characters found. Create a new one!");
            noCharactersLabel.setTextFill(Color.web("#adb5bd"));
            characterListVBox.getChildren().add(noCharactersLabel);
        } else {
            for (String characterName : characterNames) {
                Button characterButton = new Button(characterName.replace(".dnd", ""));
                String buttonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-background-color: #007BFF; -fx-text-fill: white; -fx-border-radius: 5px;";
                characterButton.setStyle(buttonStyle);
                characterButton.setMinWidth(200);
                characterButton.setOnAction(e -> {
                    Character character = CharacterFileManager.loadCharacter(characterName);
                    if (character != null) {
                        CharacterSheetPage characterSheetPage = new CharacterSheetPage(character, primaryStage, createScene());
                        primaryStage.setScene(characterSheetPage.createScene());
                        primaryStage.setTitle("Character Sheet - " + character.getName());
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Failed to load character: " + characterName);
                        alert.showAndWait();
                    }
                });
                characterListVBox.getChildren().add(characterButton);
            }
        }

        Button createCharacterButton = new Button("Create New Character");
        String createButtonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #28a745; -fx-text-fill: white;";
        createCharacterButton.setStyle(createButtonStyle);
        createCharacterButton.setOnAction(e -> {
            CharacterCreationPage characterCreationPage = new CharacterCreationPage(primaryStage, createScene());
            primaryStage.setScene(characterCreationPage.createScene());
            primaryStage.setTitle("Character Creation");
        });


        root.getChildren().addAll(title, backButton, createCharacterButton, characterListVBox);

        return new Scene(root, 600, 400);
    }
}