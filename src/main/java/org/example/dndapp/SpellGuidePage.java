package org.example.dndapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.web.WebView;
import java.io.File;
import java.net.MalformedURLException;

public class SpellGuidePage {

    private final Stage primaryStage;
    private final Scene playerScene;
    private static final String PDF_PATH = "src/main/resources/Spells.pdf";

    public SpellGuidePage(Stage primaryStage, Scene playerScene) {
        this.primaryStage = primaryStage;
        this.playerScene = playerScene;
    }

    public Scene createScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #000;");

        Label title = new Label("Spell Guide");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        Button backButton = new Button("Go Back");
        backButton.setStyle("-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;");
        backButton.setOnAction(e -> primaryStage.setScene(playerScene));

        WebView webView = new WebView();
        webView.setPrefSize(800, 600);

        try {
            File pdfFile = new File(PDF_PATH);
            String pdfUrl = pdfFile.toURI().toURL().toExternalForm();
            webView.getEngine().load(pdfUrl);
        } catch (MalformedURLException e) {
            System.err.println("Error loading PDF: " + e.getMessage());
            Label errorLabel = new Label("Error loading PDF file. Check the file path.");
            errorLabel.setTextFill(Color.RED);
            root.getChildren().add(errorLabel);
        }

        root.getChildren().addAll(backButton, title, webView);

        return new Scene(root);
    }
}