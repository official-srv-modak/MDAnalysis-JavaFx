package com.modakdev.mdanalysis;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.modakdev.mdanalysis.UrlValues.*;
import static javafx.scene.layout.Region.USE_PREF_SIZE;


public abstract class UIModuleProcessing {

    static BackStackInfo backStackInfo = new BackStackInfo();

    public static String AI_CHAT_STYLE = "-fx-font-family: 'Courier New'; " + // Monospaced font
            "-fx-font-size: 12px; " + // Font size
            "-fx-background-color: #f7f7f7; " + // Light background color
            "-fx-border-color: #ccc; " + // Border color
            "-fx-border-width: 1; " + // Border width
            "-fx-padding: 5;" + // Padding
            "-fx-text-fill: #333;"; // Text color

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

    }

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


    public static void loadChatResponse(String query, String urlStr, TextArea descriptionTextArea) {
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
            }
        }).start();
    }


}
