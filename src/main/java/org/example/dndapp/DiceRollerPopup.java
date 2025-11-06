package org.example.dndapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiceRollerPopup extends Stage {

    // --- Color Constants from CharacterSheetPage ---
    private static final String BG_BLACK = "#000000";       // Black Background
    private static final String SECTION_BG_DARK = "#1a1a1a"; // Off-Black for sections
    private static final String ACCENT_BLUE = "#3399FF";    // Bright Blue for Titles/Borders
    private static final String ACCENT_ORANGE = "#FF8C00";  // Orange for Labels/Data
    private static final String TEXT_LIGHT = "#f0f0f0";     // Near White for main text
    private static final String FIELD_BG = "#333333";       // Darker Gray for input fields

    private final TextArea resultsArea;
    private final Random random = new Random();

    public DiceRollerPopup() {
        setTitle("D&D Dice Roller");
        // Use UNDECORATED to remove standard title bar, allowing the user to move it
        // based on the floating icon logic (which we'll do in the next step).
        // For now, it opens as a simple window.
        initStyle(StageStyle.DECORATED);
        setResizable(false);

        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: " + SECTION_BG_DARK + "; -fx-border-color: " + ACCENT_BLUE + "; -fx-border-width: 2; -fx-border-radius: 5;");

        // --- Title ---
        Label title = new Label("Roll Dice");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: " + ACCENT_BLUE + ";");

        // --- Results Area ---
        resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.setPrefHeight(120);
        resultsArea.setWrapText(true);
        resultsArea.setPromptText("Results will appear here...");
        resultsArea.setStyle(
                "-fx-control-inner-background: " + FIELD_BG + ";" +
                        "-fx-text-fill: " + ACCENT_ORANGE + ";" + // Roll results in orange
                        "-fx-font-family: 'Monospaced';" +
                        "-fx-border-color: " + ACCENT_ORANGE + ";" +
                        "-fx-border-width: 1;"
        );

        // --- Dice Buttons ---
        GridPane diceGrid = createDiceButtonGrid();

        // --- Custom Roll Input ---
        HBox customRollBox = createCustomRollInput();

        root.getChildren().addAll(title, resultsArea, diceGrid, customRollBox);

        Scene scene = new Scene(root, 300, 450);
        setScene(scene);
    }

    private GridPane createDiceButtonGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        int[] diceSizes = {4, 6, 8, 10, 12, 20, 100};

        for (int i = 0; i < diceSizes.length; i++) {
            int sides = diceSizes[i];
            Button dieButton = createDieButton("d" + sides);
            dieButton.setOnAction(e -> rollDice("1d" + sides));

            // Layout: d4, d6, d8 on row 0; d10, d12, d20 on row 1; d100 alone.
            int row = i < 3 ? 0 : (i < 6 ? 1 : 2);
            int col = i % 3;

            if (sides == 100) {
                // d100 button in the center of the third row
                grid.add(dieButton, 1, 2);
            } else {
                grid.add(dieButton, col, row);
            }
        }
        return grid;
    }

    private Button createDieButton(String text) {
        Button button = new Button(text);
        button.setPrefSize(75, 45);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Use a vibrant red for the die buttons
        button.setStyle(
                "-fx-background-color: #FF3333;" + // RED ACCENT
                        "-fx-text-fill: " + TEXT_LIGHT + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        );
        return button;
    }

    private HBox createCustomRollInput() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);

        TextField rollInput = new TextField();
        rollInput.setPromptText("e.g., 2d8+3");
        rollInput.setPrefWidth(120);
        rollInput.setStyle(
                "-fx-background-color: " + FIELD_BG + ";" +
                        "-fx-text-fill: " + TEXT_LIGHT + ";" +
                        "-fx-prompt-text-fill: #999999;" +
                        "-fx-border-color: " + ACCENT_ORANGE + ";"
        );

        Button rollButton = new Button("Roll Custom");
        rollButton.setStyle(
                "-fx-background-color: " + ACCENT_BLUE + ";" + // BLUE ACCENT
                        "-fx-text-fill: " + TEXT_LIGHT + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        );

        rollButton.setOnAction(e -> {
            rollDice(rollInput.getText());
            rollInput.clear();
        });

        box.getChildren().addAll(rollInput, rollButton);
        return box;
    }

    /**
     * Parses and rolls a standard D&D dice string (e.g., "2d20+5" or "1d4-1").
     * @param notation The dice string.
     */
    private void rollDice(String notation) {
        if (notation == null || notation.trim().isEmpty()) {
            resultsArea.appendText("Please enter a roll notation (e.g., 2d6).\n");
            return;
        }

        notation = notation.toLowerCase().replaceAll("\\s+", "");

        // Regex to match NdS+/-M format
        Pattern pattern = Pattern.compile("(\\d*)d(\\d+)([+-]\\d+)?");
        Matcher matcher = pattern.matcher(notation);

        if (!matcher.matches()) {
            resultsArea.appendText("Invalid roll format: " + notation + ". Use XdY+/-Z.\n");
            return;
        }

        int numDice = matcher.group(1).isEmpty() ? 1 : Integer.parseInt(matcher.group(1));
        int sides = Integer.parseInt(matcher.group(2));
        String modifierGroup = matcher.group(3);
        int modifier = 0;

        if (modifierGroup != null) {
            modifier = Integer.parseInt(modifierGroup);
        }

        // Validate dice limits
        if (numDice > 100 || sides > 1000 || numDice <= 0 || sides <= 0) {
            resultsArea.appendText("Roll too complex. Max 100 dice and d1000.\n");
            return;
        }

        // Perform the roll
        int total = 0;
        StringBuilder rollDetails = new StringBuilder("(");

        for (int i = 0; i < numDice; i++) {
            int roll = random.nextInt(sides) + 1;
            total += roll;
            rollDetails.append(roll);
            if (i < numDice - 1) {
                rollDetails.append(" + ");
            }
        }
        rollDetails.append(")");

        int finalTotal = total + modifier;

        // Log the result
        String logEntry = String.format("Roll %s%s%s = %d\n",
                notation.toUpperCase(),
                (modifier != 0 ? (modifier > 0 ? " + " + modifier : " - " + Math.abs(modifier)) : ""),
                rollDetails.toString(),
                finalTotal
        );

        resultsArea.appendText(logEntry);
    }
}