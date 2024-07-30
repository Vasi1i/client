package org.example.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AlertController {
    @FXML
    private Label alertMessage;

    @FXML
    private void initialize() {
        alertMessage.setText("Critical temperature reached!");
    }
}
