package org.example.client;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.example.client.dto.Request;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MainController {

    @FXML
    private ListView<Float> temperatureListView;

    @FXML
    private TextField temperatureInput;

    @FXML
    private Button getTemperatureButton;

    @FXML
    private Button setTemperatureButton;

    @FXML
    private void initialize() {
        getTemperatureButton.setOnAction(event -> getRecentTemperatures());
        setTemperatureButton.setOnAction(event -> setTemperature());
    }

        private void getRecentTemperatures() {
        new Thread(() -> {
            try {
                Random random = new Random();
                int count = 3 + random.nextInt(10);
                URI uri = new URI("http", null, "localhost", 8080, "/api/temperature/recent", "count=" + count + "&offset=0", null);
                URL url = uri.toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String response = in.lines().collect(Collectors.joining());
                    List<Float> temperatures = parseTemperatures(response);
                    Platform.runLater(() -> {
                        temperatureListView.getItems().clear();
                        temperatureListView.getItems().addAll(temperatures);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setTemperature() {
        try {
            float temperature = Float.parseFloat(temperatureInput.getText());
            if (temperature < -200 || temperature > 1000) {
                showAlert();
                return;
            }

            new Thread(() -> {
                try {
                    HttpURLConnection conn = getHttpURLConnection(temperature);
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        URI currentUri = new URI("http", null, "localhost", 8080, "/api/temperature/current", null, null);
                        URL currentUrl = currentUri.toURL();
                        HttpURLConnection currentConn = (HttpURLConnection) currentUrl.openConnection();
                        currentConn.setRequestMethod("GET");
                        currentConn.setRequestProperty("Accept", "application/json");
                        try (BufferedReader in = new BufferedReader(new InputStreamReader(currentConn.getInputStream()))) {
                            String response = in.lines().collect(Collectors.joining());
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode jsonResponse = mapper.readTree(response);
                            float currentTemperature = (float) jsonResponse.get("currentTemperature").asDouble();
                            Platform.runLater(() -> {
                                temperatureListView.getItems().clear();
                                temperatureListView.getItems().add(currentTemperature);
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (NumberFormatException e) {
            showError("Invalid temperature format");
        }
    }

    private HttpURLConnection getHttpURLConnection(float temperature) throws URISyntaxException, IOException {
        URI uri = new URI("http", null, "localhost", 8080, "/api/temperature/setting", null, null);
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        Request request = new Request(temperature);
        ObjectMapper mapper = new ObjectMapper();
        String jsonRequest = mapper.writeValueAsString(request);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return conn;
    }

    private void showAlert() {
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Задержка 3 секунды
                Platform.runLater(() -> {
                    try {
                        Stage alertStage = new Stage();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AlertView.fxml"));
                        alertStage.setScene(new Scene(loader.load()));
                        alertStage.setTitle("Warning");
                        alertStage.initModality(Modality.APPLICATION_MODAL);
                        alertStage.setWidth(290);
                        alertStage.show();
                        alertStage.toFront();
                        alertStage.requestFocus();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private List<Float> parseTemperatures(String json) {
        List<Float> temperatures = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode tempArray = rootNode.path("recentTemperatures");
            if (tempArray.isArray()) {
                for (JsonNode tempNode : tempArray) {
                    temperatures.add((float) tempNode.asDouble());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temperatures;
    }
}