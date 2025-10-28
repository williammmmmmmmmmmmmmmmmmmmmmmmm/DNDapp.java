package org.example.dndapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class BestiaryPage {

    private Stage primaryStage;
    private Scene previousScene;

    public BestiaryPage(Stage primaryStage, Scene previousScene) {
        this.primaryStage = primaryStage;
        this.previousScene = previousScene;
    }

    public Scene createScene() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000;");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        Label message = new Label("Sorry! Only available in the Webapp!");
        message.setFont(Font.font("Inter", FontWeight.BOLD, 24));
        message.setTextFill(Color.web("#ff0000"));

        Button backButton = new Button("Go Back");
        backButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 20; -fx-padding: 5 15;");
        backButton.setOnAction(e -> primaryStage.setScene(previousScene));

        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(15));

        content.getChildren().add(message);
        root.getChildren().addAll(content, backButton);

        return new Scene(root);
    }
}