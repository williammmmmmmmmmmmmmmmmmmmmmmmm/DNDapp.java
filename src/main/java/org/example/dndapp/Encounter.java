package org.example.dndapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Encounter implements Serializable {
    private String name;
    private List<Monster> monsters;

    public Encounter(String name) {
        this.name = name;
        this.monsters = new ArrayList<>();
    }

    public void addMonster(Monster m) { monsters.add(m); }
    public String getName() { return name; }
    public List<Monster> getMonsters() { return monsters; }

    public String getSummary() {
        return monsters.size() + " monsters (Total CR: " + calculateTotalCR() + ")";
    }

    private String calculateTotalCR() {
        // Simple CR summation logic
        double total = 0;
        for (Monster m : monsters) {
            try {
                String crStr = m.getCr().split("/")[0]; // Handles 1/2, 1/4
                if (m.getCr().contains("/")) {
                    total += 1.0 / Double.parseDouble(m.getCr().split("/")[1]);
                } else {
                    total += Double.parseDouble(crStr);
                }
            } catch (Exception e) { /* Ignore non-numeric CR */ }
        }
        return String.valueOf(total);
    }
    public void setName(String name) {
        this.name = name;
    }
}