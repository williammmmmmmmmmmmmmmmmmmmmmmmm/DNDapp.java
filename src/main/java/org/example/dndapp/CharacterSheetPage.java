package org.example.dndapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CharacterSheetPage {
    private final Character character;
    private final Stage primaryStage;
    private final Scene previousScene;
    private final VBox root;

    private final Map<String, VBox> equipmentSlots = new LinkedHashMap<>();
    private final List<String> nonSpellcastingClasses = Arrays.asList("Barbarian", "Fighter", "Monk", "Rogue");
    private final Map<Integer, List<String>> availableSpells = new HashMap<>();

    // New data maps to store base values for calculations
    private final Map<String, Integer> speciesBaseSpeed = new HashMap<>();
    private final Map<String, Integer> classHitDice = new HashMap<>();
    private ListView<Item> inventoryList;

    public CharacterSheetPage(Character character, Stage primaryStage, Scene previousScene) {
        this.character = character;
        this.primaryStage = primaryStage;
        this.previousScene = previousScene;
        this.root = new VBox();
        this.root.setPadding(new Insets(20));
        this.root.setStyle("-fx-background-color: #212529;");

        // Populate the new data maps
        populateSpeciesBaseSpeed();
        populateClassHitDice();

        if (!nonSpellcastingClasses.contains(character.getSelectedClass())) {
            loadAvailableSpells();
        }
    }

    // Helper method to populate species base speed data
    private void populateSpeciesBaseSpeed() {
        speciesBaseSpeed.put("Dragonborn", 30);
        speciesBaseSpeed.put("Dwarf", 25);
        speciesBaseSpeed.put("Elf", 30);
        speciesBaseSpeed.put("Gnome", 25);
        speciesBaseSpeed.put("Half-Elf", 30);
        speciesBaseSpeed.put("Half-Orc", 30);
        speciesBaseSpeed.put("Halfling", 25);
        speciesBaseSpeed.put("Human", 30);
        speciesBaseSpeed.put("Tiefling", 30);
    }

    // Helper method to populate class hit dice data
    private void populateClassHitDice() {
        classHitDice.put("Barbarian", 12);
        classHitDice.put("Bard", 8);
        classHitDice.put("Cleric", 8);
        classHitDice.put("Druid", 8);
        classHitDice.put("Fighter", 10);
        classHitDice.put("Monk", 8);
        classHitDice.put("Paladin", 10);
        classHitDice.put("Ranger", 10);
        classHitDice.put("Rogue", 8);
        classHitDice.put("Sorcerer", 6);
        classHitDice.put("Warlock", 8);
        classHitDice.put("Wizard", 6);
    }

    public Scene createScene() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(createHeader());
        mainLayout.setCenter(createMainContent());
        mainLayout.setPadding(new Insets(15));
        mainLayout.setStyle("-fx-background-color: #212529;");

        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setStyle("-fx-background-color: #212529;");

        Button backButton = new Button("Back to My Characters");
        backButton.setStyle("-fx-padding: 10 20; -fx-font-size: 16px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-color: #007BFF; -fx-text-fill: white;");
        backButton.setOnAction(e -> primaryStage.setScene(previousScene));
        backButton.setPrefWidth(250);

        HBox buttonBox = new HBox(backButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        VBox fullRoot = new VBox(scrollPane, buttonBox);
        fullRoot.setFillWidth(true);

        Scene scene = new Scene(fullRoot, 1200, 800);
        return scene;
    }

    private VBox createHeader() {
        VBox header = new VBox();
        header.setAlignment(Pos.CENTER);
        header.setSpacing(5);

        Label nameLabel = new Label(character.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        nameLabel.setTextFill(Color.web("#dee2e6"));

        Label classSpeciesLabel = new Label(character.getSelectedClass() + " | " + character.getSelectedSpecies());
        classSpeciesLabel.setFont(Font.font("Arial", 18));
        classSpeciesLabel.setTextFill(Color.web("#adb5bd"));

        header.getChildren().addAll(nameLabel, classSpeciesLabel);
        return header;
    }

    private GridPane createMainContent() {
        GridPane mainGrid = new GridPane();
        mainGrid.setHgap(20);
        mainGrid.setVgap(20);
        mainGrid.setPadding(new Insets(20, 0, 0, 0));
        mainGrid.setAlignment(Pos.TOP_CENTER);

        // Left Column
        VBox leftColumn = new VBox(20);
        leftColumn.setAlignment(Pos.TOP_CENTER);
        leftColumn.getChildren().addAll(
                createVitalsSection(),
                createAbilityScoresSection(),
                createSavingThrowsSection(),
                createSkillsSection()
        );

        // Center Column
        VBox centerColumn = new VBox(20);
        centerColumn.setAlignment(Pos.TOP_CENTER);
        centerColumn.getChildren().addAll(
                createEquipmentSection(),
                createInventorySection(),
                createPhysicalAndPersonalSection()
        );

        // Right Column
        VBox rightColumn = new VBox(20);
        rightColumn.setAlignment(Pos.TOP_CENTER);
        rightColumn.getChildren().addAll(
                createActionsSection(),
                createFeatsAndTraitsSection()
        );
        if (!nonSpellcastingClasses.contains(character.getSelectedClass())) {
            rightColumn.getChildren().add(0, createSpellsSection());
        }

        mainGrid.add(leftColumn, 0, 0);
        mainGrid.add(centerColumn, 1, 0);
        mainGrid.add(rightColumn, 2, 0);

        // Stretching the columns to fill the space
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setHgrow(Priority.ALWAYS);
        mainGrid.getColumnConstraints().addAll(col1, col2, col3);

        return mainGrid;
    }

    private VBox createSection(String title) {
        VBox section = new VBox(10);
        section.setStyle("-fx-background-color: #343a40; -fx-padding: 15; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #495057; -fx-border-width: 1;");
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#e9ecef"));
        section.getChildren().add(titleLabel);
        return section;
    }

    private VBox createEquipmentSection() {
        VBox equipmentSection = createSection("Equipment");
        equipmentSection.setAlignment(Pos.CENTER);

        GridPane gearGrid = new GridPane();
        gearGrid.setHgap(5);
        gearGrid.setVgap(5);
        gearGrid.setAlignment(Pos.CENTER);
        gearGrid.setPadding(new Insets(10));

        // Load the species silhouette image
        String speciesName = character.getSelectedSpecies().replaceAll("\\s+", ""); // Remove spaces
        Image speciesSilhouette = new Image(
                new File("src/main/resources/Silhouettes/" + speciesName + ".png").toURI().toString()
        );
        ImageView silhouetteView = new ImageView(speciesSilhouette);
        silhouetteView.setFitWidth(150);
        silhouetteView.setPreserveRatio(true);

        // Create the gear slots
        VBox primaryWeapon = createEquipmentSlot("Primary Weapon");
        VBox offHand = createEquipmentSlot("Off-Hand");
        VBox twoHanded = createEquipmentSlot("Two-Handed");
        VBox secondary = createEquipmentSlot("Secondary");
        VBox armor = createEquipmentSlot("Armor");
        VBox helm = createEquipmentSlot("Helm");
        VBox gauntlets = createEquipmentSlot("Gauntlets");
        VBox boots = createEquipmentSlot("Boots");
        VBox necklace = createEquipmentSlot("Necklace");
        VBox ring1 = createEquipmentSlot("Ring 1");
        VBox ring2 = createEquipmentSlot("Ring 2");

        // Store references to the slots
        equipmentSlots.put("Primary Weapon", primaryWeapon);
        equipmentSlots.put("Off-Hand", offHand);
        equipmentSlots.put("Two-Handed", twoHanded);
        equipmentSlots.put("Secondary", secondary);
        equipmentSlots.put("Armor", armor);
        equipmentSlots.put("Helm", helm);
        equipmentSlots.put("Gauntlets", gauntlets);
        equipmentSlots.put("Boots", boots);
        equipmentSlots.put("Necklace", necklace);
        equipmentSlots.put("Ring 1", ring1);
        equipmentSlots.put("Ring 2", ring2);

        // Place the items in a clean layout around the silhouette
        gearGrid.add(twoHanded, 0, 0);
        gearGrid.add(helm, 1, 0);
        gearGrid.add(primaryWeapon, 0, 1);
        gearGrid.add(silhouetteView, 1, 1);
        gearGrid.add(offHand, 2, 1);
        gearGrid.add(gauntlets, 0, 2);
        gearGrid.add(armor, 1, 2);
        gearGrid.add(boots, 2, 2);
        gearGrid.add(ring1, 0, 3);
        gearGrid.add(necklace, 1, 3);
        gearGrid.add(ring2, 2, 3);
        gearGrid.add(secondary, 1, 4);

        equipmentSection.getChildren().add(gearGrid);
        updateEquippedItemsDisplay();
        return equipmentSection;
    }

    private VBox createEquipmentSlot(String name) {
        VBox slot = new VBox(3);
        slot.setAlignment(Pos.CENTER);
        slot.setStyle("-fx-border-color: #6c757d; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 5; -fx-background-color: #495057;");
        slot.setPrefSize(80, 40);

        Label label = new Label(name);
        label.setFont(Font.font("Arial", 10));
        label.setTextFill(Color.web("#e9ecef"));

        Label itemLabel = new Label("Empty");
        itemLabel.setFont(Font.font("Arial", FontWeight.BOLD, 8));
        itemLabel.setTextFill(Color.web("#ffc107"));

        slot.getChildren().addAll(label, itemLabel);
        return slot;
    }

    private void updateEquippedItemsDisplay() {
        // First, clear all slots
        for (VBox slot : equipmentSlots.values()) {
            ((Label) slot.getChildren().get(1)).setText("Empty");
        }

        // Then, update with equipped items
        if (character.getEquippedItems() != null) {
            for (Map.Entry<String, Item> entry : character.getEquippedItems().entrySet()) {
                VBox slot = equipmentSlots.get(entry.getKey());
                if (slot != null) {
                    Label itemLabel = (Label) slot.getChildren().get(1);
                    itemLabel.setText(entry.getValue().getName());
                }
            }
        }
    }

    private VBox createInventorySection() {
        VBox inventorySection = createSection("Inventory");
        inventoryList = new ListView<>();
        inventoryList.setItems(javafx.collections.FXCollections.observableArrayList(character.getInventory()));
        inventoryList.setPrefHeight(200);

        // Context menu for equipping and editing items
        ContextMenu contextMenu = new ContextMenu();
        MenuItem equipItem = new MenuItem("Equip Item");
        MenuItem editDetails = new MenuItem("Edit Details");
        contextMenu.getItems().addAll(equipItem, editDetails);

        inventoryList.setContextMenu(contextMenu);

        // Action for equipping an item
        equipItem.setOnAction(event -> {
            Item selectedItem = inventoryList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                // Show a dialog to select the equipment slot
                Dialog<String> slotDialog = new Dialog<>();
                slotDialog.setTitle("Select Equipment Slot");
                slotDialog.setHeaderText("Choose a slot to equip " + selectedItem.getName());
                ButtonType confirmButtonType = new ButtonType("Equip", ButtonBar.ButtonData.OK_DONE);
                slotDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

                ComboBox<String> slotComboBox = new ComboBox<>();
                slotComboBox.getItems().addAll("Primary Weapon", "Off-Hand", "Two-Handed", "Secondary", "Armor", "Helm", "Gauntlets", "Boots", "Necklace", "Ring 1", "Ring 2");
                slotDialog.getDialogPane().setContent(slotComboBox);

                Optional<String> result = slotDialog.showAndWait();
                result.ifPresent(slotName -> {
                    character.equipItem(selectedItem, slotComboBox.getValue());
                    inventoryList.getItems().setAll(character.getInventory());
                    updateEquippedItemsDisplay();
                    CharacterFileManager.saveCharacter(character);
                });
            }
        });

        // Action for editing item details
        editDetails.setOnAction(event -> {
            Item selectedItem = inventoryList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                // Show a dialog to edit item details
                Dialog<Item> editDialog = new Dialog<>();
                editDialog.setTitle("Edit Item Details");
                editDialog.setHeaderText("Editing details for " + selectedItem.getName());
                ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
                editDialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));

                TextField nameField = new TextField(selectedItem.getName());
                TextArea descriptionArea = new TextArea(selectedItem.getDescription());
                descriptionArea.setWrapText(true);
                descriptionArea.setPrefHeight(100);

                grid.add(new Label("Name:"), 0, 0);
                grid.add(nameField, 1, 0);
                grid.add(new Label("Description:"), 0, 1);
                grid.add(descriptionArea, 1, 1);

                editDialog.getDialogPane().setContent(grid);

                // Convert the result to an Item object when the Save button is clicked
                editDialog.setResultConverter(dialogButton -> {
                    if (dialogButton == saveButtonType) {
                        selectedItem.setName(nameField.getText());
                        selectedItem.setDescription(descriptionArea.getText());
                        return selectedItem;
                    }
                    return null;
                });

                Optional<Item> result = editDialog.showAndWait();
                result.ifPresent(item -> {
                    inventoryList.refresh();
                    CharacterFileManager.saveCharacter(character);
                });
            }
        });


        TextField newItemField = new TextField();
        newItemField.setPromptText("Enter new item name...");
        newItemField.setStyle("-fx-background-color: #495057; -fx-text-fill: #e9ecef; -fx-prompt-text-fill: #adb5bd; -fx-border-color: #6c757d; -fx-border-width: 1; -fx-border-radius: 5;");
        newItemField.setPrefWidth(200);

        Button addItemButton = new Button("Add Item");
        addItemButton.setStyle("-fx-padding: 5 10; -fx-font-size: 12px; -fx-cursor: hand; -fx-background-color: #28a745; -fx-text-fill: white;");

        addItemButton.setOnAction(event -> {
            String itemName = newItemField.getText().trim();
            if (!itemName.isEmpty()) {
                Item newItem = new Item(itemName, ""); // Assuming a simple item with no description for now
                character.getInventory().add(newItem);
                inventoryList.getItems().add(newItem);
                newItemField.clear();
                CharacterFileManager.saveCharacter(character);
            }
        });

        HBox inventoryControls = new HBox(10, newItemField, addItemButton);
        inventoryControls.setAlignment(Pos.CENTER);
        inventoryControls.setPadding(new Insets(10, 0, 0, 0));

        inventorySection.getChildren().addAll(inventoryList, inventoryControls);
        return inventorySection;
    }

    private VBox createPhysicalAndPersonalSection() {
        VBox section = createSection("Physical & Personal Details");
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(15);
        detailsGrid.setVgap(5);

        // Add details to the grid
        detailsGrid.add(createDetailLabel("Alignment"), 0, 0);
        detailsGrid.add(createDetailValue(character.getAlignment()), 1, 0);
        detailsGrid.add(createDetailLabel("Age"), 0, 1);
        detailsGrid.add(createDetailValue(character.getAge()), 1, 1);
        detailsGrid.add(createDetailLabel("Height"), 0, 2);
        detailsGrid.add(createDetailValue(character.getHeight()), 1, 2);
        detailsGrid.add(createDetailLabel("Weight"), 0, 3);
        detailsGrid.add(createDetailValue(character.getWeight()), 1, 3);
        detailsGrid.add(createDetailLabel("Hair"), 0, 4);
        detailsGrid.add(createDetailValue(character.getHair()), 1, 4);
        detailsGrid.add(createDetailLabel("Eyes"), 0, 5);
        detailsGrid.add(createDetailValue(character.getEyes()), 1, 5);
        detailsGrid.add(createDetailLabel("Skin"), 0, 6);
        detailsGrid.add(createDetailValue(character.getSkin()), 1, 6);
        detailsGrid.add(createDetailLabel("Faith"), 0, 7);
        detailsGrid.add(createDetailValue(character.getFaith()), 1, 7);
        detailsGrid.add(createDetailLabel("Lifestyle"), 0, 8);
        detailsGrid.add(createDetailValue(character.getLifestyle()), 1, 8);

        section.getChildren().add(detailsGrid);
        return section;
    }

    private Label createDetailLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setTextFill(Color.web("#adb5bd"));
        return label;
    }

    private Label createDetailValue(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", 14));
        label.setTextFill(Color.web("#e9ecef"));
        return label;
    }

    private VBox createVitalsSection() {
        VBox vitals = createSection("Vitals");
        GridPane vitalsGrid = new GridPane();
        vitalsGrid.setHgap(10);
        vitalsGrid.setVgap(5);

        vitalsGrid.add(createVitalsStatLabel("Level"), 0, 0);
        vitalsGrid.add(createVitalsStatValue(String.valueOf(character.getLevel())), 1, 0);
        vitalsGrid.add(createVitalsStatLabel("XP"), 0, 1);
        vitalsGrid.add(createVitalsStatValue(String.valueOf(character.getExperience())), 1, 1);
        vitalsGrid.add(createVitalsStatLabel("HP"), 0, 2);
        vitalsGrid.add(createVitalsStatValue(String.valueOf(character.getHitPoints())), 1, 2);
        vitalsGrid.add(createVitalsStatLabel("AC"), 0, 3);
        vitalsGrid.add(createVitalsStatValue(String.valueOf(character.getArmorClass())), 1, 3);
        vitalsGrid.add(createVitalsStatLabel("Speed"), 0, 4);
        vitalsGrid.add(createVitalsStatValue(String.valueOf(character.getSpeed())), 1, 4);
        vitalsGrid.add(createVitalsStatLabel("Hit Dice"), 0, 5);
        vitalsGrid.add(createVitalsStatValue(character.getHitDice()), 1, 5);

        vitals.getChildren().add(vitalsGrid);
        return vitals;
    }

    private Label createVitalsStatLabel(String text) {
        Label label = new Label(text + ":");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setTextFill(Color.web("#adb5bd"));
        return label;
    }

    private Label createVitalsStatValue(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", 14));
        label.setTextFill(Color.web("#e9ecef"));
        return label;
    }

    private VBox createAbilityScoresSection() {
        VBox section = createSection("Ability Scores");
        GridPane abilityGrid = new GridPane();
        abilityGrid.setHgap(10);
        abilityGrid.setVgap(5);

        int row = 0;
        for (Map.Entry<String, Integer> entry : character.getAbilityScores().entrySet()) {
            String ability = entry.getKey();
            int score = entry.getValue();
            int modifier = (score - 10) / 2;

            abilityGrid.add(createAbilityScoreLabel(ability), 0, row);
            abilityGrid.add(createAbilityScoreValue(String.valueOf(score)), 1, row);
            abilityGrid.add(createAbilityModifierValue(" (" + (modifier >= 0 ? "+" : "") + modifier + ")"), 2, row);
            row++;
        }
        section.getChildren().add(abilityGrid);
        return section;
    }

    private Label createAbilityScoreLabel(String text) {
        Label label = new Label(text + ":");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setTextFill(Color.web("#adb5bd"));
        return label;
    }

    private Label createAbilityScoreValue(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setTextFill(Color.web("#e9ecef"));
        return label;
    }

    private Label createAbilityModifierValue(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", 12));
        label.setTextFill(Color.web("#e9ecef"));
        return label;
    }

    private VBox createSavingThrowsSection() {
        VBox section = createSection("Saving Throws");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);

        int row = 0;
        for (String ability : character.getAbilityScores().keySet()) {
            int modifier = (character.getAbilityScores().get(ability) - 10) / 2;
            boolean isProficient = false; // Placeholder for proficiency check
            String displayModifier = (modifier >= 0 ? "+" : "") + modifier;
            String text = (isProficient ? "• " : "  ") + ability + ": " + displayModifier;

            Label label = new Label(text);
            label.setFont(Font.font("Arial", 14));
            label.setTextFill(Color.web("#e9ecef"));
            grid.add(label, 0, row);
            row++;
        }
        section.getChildren().add(grid);
        return section;
    }

    private VBox createSkillsSection() {
        VBox section = createSection("Skills");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);

        // This would require a more detailed skill proficiency system
        Label placeholder = new Label("Skill list goes here...");
        placeholder.setTextFill(Color.web("#adb5bd"));
        grid.add(placeholder, 0, 0);

        section.getChildren().add(grid);
        return section;
    }

    private VBox createActionsSection() {
        VBox actionsSection = createSection("Actions");
        // Placeholder for actions
        Label placeholder = new Label("Attacks, Spells, and Actions list goes here...");
        placeholder.setTextFill(Color.web("#adb5bd"));
        actionsSection.getChildren().add(placeholder);
        return actionsSection;
    }

    private VBox createFeatsAndTraitsSection() {
        VBox feats = createSection("Features & Traits");
        VBox traitsContainer = new VBox(10);
        traitsContainer.setSpacing(10);
        traitsContainer.setAlignment(Pos.TOP_LEFT);

        Label feature1Title = new Label("Darkvision");
        feature1Title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        feature1Title.setTextFill(Color.web("#e9ecef"));
        TextFlow feature1Description = new TextFlow(
                new Text("You can see in dim light within 60 feet of you as if it were bright light, and in darkness as if it were dim light.")
        );
        feature1Description.setStyle("-fx-fill: #adb5bd;");

        Label feature2Title = new Label("Fey Ancestry");
        feature2Title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        feature2Title.setTextFill(Color.web("#e9ecef"));
        TextFlow feature2Description = new TextFlow(
                new Text("You have advantage on saving throws against being charmed, and magic can't put you to sleep.")
        );
        feature2Description.setStyle("-fx-fill: #adb5bd;");

        traitsContainer.getChildren().addAll(feature1Title, feature1Description, feature2Title, feature2Description);
        feats.getChildren().add(traitsContainer);
        return feats;
    }

    private void loadAvailableSpells() {
        String classFolder = character.getSelectedClass();
        Path classPath = new File(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "Classes" + File.separator + classFolder).toPath();

        try {
            if (Files.exists(classPath) && Files.isDirectory(classPath)) {
                Files.list(classPath)
                        .filter(Files::isDirectory)
                        .forEach(levelPath -> {
                            try {
                                int level = Integer.parseInt(levelPath.getFileName().toString());
                                List<String> spellsAtLevel = Files.list(levelPath)
                                        .filter(p -> p.toString().endsWith(".png"))
                                        .map(p -> p.getFileName().toString().replace(".png", ""))
                                        .collect(Collectors.toList());
                                availableSpells.put(level, spellsAtLevel);
                            } catch (IOException | NumberFormatException e) {
                                e.printStackTrace();
                            }
                        });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private VBox createSpellsSection() {
        VBox spellsSection = createSection("Spells");
        spellsSection.setSpacing(10);

        Label playerSpellsTitle = new Label("Known Spells");
        playerSpellsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        playerSpellsTitle.setTextFill(Color.web("#e9ecef"));
        ListView<String> knownSpellsList = new ListView<>();
        knownSpellsList.setPrefHeight(200);

        final Map<Integer, List<String>> knownSpells = character.getKnownSpells() != null ? character.getKnownSpells() : new HashMap<>();

        knownSpells.values().forEach(knownSpellsList.getItems()::addAll);
        knownSpellsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String spell, boolean empty) {
                super.updateItem(spell, empty);
                setText(spell);
                if (empty || spell == null) {
                    setTooltip(null);
                } else {
                    Tooltip tooltip = getSpellImageTooltip(spell);
                    setTooltip(tooltip);
                }
            }
        });

        // Double-click to remove spell from known spells
        knownSpellsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedSpell = knownSpellsList.getSelectionModel().getSelectedItem();
                if (selectedSpell != null) {
                    knownSpellsList.getItems().remove(selectedSpell);
                    knownSpells.values().forEach(list -> list.remove(selectedSpell));
                }
            }
        });

        Label availableSpellsTitle = new Label("Available Spells");
        availableSpellsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        availableSpellsTitle.setTextFill(Color.web("#e9ecef"));
        TabPane spellsTabPane = new TabPane();
        spellsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        spellsTabPane.getStyleClass().add("floating-tab-pane");

        for (Map.Entry<Integer, List<String>> entry : availableSpells.entrySet()) {
            String levelTitle = entry.getKey() == 0 ? "Cantrips" : "Level " + entry.getKey();
            ListView<String> levelSpellList = new ListView<>();
            levelSpellList.getItems().addAll(entry.getValue());
            levelSpellList.getStyleClass().add("available-spell-list");

            // Double-click to add spell to known spells
            levelSpellList.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    String selectedSpell = levelSpellList.getSelectionModel().getSelectedItem();
                    if (selectedSpell != null && !knownSpellsList.getItems().contains(selectedSpell)) {
                        knownSpellsList.getItems().add(selectedSpell);
                        Tab selectedTab = spellsTabPane.getSelectionModel().getSelectedItem();
                        int level = Integer.parseInt(selectedTab.getText().replace("Level ", "").replace("Cantrips", "0"));
                        knownSpells.computeIfAbsent(level, k -> new ArrayList<>()).add(selectedSpell);
                    }
                }
            });

            Tab tab = new Tab(levelTitle, levelSpellList);
            spellsTabPane.getTabs().add(tab);
        }

        Button addSpellButton = new Button("Add Selected Spell");
        addSpellButton.setStyle("-fx-padding: 8 16; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-color: #28a745; -fx-text-fill: white;");
        addSpellButton.setOnAction(e -> {
            Tab selectedTab = spellsTabPane.getSelectionModel().getSelectedItem();
            if (selectedTab != null) {
                ListView<String> currentListView = (ListView<String>) selectedTab.getContent();
                String selectedSpell = currentListView.getSelectionModel().getSelectedItem();
                if (selectedSpell != null && !knownSpellsList.getItems().contains(selectedSpell)) {
                    knownSpellsList.getItems().add(selectedSpell);
                    knownSpells.computeIfAbsent(
                            Integer.parseInt(selectedTab.getText().replace("Level ", "").replace("Cantrips", "0")),
                            k -> new ArrayList<>()
                    ).add(selectedSpell);
                }
            }
        });

        spellsSection.getChildren().addAll(
                playerSpellsTitle,
                knownSpellsList,
                availableSpellsTitle,
                spellsTabPane,
                addSpellButton
        );
        return spellsSection;
    }

    private Tooltip getSpellImageTooltip(String spellName) {
        String classFolder = character.getSelectedClass();
        for (Map.Entry<Integer, List<String>> entry : availableSpells.entrySet()) {
            if (entry.getValue().contains(spellName)) {
                String levelFolder = String.valueOf(entry.getKey());
                File imageFile = new File("src/main/resources/Classes/" + classFolder + "/" + levelFolder + "/" + spellName + ".png");
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(400);
                    imageView.setFitWidth(300);
                    Tooltip tooltip = new Tooltip();
                    tooltip.setGraphic(imageView);
                    return tooltip;
                }
            }
        }
        return null;
    }

    private VBox createAbilityScoreSection(String ability, int score) {
        VBox abilityBox = new VBox(5);
        abilityBox.setAlignment(Pos.CENTER);
        abilityBox.setPadding(new Insets(10));
        abilityBox.setStyle("-fx-background-color: #343a40; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #495057; -fx-border-width: 1;");

        Label abilityLabel = new Label(ability);
        abilityLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        abilityLabel.setTextFill(Color.web("#e9ecef"));

        Label scoreLabel = new Label(String.valueOf(score));
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        scoreLabel.setTextFill(Color.web("#ffc107"));

        int modifier = (score - 10) / 2;
        Label modifierLabel = new Label((modifier >= 0 ? "+" : "") + modifier);
        modifierLabel.setFont(Font.font("Arial", 14));
        modifierLabel.setTextFill(Color.web("#adb5bd"));

        abilityBox.getChildren().addAll(abilityLabel, scoreLabel, modifierLabel);
        return abilityBox;
    }
}