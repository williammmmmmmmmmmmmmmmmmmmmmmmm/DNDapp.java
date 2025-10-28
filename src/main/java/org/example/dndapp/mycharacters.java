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

public class mycharacters {
    private final Stage primaryStage;
    private final Scene previousScene;

    public mycharacters(Stage primaryStage, Scene previousScene) {
        this.primaryStage = primaryStage;
        this.previousScene = previousScene;
    }

    public Scene createScene() {
        Button backButton = new Button("Go Back");
        String buttonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";
        backButton.setStyle(buttonStyle);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50, 20, 20, 20));
        root.setStyle("-fx-background-color: #000;");

        Label title = new Label("My Characters");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        GridPane characterGrid = new GridPane();
        characterGrid.setAlignment(Pos.CENTER);
        characterGrid.setHgap(15);
        characterGrid.setVgap(15);

        // Placeholder for characters
        backButton.setOnAction(e -> primaryStage.setScene(previousScene));
        Label placeholderLabel = new Label("Finish this: Display list of characters here");
        placeholderLabel.setTextFill(Color.web("#fff"));
        characterGrid.add(placeholderLabel, 0, 0);


        root.getChildren().addAll(title, backButton,characterGrid);

        return new Scene(root);
    }
}