package com.modakdev.mdanalysis.libraries;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.modakdev.mdanalysis.values.UrlValues.TRAIN_MODEL;



public abstract class UIModuleProcessing {


    static BackStackInfo backStackInfo = new BackStackInfo();

    public static String AI_CHAT_STYLE = "-fx-font-family: 'Arial'; " + // Monospaced font
            "-fx-font-size: 12px; " + // Font size
            "-fx-background-color: #f7f7f7; " + // Light background color
            "-fx-border-color: #ccc; " + // Border color
            "-fx-border-width: 1; " + // Border width
            "-fx-padding: 5;"; // Padding

/*
    public static String AI_CHAT_STYLE_FLOW = "-fx-font-family: 'Courier New'; " + // Monospaced font

            "-fx-border-color: #ccc; " + // Border color
            "-fx-border-width: 1; " + // Border width
            "-fx-padding: 5;" ; // Padding*/


    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    public static boolean validateFields(Node... nodes) {
        for (Node node : nodes) {
            if (node instanceof TextInputControl) {
                TextInputControl inputControl = (TextInputControl) node;
                if (inputControl==null || inputControl.getText() == null || inputControl.getText().isEmpty()) {
                    return false;
                }
            } else if (node instanceof ComboBoxBase) {
                ComboBoxBase<?> comboBox = (ComboBoxBase<?>) node;
                if (comboBox.getValue() == null) {
                    return false;
                }
            }
        }
        return true;
    }



