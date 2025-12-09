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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        // Updated prompt to reflect new functionality
        rollInput.setPromptText("e.g., 2d8+3, 4d6kh3, or 1d8+2d6");
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
     * Entry point for all dice rolls. It delegates to the complex roller if
     * multiple dice units are detected.
     * @param notation The dice string.
     */
    private void rollDice(String notation) {
        if (notation == null || notation.trim().isEmpty()) {
            resultsArea.appendText("Please enter a roll notation (e.g., 2d6).\n");
            return;
        }

        notation = notation.toLowerCase().replaceAll("\\s+", "");

        // Check if the notation contains multiple dice units (e.g., 1d8+2d6)
        // If it contains '+' or '-' and a 'd', it's complex and should be delegated.
        if (notation.matches(".*[+-].*d.*")) {
            rollDiceComplex(notation);
            return;
        }

        // --- Line 1: Standard Notation for Advantage/Disadvantage ---
        // Regex to match NdS+/-M format, now including optional Keep/Drop notation.
        // khX (Keep Highest X) | dlY (Drop Lowest Y)
        Pattern pattern = Pattern.compile("(\\d*)d(\\d+)([+-]\\d+)?(k[hl]\\d+|d[lh]\\d+)?");
        Matcher matcher = pattern.matcher(notation);

        if (!matcher.matches()) {
            resultsArea.appendText("Invalid roll format: " + notation + ". Use XdY+/-Z or XdYkhZ.\n");
            return;
        }

        int numDice = matcher.group(1).isEmpty() ? 1 : Integer.parseInt(matcher.group(1));
        int sides = Integer.parseInt(matcher.group(2));
        String modifierGroup = matcher.group(3);
        String keepDropGroup = matcher.group(4);
        int modifier = 0;

        if (modifierGroup != null) {
            modifier = Integer.parseInt(modifierGroup);
        }

        // Validate dice limits
        if (numDice > 100 || sides > 1000 || numDice <= 0 || sides <= 0) {
            resultsArea.appendText("Roll too complex. Max 100 dice and d1000.\n");
            return;
        }

        // --- Perform the Roll ---
        List<Integer> rolls = new ArrayList<>();
        for (int i = 0; i < numDice; i++) {
            rolls.add(random.nextInt(sides) + 1);
        }

        // --- Apply Keep/Drop Logic ---
        int total = 0;
        List<Integer> keptRolls = new ArrayList<>(rolls);
        String keepDropAction = "";

        if (keepDropGroup != null) {
            char type = keepDropGroup.charAt(0); // 'k' or 'd'
            char highLow = keepDropGroup.charAt(1); // 'h' or 'l'
            int count = Integer.parseInt(keepDropGroup.substring(2));

            // Sort rolls to easily find highest/lowest
            Collections.sort(keptRolls);

            if (type == 'k') { // Keep logic
                if (highLow == 'h') {
                    // Keep the highest 'count' rolls
                    keptRolls = keptRolls.subList(numDice - count, numDice);
                    keepDropAction = String.format(" (Kept Highest %d)", count);
                } else {
                    // Keep the lowest 'count' rolls
                    keptRolls = keptRolls.subList(0, count);
                    keepDropAction = String.format(" (Kept Lowest %d)", count);
                }
            } else if (type == 'd') { // Drop logic
                if (highLow == 'l') {
                    // Drop the lowest 'count' rolls
                    keptRolls = keptRolls.subList(count, numDice);
                    keepDropAction = String.format(" (Dropped Lowest %d)", count);
                } else {
                    // Drop the highest 'count' rolls
                    keptRolls = keptRolls.subList(0, numDice - count);
                    keepDropAction = String.format(" (Dropped Highest %d)", count);
                }
            }
        }

        // Calculate total from kept rolls
        for (int roll : keptRolls) {
            total += roll;
        }

        // --- Logging ---
        int finalTotal = total + modifier;

        String rollDetails = rolls.toString().replaceAll("[\\[\\]]", ""); // (1 + 5 + 3)
        String keptDetails = keptRolls.toString().replaceAll("[\\[\\]]", ""); // (5 + 3)

        // Log the result
        String logEntry = String.format("Roll %s%s: %s%s = %d\n  Final Total: %d\n",
                notation.toUpperCase(),
                keepDropAction,
                rollDetails,
                (keepDropGroup != null ? " -> Kept: " + keptDetails : ""),
                total,
                finalTotal
        );

        resultsArea.appendText(logEntry);
    }

    /**
     * --- Line 2: Multiple Die Logic ---
     * Parses and rolls a complex string with multiple dice types (e.g., "1d8+2d6-4").
     * @param notation The complex dice string.
     */
    private void rollDiceComplex(String notation) {
        // This pattern identifies each individual component (XdY or +/-Z)
        // It matches a dice roll ((\d*)d(\d+)) or a modifier ([+-]\d+).
        Pattern pattern = Pattern.compile("(\\d*d\\d+|[+-]\\d+)");
        Matcher matcher = pattern.matcher(notation);

        int grandTotal = 0;
        StringBuilder logDetails = new StringBuilder();

        while (matcher.find()) {
            String unit = matcher.group(0); // e.g., "1d8", "+2d6", "-4"

            if (unit.matches(".*d.*")) {
                // This is a dice unit (e.g., 1d8, 2d6)
                // Extract sign, number of dice, and sides

                // Determine if the unit is being added or subtracted from the grandTotal
                char sign = unit.charAt(0);
                boolean isSubtract = (sign == '-');
                String cleanUnit = (sign == '+' || sign == '-') ? unit.substring(1) : unit;

                // Use a standard pattern (without keep/drop) to parse the unit
                Pattern unitPattern = Pattern.compile("(\\d*)d(\\d+)");
                Matcher unitMatcher = unitPattern.matcher(cleanUnit);

                if (unitMatcher.matches()) {
                    int numDice = unitMatcher.group(1).isEmpty() ? 1 : Integer.parseInt(unitMatcher.group(1));
                    int sides = Integer.parseInt(unitMatcher.group(2));

                    int unitTotal = 0;
                    StringBuilder unitRolls = new StringBuilder();

                    for (int i = 0; i < numDice; i++) {
                        int roll = random.nextInt(sides) + 1;
                        unitTotal += roll;
                        unitRolls.append(roll).append(i < numDice - 1 ? " + " : "");
                    }

                    // Apply sign to the unit total
                    int signedUnitTotal = isSubtract ? -unitTotal : unitTotal;
                    grandTotal += signedUnitTotal;

                    // Add details to the log
                    if (logDetails.length() > 0) {
                        logDetails.append(isSubtract ? " - " : " + ");
                    } else if (isSubtract) {
                        logDetails.append("-");
                    }
                    logDetails.append(unit.toUpperCase()).append("[").append(unitRolls.toString()).append("]");

                }
            } else {
                // This is a raw modifier (e.g., +5, -2)
                int modifier = Integer.parseInt(unit);
                grandTotal += modifier;

                // Add details to the log
                logDetails.append(unit);
            }
        }

        // Log the result
        String logEntry = String.format("Roll %s = %d\n",
                logDetails.toString(),
                grandTotal
        );

        resultsArea.appendText("--- COMPLEX ROLL: " + notation.toUpperCase() + " ---\n");
        resultsArea.appendText(logEntry);
    }
}