package org.example.dndapp;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class CharacterSheetPage {
    private final Character character;
    private final Stage primaryStage;
    private final Scene previousScene;
    private final VBox root;

    private final Map<String, VBox> equipmentSlots = new LinkedHashMap<>();
    private final List<String> nonSpellcastingClasses = Arrays.asList("Barbarian", "Fighter", "Monk", "Rogue");
    private final Map<Integer, List<String>> availableSpells = new HashMap<>();

    // Data maps
    private final Map<String, Integer> speciesBaseSpeed = new HashMap<>();
    private final Map<String, Integer> classHitDice = new HashMap<>();
    private ListView<Item> inventoryList;

    // Fields for the Vitals and Details labels that need to be updated
    private VBox actionsSectionVBox;
    private VBox featuresSectionVBox;
    private VBox skillsSectionVBox;
    private Label levelValueLabel;
    private Label expValueLabel;
    private Label hpValueLabel;
    private Label acValueLabel;
    private Label speedValueLabel;
    private Label hitDiceValueLabel;
    private Map<String, Label> detailValueLabels = new HashMap<>();
    private Map<String, Label> abilityScoreValueLabels = new HashMap<>();

    // Static placeholder for skills (since no field was added to Character.java)
    private static List<String> placeholderSkills = new ArrayList<>(Arrays.asList(
            "Acrobatics (DEX): +2",
            "Animal Handling (WIS): -1",
            "Arcana (INT): +0",
            "Athletics (STR): +4 (P)"
    ));


    public CharacterSheetPage(Character character, Stage primaryStage, Scene previousScene) {
        this.character = character;
        this.primaryStage = primaryStage;
        this.previousScene = previousScene;
        this.root = new VBox();
        this.root.setPadding(new Insets(20));
        this.root.setStyle("-fx-background-color: #212529;");

        populateSpeciesBaseSpeed();
        populateClassHitDice();

        if (!nonSpellcastingClasses.contains(character.getSelectedClass())) {
            loadAvailableSpells();
        }
        // Initialize placeholder lists if they are empty from a new character
        if (character.getFeaturesAndTraits().isEmpty()) {
            character.getFeaturesAndTraits().add("Darkvision | You can see in dim light within 60 feet of you.");
            character.getFeaturesAndTraits().add("Fey Ancestry | You have advantage on saving throws against being charmed.");
        }
        // Add a placeholder action if empty
        if (character.getCustomActions().isEmpty()) {
            character.getCustomActions().add("Unarmed Strike | 1d4 Bludgeoning Damage");
        }
    }

    private void populateSpeciesBaseSpeed() {
        // ... (Unchanged helper method)
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

    private void populateClassHitDice() {
        // ... (Unchanged helper method)
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
        // ... (Unchanged)
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
        // ... (Unchanged layout)
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
                createSkillsSection() // Section 3
        );

        // Center Column
        VBox centerColumn = new VBox(20);
        centerColumn.setAlignment(Pos.TOP_CENTER);
        centerColumn.getChildren().addAll(
                createEquipmentSection(),
                createInventorySection(),
                createPhysicalAndPersonalSection() // Section 2
        );

        // Right Column
        VBox rightColumn = new VBox(20);
        rightColumn.setAlignment(Pos.TOP_CENTER);
        rightColumn.getChildren().addAll(
                createActionsSection(), // Section 1
                createFeatsAndTraitsSection() // Section 4
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
        // ... (Unchanged)
        VBox equipmentSection = createSection("Equipment");
        equipmentSection.setAlignment(Pos.CENTER);

        GridPane gearGrid = new GridPane();
        gearGrid.setHgap(5);
        gearGrid.setVgap(5);
        gearGrid.setAlignment(Pos.CENTER);
        gearGrid.setPadding(new Insets(10));

        String speciesName = character.getSelectedSpecies().replaceAll("\\s+", "");
        Image speciesSilhouette = new Image(
                new File("src/main/resources/Silhouettes/" + speciesName + ".png").toURI().toString()
        );
        ImageView silhouetteView = new ImageView(speciesSilhouette);
        silhouetteView.setFitWidth(150);
        silhouetteView.setPreserveRatio(true);

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
        // ... (Unchanged)
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
        // ... (Unchanged, calls updateActionsSection)
        for (VBox slot : equipmentSlots.values()) {
            ((Label) slot.getChildren().get(1)).setText("Empty");
            slot.setOnContextMenuRequested(null);
        }

        if (character.getEquippedItems() != null) {
            for (Map.Entry<String, Item> entry : character.getEquippedItems().entrySet()) {
                final String slotName = entry.getKey();
                VBox slot = equipmentSlots.get(slotName);
                final Item equippedItem = entry.getValue();

                if (slot != null) {
                    Label itemLabel = (Label) slot.getChildren().get(1);
                    itemLabel.setText(equippedItem.getName());

                    ContextMenu equippedContextMenu = new ContextMenu();
                    MenuItem unequipItem = new MenuItem("Unequip " + equippedItem.getName());
                    equippedContextMenu.getItems().add(unequipItem);

                    slot.setOnContextMenuRequested(event -> {
                        equippedContextMenu.show(slot, event.getScreenX(), event.getScreenY());
                        event.consume();
                    });

                    unequipItem.setOnAction(e -> {
                        character.unequipItem(slotName);
                        updateEquippedItemsDisplay();
                        if (inventoryList != null) {
                            inventoryList.getItems().setAll(character.getInventory());
                        }
                        CharacterFileManager.saveCharacter(character);
                        updateActionsSection();
                    });
                }
            }
        }
        updateActionsSection();
    }

    private VBox createInventorySection() {
        // ... (Unchanged item management logic)
        VBox inventorySection = createSection("Inventory");
        inventoryList = new ListView<>();
        inventoryList.setItems(FXCollections.observableArrayList(character.getInventory()));
        inventoryList.setPrefHeight(200);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem equipItem = new MenuItem("Equip Item");
        MenuItem editDetails = new MenuItem("Edit Details");
        MenuItem deleteItem = new MenuItem("Delete Item");

        contextMenu.getItems().addAll(equipItem, editDetails, new SeparatorMenuItem(), deleteItem);

        inventoryList.setContextMenu(contextMenu);

        equipItem.setOnAction(event -> {
            Item selectedItem = inventoryList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                Dialog<ButtonType> slotDialog = new Dialog<>();
                slotDialog.setTitle("Select Equipment Slot");
                slotDialog.setHeaderText("Choose a slot to equip " + selectedItem.getName());
                ButtonType confirmButtonType = new ButtonType("Equip", ButtonBar.ButtonData.OK_DONE);
                slotDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

                ComboBox<String> slotComboBox = new ComboBox<>();
                slotComboBox.getItems().addAll("Primary Weapon", "Off-Hand", "Two-Handed", "Secondary", "Armor", "Helm", "Gauntlets", "Boots", "Necklace", "Ring 1", "Ring 2");
                slotDialog.getDialogPane().setContent(slotComboBox);

                Optional<ButtonType> result = slotDialog.showAndWait();

                result.ifPresent(buttonType -> {
                    if (buttonType == confirmButtonType && slotComboBox.getValue() != null) {
                        String slotName = slotComboBox.getValue();
                        character.equipItem(selectedItem, slotName);
                        inventoryList.getItems().setAll(character.getInventory());
                        updateEquippedItemsDisplay();
                        CharacterFileManager.saveCharacter(character);
                    }
                });
            }
        });

        editDetails.setOnAction(event -> {
            Item selectedItem = inventoryList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
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

                editDialog.setResultConverter(dialogButton -> {
                    if (dialogButton == saveButtonType) {
                        // Item.java has setters, so this works without Character.java changes
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
                    updateActionsSection();
                });
            }
        });

        deleteItem.setOnAction(event -> {
            Item selectedItem = inventoryList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Confirm Deletion");
                confirmationAlert.setHeaderText("Permanently delete " + selectedItem.getName() + "?");
                confirmationAlert.setContentText("This action cannot be undone.");

                Optional<ButtonType> result = confirmationAlert.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    character.getInventory().remove(selectedItem);
                    inventoryList.getItems().remove(selectedItem);
                    CharacterFileManager.saveCharacter(character);
                    updateEquippedItemsDisplay();
                }
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
                Item newItem = new Item(itemName, "");
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

    // NEW: Functional Interface for setters
    @FunctionalInterface
    private interface Setter<T> {
        void set(T value);
    }

    // Section 2: Physical & Personal Details
    private VBox createPhysicalAndPersonalSection() {
        VBox section = createSection("Physical & Personal Details");
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(15);
        detailsGrid.setVgap(5);

        // Map of detail names to their getter and setter references
        Map<String, Setter<String>> detailSetters = new LinkedHashMap<>();
        detailSetters.put("Alignment", character::setAlignment);
        detailSetters.put("Age", character::setAge);
        detailSetters.put("Height", character::setHeight);
        detailSetters.put("Weight", character::setWeight);
        detailSetters.put("Hair", character::setHair);
        detailSetters.put("Eyes", character::setEyes);
        detailSetters.put("Skin", character::setSkin);
        detailSetters.put("Faith", character::setFaith);
        detailSetters.put("Lifestyle", character::setLifestyle);

        int row = 0;
        for (Map.Entry<String, Setter<String>> entry : detailSetters.entrySet()) {
            String detailName = entry.getKey();
            Setter<String> setter = entry.getValue();

            // Use switch to get the current value (requires the new setters)
            String currentValue;
            switch (detailName) {
                case "Alignment": currentValue = character.getAlignment(); break;
                case "Age": currentValue = character.getAge(); break;
                case "Height": currentValue = character.getHeight(); break;
                case "Weight": currentValue = character.getWeight(); break;
                case "Hair": currentValue = character.getHair(); break;
                case "Eyes": currentValue = character.getEyes(); break;
                case "Skin": currentValue = character.getSkin(); break;
                case "Faith": currentValue = character.getFaith(); break;
                case "Lifestyle": currentValue = character.getLifestyle(); break;
                default: currentValue = ""; break;
            }

            Label valueLabel = createDetailValue(currentValue);
            detailValueLabels.put(detailName, valueLabel);

            detailsGrid.add(createDetailLabel(detailName), 0, row);
            detailsGrid.add(valueLabel, 1, row);

            Button editButton = new Button("Edit");
            editButton.setStyle("-fx-padding: 2 5; -fx-font-size: 10px; -fx-cursor: hand; -fx-background-color: #007BFF; -fx-text-fill: white; -fx-border-radius: 3;");
            editButton.setOnAction(e -> editPersonalDetail(detailName, valueLabel, valueLabel.getText(), setter));
            detailsGrid.add(editButton, 2, row);

            row++;
        }

        section.getChildren().add(detailsGrid);
        return section;
    }

    private void editPersonalDetail(String detailName, Label valueLabel, String currentValue, Setter<String> setter) {
        TextInputDialog dialog = new TextInputDialog(currentValue);
        dialog.setTitle("Edit " + detailName);
        dialog.setHeaderText("Update the value for " + detailName + ":");
        dialog.setContentText(detailName + ":");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newValue -> {
            setter.set(newValue);
            valueLabel.setText(newValue);
            CharacterFileManager.saveCharacter(character);
        });
    }

    private Label createDetailLabel(String text) {
        // ... (Unchanged)
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setTextFill(Color.web("#adb5bd"));
        return label;
    }

    private Label createDetailValue(String text) {
        // ... (Unchanged)
        Label label = new Label(text);
        label.setFont(Font.font("Arial", 14));
        label.setTextFill(Color.web("#e9ecef"));
        return label;
    }

    // NEW: Method to update all vitals labels
    private void updateVitalsDisplay() {
        int level = character.getLevel();
        int hp = character.getHitPoints();
        int ac = character.getArmorClass();
        int speed = character.getSpeed();
        String hitDice = character.getHitDice();

        if (levelValueLabel != null) levelValueLabel.setText(String.valueOf(level));
        if (expValueLabel != null) expValueLabel.setText(String.valueOf(character.getExperience()));
        if (hpValueLabel != null) hpValueLabel.setText(String.valueOf(hp));
        if (acValueLabel != null) acValueLabel.setText(String.valueOf(ac));
        if (speedValueLabel != null) speedValueLabel.setText(String.valueOf(speed));
        if (hitDiceValueLabel != null) hitDiceValueLabel.setText(hitDice);
    }

    // Section 5: Vitals (XP is editable)
    private VBox createVitalsSection() {
        VBox vitals = createSection("Vitals");
        GridPane vitalsGrid = new GridPane();
        vitalsGrid.setHgap(10);
        vitalsGrid.setVgap(5);

        // Level
        levelValueLabel = createVitalsStatValue(String.valueOf(character.getLevel()));
        vitalsGrid.add(createVitalsStatLabel("Level"), 0, 0);
        vitalsGrid.add(levelValueLabel, 1, 0);

        // XP (Editable - requires Character.setExperience)
        expValueLabel = createVitalsStatValue(String.valueOf(character.getExperience()));
        vitalsGrid.add(createVitalsStatLabel("XP"), 0, 1);
        vitalsGrid.add(expValueLabel, 1, 1);
        Button editExpButton = new Button("Edit");
        editExpButton.setStyle("-fx-padding: 2 5; -fx-font-size: 10px; -fx-cursor: hand; -fx-background-color: #007BFF; -fx-text-fill: white; -fx-border-radius: 3;");
        editExpButton.setOnAction(e -> editExperience());
        vitalsGrid.add(editExpButton, 2, 1);

        // HP
        hpValueLabel = createVitalsStatValue(String.valueOf(character.getHitPoints()));
        vitalsGrid.add(createVitalsStatLabel("HP"), 0, 2);
        vitalsGrid.add(hpValueLabel, 1, 2);

        // AC
        acValueLabel = createVitalsStatValue(String.valueOf(character.getArmorClass()));
        vitalsGrid.add(createVitalsStatLabel("AC"), 0, 3);
        vitalsGrid.add(acValueLabel, 1, 3);

        // Speed
        speedValueLabel = createVitalsStatValue(String.valueOf(character.getSpeed()));
        vitalsGrid.add(createVitalsStatLabel("Speed"), 0, 4);
        vitalsGrid.add(speedValueLabel, 1, 4);

        // Hit Dice
        hitDiceValueLabel = createVitalsStatValue(character.getHitDice());
        vitalsGrid.add(createVitalsStatLabel("Hit Dice"), 0, 5);
        vitalsGrid.add(hitDiceValueLabel, 1, 5);

        vitals.getChildren().add(vitalsGrid);
        return vitals;
    }

    private void editExperience() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(character.getExperience()));
        dialog.setTitle("Edit Experience");
        dialog.setHeaderText("Enter the new total Experience Points (XP):");
        dialog.setContentText("XP:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(xpString -> {
            try {
                int newXP = Integer.parseInt(xpString);
                int oldLevel = character.getLevel();
                character.setExperience(Math.max(0, newXP));
                CharacterFileManager.saveCharacter(character);
                updateVitalsDisplay();

                if (character.getLevel() != oldLevel) {
                    Alert levelUpAlert = new Alert(Alert.AlertType.INFORMATION);
                    levelUpAlert.setTitle("Level Up!");
                    levelUpAlert.setHeaderText(character.getName() + " is now Level " + character.getLevel() + "!");
                    levelUpAlert.setContentText("Check your vitals for updated HP and Hit Dice.");
                    levelUpAlert.showAndWait();
                    updateAbilityScoresSection();
                }
            } catch (NumberFormatException e) {
                Alert error = new Alert(Alert.AlertType.ERROR, "Invalid number entered for XP.");
                error.showAndWait();
            }
        });
    }

    private Label createVitalsStatLabel(String text) {
        // ... (Unchanged)
        Label label = new Label(text + ":");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setTextFill(Color.web("#adb5bd"));
        return label;
    }

    private Label createVitalsStatValue(String text) {
        // ... (Unchanged)
        Label label = new Label(text);
        label.setFont(Font.font("Arial", 14));
        label.setTextFill(Color.web("#e9ecef"));
        return label;
    }

    // Section 4: Ability Scores
    private void updateAbilityScoresSection() {
        // Find the parent grid and replace the ability scores section
        for (Map.Entry<String, Label> entry : abilityScoreValueLabels.entrySet()) {
            String ability = entry.getKey();
            Label scoreLabel = entry.getValue();
            Label modLabel = (Label) ((HBox)scoreLabel.getParent()).getChildren().get(2);

            int score = character.getAbilityScores().getOrDefault(ability, 10);
            int modifier = (score - 10) / 2;

            scoreLabel.setText(String.valueOf(score));
            modLabel.setText(" (" + (modifier >= 0 ? "+" : "") + modifier + ")");
        }
        updateSavingThrowsSection();
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

            Label scoreLabel = createAbilityScoreValue(String.valueOf(score));
            Label modifierLabel = createAbilityModifierValue(" (" + (modifier >= 0 ? "+" : "") + modifier + ")");

            abilityScoreValueLabels.put(ability, scoreLabel);

            HBox scoreBox = new HBox(5, scoreLabel, modifierLabel);
            scoreBox.setAlignment(Pos.CENTER_LEFT);

            abilityGrid.add(createAbilityScoreLabel(ability), 0, row);
            abilityGrid.add(scoreBox, 1, row);

            // Add edit button for ability score
            Button editButton = new Button("Edit");
            editButton.setStyle("-fx-padding: 2 5; -fx-font-size: 10px; -fx-cursor: hand; -fx-background-color: #007BFF; -fx-text-fill: white; -fx-border-radius: 3;");
            editButton.setOnAction(e -> editAbilityScore(ability, scoreLabel, modifierLabel));
            abilityGrid.add(editButton, 2, row);

            row++;
        }
        section.getChildren().add(abilityGrid);
        return section;
    }

    private void editAbilityScore(String ability, Label scoreLabel, Label modLabel) {
        TextInputDialog dialog = new TextInputDialog(scoreLabel.getText());
        dialog.setTitle("Edit " + ability + " Score");
        dialog.setHeaderText("Enter the new score for " + ability + ":");
        dialog.setContentText(ability + " Score (1-30):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(scoreString -> {
            try {
                int newScore = Integer.parseInt(scoreString);
                if (newScore >= 1 && newScore <= 30) {
                    // Update score using the new setter
                    character.setAbilityScore(ability, newScore);
                    CharacterFileManager.saveCharacter(character);
                    scoreLabel.setText(String.valueOf(newScore));
                    int newModifier = (newScore - 10) / 2;
                    modLabel.setText(" (" + (newModifier >= 0 ? "+" : "") + newModifier + ")");
                    updateVitalsDisplay();
                    updateActionsSection();
                    updateSavingThrowsSection();
                } else {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                Alert error = new Alert(Alert.AlertType.ERROR, "Invalid score. Please enter a number between 1 and 30.");
                error.showAndWait();
            }
        });
    }

    private Label createAbilityScoreLabel(String text) {
        // ... (Unchanged)
        Label label = new Label(text + ":");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setTextFill(Color.web("#adb5bd"));
        return label;
    }

    private Label createAbilityScoreValue(String text) {
        // ... (Unchanged)
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setTextFill(Color.web("#e9ecef"));
        return label;
    }

    private Label createAbilityModifierValue(String text) {
        // ... (Unchanged)
        Label label = new Label(text);
        label.setFont(Font.font("Arial", 12));
        label.setTextFill(Color.web("#e9ecef"));
        return label;
    }

    private VBox createSavingThrowsSection() {
        VBox section = createSection("Saving Throws");
        this.savingThrowsGrid = new GridPane(); // Store the grid to allow updates
        updateSavingThrowsSection();
        section.getChildren().add(this.savingThrowsGrid);
        return section;
    }

    // New/Updated: Method to update saving throws display based on new ability scores
    private GridPane savingThrowsGrid; // Declare a field to store the grid

    private void updateSavingThrowsSection() {
        if (this.savingThrowsGrid == null) return;
        this.savingThrowsGrid.getChildren().clear();

        int row = 0;
        for (String ability : character.getAbilityScores().keySet()) {
            int modifier = (character.getAbilityScores().get(ability) - 10) / 2;
            boolean isProficient = false; // Placeholder for proficiency check
            String displayModifier = (modifier >= 0 ? "+" : "") + modifier;
            String text = (isProficient ? "• " : "  ") + ability + ": " + displayModifier;

            Label label = new Label(text);
            label.setFont(Font.font("Arial", 14));
            label.setTextFill(Color.web("#e9ecef"));
            this.savingThrowsGrid.add(label, 0, row);
            row++;
        }
    }


    // Section 3: Skills (Editable - No Persistence)
    private VBox createSkillsSection() {
        VBox section = createSection("Skills");
        this.skillsSectionVBox = new VBox(5);

        // Using a ListView backed by the placeholder list (unsaved, per user constraint)
        ListView<String> skillList = new ListView<>();
        skillList.setItems(FXCollections.observableArrayList(placeholderSkills));
        skillList.setPrefHeight(150);
        skillList.setEditable(true);

        // Control Box for adding new skills
        HBox controlBox = new HBox(10);
        TextField newSkillField = new TextField();
        newSkillField.setPromptText("e.g. History (INT): +3");
        newSkillField.setStyle("-fx-background-color: #495057; -fx-text-fill: #e9ecef; -fx-prompt-text-fill: #adb5bd; -fx-border-color: #6c757d; -fx-border-width: 1; -fx-border-radius: 5;");
        Button addSkillButton = new Button("Add Skill");
        addSkillButton.setStyle("-fx-padding: 5 10; -fx-font-size: 12px; -fx-cursor: hand; -fx-background-color: #28a745; -fx-text-fill: white;");
        controlBox.getChildren().addAll(newSkillField, addSkillButton);

        addSkillButton.setOnAction(e -> {
            String skill = newSkillField.getText().trim();
            if (!skill.isEmpty() && !skillList.getItems().contains(skill)) {
                // Update the static placeholderSkills list
                placeholderSkills.add(skill);
                skillList.getItems().add(skill);
                newSkillField.clear();
            }
        });

        // Context Menu to delete a skill
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Remove Skill");
        contextMenu.getItems().add(deleteItem);
        skillList.setContextMenu(contextMenu);
        deleteItem.setOnAction(e -> {
            String selected = skillList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Update the static placeholderSkills list
                placeholderSkills.remove(selected);
                skillList.getItems().remove(selected);
            }
        });

        this.skillsSectionVBox.getChildren().addAll(skillList, controlBox);
        section.getChildren().add(this.skillsSectionVBox);
        return section;
    }

    // Section 1: Actions & Attack Rolls (Editable with Persistence via List<String>)
    private VBox createActionsSection() {
        VBox actionsSection = createSection("Actions & Attack Rolls");
        this.actionsSectionVBox = actionsSection;
        updateActionsSection();
        return actionsSection;
    }

    public void updateActionsSection() {
        if (this.actionsSectionVBox == null) {
            return;
        }

        if (this.actionsSectionVBox.getChildren().size() > 1) {
            this.actionsSectionVBox.getChildren().remove(1, this.actionsSectionVBox.getChildren().size());
        }

        VBox actionContent = new VBox(10);
        actionContent.setPadding(new Insets(5, 0, 0, 0));

        // --- Predefined Attack Rolls Logic (Melee/Ranged) ---
        int profBonus = (character.getLevel() - 1) / 4 + 2;
        int strScore = character.getAbilityScores().getOrDefault("STR", 10);
        int dexScore = character.getAbilityScores().getOrDefault("DEX", 10);
        int strMod = (strScore - 10) / 2;
        int dexMod = (dexScore - 10) / 2;
        int meleeAttackBonus = strMod + profBonus;
        int rangedAttackBonus = dexMod + profBonus;

        actionContent.getChildren().add(
                createAttackRollLabel("Melee Attack (STR)", meleeAttackBonus)
        );
        actionContent.getChildren().add(
                createAttackRollLabel("Ranged Attack (DEX)", rangedAttackBonus)
        );

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #6c757d;");
        actionContent.getChildren().add(separator);

        // --- Custom Actions List View ---
        Label customTitle = new Label("Custom Actions (Name | Details)");
        customTitle.setStyle("-fx-text-fill: #adb5bd; -fx-font-weight: bold; -fx-font-size: 14px;");
        actionContent.getChildren().add(customTitle);

        ListView<String> actionListView = new ListView<>();
        actionListView.setItems(FXCollections.observableArrayList(character.getCustomActions()));
        actionListView.setPrefHeight(120);
        actionListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Splits the single string "Name | Description" for display
                    String[] parts = item.split(" \\| ", 2);
                    setText(parts[0]);
                    if (parts.length > 1) {
                        Tooltip tooltip = new Tooltip(parts[1]);
                        setTooltip(tooltip);
                    }
                }
            }
        });
        actionContent.getChildren().add(actionListView);

        // Context Menu for editing/deleting custom actions
        ContextMenu actionContextMenu = new ContextMenu();
        MenuItem editAction = new MenuItem("Edit Action");
        MenuItem deleteAction = new MenuItem("Delete Action");
        actionContextMenu.getItems().addAll(editAction, deleteAction);
        actionListView.setContextMenu(actionContextMenu);

        // Edit Action Logic
        editAction.setOnAction(e -> {
            String selectedAction = actionListView.getSelectionModel().getSelectedItem();
            int selectedIndex = actionListView.getSelectionModel().getSelectedIndex();
            if (selectedAction != null && selectedIndex != -1) {
                editActionDialog(actionListView, selectedAction, selectedIndex);
            }
        });

        // Delete Action Logic
        deleteAction.setOnAction(e -> {
            String selectedAction = actionListView.getSelectionModel().getSelectedItem();
            if (selectedAction != null) {
                character.getCustomActions().remove(selectedAction);
                actionListView.getItems().remove(selectedAction);
                CharacterFileManager.saveCharacter(character);
            }
        });

        // Add Action Button
        Button addActionButton = new Button("Add New Action");
        addActionButton.setStyle("-fx-padding: 5 10; -fx-font-size: 12px; -fx-cursor: hand; -fx-background-color: #28a745; -fx-text-fill: white;");
        addActionButton.setOnAction(e -> addActionDialog(actionListView));
        actionContent.getChildren().add(addActionButton);

        this.actionsSectionVBox.getChildren().add(actionContent);
    }

    private Label createAttackRollLabel(String name, int bonus) {
        Label label = new Label(name + ": " + (bonus >= 0 ? "+" : "") + bonus);
        label.setStyle("-fx-text-fill: #e9ecef; -fx-font-weight: bold; -fx-font-size: 14px;");
        return label;
    }

    private void addActionDialog(ListView<String> actionListView) {
        Dialog<String> dialog = createActionEditDialog(null);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newActionString -> {
            character.getCustomActions().add(newActionString);
            actionListView.getItems().add(newActionString);
            CharacterFileManager.saveCharacter(character);
        });
    }

    private void editActionDialog(ListView<String> actionListView, String currentAction, int index) {
        Dialog<String> dialog = createActionEditDialog(currentAction);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(updatedActionString -> {
            character.getCustomActions().set(index, updatedActionString);
            actionListView.getItems().set(index, updatedActionString);
            actionListView.refresh();
            CharacterFileManager.saveCharacter(character);
        });
    }

    private Dialog<String> createActionEditDialog(String currentAction) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(currentAction == null ? "Add New Action" : "Edit Action");
        dialog.setHeaderText(currentAction == null ? "Create a new attack roll or custom action." : "Editing Action Details");
        ButtonType buttonType = new ButtonType(currentAction == null ? "Add" : "Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Initialize fields with current data if editing
        String[] parts = currentAction != null ? currentAction.split(" \\| ", 2) : new String[]{"", ""};
        TextField nameField = new TextField(parts[0]);
        TextArea descriptionArea = new TextArea(parts.length > 1 ? parts[1] : "");
        descriptionArea.setWrapText(true);
        descriptionArea.setPromptText("Damage, Range, Save DC, etc.");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Details:"), 0, 1);
        grid.add(descriptionArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonType && !nameField.getText().trim().isEmpty()) {
                // Combine the two fields into a single String for the List<String> field
                return nameField.getText().trim() + " | " + descriptionArea.getText();
            }
            return null;
        });
        return dialog;
    }


    // Section 4: Features & Traits (Editable with Persistence via List<String>)
    private VBox createFeatsAndTraitsSection() {
        VBox feats = createSection("Features & Traits");
        this.featuresSectionVBox = new VBox(10);

        // ListView to display the features
        ListView<String> traitListView = new ListView<>();
        traitListView.setItems(FXCollections.observableArrayList(character.getFeaturesAndTraits()));
        traitListView.setPrefHeight(200);

        // Custom Cell Factory to split the single string "Name | Description" for display
        traitListView.setCellFactory(lv -> new ListCell<>() {
            private final VBox container = new VBox(3);
            private final Label nameLabel = new Label();
            private final Label descriptionLabel = new Label();

            {
                nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                nameLabel.setTextFill(Color.web("#e9ecef"));
                descriptionLabel.setFont(Font.font("Arial", 12));
                descriptionLabel.setTextFill(Color.web("#adb5bd"));
                descriptionLabel.setWrapText(true);
                container.getChildren().addAll(nameLabel, descriptionLabel);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String[] parts = item.split(" \\| ", 2);
                    nameLabel.setText(parts[0]);
                    descriptionLabel.setText(parts.length > 1 ? parts[1] : "No Description");
                    setGraphic(container);
                    setText(null);
                }
            }
        });

        // Context Menu for editing/deleting features
        ContextMenu traitContextMenu = new ContextMenu();
        MenuItem editTrait = new MenuItem("Edit Feature/Trait");
        MenuItem deleteTrait = new MenuItem("Delete Feature/Trait");
        traitContextMenu.getItems().addAll(editTrait, deleteTrait);
        traitListView.setContextMenu(traitContextMenu);

        // Edit Trait Logic
        editTrait.setOnAction(e -> {
            String selectedTrait = traitListView.getSelectionModel().getSelectedItem();
            int selectedIndex = traitListView.getSelectionModel().getSelectedIndex();
            if (selectedTrait != null && selectedIndex != -1) {
                editFeatureTraitDialog(traitListView, selectedTrait, selectedIndex);
            }
        });

        // Delete Trait Logic
        deleteTrait.setOnAction(e -> {
            String selectedTrait = traitListView.getSelectionModel().getSelectedItem();
            if (selectedTrait != null) {
                character.getFeaturesAndTraits().remove(selectedTrait);
                traitListView.getItems().remove(selectedTrait);
                CharacterFileManager.saveCharacter(character);
            }
        });

        // Add Feature/Trait Button
        Button addTraitButton = new Button("Add New Feature/Trait");
        addTraitButton.setStyle("-fx-padding: 5 10; -fx-font-size: 12px; -fx-cursor: hand; -fx-background-color: #28a745; -fx-text-fill: white;");
        addTraitButton.setOnAction(e -> addFeatureTraitDialog(traitListView));

        this.featuresSectionVBox.getChildren().addAll(traitListView, addTraitButton);
        feats.getChildren().add(this.featuresSectionVBox);
        return feats;
    }

    private void addFeatureTraitDialog(ListView<String> traitListView) {
        Dialog<String> dialog = createFeatureTraitEditDialog(null);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newTraitString -> {
            character.getFeaturesAndTraits().add(newTraitString);
            traitListView.getItems().add(newTraitString);
            CharacterFileManager.saveCharacter(character);
        });
    }

    private void editFeatureTraitDialog(ListView<String> traitListView, String currentTrait, int index) {
        Dialog<String> dialog = createFeatureTraitEditDialog(currentTrait);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(updatedTraitString -> {
            character.getFeaturesAndTraits().set(index, updatedTraitString);
            traitListView.getItems().set(index, updatedTraitString);
            traitListView.refresh();
            CharacterFileManager.saveCharacter(character);
        });
    }

    private Dialog<String> createFeatureTraitEditDialog(String currentTrait) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(currentTrait == null ? "Add New Feature/Trait" : "Edit Feature/Trait");
        dialog.setHeaderText(currentTrait == null ? "Create a new character feature or trait." : "Editing Feature/Trait Details");
        ButtonType buttonType = new ButtonType(currentTrait == null ? "Add" : "Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Initialize fields with current data if editing
        String[] parts = currentTrait != null ? currentTrait.split(" \\| ", 2) : new String[]{"", ""};
        TextField nameField = new TextField(parts[0]);
        TextArea descriptionArea = new TextArea(parts.length > 1 ? parts[1] : "");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefHeight(100);
        descriptionArea.setPromptText("Detailed description of the feature or trait.");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonType && !nameField.getText().trim().isEmpty()) {
                // Combine the two fields into a single String for the List<String> field
                return nameField.getText().trim() + " | " + descriptionArea.getText();
            }
            return null;
        });
        return dialog;
    }
    // ... (Spell methods are unchanged for brevity)
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
}