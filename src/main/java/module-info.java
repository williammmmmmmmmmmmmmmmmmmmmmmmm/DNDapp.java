module org.example.dndapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web; // This is the line you need to add
    requires Java.WebSocket;
    requires java.logging;
    requires com.google.gson;
    requires java.desktop;
    requires java.prefs;

    opens org.example.dndapp to com.google.gson, javafx.fxml;
    exports org.example.dndapp;
}