    public static void getSuccessBox(String message, String ... val)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(String.format(message, val));
        alert.getButtonTypes().clear();
        alert.getButtonTypes().add(ButtonType.OK);
        alert.showAndWait();
    }

    public static void getConfirmationBox(String message, Runnable onConfirmation, String ... val) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(String.format(message, val));

        ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.NO);

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeYes) {
            // If user clicks Yes, execute the provided action
            if (onConfirmation != null) {
                onConfirmation.run();
            }
        }
    }

    public static String getDirectoryPicker(Stage primaryStage)
    {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory to export the file");

        File selectedDirectory = directoryChooser.showDialog(primaryStage);

        if (selectedDirectory != null) {
            System.out.println("Selected Directory: " + selectedDirectory.getAbsolutePath());
        } else {
            System.out.println("No directory selected.");
        }
        if(selectedDirectory!=null)
            return selectedDirectory.getAbsolutePath();
        else
            return "";
    }


    public static void addScene(String title, Scene scene, Stage primaryStage){
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
        backStackInfo.titleStack.add(title);
        backStackInfo.sceneStack.add(scene);

        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            stopStreaming();
        });

    }

    /*public static void addScene(String title, Scene scene, Stage primaryStage, double width, double height){
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
        primaryStage.show();
        backStackInfo.titleStack.add(title);
        backStackInfo.sceneStack.add(scene);

    }*/

    public static Scene popScene()
    {
        backStackInfo.titleStack.pop();
        return backStackInfo.sceneStack.pop();
    }

    public static void goBack(Stage primaryStage)
    {
        backStackInfo.sceneStack.pop();
        backStackInfo.titleStack.pop();

        Scene scene = backStackInfo.sceneStack.pop();
        String title = backStackInfo.titleStack.pop();

        addScene(title, scene, primaryStage);

    }

    public static Label increaseFontSize(Label textField, double fontSize) {
        textField.setStyle("-fx-font-size: " + fontSize + "pt;"); // Set font size
        return textField;
    }


    public static volatile boolean isStreaming = true;
    private static HttpURLConnection connection = null;

    public static void loadChatResponse(String query, String urlStr, TextArea descriptionTextArea, Button toggleButton, String placeholderText) {
        // Set the initial state of the toggle button and start the stream
        toggleButton.setText("Cancel Analysis");

        // Start the stream immediately when the method is called
        startStreaming(query, urlStr, descriptionTextArea, toggleButton, placeholderText);

        // Toggle button event listener
        toggleButton.setOnAction(event -> {
            if (isStreaming) {
                // Stop the stream
                isStreaming = false;
                toggleButton.setText("Start Analysis");

                // Disconnect the connection if it's active
                if (connection != null) {
                    connection.disconnect();
                    connection = null;
                }
            } else {
                // Start the stream again
                isStreaming = true;
                toggleButton.setText("Cancel Analysis");
                startStreaming(query, urlStr, descriptionTextArea, toggleButton, placeholderText);
            }
        });
    }

    public static Button toggleButtonParent;
    private static void startStreaming(String query, String urlStr, TextArea descriptionTextArea, Button toggleButton, String placeholderText) {
        // Thread to handle the stream API call
        new Thread(() -> {
            try {
                // Set placeholder text before starting the stream
                Platform.runLater(() -> descriptionTextArea.setText(placeholderText));

                // API endpoint URL for chat response
                URL url = new URL(urlStr);

                // Open connection
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Create the JSON payload
                JsonObject payObj = new JsonObject();
                payObj.addProperty("query", query);
                String payLoad = payObj.toString();

                // Write the JSON input to the connection output stream
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = payLoad.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Check response code
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Clear the placeholder once the stream starts
                    Platform.runLater(() -> descriptionTextArea.clear());

                    // Read the input stream as a stream of characters
                    try (InputStream inputStream = connection.getInputStream();
                         Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {

                        StringBuilder responseBuilder = new StringBuilder();
                        int character;

                        // While loop to read the input stream continuously
                        while (isStreaming && (character = reader.read()) != -1) {
                            // Append the character to the response builder
                            responseBuilder.append((char) character);

                            // Update the UI with the new response
                            String currentResponse = responseBuilder.toString();
                            Platform.runLater(() -> {
                                descriptionTextArea.setText(currentResponse);
                                // Ensure the TextArea scrolls to the bottom
                                descriptionTextArea.setScrollTop(Double.MAX_VALUE);
                            });
                        }
                    }
                } else {
                    System.err.println("Request failed. Response code: " + responseCode);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // Disconnect the connection in the finally block
                if (connection != null) {
                    connection.disconnect();
                    connection = null;
                }

                // Update the UI when the stream stops
                Platform.runLater(() -> {
                    if (connection == null) {
                        descriptionTextArea.appendText("\nStream stopped.");
                    }

                    // Reset button state when stream stops
                    toggleButton.setText("Start Analysis");
                });
            }
        }).start();
    }


    private static void stopStreaming() {
        isStreaming = false; // Set the streaming flag to false
        toggleButtonParent.setText("Start Analysis"); // Update button text to indicate streaming has stopped
    }


    public static String getTextFromTextFlow(TextFlow textFlow) {
        StringBuilder fullText = new StringBuilder();

        for (var node : textFlow.getChildren()) {
            if (node instanceof Text) {
                fullText.append(((Text) node).getText());
            }
        }

        return fullText.toString();
    }

    public static void setTextInTextFlow(TextFlow textFlow, String newText) {
        // Clear existing text
        textFlow.getChildren().clear();

        // Split the new text into lines if you want to maintain line breaks
        String[] lines = newText.split("\n");

        for (String line : lines) {
            // Create a new Text node for each line
            Text textNode = new Text(line);

            // Optionally set additional styles, like font size or color
            textNode.setStyle("-fx-font-size: 14px; -fx-fill: black;");

            // Add the Text node to the TextFlow
            textFlow.getChildren().add(textNode);

            // Add a line break if needed
            textFlow.getChildren().add(new Text("\n"));
        }
    }


    public static void loadChatResponse(String query, String urlStr, TextFlow textFlow) {
        new Thread(() -> {
            try {
                // API endpoint URL for chat response
                URL url = new URL(urlStr);

                // Open connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Create the JSON payload
                JsonObject payObj = new JsonObject();
                payObj.addProperty("query", query);
                String payLoad = payObj.toString();

                // Write the JSON input to the connection output stream
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = payLoad.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Check response code
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the input stream as a stream of characters
                    try (InputStream inputStream = connection.getInputStream();
                         Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {

                        StringBuilder responseBuilder = new StringBuilder();
                        int character;
                        while ((character = reader.read()) != -1) {
                            // Append the character to the response builder
                            responseBuilder.append((char) character);

                            // Update the UI with the new response
                            String currentResponse = responseBuilder.toString();
                            Platform.runLater(() -> {
                                // Clear the previous content
                                textFlow.getChildren().clear();
                                // Apply formatting to the current response
                                formatResponse(currentResponse, textFlow);
                                // Ensure the TextFlow scrolls to the bottom (if needed)
                                textFlow.setLayoutY(textFlow.getLayoutY());
                            });
                        }

                    }
                } else {
                    System.err.println("Request failed. Response code: " + responseCode);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Method to format the response text
    private static void formatResponse(String response, TextFlow textFlow) {
        String[] lines = response.split("\n");

        for (String line : lines) {
            // Check for code blocks
            if (line.startsWith("```")) {
                // Check for end of code block
                if (line.equals("```")) {
                    // End code block
                    continue;
                } else {
                    // Start code block; use monospace font
                    Text codeText = new Text();
                    codeText.setFont(Font.font("Monospaced")); // Monospace font for code
                    codeText.setText(line + "\n");
                    textFlow.getChildren().add(codeText);
                    continue;
                }
            }

            // Check for bold text
            if (line.contains("*")) {
                line = line.replaceAll("\\*(.*?)\\*", "$1"); // Remove asterisks for processing
                Text boldText = new Text(line);
                boldText.setFont(Font.font("Arial", FontWeight.BOLD, 12)); // Bold font
                textFlow.getChildren().add(boldText);
            } else {
                // Regular statement, use Arial
                Text normalText = new Text(line + "\n");
                normalText.setFont(Font.font("Arial", 12)); // Regular font
                textFlow.getChildren().add(normalText);
            }
        }
    }


    public static String getCorrelationalMatrix(String trainsetName, String modelName, String resultColumn, int numberOfTrainData) {
        try {
            // Create the URL and open the connection
            URL url = new URL("http://127.0.0.1:7654/api/get-correlation-matrix-info");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Create the JSON payload
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("train_set_name", trainsetName);
            jsonObject.addProperty("model_name", modelName);
            jsonObject.addProperty("columns_to_encode", "");
            jsonObject.addProperty("result_column", resultColumn);
            jsonObject.addProperty("number_of_train_data", numberOfTrainData);
            String jsonPayload = jsonObject.toString();

            // Write the payload to the connection output stream
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Get the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Remove all double quotes from the response
                return response.toString().replaceAll("\"", "");
            } else {
                System.err.println("Error: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void trainModel(String trainFilePath, String decisionColumn, String testFilePath,
                                  String modelName, String[] encodedColumns, Label accuracyLabel) {
        new Thread(() -> {
            try {
                // API endpoint URL for training the model
                URL url = new URL(TRAIN_MODEL.getUrl());

                // Open connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Create JSON payload
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("trainFilePath", trainFilePath);
                jsonObject.addProperty("decisionColumn", decisionColumn);
                jsonObject.addProperty("testFilePath", testFilePath);
                jsonObject.addProperty("modelName", modelName);
                jsonObject.addProperty("modelFlag", 1);

                // Create JSON array for encoded columns
                JsonArray encodedColumnsJsonArray = new JsonArray();
                for (String column : encodedColumns) {
                    encodedColumnsJsonArray.add(column);
                }
                jsonObject.add("encodedColumns", encodedColumnsJsonArray); // Add the JSON array

                String jsonPayload = jsonObject.toString();

                // Write JSON payload to output stream
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }




                // Get response code
                int responseCode = connection.getResponseCode();
                StringBuilder responseBuilder = new StringBuilder();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response body
                    try (InputStream inputStream = connection.getInputStream();
                         Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                        char[] buffer = new char[1024];
                        int bytesRead;
                        while ((bytesRead = reader.read(buffer)) != -1) {
                            responseBuilder.append(buffer, 0, bytesRead);
                        }
                    }

                    // Parse the response to JSON
                    String response = responseBuilder.toString();
                    JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

                    // You can access specific fields from jsonResponse as needed
                    // For example, to get an accuracy value:
                    String accuracy = jsonResponse.get("accuracy").getAsString(); // Change "accuracy" based on actual field name
                    DecimalFormat df = new DecimalFormat("#.00"); // Format to 2 decimal places
                    accuracy = "ACCURACY : "+df.format(Double.parseDouble(accuracy)*100) + "%";
                    // Display success alert and response
                    String finalAccuracy = accuracy;
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Training Successful", "Model training completed successfully. Accuracy is : " + finalAccuracy);
                        accuracyLabel.setText(finalAccuracy); // Display the accuracy in the label
                    });
                } else {
                    // Read error response
                    try (InputStream errorStream = connection.getErrorStream();
                         Reader errorReader = new InputStreamReader(errorStream, StandardCharsets.UTF_8)) {
                        char[] buffer = new char[1024];
                        int bytesRead;
                        while ((bytesRead = errorReader.read(buffer)) != -1) {
                            responseBuilder.append(buffer, 0, bytesRead);
                        }
                    }

                    // Display error alert with response
                    String errorResponse = responseBuilder.toString();
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Training Failed", "Failed to train model. Response code: " + responseCode + "\n" + errorResponse);
                    });
                }



            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
                });
            }
        }).start();
    }


    // Method to show alerts
    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
