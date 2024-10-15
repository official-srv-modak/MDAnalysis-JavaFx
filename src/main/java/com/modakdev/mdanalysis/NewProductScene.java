package com.modakdev.mdanalysis;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NewProductScene {
    private static File trainFile; // Store the train file
    private static File testFile; // Store the test file
    private static TextField splitValueField; // Split value input field
    private static VBox addProductLayout; // Layout container

    public static void initializeAddProductScene(Stage primaryStage) {
        // Create a new VBox for the add product layout
        addProductLayout = new VBox(15);
        addProductLayout.setStyle("-fx-padding: 20; -fx-background-color: #ffffff;");

        // Create UI elements for product input
        TextField productNameField = new TextField();
        productNameField.setPromptText("Enter Product Name");

        // Media choosers for train and test files
        Button trainFileButton = new Button("Choose Train Set (CSV)");
        Button testFileButton = new Button("Choose Test Set (CSV)");
        Button uploadFilesButton = new Button("Upload Files"); // Upload button

        // Decision column dropdown
        ComboBox<String> decisionColumnDropdown = new ComboBox<>();

        // Split value input
        splitValueField = new TextField();
        splitValueField.setPromptText("Enter Split Value");

        // File chooser instance
        FileChooser fileChooser = new FileChooser();

        trainFileButton.setOnAction(e -> {
            trainFile = chooseFile(fileChooser, "Choose Train Set (CSV)", primaryStage);
            if (trainFile != null) {
                trainFileButton.setText(trainFile.getName()); // Change button text to file name
                // Read the CSV headers and populate the dropdown
                List<String> headers = readCsvHeaders(trainFile);
                decisionColumnDropdown.getItems().clear();
                decisionColumnDropdown.getItems().addAll(headers);

                // If there's a test file, hide the split value field
                if (testFile != null) {
                    removeSplitValueField();
                }
            }
        });

        testFileButton.setOnAction(e -> {
            testFile = chooseFile(fileChooser, "Choose Test Set (CSV)", primaryStage);
            if (testFile != null) {
                testFileButton.setText(testFile.getName()); // Change button text to file name
                // Read the CSV headers and populate the dropdown
                List<String> headers = readCsvHeaders(testFile);
                decisionColumnDropdown.getItems().clear();
                decisionColumnDropdown.getItems().addAll(headers);

                // If a test file is selected, remove the split value field
                removeSplitValueField();
            }
        });

        // Upload button action
        uploadFilesButton.setOnAction(e -> {
            if (trainFile != null && testFile != null) {
                uploadFiles(trainFile, testFile, productNameField.getText(), decisionColumnDropdown.getValue());
            } else {
                showAlert("Error", "Please select both train and test files.");
            }
        });

        // Add all elements to the layout
        addProductLayout.getChildren().addAll(
                new Label("Product Name:"), productNameField,
                trainFileButton,
                testFileButton,
                splitValueField, // Initially added to layout
                new Label("Decision Column:"), decisionColumnDropdown,
                uploadFilesButton // Add the upload button
        );

        // Create a new Scene for adding a product
        Scene addProductScene = new Scene(addProductLayout, 400, 400);
        primaryStage.setScene(addProductScene);
        primaryStage.setMaximized(true); // Set the window maximized
    }

    // Method to choose file
    private static File chooseFile(FileChooser fileChooser, String title, Stage primaryStage) {
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        return fileChooser.showOpenDialog(primaryStage);
    }

    // Method to read CSV headers
    private static List<String> readCsvHeaders(File csvFile) {
        List<String> headers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line = br.readLine(); // Read the first line
            if (line != null) {
                String[] headerArray = line.split(","); // Split by comma
                for (String header : headerArray) {
                    headers.add(header.trim()); // Add headers to the list
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return headers;
    }

    // Remove split value field from layout
    private static void removeSplitValueField() {
        if (addProductLayout.getChildren().contains(splitValueField)) {
            addProductLayout.getChildren().remove(splitValueField);
        }
    }

    // Method to upload files
    private static void uploadFiles(File trainFile, File testFile, String productName, String decisionColumn) {
        // Make sure to replace the URL and header accordingly
        String apiUrl = "http://10.0.0.47:8765/product-catalog-module/product/upload-files";
        String authorization = "Basic YWRtaW46YWRtaW4="; // Your Authorization header

        try {
            // Initialize HTTP connection
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", authorization);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=---Boundary");

            // Write files and additional parameters
            // Implement file uploading logic here...

            // After uploading, show success message
            showAlert("Success", "Files uploaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to upload files: " + e.getMessage());
        }
    }

    // Method to show alerts
    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
