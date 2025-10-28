package org.example.dndapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class CharacterCreationPage {
    private final Stage primaryStage;
    private final Scene previousScene;
    private final VBox root;
    private final Scene mainScene;

    // Data maps
    private final Map<String, String[]> classSubclasses = new LinkedHashMap<>();
    private final Map<String, String[]> speciesData = new LinkedHashMap<>();
    private final Map<String, String[]> classEquipment = new LinkedHashMap<>();
    private final String[] alignments = {"Lawful Good", "Neutral Good", "Chaotic Good", "Lawful Neutral", "True Neutral", "Chaotic Neutral", "Lawful Evil", "Neutral Evil", "Chaotic Evil"};

    // Character data fields
    private String characterName;
    private String playerName;
    private String selectedClass;
    private String selectedSubclass;
    private String selectedSpecies;
    private String background;
    private int experience;
    private final Map<String, Integer> abilityScores = new LinkedHashMap<>();
    private TextField ageField, heightField, weightField, hairField, eyesField, skinField, faithField, lifestyleField;
    private ComboBox<String> alignmentComboBox;
    private ComboBox<String> backgroundComboBox;
    private TextField experienceField;

    public CharacterCreationPage(Stage primaryStage, Scene previousScene) {
        this.primaryStage = primaryStage;
        this.previousScene = previousScene;
        this.root = new VBox(20);
        this.root.setAlignment(Pos.CENTER);
        this.root.setPadding(new Insets(50, 20, 20, 20));
        this.root.setStyle("-fx-background-color: #000;");
        this.mainScene = new Scene(this.root);

        // Populate data
        populateClassSubclasses();
        populateSpeciesData();
        populateClassEquipment();
        populateBackgrounds();
    }

    public Scene createScene() {
        showNameAndClassSelection();
        return mainScene;
    }

    private void showNameAndClassSelection() {
        root.getChildren().clear();

        Label title = new Label("Create a Character");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        GridPane nameGrid = new GridPane();
        nameGrid.setAlignment(Pos.CENTER);
        nameGrid.setHgap(15);
        nameGrid.setVgap(15);
        Label nameLabel = new Label("Character Name:");
        nameLabel.setTextFill(Color.web("#fff"));
        TextField nameField = new TextField();
        nameField.setText(characterName);
        nameGrid.addRow(0, nameLabel, nameField);

        Label playerLabel = new Label("Player Name:");
        playerLabel.setTextFill(Color.web("#fff"));
        TextField playerField = new TextField();
        playerField.setText(playerName);
        nameGrid.addRow(1, playerLabel, playerField);

        Label classTitle = new Label("Choose Your Class");
        classTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        classTitle.setTextFill(Color.web("#fff"));

        GridPane classGrid = new GridPane();
        classGrid.setAlignment(Pos.CENTER);
        classGrid.setHgap(15);
        classGrid.setVgap(15);

        String buttonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";

        int col = 0;
        int row = 0;
        for (String className : classSubclasses.keySet()) {
            Button classButton = new Button(className);
            classButton.setStyle(buttonStyle);
            classButton.setPrefWidth(200);
            classButton.setOnAction(e -> {
                // FIX: Check for null text first to prevent NullPointerException
                String charNameText = nameField.getText();
                String playerText = playerField.getText();

                if (charNameText == null || charNameText.trim().isEmpty() ||
                        playerText == null || playerText.trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Please enter both a character name and a player name.");
                    alert.showAndWait();
                    return;
                }
                characterName = charNameText.trim();
                playerName = playerText.trim();
                selectedClass = className;
                showSubclasses();
            });
            classGrid.add(classButton, col, row);
            col++;
            if (col > 3) {
                col = 0;
                row++;
            }
        }

        Button backButton = new Button("Go Back");
        backButton.setStyle(buttonStyle);
        backButton.setOnAction(e -> primaryStage.setScene(previousScene));

        root.getChildren().addAll(title, nameGrid, classTitle, classGrid, backButton);
    }
    private void showSubclasses() {
        root.getChildren().clear();

        Label title = new Label(selectedClass + " Subclasses");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        GridPane subclassGrid = new GridPane();
        subclassGrid.setAlignment(Pos.CENTER);
        subclassGrid.setHgap(15);
        subclassGrid.setVgap(15);

        String buttonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";

        String[] subclasses = classSubclasses.get(selectedClass);
        int col = 0;
        int row = 0;
        for (String subclassName : subclasses) {
            Button subclassButton = new Button(subclassName);
            subclassButton.setStyle(buttonStyle);
            subclassButton.setPrefWidth(200);
            subclassButton.setOnAction(e -> {
                selectedSubclass = subclassName;
                showSpeciesSelection();
            });
            subclassGrid.add(subclassButton, col, row);
            col++;
            if (col > 3) {
                col = 0;
                row++;
            }
        }

        Button backToClassesButton = new Button("Go Back to Classes");
        backToClassesButton.setStyle(buttonStyle);
        backToClassesButton.setOnAction(e -> showNameAndClassSelection());

        root.getChildren().addAll(title, subclassGrid, backToClassesButton);
    }

    private void showSpeciesSelection() {
        root.getChildren().clear();
        Label title = new Label("Choose Your Species");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        GridPane speciesGrid = new GridPane();
        speciesGrid.setAlignment(Pos.CENTER);
        speciesGrid.setHgap(15);
        speciesGrid.setVgap(15);

        String buttonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";

        int col = 0;
        int row = 0;
        for (String speciesName : speciesData.keySet()) {
            Button speciesButton = new Button(speciesName);
            speciesButton.setStyle(buttonStyle);
            speciesButton.setPrefWidth(200);
            speciesButton.setOnAction(e -> {
                selectedSpecies = speciesName;
                showAbilityScoreGeneration();
            });
            speciesGrid.add(speciesButton, col, row);
            col++;
            if (col > 4) {
                col = 0;
                row++;
            }
        }

        Button backButton = new Button("Go Back to Subclasses");
        backButton.setStyle(buttonStyle);
        backButton.setOnAction(e -> showSubclasses());

        root.getChildren().addAll(title, speciesGrid, backButton);
    }

    private void showAbilityScoreGeneration() {
        root.getChildren().clear();
        Label title = new Label("Ability Score Generation");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        Button standardArrayButton = new Button("Standard Array (15, 14, 13, 12, 10, 8)");
        Button pointBuyButton = new Button("Point Buy (27 points)");
        Button rolledButton = new Button("Manual / Rolled");

        String buttonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";
        standardArrayButton.setStyle(buttonStyle);
        pointBuyButton.setStyle(buttonStyle);
        rolledButton.setStyle(buttonStyle);

        standardArrayButton.setOnAction(e -> {
            abilityScores.put("STR", 15);
            abilityScores.put("DEX", 14);
            abilityScores.put("CON", 13);
            abilityScores.put("INT", 12);
            abilityScores.put("WIS", 10);
            abilityScores.put("CHA", 8);
            showBackgroundAndExperience();
        });

        pointBuyButton.setOnAction(e -> showPointBuy());
        rolledButton.setOnAction(e -> showManualEntry());

        Button backButton = new Button("Go Back");
        backButton.setStyle(buttonStyle);
        backButton.setOnAction(e -> showSpeciesSelection());

        root.getChildren().addAll(title, standardArrayButton, pointBuyButton, rolledButton, backButton);
    }

    private void showPointBuy() {
        root.getChildren().clear();
        Label title = new Label("Point Buy (27 Points)");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        Map<String, Spinner<Integer>> spinners = new LinkedHashMap<>();
        Label pointsLeftLabel = new Label("Points Left: 27");
        pointsLeftLabel.setTextFill(Color.web("#fff"));

        int row = 0;
        String[] stats = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        for (String stat : stats) {
            Label statLabel = new Label(stat + ":");
            statLabel.setTextFill(Color.web("#fff"));
            Spinner<Integer> spinner = new Spinner<>(8, 15, 8);
            spinners.put(stat, spinner);
            grid.add(statLabel, 0, row);
            grid.add(spinner, 1, row);
            row++;
        }

        Runnable updatePoints = () -> {
            int totalPoints = 0;
            for (Spinner<Integer> spinner : spinners.values()) {
                int value = spinner.getValue();
                if (value <= 13) {
                    totalPoints += value - 8;
                } else {
                    totalPoints += 7 + (value - 13) * 2;
                }
            }
            pointsLeftLabel.setText("Points Left: " + (27 - totalPoints));
        };

        for (Spinner<Integer> spinner : spinners.values()) {
            spinner.valueProperty().addListener((obs, oldVal, newVal) -> updatePoints.run());
        }

        Button nextButton = new Button("Next");
        String buttonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";
        nextButton.setStyle(buttonStyle);
        nextButton.setOnAction(e -> {
            int totalPoints = 0;
            for (Spinner<Integer> spinner : spinners.values()) {
                int value = spinner.getValue();
                if (value <= 13) {
                    totalPoints += value - 8;
                } else {
                    totalPoints += 7 + (value - 13) * 2;
                }
            }
            if (totalPoints == 27) {
                for (String stat : stats) {
                    abilityScores.put(stat, spinners.get(stat).getValue());
                }
                showBackgroundAndExperience();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("You must use exactly 27 points.");
                alert.showAndWait();
            }
        });

        Button backButton = new Button("Go Back");
        backButton.setStyle(buttonStyle);
        backButton.setOnAction(e -> showAbilityScoreGeneration());

        root.getChildren().addAll(title, pointsLeftLabel, grid, nextButton, backButton);
    }

    private void showManualEntry() {
        root.getChildren().clear();
        Label title = new Label("Manual Entry (Roll 4d6, drop lowest)");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        Map<String, TextField> textFields = new LinkedHashMap<>();
        int row = 0;
        String[] stats = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        for (String stat : stats) {
            Label statLabel = new Label(stat + ":");
            statLabel.setTextFill(Color.web("#fff"));
            TextField textField = new TextField();
            textFields.put(stat, textField);
            grid.add(statLabel, 0, row);
            grid.add(textField, 1, row);
            row++;
        }

        Button nextButton = new Button("Next");
        String buttonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";
        nextButton.setStyle(buttonStyle);
        nextButton.setOnAction(e -> {
            try {
                for (String stat : stats) {
                    abilityScores.put(stat, Integer.parseInt(textFields.get(stat).getText()));
                }
                showBackgroundAndExperience();
            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Please enter valid numbers for all ability scores.");
                alert.showAndWait();
            }
        });

        Button backButton = new Button("Go Back");
        backButton.setStyle(buttonStyle);
        backButton.setOnAction(e -> showAbilityScoreGeneration());

        root.getChildren().addAll(title, grid, nextButton, backButton);
    }

    private void showBackgroundAndExperience() {
        root.getChildren().clear();
        Label title = new Label("Background & Experience");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setAlignment(Pos.CENTER);

        Label backgroundLabel = createLabel("Background:");
        backgroundComboBox = new ComboBox<>();
        backgroundComboBox.getItems().addAll(
                "Acolyte", "Charlatan", "Criminal", "Entertainer", "Folk Hero",
                "Guild Artisan", "Hermit", "Noble", "Outlander", "Sage",
                "Sailor", "Soldier", "Urchin"
        );
        backgroundComboBox.setValue(background);
        formGrid.addRow(0, backgroundLabel, backgroundComboBox);

        Label experienceLabel = createLabel("Experience Points:");
        experienceField = new TextField();
        experienceField.setPromptText("Enter Experience Points");
        experienceField.setText(String.valueOf(experience));
        formGrid.addRow(1, experienceLabel, experienceField);

        Button nextButton = new Button("Next");
        String buttonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";
        nextButton.setStyle(buttonStyle);
        nextButton.setOnAction(e -> {
            if (backgroundComboBox.getValue() == null) {
                showAlert("Error", "Please select a background.");
                return;
            }
            background = backgroundComboBox.getValue();
            try {
                experience = Integer.parseInt(experienceField.getText());
            } catch (NumberFormatException ex) {
                experience = 0;
            }
            showPhysicalAndPersonalCharacteristics();
        });

        Button backButton = new Button("Go Back");
        backButton.setStyle(buttonStyle);
        backButton.setOnAction(e -> showAbilityScoreGeneration());

        root.getChildren().addAll(title, formGrid, nextButton, backButton);
    }

    private void showPhysicalAndPersonalCharacteristics() {
        root.getChildren().clear();
        Label title = new Label("Physical & Personal Characteristics");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setAlignment(Pos.CENTER);

        ageField = new TextField();
        heightField = new TextField();
        weightField = new TextField();
        hairField = new TextField();
        eyesField = new TextField();
        skinField = new TextField();

        formGrid.addRow(0, createLabel("Age:"), ageField);
        formGrid.addRow(1, createLabel("Height:"), heightField);
        formGrid.addRow(2, createLabel("Weight:"), weightField);
        formGrid.addRow(3, createLabel("Hair:"), hairField);
        formGrid.addRow(4, createLabel("Eyes:"), eyesField);
        formGrid.addRow(5, createLabel("Skin:"), skinField);

        Button nextButton = new Button("Next");
        String buttonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";
        nextButton.setStyle(buttonStyle);
        nextButton.setOnAction(e -> showAlignmentAndEquipment());

        Button backButton = new Button("Go Back");
        backButton.setStyle(buttonStyle);
        backButton.setOnAction(e -> showBackgroundAndExperience());

        root.getChildren().addAll(title, formGrid, nextButton, backButton);
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web("#fff"));
        return label;
    }

    private void showAlignmentAndEquipment() {
        root.getChildren().clear();
        Label title = new Label("Alignment & Equipment");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setAlignment(Pos.CENTER);

        Label alignmentLabel = createLabel("Alignment:");
        alignmentComboBox = new ComboBox<>();
        alignmentComboBox.getItems().addAll(alignments);
        formGrid.add(alignmentLabel, 0, 0);
        formGrid.add(alignmentComboBox, 1, 0);

        Label faithLabel = createLabel("Faith:");
        faithField = new TextField();
        formGrid.add(faithLabel, 0, 1);
        formGrid.add(faithField, 1, 1);

        Label lifestyleLabel = createLabel("Lifestyle:");
        lifestyleField = new TextField();
        formGrid.add(lifestyleLabel, 0, 2);
        formGrid.add(lifestyleField, 1, 2);

        Label equipmentLabel = createLabel("Choose Equipment:");
        RadioButton startingEquipmentRadio = new RadioButton("Starting Equipment");
        RadioButton startingGoldRadio = new RadioButton("Starting Gold");
        ToggleGroup equipmentGroup = new ToggleGroup();
        startingEquipmentRadio.setToggleGroup(equipmentGroup);
        startingGoldRadio.setToggleGroup(equipmentGroup);
        startingEquipmentRadio.setSelected(true);

        formGrid.add(equipmentLabel, 0, 3);
        formGrid.add(startingEquipmentRadio, 1, 3);
        formGrid.add(startingGoldRadio, 1, 4);

        startingEquipmentRadio.setTextFill(Color.WHITE);
        startingGoldRadio.setTextFill(Color.WHITE);

        Button manageEquipmentButton = new Button("Manage Equipment");
        String manageButtonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";
        manageEquipmentButton.setStyle(manageButtonStyle);
        manageEquipmentButton.setOnAction(e -> showStartingEquipment());

        Button finishButton = new Button("Finish Character");
        String finishButtonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #28a745; -fx-text-fill: white;";
        finishButton.setStyle(finishButtonStyle);
        finishButton.setOnAction(e -> {
            if (alignmentComboBox.getValue() == null) {
                showAlert("Error", "Please select an alignment.");
                return;
            }
            showCharacterSheetOptions();
        });

        Button backButton = new Button("Go Back");
        String backButtonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";
        backButton.setStyle(backButtonStyle);
        backButton.setOnAction(e -> showPhysicalAndPersonalCharacteristics());

        root.getChildren().addAll(title, formGrid, manageEquipmentButton, finishButton, backButton);
    }

    private void showStartingEquipment() {
        root.getChildren().clear();

        Label title = new Label(selectedClass + " Starting Equipment");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#ff0000"));

        VBox equipmentList = new VBox(10);
        equipmentList.setAlignment(Pos.CENTER_LEFT);

        String[] equipmentItems = classEquipment.getOrDefault(selectedClass, new String[]{"No equipment specified for this class."});

        for (String item : equipmentItems) {
            Label itemLabel = new Label("• " + item);
            itemLabel.setTextFill(Color.web("#fff"));
            itemLabel.setFont(Font.font("Arial", 16));
            equipmentList.getChildren().add(itemLabel);
        }

        ScrollPane scrollPane = new ScrollPane(equipmentList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #000; -fx-border-color: #000;");

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button backButton = new Button("Go Back");
        String backButtonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";
        backButton.setStyle(backButtonStyle);
        backButton.setOnAction(e -> showAlignmentAndEquipment());

        buttonBox.getChildren().addAll(backButton);
        root.getChildren().addAll(title, scrollPane, buttonBox);
    }

    private void showCharacterSheetOptions() {
        // Create the Character object
        Character newCharacter = new Character(
                characterName,
                playerName,
                selectedClass,
                selectedSubclass,
                selectedSpecies,
                background,
                experience,
                abilityScores,
                alignmentComboBox.getValue(),
                ageField.getText(),
                heightField.getText(),
                weightField.getText(),
                hairField.getText(),
                eyesField.getText(),
                skinField.getText(),
                faithField.getText(),
                lifestyleField.getText()
        );

        // Save the character to a file
        CharacterFileManager.saveCharacter(newCharacter);

        root.getChildren().clear();
        Label title = new Label("Character Complete!");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#28a745"));

        Label subtitle = new Label("What would you like to do next?");
        subtitle.setTextFill(Color.web("#fff"));

        Button viewSheetButton = new Button("Go to Sheet");
        Button createPdfButton = new Button("Create PDF");

        String buttonStyle = "-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;";
        viewSheetButton.setStyle(buttonStyle);
        createPdfButton.setStyle(buttonStyle);

        viewSheetButton.setOnAction(e -> {
            CharacterSheetPage sheetPage = new CharacterSheetPage(newCharacter, primaryStage, new MyCharactersPage(primaryStage, null).createScene());
            primaryStage.setScene(sheetPage.createScene());
            primaryStage.setTitle(newCharacter.getName() + " Character Sheet");
        });

        createPdfButton.setOnAction(e -> {
            PdfGenerator pdfGenerator = new PdfGenerator();
            pdfGenerator.generatePdf(newCharacter);
        });

        Button backToMenuButton = new Button("Back to Main Menu");
        backToMenuButton.setStyle(buttonStyle);
        backToMenuButton.setOnAction(e -> primaryStage.setScene(previousScene));

        root.getChildren().addAll(title, subtitle, viewSheetButton, createPdfButton, backToMenuButton);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void populateClassSubclasses() {
        classSubclasses.put("Artificer", new String[]{"Armorer(TC)", "Alchemist(TC)", "Artillerist(TC)", "Battle Smith(TC)"});
        classSubclasses.put("Barbarian", new String[]{"Berserker(PH)", "Totem Warrior(PH)", "Ancestral Guardian(XG)", "Storm Herald(XG)", "Zealot(XG)", "Beast(TC)", "Wild Soul(TC)", "Battlerager(SC)"});
        classSubclasses.put("Bard", new String[]{"College of Lore(PH)", "College of Valor(PH)", "College of Creation(TC)", "College of Glamor(XG)", "College of Swords(XG)", "College of Whispers(XG)", "College of Eloquence(TC)", "College of Spirits(RL)"});
        classSubclasses.put("Cleric", new String[]{"Knowledge Domain(PH)", "Life Domain(PH)", "Light Domain(PH)", "Nature Domain(PH)", "Tempest Domain(PH)", "Trickery Domain(PH)", "War Domain(PH)", "Death Domain(DM)", "Twilight Domain(TC)", "Order Domain(TC)", "Forge Domain(XG)", "Grave Domain(XG)", "Peace Domain(TC)", "Arcane Domain(SC)"});
        classSubclasses.put("Druid", new String[]{"Circle of the Land(PH)", "Circle of the Moon(PH)", "Circle of Dreams(XG)", "Circle of the Shepherd(XG)", "Circle of Spores(TC)", "Circle of Stars(TC)", "Circle of Wildfire(TC)"});
        classSubclasses.put("Fighter", new String[]{"Champion(PH)", "Battle Master(PH)", "Eldritch Knight(PH)", "Arcane Archer(XG)", "Cavalier(XG)", "Samurai(XG)", "Psi Warrior(TC)", "Rune Knight(TC)", "Echo Fighter(WM)", "Purple Dragon Knight(SC)"});
        classSubclasses.put("Monk", new String[]{"Way of the Open Hand(PH)", "Way of the Shadow(PH)", "Way of the Four Elements(PH)", "Way of Mercy(TC)", "Way of the Astral Self(TC)", "Way of the Drunken Master(XG)", "Way of the Kensei(XG)", "Way of the Sun Soul(XG)", "Way of Long Death(SC)", "Way of the Ascendant Dragon(FD)"});
        classSubclasses.put("Paladin", new String[]{"Oath of Devotion(PH)", "Oath of the Ancients(PH)", "Oath of Vengeance(PH)", "Oathbreaker(DM)", "Oath of Conquest(XG)", "Oath of Redemption(XG)", "Oath of Glory(TC)", "Oath of the Watchers(TC)", "Oath of the Crown(SC)"});
        classSubclasses.put("Ranger", new String[]{"Fey Wanderer(TC)", "Swarmkeeper(TC)", "Gloom Stalker(XG)", "Horizon Walker(XG)", "Monster Slayer(XG)", "Hunter(PH)", "Beast Master(PH)", "Drakewarden(FD)"});
        classSubclasses.put("Rogue", new String[]{"Thief(PH)", "Assassin(PH)", "Arcane Trickster(PH)", "Inquisitive(XG)", "Mastermind(XG)", "Scout(XG)", "Swashbuckler(XG)", "Phantom(TC)", "Soulknife(TC)"});
        classSubclasses.put("Sorcerer", new String[]{"Aberrant Mind(TC)", "Clockwork Soul(TC)", "Divine Soul(XG)", "Shadow Magic(XG)", "Storm Sorcery(XG)", "Draconic Bloodline(PH)", "Wild Magic(PH)"});
        classSubclasses.put("Warlock", new String[]{"The Archfey(PH)", "The Fiend(PH)", "The Great Old One(PH)", "The Celestial(XG)", "Undying(SC)", "The Hexblade(XG)", "The Fathomless(TC)", "The Genie(TC)", "The Undead(RL)"});
        classSubclasses.put("Wizard", new String[]{"School of Abjuration(PH)", "School of Conjuration(PH)", "School of Divination(PH)", "School of Enchantment(PH)", "School of Evocation(PH)", "School of Illusion(PH)", "School of Necromancy(PH)", "School of Transmutation(PH)", "School of Graviturgy(WM)", "School of Chronurgy(WM)", "War Magic(XG)", "Bladesinging(TC)", "Order of Scribes(TC)"});
    }

    private void populateSpeciesData() {
        speciesData.put("Aarakocra", new String[]{"Flight", "Talons"});
        speciesData.put("Aasimar", new String[]{"Celestial Resistance", "Innate Spellcasting"});
        speciesData.put("Autognome", new String[]{"Built for Success", "Durable body"});
        speciesData.put("Bugbear", new String[]{"Surprise Attack", "Long Limbs"});
        speciesData.put("Centaur", new String[]{"Charge", "High speed"});
        speciesData.put("Changeling", new String[]{"Shapeshifting", "Social Skills"});
        speciesData.put("Custom Lineage", new String[]{"Darkvision", "Feat at level 1"});
        speciesData.put("Dhampir Lineage", new String[]{"Vampire bite", "Spider Climb"});
        speciesData.put("Dragonborn", new String[]{"Breath Weapon", "Damage Resistance"});
        speciesData.put("Dwarf", new String[]{"Poison Resistance", "Weapon Proficiency"});
        speciesData.put("Duergar", new String[]{"Superior Darkvision", "Innate Spellcasting"});
        speciesData.put("Elf", new String[]{"Trance", "Perception Proficiency"});
        speciesData.put("Astral Elf", new String[]{"Astral Projection", "Free Cantrip"});
        speciesData.put("Eladrin", new String[]{"Fey Step", "Trance"});
        speciesData.put("Sea Elf", new String[]{"Swim speed", "Communicate with fish"});
        speciesData.put("Shadar-Kai", new String[]{"Necrotic Resistance", "Teleportation"});
        speciesData.put("Fairy", new String[]{"Flight", "Faerie Fire"});
        speciesData.put("Firbolg", new String[]{"Hidden Step", "Speech of Beast and Leaf"});
        speciesData.put("Genasi", new String[]{"Elemental Resistance", "Innate Spellcasting"});
        speciesData.put("Air Genasi", new String[]{"Unending Breath", "Levitate"});
        speciesData.put("Earth Genasi", new String[]{"Merge with Stone", "Pass without Trace"});
        speciesData.put("Fire Genasi", new String[]{"Produce Flame", "Hellish Rebuke"});
        speciesData.put("Water Genasi", new String[]{"Acid Resistance", "Swim speed"});
        speciesData.put("Giff", new String[]{"Firearms Proficiency", "Grapple and Shove"});
        speciesData.put("Gith", new String[]{"Psionics", "Extra Proficiencies"});
        speciesData.put("Githyanki", new String[]{"Martial Psionics", "Jump"});
        speciesData.put("Githzerai", new String[]{"Defensive Psionics", "Shield"});
        speciesData.put("Gnome", new String[]{"Gnome Cunning", "Artificer's Lore"});
        speciesData.put("Deep Gnome", new String[]{"Superior Darkvision", "Stone Camouflage"});
        speciesData.put("Goblin", new String[]{"Fury of the Small", "Nimble Escape"});
        speciesData.put("Goliath", new String[]{"Stone's Endurance", "Cold Resistance"});
        speciesData.put("Hadozee", new String[]{"Gliding", "Dexterous feet"});
        speciesData.put("Half-Elf", new String[]{"Ability Score Versatility", "Skill Versatility"});
        speciesData.put("Half-Orc", new String[]{"Relentless Endurance", "Savage Attacks"});
        speciesData.put("Halfling", new String[]{"Lucky", "Halfling Nimbleness"});
        speciesData.put("Harengon", new String[]{"Rabbit Hop", "Lucky Footwork"});
        speciesData.put("Hexblood Lineage", new String[]{"Hex Magic", "Fey Touched"});
        speciesData.put("Hobgoblin", new String[]{"Fey Gift", "Saving Face"});
        speciesData.put("Human", new String[]{"Versatility", "Extra Skill"});
        speciesData.put("Kalashtar", new String[]{"Telepathy", "Psychic Resistance"});
        speciesData.put("Kender", new String[]{"Taunt", "Kender's Kindness"});
        speciesData.put("Kenku", new String[]{"Mimicry", "Expert Forgery"});
        speciesData.put("Kobold", new String[]{"Draconic Cry", "Legacy of the Wyrm"});
        speciesData.put("Leonin", new String[]{"Daunting Roar", "Claws"});
        speciesData.put("Lizardfolk", new String[]{"Natural Armor", "Bite"});
        speciesData.put("Loxodon", new String[]{"Natural Armor", "Trunk"});
        speciesData.put("Minotaur", new String[]{"Horns", "Goring Rush"});
        speciesData.put("Orc", new String[]{"Adrenaline Rush", "Powerful Build"});
        speciesData.put("Owlin", new String[]{"Flight", "Darkvision"});
        speciesData.put("Plasmoid", new String[]{"Amorphous", "Unarmed strike"});
        speciesData.put("Reborn Lineage", new String[]{"Knowledge from a Past Life", "Deathless Nature"});
        speciesData.put("Satyr", new String[]{"Magic Resistance", "Ram"});
        speciesData.put("Shifter", new String[]{"Shifting", "Bestial Traits"});
        speciesData.put("Simic Hybrid", new String[]{"Animal Enhancements", "Grappling Appendages"});
        speciesData.put("Tabaxi", new String[]{"Feline Agility", "Claws"});
        speciesData.put("Thri-Kreen", new String[]{"Multiple Arms", "Chameleon Carapace"});
        speciesData.put("Tiefling", new String[]{"Fire Resistance", "Hellish Rebuke"});
        speciesData.put("Tortle", new String[]{"Natural Armor", "Shell Defense"});
        speciesData.put("Triton", new String[]{"Amphibious", "Control Water"});
        speciesData.put("Vedalken", new String[]{"Vedalken Dispassion", "Tireless Precision"});
        speciesData.put("Verdan", new String[]{"Telepathy", "Limited Telepathy"});
        speciesData.put("Warforged", new String[]{"Constructed Resilience", "Integrated Protection"});
        speciesData.put("Yuan-Ti", new String[]{"Poison Immunity", "Magic Resistance"});
    }

    private void populateClassEquipment() {
        classEquipment.put("Artificer", new String[]{
                "any two simple weapons of your choice",
                "a light crossbow and 20 bolts",
                "a a studded leather armor or scale mail (if you are proficient with it)",
                "a set of smith's tools or mason's tools or tinker's tools",
                "a set of theives' tools or an arcane focus",
                "a dungeoneer's pack or an explorer's pack"});
        classEquipment.put("Barbarian", new String[]{
                "a greataxe or any martial weapon",
                "two handaxes or any simple weapon",
                "an explorer's pack",
                "four javelins"});
        classEquipment.put("Bard", new String[]{
                "a rapier, longsword, or any simple weapon",
                "a diplomat's pack or an entertainer's pack",
                "a lute or any other musical instrument",
                "leather armor and a dagger"});
        classEquipment.put("Cleric", new String[]{
                "a mace or warhammer",
                "a shield or a light crossbow and 20 bolts",
                "a priest's pack or an explorer's pack",
                "a holy symbol, light armor, and a shield"});
        classEquipment.put("Druid", new String[]{"a shield or any simple weapon", "a scimitar or any simple melee weapon", "leather armor, an explorer's pack, and a druidic focus"});
        classEquipment.put("Fighter", new String[]{
                "chain mail or leather armor, longbow, and 20 arrows",
                "a martial weapon and a shield or two martial weapons",
                "a light crossbow and 20 bolts or two handaxes",
                "a dungeoneer's pack or an explorer's pack"});
        classEquipment.put("Monk", new String[]{"a shortsword or any simple weapon", "a dungeoneer's pack or an explorer's pack", "10 darts"});
        classEquipment.put("Paladin", new String[]{
                "a martial weapon and a shield or two martial weapons",
                "five javelins or a light crossbow and 20 bolts",
                "a priest's pack or an explorer's pack",
                "chain mail and a holy symbol"});
        classEquipment.put("Ranger", new String[]{
                "scale mail or leather armor",
                "two shortswords or two simple melee weapons",
                "a dungeoneer's pack or an explorer's pack",
                "a longbow and 20 arrows"});
        classEquipment.put("Rogue", new String[]{
                "a rapier or a shortsword",
                "a shortbow and 20 arrows or a shortsword",
                "a burglar's pack, dungeoneer's pack, or an explorer's pack",
                "leather armor, two daggers, and thieves' tools"});
        classEquipment.put("Sorcerer", new String[]{
                "a light crossbow and 20 bolts or any simple weapon",
                "a component pouch or an arcane focus",
                "a dungeoneer's pack or an explorer's pack",
                "two daggers"});
        classEquipment.put("Warlock", new String[]{
                "a light crossbow and 20 bolts or any simple weapon",
                "a component pouch or an arcane focus",
                "a scholar's pack or a dungeoneer's pack",
                "leather armor, any simple weapon, and two daggers"});
        classEquipment.put("Wizard", new String[]{
                "a quarterstaff or a dagger",
                "a component pouch or an arcane focus",
                "a scholar's pack or an explorer's pack",
                "a spellbook"});
    }

    private void populateBackgrounds() {
        // This is a placeholder for a list of common D&D backgrounds
        // In a real application, this would be read from a file or database
        // to avoid hardcoding.
    }
}