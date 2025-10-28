package org.example.dndapp;

public class PlayerSession {
    private static String playerName = "Guest"; // Default name

    public static String getPlayerName() {
        return playerName;
    }

    public static void setPlayerName(String name) {
        playerName = name;
    }
}