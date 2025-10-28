package org.example.dndapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class AccountPage {

    private final Stage primaryStage;
    private final Scene homeScene;

    public AccountPage(Stage primaryStage, Scene homeScene) {
        this.primaryStage = primaryStage;
        this.homeScene = homeScene;
    }

    public Scene createScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50, 20, 20, 20));
        root.setStyle("-fx-background-color: #000;");

        Label title = new Label("Set Your Account Name");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        Label nameLabel = new Label("Player Name:");
        nameLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        nameLabel.setTextFill(Color.web("#d3d3d3"));

        TextField nameField = new TextField();
        nameField.setMaxWidth(250);
        nameField.setPromptText("Enter your name");
        nameField.setStyle("-fx-font-size: 14px;");

        Button saveButton = new Button("Save Name");
        String buttonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #28a745; -fx-text-fill: white;";
        saveButton.setStyle(buttonStyle);

        saveButton.setOnAction(e -> {
            String accountName = nameField.getText().trim();
            if (!accountName.isEmpty()) {
                // Set the player's name in the session
                PlayerSession.setPlayerName(accountName);

                System.out.println("Account name saved: " + PlayerSession.getPlayerName());

                primaryStage.setScene(homeScene);
                primaryStage.setTitle("Williams D&D App");
            } else {
                nameField.setPromptText("Name cannot be empty!");
                nameField.setStyle("-fx-prompt-text-fill: #ff0000; -fx-font-size: 14px;");
            }
        });

        Button backButton = new Button("Go Back");
        backButton.setStyle("-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;");
        backButton.setOnAction(e -> primaryStage.setScene(homeScene));

        root.getChildren().addAll(title, nameLabel, nameField, saveButton, backButton);

        return new Scene(root);
    }
}