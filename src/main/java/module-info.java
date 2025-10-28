module org.example.dndapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.dndapp to javafx.fxml;
    exports org.example.dndapp;
}