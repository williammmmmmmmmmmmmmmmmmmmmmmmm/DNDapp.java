package org.example.dndapp;

public class Monster {
    private String name;
    private String type;
    private String armorClass;
    private String hitPoints;
    private String speed;
    private String stats;
    private String senses;
    private String languages;
    private String challenge;
    private String proficiencyBonus;
    private String skills;
    private String abilities;

    public Monster(String name, String type, String armorClass, String hitPoints, String speed, String stats, String senses, String languages, String challenge, String proficiencyBonus, String skills, String abilities) {
        this.name = name;
        this.type = type;
        this.armorClass = armorClass;
        this.hitPoints = hitPoints;
        this.speed = speed;
        this.stats = stats;
        this.senses = senses;
        this.languages = languages;
        this.challenge = challenge;
        this.proficiencyBonus = proficiencyBonus;
        this.skills = skills;
        this.abilities = abilities;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getArmorClass() {
        return armorClass;
    }

    public String getHitPoints() {
        return hitPoints;
    }

    public String getSpeed() {
        return speed;
    }

    public String getStats() {
        return stats;
    }

    public String getSenses() {
        return senses;
    }

    public String getLanguages() {
        return languages;
    }

    public String getChallenge() {
        return challenge;
    }

    public String getProficiencyBonus() {
        return proficiencyBonus;
    }

    public String getSkills() {
        return skills;
    }

    public String getAbilities() {
        return abilities;
    }
}