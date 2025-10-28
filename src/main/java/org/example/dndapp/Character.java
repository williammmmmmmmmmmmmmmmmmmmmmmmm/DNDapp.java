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
    private Map<Integer, List<String>> knownSpells;
    private Map<String, Item> equippedItems;
    private List<Item> inventory;

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
        this.knownSpells = new HashMap<>();
        this.equippedItems = new LinkedHashMap<>();
        this.inventory = new ArrayList<>();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.knownSpells == null) {
            this.knownSpells = new HashMap<>();
        }
        if (this.equippedItems == null) {
            this.equippedItems = new LinkedHashMap<>();
        }
        if (this.inventory == null) {
            this.inventory = new ArrayList<>();
        }
    }

    // New methods to calculate vitals
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

    public int getHitPoints() {
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

    public int getArmorClass() {
        // Simple base AC + Dexterity modifier
        // This can be expanded later with armor and shield calculations
        return 10 + getAbilityModifier("DEX");
    }

    public int getSpeed() {
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

    public String getHitDice() {
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

    // New method to equip an item
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

    // Getters
    public String getName() {
        return name;
    }

    public String getPlayer() {
        return player;
    }

    public String getSelectedClass() {
        return selectedClass;
    }

    public String getSelectedSubclass() {
        return selectedSubclass;
    }

    public String getSelectedSpecies() {
        return selectedSpecies;
    }

    public String getBackground() {
        return background;
    }

    public int getExperience() {
        return experience;
    }

    public Map<String, Integer> getAbilityScores() {
        return abilityScores;
    }

    public String getAlignment() {
        return alignment;
    }

    public String getAge() {
        return age;
    }

    public String getHeight() {
        return height;
    }

    public String getWeight() {
        return weight;
    }

    public String getHair() {
        return hair;
    }

    public String getEyes() {
        return eyes;
    }

    public String getSkin() {
        return skin;
    }

    public String getFaith() {
        return faith;
    }

    public String getLifestyle() {
        return lifestyle;
    }

    public Map<Integer, List<String>> getKnownSpells() {
        return knownSpells;
    }

    public Map<String, Item> getEquippedItems() {
        return equippedItems;
    }

    public List<Item> getInventory() {
        return inventory;
    }
}