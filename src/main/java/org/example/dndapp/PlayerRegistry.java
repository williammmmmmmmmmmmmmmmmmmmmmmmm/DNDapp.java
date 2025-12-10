package org.example.dndapp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages the client-side mapping of Server Connection ID (Address String) to Player Name.
 * This is crucial for translating server-broadcasted IDs into user-friendly names
 * without modifying the GameServer.
 */
public class PlayerRegistry {
    private static final Logger LOGGER = Logger.getLogger(PlayerRegistry.class.getName());

    // Map: Server ID (e.g., /127.0.0.1:55555) -> Player Name
    private static final Map<String, String> idToNameMap = new ConcurrentHashMap<>();

    /**
     * Stores a new mapping. This is called when a message with the shadow text is received.
     */
    public static void registerPlayer(String serverId, String playerName) {
        // Sanitize input before storing
        String safeName = playerName.replace(":", "").replace("]", "").replace("[", "").trim();
        if (safeName.isEmpty()) return;

        // Only store if the name is new or updated
        if (!safeName.equals(idToNameMap.get(serverId))) {
            idToNameMap.put(serverId, safeName);
            LOGGER.info("Registered player: ID " + serverId + " -> Name " + safeName);
        }
    }

    /**
     * Retrieves a player's name for a given ID.
     */
    public static String getPlayerName(String serverId) {
        // Fallback: If name not registered, use a shortened version of the ID (e.g., "Player [55555]")
        String shortId = serverId;
        try {
            // Attempt to extract just the port number
            shortId = serverId.substring(serverId.lastIndexOf(":") + 1);
        } catch (Exception e) {
            // Ignore extraction failure
        }
        return idToNameMap.getOrDefault(serverId, "Player [" + shortId + "]");
    }
}