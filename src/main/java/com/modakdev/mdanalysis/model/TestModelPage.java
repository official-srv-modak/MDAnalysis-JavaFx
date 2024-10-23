package com.modakdev.mdanalysis.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.modakdev.mdanalysis.libraries.UIModuleProcessing;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static javafx.scene.layout.Region.USE_PREF_SIZE;

public class TestModelPage {

    private static final String CACHE_FILE_PATH = "user_input_cache.json"; // File to store user inputs

    // Method to show the encoded columns page
    public static void showEncodedColumnsPage(Stage primaryStage, JsonArray encodedColumns, String id, String decisionColumn, String modelName) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        // Store user inputs in a map
        Map<String, TextField> userInputs = new LinkedHashMap<>();

        // Load previously stored user inputs, if any
        Map<String, String> previousInputs = loadUserInputs(modelName);

        // Create editable fields for each encoded column
        for (int i = 0; i < encodedColumns.size(); i++) {
            String columnName = encodedColumns.get(i).getAsString();
            Label lbl = new Label(columnName + ":");
            TextField textField = new TextField();

            // Pre-fill the text field if previous input is available
            if (previousInputs != null && previousInputs.containsKey(columnName)) {
                textField.setText(previousInputs.get(columnName));
            } else {
                textField.setPromptText(columnName);
            }

            grid.add(lbl, 0, i);
            grid.add(textField, 1, i);
            userInputs.put(columnName, textField);
        }

        // Create a Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(event -> UIModuleProcessing.goBack(primaryStage));

        // Create a Test Model button
        Button testModelButton = new Button("Test Model");
        testModelButton.setOnAction(event -> {
            try {
                // Gather user inputs into a JSON object
                JsonObject singleDataPoint = new JsonObject();
                boolean hasEmptyFields = false;

                for (Map.Entry<String, TextField> entry : userInputs.entrySet()) {
                    String value = entry.getValue().getText();
                    if (value.isEmpty()) {
                        hasEmptyFields = true;
                        break;
                    }
                    singleDataPoint.addProperty(entry.getKey(), value);
                }

                if (hasEmptyFields) {
                    System.out.println("Please fill all fields!");
                    return;
                }

                // Call the method to send the request
                sendTestModelRequest(singleDataPoint, decisionColumn, modelName);

                // Save the user inputs to a file after clicking "Test Model"
                saveUserInputs(userInputs, modelName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Add buttons to the grid
        grid.add(backButton, 0, encodedColumns.size());
        grid.add(testModelButton, 1, encodedColumns.size());

        // Create a new scene and set it on the primary stage
        Scene scene = new Scene(grid);
        UIModuleProcessing.addScene("Model Details", scene, primaryStage);
    }

    // Method to send the test model request using HttpURLConnection
    private static void sendTestModelRequest(JsonObject singleDataPoint, String decisionColumn, String modelName) throws Exception {
        URL url = new URL("http://localhost:7654/api/test-model");  // Replace with your actual API URL

        // Open connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Create JSON payload
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("model_name", modelName);
        jsonObject.add("single_data_point", singleDataPoint);
        jsonObject.addProperty("decision_column", decisionColumn);

        String jsonPayload = jsonObject.toString();

        // Write JSON payload to output stream
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Get response code
        int responseCode = connection.getResponseCode();
        StringBuilder responseBuilder = new StringBuilder();

        // Handle successful response
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = reader.readLine()) != null) {
                    responseBuilder.append(responseLine.trim());
                }
            }

            // Parse and display the result
            String jsonResponse = responseBuilder.toString();
            System.out.println("Response: " + jsonResponse);

            // Show success alert with formatted response
            showFormattedResponseAlert(jsonResponse);

        } else { // Handle failure response
            try (var errorReader = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    responseBuilder.append(errorLine.trim());
                }
            }

            // Show error alert with HTTP error code and message
            showErrorAlert(responseCode, responseBuilder.toString());
        }
    }

    // Method to save user inputs to a file
    private static void saveUserInputs(Map<String, TextField> userInputs, String modelName) {
        JsonObject cacheObject = new JsonObject();
        JsonObject modelObject = new JsonObject();

        for (Map.Entry<String, TextField> entry : userInputs.entrySet()) {
            modelObject.addProperty(entry.getKey(), entry.getValue().getText());
        }

        cacheObject.add(modelName, modelObject);

        try (FileWriter file = new FileWriter(CACHE_FILE_PATH)) {
            file.write(cacheObject.toString());
            System.out.println("Successfully saved user inputs to " + CACHE_FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to load user inputs from a file
    private static Map<String, String> loadUserInputs(String modelName) {
        try {
            if (!Files.exists(Paths.get(CACHE_FILE_PATH))) {
                return null;
            }

            FileReader reader = new FileReader(CACHE_FILE_PATH);
            JsonObject cacheObject = JsonParser.parseReader(reader).getAsJsonObject();

            if (cacheObject.has(modelName)) {
                JsonObject modelObject = cacheObject.getAsJsonObject(modelName);
                Map<String, String> userInputs = new HashMap<>();
                for (Map.Entry<String, com.google.gson.JsonElement> entry : modelObject.entrySet()) {
                    userInputs.put(entry.getKey(), entry.getValue().getAsString());
                }
                return userInputs;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Method to display the formatted success response in an alert
    private static void showFormattedResponseAlert(String jsonResponse) {
        // Parse the JSON response
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        // Create a TextFlow to hold the formatted key-value pairs
        TextFlow textFlow = new TextFlow();

        // Iterate through the JSON object
        for (String key : jsonObject.keySet()) {
            String value = jsonObject.get(key).getAsString();

            // Create Text elements for key and value
            Text boldKey = new Text(key + ": ");
            boldKey.setStyle("-fx-font-weight: bold;");

            Text normalValue = new Text(value + "\n");

            // Add the key and value to the TextFlow
            textFlow.getChildren().addAll(boldKey, normalValue);
        }

        // Create an alert dialog to show the response
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Server Response");
        alert.setHeaderText("Model Test Result");
        alert.getDialogPane().setContent(textFlow); // Set the content to the formatted TextFlow
        alert.showAndWait();
    }

    // Method to display an error response in an alert
    private static void showErrorAlert(int responseCode, String errorMessage) {
        // Create an alert dialog for the error
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Failed: HTTP error code " + responseCode);

        // Create a TextFlow for error details
        TextFlow textFlow = new TextFlow();
        Text errorText = new Text(errorMessage);

        textFlow.getChildren().add(errorText);

        // Set the content and show the alert
        alert.getDialogPane().setContent(textFlow);
        alert.showAndWait();
    }
}
