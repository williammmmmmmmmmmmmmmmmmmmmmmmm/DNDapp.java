package org.example.dndapp;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppController {
    private static Stage primaryStage;

    public static void initialize(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Changes the scene and forces a window state toggle to fix the "shrinking" bug.
     */
    public static void goTo(Scene nextScene) {
        if (primaryStage == null || nextScene == null) return;

        // 1. Check if we were maximized before switching
        boolean wasMaximized = primaryStage.isMaximized();

        // 2. Set the new scene
        primaryStage.setScene(nextScene);

        // 3. If we were maximized, perform the "Toggle Reset"
        if (wasMaximized) {
            // We use Platform.runLater to ensure this happens AFTER the scene is fully swapped
            Platform.runLater(() -> {
                // This 'flicker' is necessary to force the OS to recalculate the window bounds
                primaryStage.setMaximized(false);
                primaryStage.setMaximized(true);
            });
        }
    }
}