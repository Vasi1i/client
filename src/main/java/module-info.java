module org.example.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires static lombok;


    opens org.example.client to javafx.fxml;
    exports org.example.client;

    exports org.example.client.dto;

    opens org.example.client.dto to com.fasterxml.jackson.databind;
}