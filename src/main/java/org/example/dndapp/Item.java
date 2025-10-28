package org.example.dndapp;

import java.io.Serializable;

/**
 * Represents an item in the game, such as equipment or inventory items.
 * It is Serializable so it can be saved and loaded with the Character object.
 */
public class Item implements Serializable {
    // Basic properties of an item
    private String name;
    private String description;

    /**
     * Constructor for the Item class.
     * @param name The name of the item.
     * @param description A brief description of the item.
     */
    public Item(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // Setters

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Overrides the toString method to display just the item name,
     * which is useful for displaying in lists and combos (like the ComboBox in the sheet).
     * @return The name of the item.
     */
    @Override
    public String toString() {
        return name;
    }
}
