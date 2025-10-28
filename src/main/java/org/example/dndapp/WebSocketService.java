package org.example.dndapp;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.enums.ReadyState;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class WebSocketService {

    private static final Logger LOGGER = Logger.getLogger(WebSocketService.class.getName());
    private static final String WS_URL = "wss://dnd-game-server.onrender.com";

    private WebSocketClient webSocketClient;
    private Consumer<String> onMessageReceived;

    public void connect() {
        if (webSocketClient != null && (webSocketClient.getReadyState().equals(ReadyState.OPEN) || webSocketClient.getReadyState().equals(ReadyState.NOT_YET_CONNECTED))) {
            LOGGER.info("WebSocket client is already connected or connecting.");
            return;
        }
        try {
            webSocketClient = new WebSocketClient(new URI(WS_URL)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    LOGGER.info("WebSocket connection opened.");
                }

                @Override
                public void onMessage(String message) {
                    if (onMessageReceived != null) {
                        onMessageReceived.accept(message);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    LOGGER.info("WebSocket connection closed: " + reason);
                    if (remote) {
                        reconnect();
                    }
                }

                @Override
                public void onError(Exception ex) {
                    LOGGER.severe("WebSocket error: " + ex.getMessage());
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            LOGGER.severe("Invalid WebSocket URL: " + WS_URL);
        }
    }

    public void reconnect() {
        LOGGER.info("Attempting to reconnect...");
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Wait 3 seconds
                connect();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void disconnect() {
        if (webSocketClient != null && webSocketClient.getReadyState().equals(ReadyState.OPEN)) {
            webSocketClient.close();
        }
    }

    public void sendMessage(String message) {
        if (webSocketClient != null && webSocketClient.getReadyState().equals(ReadyState.OPEN)) {
            webSocketClient.send(message);
        } else {
            LOGGER.warning("Failed to send message: WebSocket not connected.");
        }
    }

    public void setOnMessageReceived(Consumer<String> consumer) {
        this.onMessageReceived = consumer;
    }
}