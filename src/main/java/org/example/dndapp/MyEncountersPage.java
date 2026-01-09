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

public class MyEncountersPage {
    private final Stage stage;
    private final Scene prev;
    private final List<Encounter> savedList;
    private final VBox listContainer = new VBox(15);

    public MyEncountersPage(Stage stage, Scene prev, List<Encounter> savedList) {
        this.stage = stage;
        this.prev = prev;
        this.savedList = savedList;
    }

    public Scene createScene() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000;");

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(60, 20, 20, 20));
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #ff0000; -fx-border-width: 2;");

        Label title = new Label("MY SAVED ENCOUNTERS");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#ff0000"));

        ScrollPane scroll = new ScrollPane(listContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        listContainer.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 10;");

        refreshList();

        mainContent.getChildren().addAll(title, scroll);

        Button backBtn = new Button("Go Back");
        backBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 20; -fx-padding: 5 15;");
        backBtn.setOnAction(e -> stage.setScene(prev));

        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        StackPane.setMargin(backBtn, new Insets(15));

        root.getChildren().addAll(mainContent, backBtn);
        return new Scene(root, 1000, 800);
    }

    private void refreshList() {
        listContainer.getChildren().clear();
        for (Encounter enc : savedList) {
            VBox card = new VBox(10);
            card.setPadding(new Insets(15));
            card.setStyle("-fx-background-color: #222; -fx-border-color: #444; -fx-border-radius: 5;");

            HBox header = new HBox();
            Label nameLabel = new Label(enc.getName().toUpperCase());
            nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            nameLabel.setTextFill(Color.web("#007BFF"));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button delBtn = new Button("Delete");
            delBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand;");
            delBtn.setOnAction(e -> {
                savedList.remove(enc);
                EncountersPage.saveToDisk(); // Update persistent file
                refreshList();
            });

            header.getChildren().addAll(nameLabel, spacer, delBtn);
            card.getChildren().add(header);

            for (Monster m : enc.getMonsters()) {
                Label mLabel = new Label(" • " + m.getName() + " (CR " + m.getCr() + ")");
                mLabel.setTextFill(Color.WHITE);
                card.getChildren().add(mLabel);
            }
            listContainer.getChildren().add(card);
        }
    }
}