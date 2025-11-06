package org.example.dndapp;

import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class FloatingDieIcon extends StackPane {

    // --- Color Constants ---
    private static final String ACCENT_RED = "#FF3333";
    private static final String TEXT_LIGHT = "#f0f0f0";

    // ICON SIZE: Fixed size
    private static final int ICON_SIZE = 45;

    private double xOffset = 0;
    private double yOffset = 0;
    private final DiceRollerPopup diceRoller;

    public FloatingDieIcon() {
        // 1. Initialize the Popup window
        this.diceRoller = new DiceRollerPopup();

        // 2. Create the visual icon
        Label dieLabel = new Label("Dice");
        dieLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        dieLabel.setStyle(
                "-fx-text-fill: " + TEXT_LIGHT + ";"
        );

        this.getChildren().add(dieLabel);
        this.setPrefSize(ICON_SIZE, ICON_SIZE);

        // FIX 1: Set max size to prevent stretching/resizing
        this.setMaxSize(ICON_SIZE, ICON_SIZE);

        // 3. APPLY CIRCULAR STYLE AND BORDER
        this.setStyle(
                "-fx-background-color: " + ACCENT_RED + ";" +
                        "-fx-background-radius: 50%;" +
                        "-fx-border-color: " + TEXT_LIGHT + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 50%;" +
                        "-fx-cursor: hand;"
        );

        // 4. Setup interaction listeners (Drag and Double-Click)
        setupDragListeners();
        setupClickListener();
    }

    private void setupDragListeners() {
        // Store the initial offset from the icon's translate position
        this.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                // Calculate the offset relative to the mouse's scene position
                xOffset = event.getSceneX() - this.getTranslateX();
                yOffset = event.getSceneY() - this.getTranslateY();
            }
        });

        // Handle mouse drag for movement
        this.setOnMouseDragged(event -> {
            // Apply the new position as a translation
            this.setTranslateX(event.getSceneX() - xOffset);
            this.setTranslateY(event.getSceneY() - yOffset);
        });
    }

    private void setupClickListener() {
        // Handle double-click to open the popup
        this.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                if (diceRoller.isShowing()) {
                    diceRoller.hide();
                } else {
                    // Set the pop-up to appear near the icon
                    diceRoller.setX(this.localToScreen(this.getBoundsInLocal()).getMaxX() + 10);
                    diceRoller.setY(this.localToScreen(this.getBoundsInLocal()).getMinY());
                    diceRoller.show();
                }
                event.consume();
            }
        });
    }

    public void toggleDiceRoller() {
        if (diceRoller.isShowing()) {
            diceRoller.hide();
        } else {
            diceRoller.show();
        }
    }
}