package org.example.dndapp;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class GameServer extends WebSocketServer {

    private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());
    private static final Map<String, Room> ROOMS = new ConcurrentHashMap<>();
    private static final Map<String, WebSocket> SESSIONS = new ConcurrentHashMap<>();
    private static final Map<WebSocket, String> playerRooms = new ConcurrentHashMap<>();

    // Inner class to represent a game room
    private static class Room {
        String name;
        String password;
        Set<WebSocket> players = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public GameServer(InetSocketAddress address) {
        super(address);
    }

    // Inside GameServer.java

    public static void main(String[] args) {
        // 1. Get port from environment variable (Render sets this), default to 10000.
        String portStr = System.getenv("PORT");
        int PORT = 10000;

        if (portStr != null && !portStr.isEmpty()) {
            try {
                PORT = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                LOGGER.severe("Invalid PORT environment variable. Using default " + PORT);
            }
        }

        LOGGER.info("Starting WebSocket server on port: " + PORT);

        WebSocketServer server = new GameServer(new InetSocketAddress(PORT));
        server.setReuseAddr(true);
        server.start(); // Changed to start() for non-blocking execution
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        LOGGER.info("New connection opened: " + conn.getRemoteSocketAddress());
        SESSIONS.put(conn.getRemoteSocketAddress().toString(), conn);
        sendRoomList(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        LOGGER.info("Connection closed: " + conn.getRemoteSocketAddress());
        SESSIONS.remove(conn.getRemoteSocketAddress().toString());

        // Find the room the player was in and handle the leave
        String roomName = playerRooms.get(conn);
        if (roomName != null) {
            handleLeaveRoom(conn, roomName);
        }
        broadcastRoomList();
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        LOGGER.info("Message received from " + conn.getRemoteSocketAddress() + ": " + message);
        try {
            String[] parts = message.split(":", 3);
            String command = parts[0];
            String roomName = parts[1];
            String passwordOrMessage = parts.length > 2 ? parts[2] : "";

            switch (command) {
                case "CREATE":
                    handleCreateRoom(roomName, passwordOrMessage, conn);
                    break;
                case "JOIN":
                    handleJoinRoom(roomName, passwordOrMessage, conn);
                    break;
                case "LEAVE":
                    handleLeaveRoom(conn, roomName);
                    break;
                case "CHAT":
                    handleChatMessage(conn, roomName, passwordOrMessage);
                    break;
                case "MOVE":
                    // Handle movement here
                    break;
            }
        } catch (Exception e) {
            LOGGER.severe("Error processing message: " + e.getMessage());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        LOGGER.severe("An error occurred on connection " + conn.getRemoteSocketAddress() + ": " + ex.getMessage());
    }

    @Override
    public void onStart() {
        LOGGER.info("WebSocket Server started on port " + getPort());
    }

    private void handleCreateRoom(String roomName, String password, WebSocket conn) {
        if (ROOMS.containsKey(roomName)) {
            sendMessage(conn, "ERROR: Room '" + roomName + "' already exists.");
            return;
        }

        Room newRoom = new Room();
        newRoom.name = roomName;
        newRoom.password = password;
        newRoom.players.add(conn);
        ROOMS.put(roomName, newRoom);
        playerRooms.put(conn, roomName);

        LOGGER.info("Room '" + roomName + "' created by " + conn.getRemoteSocketAddress());
        sendMessage(conn, "SUCCESS:DM:" + roomName);
        broadcastRoomList();
    }

    private void handleJoinRoom(String roomName, String password, WebSocket conn) {
        Room room = ROOMS.get(roomName);
        if (room == null) {
            sendMessage(conn, "ERROR: Room '" + roomName + "' does not exist.");
            return;
        }

        if (!room.password.isEmpty() && !room.password.equals(password)) {
            sendMessage(conn, "ERROR: Incorrect password for room '" + roomName + "'.");
            return;
        }

        room.players.add(conn);
        playerRooms.put(conn, roomName);
        LOGGER.info(conn.getRemoteSocketAddress() + " joined room '" + roomName + "'.");

        sendMessage(conn, "SUCCESS:PLAYER:" + roomName);
        broadcastMessage(room, "PLAYER_JOINED:" + conn.getRemoteSocketAddress().toString());
        broadcastRoomList();
    }

    private void handleLeaveRoom(WebSocket conn, String roomName) {
        Room room = ROOMS.get(roomName);
        if (room == null) {
            return; // Room doesn't exist, nothing to do.
        }

        if (room.players.remove(conn)) {
            playerRooms.remove(conn);
            LOGGER.info(conn.getRemoteSocketAddress() + " left room '" + roomName + "'.");

            // Broadcast a message to all other players in the room that a user has left
            broadcastMessage(room, "PLAYER_LEFT:" + conn.getRemoteSocketAddress().toString());

            // Check if the room is now empty and remove it
            if (room.players.isEmpty()) {
                ROOMS.remove(roomName);
                LOGGER.info("Room '" + roomName + "' is empty and has been removed.");
                broadcastRoomList();
            }
        }
    }

    private void handleChatMessage(WebSocket conn, String roomName, String chatMessage) {
        Room room = ROOMS.get(roomName);
        if (room == null) {
            sendMessage(conn, "ERROR: Room '" + roomName + "' does not exist.");
            return;
        }

        String sender = conn.getRemoteSocketAddress().toString();
        // Broadcast the chat message to all players in the room
        broadcastMessage(room, "CHAT_MESSAGE:" + sender + ":" + chatMessage);
    }

    private void broadcastRoomList() {
        String roomList = String.join(",", ROOMS.keySet());
        for (WebSocket conn : SESSIONS.values()) {
            sendMessage(conn, "ROOMLIST:" + roomList);
        }
    }

    private void sendRoomList(WebSocket conn) {
        String roomList = String.join(",", ROOMS.keySet());
        sendMessage(conn, "ROOMLIST:" + roomList);
    }

    private void broadcastMessage(Room room, String message) {
        for (WebSocket player : room.players) {
            sendMessage(player, message);
        }
    }

    private void sendMessage(WebSocket conn, String message) {
        conn.send(message);
    }
}