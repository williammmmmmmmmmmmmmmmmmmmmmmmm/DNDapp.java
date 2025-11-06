package org.example.dndapp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Character implements Serializable {
    private String name;
    private String player;
    private String selectedClass;
    private String selectedSubclass;
    private String selectedSpecies;
    private String background;
    private int experience;
    private Map<String, Integer> abilityScores;
    private String alignment;
    private String age;
    private String height;
    private String weight;
    private String hair;
    private String eyes;
    private String skin;
    private String faith;
    private String lifestyle;

    // Initialize fields at declaration.
    private Map<Integer, List<String>> knownSpells = new HashMap<>();
    private Map<String, Item> equippedItems = new LinkedHashMap<>();
    private List<Item> inventory = new ArrayList<>();

    // NEW FIELDS for editable sections (using simple List<String> to avoid new classes)
    private List<String> customActions = new ArrayList<>();
    private List<String> featuresAndTraits = new ArrayList<>();

    // NEW FIELDS for Vitals editability (Nullable Integers for overrides)
    private Integer currentHitPoints;
    private Integer armorClassOverride;
    private Integer speedOverride;
    private String hitDiceOverride;

    public Character(String name, String player, String selectedClass, String selectedSubclass, String selectedSpecies,
                     String background, int experience, Map<String, Integer> abilityScores, String alignment, String age,
                     String height, String weight, String hair, String eyes, String skin, String faith, String lifestyle) {
        this.name = name;
        this.player = player;
        this.selectedClass = selectedClass;
        this.selectedSubclass = selectedSubclass;
        this.selectedSpecies = selectedSpecies;
        this.background = background;
        this.experience = experience;
        this.abilityScores = abilityScores;
        this.alignment = alignment;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.hair = hair;
        this.eyes = eyes;
        this.skin = skin;
        this.faith = faith;
        this.lifestyle = lifestyle;
    }

    /**
     * Special method used during deserialization to ensure all fields are initialized.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Null checks for existing fields
        if (this.knownSpells == null) {
            this.knownSpells = new HashMap<>();
        }
        if (this.equippedItems == null) {
            this.equippedItems = new LinkedHashMap<>();
        }
        if (this.inventory == null) {
            this.inventory = new ArrayList<>();
        }
        // Null checks for new list fields
        if (this.customActions == null) {
            this.customActions = new ArrayList<>();
        }
        if (this.featuresAndTraits == null) {
            this.featuresAndTraits = new ArrayList<>();
        }
        // currentHitPoints will be initialized on first call to getCurrentHitPoints() if null
    }

    // Methods to calculate Vitals (Base Calculations)
    public int getLevel() {
        int[] expTable = {0, 300, 900, 2700, 6500, 14000, 23000, 34000, 48000, 64000, 85000, 100000, 120000, 140000, 165000, 195000, 225000, 265000, 305000, 355000};
        for (int i = expTable.length - 1; i >= 0; i--) {
            if (experience >= expTable[i]) {
                return i + 1;
            }
        }
        return 1;
    }

    private int getAbilityModifier(String ability) {
        if (abilityScores.containsKey(ability)) {
            return (abilityScores.get(ability) - 10) / 2;
        }
        return 0;
    }

    // RENAMED: This is now the calculated MAX HP
    public int getMaxHitPoints() {
        Map<String, Integer> classHitDice = new HashMap<>();
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

        int level = getLevel();
        int constitutionModifier = getAbilityModifier("CON");
        int hitDie = classHitDice.getOrDefault(selectedClass, 6);

        int totalHP = hitDie + constitutionModifier;
        if (level > 1) {
            // Average HP for levels 2+
            int averageHP = (int) Math.floor(hitDie / 2.0) + 1;
            totalHP += (averageHP + constitutionModifier) * (level - 1);
        }
        return Math.max(1, totalHP);
    }

    // MODIFIED: Uses override if present
    public int getArmorClass() {
        // Return override if set, otherwise calculate base AC
        if (armorClassOverride != null) {
            return armorClassOverride;
        }
        return 10 + getAbilityModifier("DEX");
    }

    // MODIFIED: Uses override if present
    public int getSpeed() {
        // Return override if set, otherwise calculate base speed
        if (speedOverride != null) {
            return speedOverride;
        }
        Map<String, Integer> speciesBaseSpeed = new HashMap<>();
        speciesBaseSpeed.put("Dragonborn", 30);
        speciesBaseSpeed.put("Dwarf", 25);
        speciesBaseSpeed.put("Elf", 30);
        speciesBaseSpeed.put("Gnome", 25);
        speciesBaseSpeed.put("Half-Elf", 30);
        speciesBaseSpeed.put("Half-Orc", 30);
        speciesBaseSpeed.put("Halfling", 25);
        speciesBaseSpeed.put("Human", 30);
        speciesBaseSpeed.put("Tiefling", 30);
        return speciesBaseSpeed.getOrDefault(selectedSpecies, 30);
    }

    // MODIFIED: Uses override if present
    public String getHitDice() {
        // Return override if set, otherwise calculate base hit dice
        if (hitDiceOverride != null && !hitDiceOverride.isEmpty()) {
            return hitDiceOverride;
        }
        Map<String, String> classHitDice = new HashMap<>();
        classHitDice.put("Barbarian", "1d12");
        classHitDice.put("Bard", "1d8");
        classHitDice.put("Cleric", "1d8");
        classHitDice.put("Druid", "1d8");
        classHitDice.put("Fighter", "1d10");
        classHitDice.put("Monk", "1d8");
        classHitDice.put("Paladin", "1d10");
        classHitDice.put("Ranger", "1d10");
        classHitDice.put("Rogue", "1d8");
        classHitDice.put("Sorcerer", "1d6");
        classHitDice.put("Warlock", "1d8");
        classHitDice.put("Wizard", "1d6");
        return getLevel() + classHitDice.getOrDefault(selectedClass, "1d6");
    }

    // Methods for equipping/unequipping items (used in CharacterSheetPage)
    public void equipItem(Item item, String slotName) {
        if (equippedItems.containsKey(slotName)) {
            // If the slot is not empty, unequip the old item first
            Item oldItem = equippedItems.remove(slotName);
            inventory.add(oldItem);
        }
        // Equip the new item and remove it from inventory
        equippedItems.put(slotName, item);
        inventory.remove(item);
    }

    public void unequipItem(String slotName) {
        if (equippedItems.containsKey(slotName)) {
            Item unequippedItem = equippedItems.remove(slotName);
            if (unequippedItem != null) {
                inventory.add(unequippedItem);
            }
        }
    }

    // Getters and SETTERS for editable fields

    // General Getters (Unchanged)
    public String getName() { return name; }
    public String getPlayer() { return player; }
    public String getSelectedClass() { return selectedClass; }
    public String getSelectedSubclass() { return selectedSubclass; }
    public String getSelectedSpecies() { return selectedSpecies; }
    public String getBackground() { return background; }
    public Map<Integer, List<String>> getKnownSpells() { return knownSpells; }
    public Map<String, Item> getEquippedItems() { return equippedItems; }
    public List<Item> getInventory() { return inventory; }

    // Vitals: Experience Getter & Setter
    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    // NEW: Current HP Getter & Setter
    public int getCurrentHitPoints() {
        if (currentHitPoints == null) {
            currentHitPoints = getMaxHitPoints(); // Initialize current HP to max HP
        }
        return currentHitPoints;
    }
    public void setCurrentHitPoints(int currentHitPoints) {
        this.currentHitPoints = currentHitPoints;
    }

    // NEW: Armor Class Override Setter
    public void setArmorClassOverride(Integer ac) {
        this.armorClassOverride = ac;
    }

    // NEW: Speed Override Setter
    public void setSpeedOverride(Integer speed) {
        this.speedOverride = speed;
    }

    // NEW: Hit Dice Override Setter
    public void setHitDiceOverride(String hitDice) {
        this.hitDiceOverride = hitDice;
    }

    // Ability Scores: Getter and Updater
    public Map<String, Integer> getAbilityScores() { return abilityScores; }
    public void setAbilityScore(String ability, int score) { this.abilityScores.put(ability, score); }

    // Physical & Personal Details: Getters & Setters
    public String getAlignment() { return alignment; }
    public void setAlignment(String alignment) { this.alignment = alignment; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getHeight() { return height; }
    public void setHeight(String height) { this.height = height; }

    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }

    public String getHair() { return hair; }
    public void setHair(String hair) { this.hair = hair; }

    public String getEyes() { return eyes; }
    public void setEyes(String eyes) { this.eyes = eyes; }

    public String getSkin() { return skin; }
    public void setSkin(String skin) { this.skin = skin; }

    public String getFaith() { return faith; }
    public void setFaith(String faith) { this.faith = faith; }

    public String getLifestyle() { return lifestyle; }
    public void setLifestyle(String lifestyle) { this.lifestyle = lifestyle; }

    // Actions & Features/Traits: Generic List Getters
    public List<String> getCustomActions() { return customActions; }
    public List<String> getFeaturesAndTraits() { return featuresAndTraits; }
}