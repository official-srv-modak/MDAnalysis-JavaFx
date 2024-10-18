package com.modakdev.mdanalysis;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.modakdev.mdanalysis.UIModuleProcessing.AI_CHAT_STYLE;

public class NewProductScene {
    private static File trainFile; // Store the train file
    private static File testFile; // Store the test file
    private static TextField splitValueField, productNameField; // Split value input field
    private static VBox addProductLayout; // Layout container
    private static List<CheckBox> headerCheckboxes; // List of checkboxes for headers
    private static ComboBox<String> decisionColumnComboBox; // ComboBox for decision columns
    private static Button analyseButton, uploadFilesButton;
    private static HBox imageViewCard;

    // Remove split value field from layout
    // Declare a variable to hold the index
    private static int splitValueFieldIndex = -1;

    public static void initializeAddProductScene(Stage primaryStage) {
        // Create a new VBox for the add product layout
        addProductLayout = new VBox(15);
        addProductLayout.setStyle("-fx-padding: 20; -fx-background-color: #ffffff;");
        headerCheckboxes = new ArrayList<>(); // Initialize header checkboxes list

        // Create UI elements for product input
        productNameField = new TextField();
        productNameField.setPromptText("Enter Product Name");

        // Create "Go Back" button
        Button goBackButton = new Button("Go Back");
        // Set an onAction event (leave empty for now)
        goBackButton.setOnAction(e -> {
            UIModuleProcessing.goBack(primaryStage);
        });

        // Media choosers for train and test files
        Button trainFileButton = new Button("Choose Train Set (CSV)");
        Button testFileButton = new Button("Choose Test Set (CSV)");
        uploadFilesButton = new Button("Upload Files"); // Upload button
        analyseButton = new Button("Analyse");

        // Split value input
        splitValueField = new TextField();
        splitValueField.setPromptText("Enter Split Value");

        // File chooser instance
        FileChooser fileChooser = new FileChooser();

        trainFileButton.setOnAction(e -> {
            trainFile = chooseFile(fileChooser, "Choose Train Set (CSV)", primaryStage);
            if (trainFile != null) {
                trainFileButton.setText(trainFile.getName()); // Change button text to file name
                // Read the CSV headers and populate the checkboxes
                List<String> headers = readCsvHeaders(trainFile);
                populateHeaderCheckboxes(headers);

                // If there's a test file, hide the split value field
                if(testFile != null && !testFile.getAbsolutePath().equalsIgnoreCase(trainFile.getAbsolutePath()))
                    removeSplitValueField();
                else if(splitValueFieldIndex != -1){
                    addSplitValueField();
                }
            }
        });

        testFileButton.setOnAction(e -> {
            testFile = chooseFile(fileChooser, "Choose Test Set (CSV)", primaryStage);
            if (testFile != null) {
                testFileButton.setText(testFile.getName()); // Change button text to file name
                // Read the CSV headers and populate the checkboxes
                List<String> headers = readCsvHeaders(testFile);
                Label checkBoxLabel = new Label("Select Columns:");
                addProductLayout.getChildren().add(checkBoxLabel);
                populateHeaderCheckboxes(headers);

                // If a test file is selected, remove the split value field
                if(!testFile.getAbsolutePath().equalsIgnoreCase(trainFile.getAbsolutePath()))
                    removeSplitValueField();
                else if(splitValueFieldIndex != -1){
                    addSplitValueField();
                }
            }
        });

        // Upload button action
        uploadFilesButton.setOnAction(e -> {
            if (trainFile != null && testFile != null) {
                List<String> selectedColumns = getSelectedColumns();
                String selectedDecisionColumn = decisionColumnComboBox.getValue();
                uploadFiles(trainFile, testFile, productNameField.getText(), selectedColumns, selectedDecisionColumn);
                imageViewCard = ImageViewCard.initialise(UrlValues.IMAGE_URL.getUrl(), trainFileButton.getText());
                List<Node> nodes = addProductLayout.getChildren();
                if(addProductLayout.getChildren().get(5) instanceof HBox)
                {
                    addProductLayout.getChildren().remove(5);
                    addProductLayout.getChildren().add(5, imageViewCard);
                }
                else
                    addProductLayout.getChildren().add(5, imageViewCard);

            } else {
                showAlert("Error", "Please select both train and test files.");
            }


        });

        analyseButton.setOnAction(actionEvent -> {
            if(!productNameField.getText().isBlank() || trainFileButton.getText().contains("Choose Train") || testFileButton.getText().contains("Choose Test"))
            {
                doAnalysis();
            }
            else {
                showAlert("Mandatory Field missing", "Name, train set and test are mandatory");
            }
        });


        // Add all elements to the layout
        addProductLayout.getChildren().addAll(
                goBackButton, // Add the Go Back button at the top
                new Label("Product Name:"), productNameField,
                trainFileButton,
                testFileButton,
                splitValueField, // Initially added to layout
                uploadFilesButton // Add the upload button

        );

        // Create a new Scene for adding a product
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(addProductLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane);
        UIModuleProcessing.addScene("New Product", scene, primaryStage);
    }

