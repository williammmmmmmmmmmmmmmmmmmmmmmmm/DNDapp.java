package org.example.dndapp;

/**
 * Static class to hold the current player's name across the client application.
 */
public class PlayerSession {
    // Default name for unconfigured accounts
    private static String playerName = "Anonymous";

    public static String getPlayerName() {
        return playerName;
    }

    public static void setPlayerName(String name) {
        playerName = name;
    }
}