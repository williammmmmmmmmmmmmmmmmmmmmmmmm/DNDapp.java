package org.example.dndapp;

import java.util.ArrayList;
import java.util.List;

public class Monster {
    private final String name;
    private final String type;
    private final String size;
    private final String alignment;
    private final String cr;
    private final String source;
    private final String page;
    private final List<String> traits = new ArrayList<>();
    private final List<String> environments = new ArrayList<>();

    public Monster(String[] data) {
        // Basic Info mapping based on your CSV structure
        this.name = data[0].replace("\"", "").trim();
        this.size = (data.length > 1 && !data[1].isEmpty()) ? data[1] : "Unknown";
        this.type = (data.length > 2 && !data[2].isEmpty()) ? data[2] : "Unknown";
        this.alignment = (data.length > 4 && !data[4].isEmpty()) ? data[4] : "Unknown";
        this.cr = (data.length > 5 && !data[5].isEmpty()) ? data[5] : "-";
        this.source = (data.length > 25) ? data[25] : "Unknown";
        this.page = (data.length > 26) ? data[26] : "?";

        // Traits (Col 7, 8, 9, 10, 11, 12)
        if (isMarked(data, 7)) traits.add("Spellcaster");
        if (isMarked(data, 8)) traits.add("Legendary");
        if (isMarked(data, 9)) traits.add("Lair Actions");
        if (isMarked(data, 10)) traits.add("Unique");
        if (isMarked(data, 11)) traits.add("Familiar");
        if (isMarked(data, 12)) traits.add("Template");

        // Environments (Col 13 - 24)
        String[] envNames = {
                "Arctic", "Coastal", "Desert", "Forest", "Grassland",
                "Hill", "Mountain", "Swamp", "Underdark", "Underwater", "Urban", "Other Plane"
        };
        for (int i = 0; i < envNames.length; i++) {
            if (isMarked(data, 13 + i)) {
                environments.add(envNames[i]);
            }
        }
    }

    private boolean isMarked(String[] data, int index) {
        if (index >= data.length) return false;
        String val = data[index].toLowerCase().trim();
        return val.equals("x") || val.equals("ø") || val.equals("true");
    }

    // Getters needed by BestiaryPage
    public String getName() { return name; }
    public String getCr() { return cr; }
    public String getType() { return type; }
    public boolean isLegendary() { return traits.contains("Legendary"); }

    public String getFormattedStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("Size/Type: ").append(size).append(" ").append(type).append("\n");
        sb.append("Alignment: ").append(alignment).append("\n");
        sb.append("Challenge: ").append(cr).append("\n\n");

        if (!traits.isEmpty()) {
            sb.append("Traits: ").append(String.join(", ", traits)).append("\n");
        }

        if (!environments.isEmpty()) {
            sb.append("Environments: ").append(String.join(", ", environments)).append("\n");
        }

        sb.append("----------------------------------\n");
        sb.append("Source: ").append(source).append(" (pg. ").append(page).append(")");

        return sb.toString();
    }
}