    private static void doAnalysis() {
        String payloadString = "Tell me which columns should i choose and my decision column based on the following details in organised with bullet points if required:-";
        payloadString += "name of the model : " + productNameField.getText();
        payloadString += "train-file-sample : " + readCsvHeadersAndRows(trainFile, 5);
        payloadString += "test-file-sample : " + readCsvHeadersAndRows(testFile, 5);
        payloadString += "split : " + splitValueField.getText();

        TextArea analysisArea = new TextArea();
        analysisArea.setStyle(AI_CHAT_STYLE);
        analysisArea.setWrapText(true);

        analysisArea.setPrefWidth(640);
        analysisArea.setPrefHeight(480);

// Set the minimum width and height to avoid collapsing
        analysisArea.setMinWidth(640); // Minimum width to ensure it doesn't collapse too much
        analysisArea.setMinHeight(480); // Minimum height to ensure sufficient space

        int analInd = addProductLayout.getChildren().indexOf(analyseButton);

        if(addProductLayout.getChildren().contains(analysisArea))
        {
            addProductLayout.getChildren().remove(analysisArea);
            addProductLayout.getChildren().add(analInd+1, analysisArea);
        }
        else
            addProductLayout.getChildren().add(analInd+1, analysisArea);

        if(payloadString.contains("\""))
            payloadString = payloadString.replaceAll("\"", "");
        UIModuleProcessing.loadChatResponse(payloadString, UrlValues.ANALYSIS_CHAT_URL_FLASK.getUrl(), analysisArea);
        /*// Define the URL of the endpoint
        String urlString = "http://10.0.0.47:8180/analysis-wrapper-module/chat-single-stream";

        try {
            // Create a URL object and open a connection
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set request method to POST
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true); // Allow sending data

            if(payloadString.contains("\""))
                payloadString = payloadString.replaceAll("\"", "");

            // Create the JSON payload with the "query" field containing jsonString
            JsonObject payObj = new JsonObject();
            payObj.addProperty("query", payloadString);
            String payLoad = payObj.toString();

            // Write the payload to the output stream
            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                byte[] input = payLoad.getBytes(StandardCharsets.UTF_8);
                outputStream.write(input, 0, input.length);
            }

            // Get the response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Read the response from the server
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                System.out.println("Response: " + response.toString());
            }

            // Close the connection
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }*/
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

    public static String readCsvHeadersAndRows(File csvFile, int data) {
        JsonObject jsonObject = new JsonObject();
        List<String> headers = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line = br.readLine(); // Read the first line (header)
            if (line != null) {
                // Read and store the headers
                String[] headerArray = line.split(",");
                for (String header : headerArray) {
                    headers.add(header.trim());
                    jsonObject.add(header.trim(), new JsonArray()); // Initialize JsonArrays for each column
                }
            }

            // Read the next rows (as we already read the header row)
            int rowCount = 0;
            while ((line = br.readLine()) != null && rowCount < data) {
                String[] rowValues = line.split(",");
                for (int i = 0; i < rowValues.length; i++) {
                    // Get the header and corresponding JsonArray
                    String header = headers.get(i);
                    JsonArray jsonArray = jsonObject.getAsJsonArray(header);
                    jsonArray.add(rowValues[i].trim());
                }
                rowCount++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Convert the JsonObject to a string and return it
        return jsonObject.toString();
    }

    // Method to populate header checkboxes
    private static void populateHeaderCheckboxes(List<String> headers) {
        addProductLayout.getChildren().removeIf(node -> node instanceof CheckBox); // Remove existing checkboxes
        headerCheckboxes.clear(); // Clear existing checkboxes list
        for (String header : headers) {
            CheckBox checkBox = new CheckBox(header);
            headerCheckboxes.add(checkBox); // Add checkbox to the list
            addProductLayout.getChildren().add(checkBox); // Add checkbox to layout
        }

        // Populate the ComboBox for decision columns
        populateDecisionColumnComboBox(headers);
    }

    // Method to populate the ComboBox for decision columns
    private static void populateDecisionColumnComboBox(List<String> headers) {
        if (decisionColumnComboBox == null) {
            decisionColumnComboBox = new ComboBox<>(); // Create ComboBox for decision columns
            addProductLayout.getChildren().add(new Label("Select Decision Column:")); // Label for ComboBox
            addProductLayout.getChildren().add(decisionColumnComboBox); // Add ComboBox to layout
        }
        decisionColumnComboBox.getItems().clear(); // Clear existing items
        decisionColumnComboBox.getItems().addAll(headers); // Add headers to ComboBox
    }

    // Method to get selected columns
    private static List<String> getSelectedColumns() {
        List<String> selectedColumns = new ArrayList<>();
        for (CheckBox checkBox : headerCheckboxes) {
            if (checkBox.isSelected()) {
                selectedColumns.add(checkBox.getText());
            }
        }
        return selectedColumns;
    }



    private static void removeSplitValueField() {
        if (addProductLayout.getChildren().contains(splitValueField)) {
            // Get the index of splitValueField before removing
            splitValueFieldIndex = addProductLayout.getChildren().indexOf(splitValueField);
            addProductLayout.getChildren().remove(splitValueField);
        }
    }

    private static void addSplitValueField() {
        if (!addProductLayout.getChildren().contains(splitValueField)) {
            // Add it back at the stored index
            if (splitValueFieldIndex != -1 && splitValueFieldIndex <= addProductLayout.getChildren().size()) {
                addProductLayout.getChildren().add(splitValueFieldIndex, splitValueField);
            } else {
                addProductLayout.getChildren().add(splitValueField); // Fallback to adding at the end
            }
        }
    }

    // Method to upload files
    private static void uploadFiles(File trainFile, File testFile, String productName, List<String> selectedColumns, String selectedDecisionColumn) {
        // Make sure to replace the URL and header accordingly
        String apiUrl = UrlValues.UPLOAD_FILE.getUrl();
        String authorization = "Basic YWRtaW46YWRtaW4="; // Your Authorization header

        try {
            // Initialize HTTP connection
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", authorization);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=---Boundary");

            // Prepare the output stream for writing data
            OutputStream outputStream = connection.getOutputStream();
            String boundary = "---Boundary";
            String lineEnd = "\r\n";

            // Write product name
            outputStream.write(("--" + boundary + lineEnd).getBytes(StandardCharsets.UTF_8));
            outputStream.write(("Content-Disposition: form-data; name=\"productName\"" + lineEnd).getBytes(StandardCharsets.UTF_8));
            outputStream.write(("Content-Type: text/plain" + lineEnd).getBytes(StandardCharsets.UTF_8));
            outputStream.write(lineEnd.getBytes(StandardCharsets.UTF_8));
            outputStream.write(productName.getBytes(StandardCharsets.UTF_8));
            outputStream.write(lineEnd.getBytes(StandardCharsets.UTF_8));

            // Write selected columns
            outputStream.write(("--" + boundary + lineEnd).getBytes(StandardCharsets.UTF_8));
            outputStream.write(("Content-Disposition: form-data; name=\"selectedColumns\"" + lineEnd).getBytes(StandardCharsets.UTF_8));
            outputStream.write(("Content-Type: text/plain" + lineEnd).getBytes(StandardCharsets.UTF_8));
            outputStream.write(lineEnd.getBytes(StandardCharsets.UTF_8));
            outputStream.write(String.join(",", selectedColumns).getBytes(StandardCharsets.UTF_8));
            outputStream.write(lineEnd.getBytes(StandardCharsets.UTF_8));

            // Write selected decision column
            outputStream.write(("--" + boundary + lineEnd).getBytes(StandardCharsets.UTF_8));
            outputStream.write(("Content-Disposition: form-data; name=\"selectedDecisionColumn\"" + lineEnd).getBytes(StandardCharsets.UTF_8));
            outputStream.write(("Content-Type: text/plain" + lineEnd).getBytes(StandardCharsets.UTF_8));
            outputStream.write(lineEnd.getBytes(StandardCharsets.UTF_8));
//            outputStream.write(selectedDecisionColumn.getBytes(StandardCharsets.UTF_8));
            outputStream.write(lineEnd.getBytes(StandardCharsets.UTF_8));

            // Write train file
            outputStream.write(("--" + boundary + lineEnd).getBytes(StandardCharsets.UTF_8));
            outputStream.write(("Content-Disposition: form-data; name=\"trainFile\"; filename=\"" + trainFile.getName() + "\"" + lineEnd).getBytes(StandardCharsets.UTF_8));
            outputStream.write(("Content-Type: application/octet-stream" + lineEnd).getBytes(StandardCharsets.UTF_8));
            outputStream.write(lineEnd.getBytes(StandardCharsets.UTF_8));

            // Read and write the train file
            try (FileInputStream fileInputStream = new FileInputStream(trainFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            outputStream.write(lineEnd.getBytes(StandardCharsets.UTF_8));

            // Write test file
            outputStream.write(("--" + boundary + lineEnd).getBytes(StandardCharsets.UTF_8));
            outputStream.write(("Content-Disposition: form-data; name=\"testFile\"; filename=\"" + testFile.getName() + "\"" + lineEnd).getBytes(StandardCharsets.UTF_8));
            outputStream.write(("Content-Type: application/octet-stream" + lineEnd).getBytes(StandardCharsets.UTF_8));
            outputStream.write(lineEnd.getBytes(StandardCharsets.UTF_8));

            // Read and write the test file
            try (FileInputStream fileInputStream = new FileInputStream(testFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            outputStream.write(lineEnd.getBytes(StandardCharsets.UTF_8));

            // End of multipart/form-data
            outputStream.write(("--" + boundary + "--" + lineEnd).getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();

            // Check the server response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                showAlert("Success", "Files uploaded successfully.");
                if(!addProductLayout.getChildren().contains(analyseButton))
                {
                    int ind = addProductLayout.getChildren().indexOf(uploadFilesButton);
                    addProductLayout.getChildren().add(ind+1, analyseButton);
                }

            } else {
                showAlert("Error", "Failed to upload files. Server returned: " + responseCode);
            }
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
