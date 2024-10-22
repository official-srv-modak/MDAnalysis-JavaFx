package com.modakdev.mdanalysis;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.modakdev.mdanalysis.libraries.UIModuleProcessing;
import com.modakdev.mdanalysis.model.DummyCard;
import com.modakdev.mdanalysis.model.ProductCard;
import com.modakdev.mdanalysis.values.UrlValues;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.modakdev.mdanalysis.model.NewProductScene.initializeAddProductScene;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Create the GridPane as the root container
        GridPane root = new GridPane();
        root.setHgap(15);
        root.setVgap(15);
        root.setPadding(new javafx.geometry.Insets(20));

        // Create the button to add a product
        Button addButton = new Button("Add Product");
        addButton.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px;");

        // Position the button at the top right of the root container
        AnchorPane.setTopAnchor(addButton, 10.0);
        AnchorPane.setRightAnchor(addButton, 10.0);
        addButton.setOnAction(e -> initializeAddProductScene(stage));

        // Add the button as the first element in the GridPane
        root.add(addButton, 0, 0, 2, 1);

        // Add the dummy card as the first card in the grid
        GridPane dummyCard = new GridPane();
        dummyCard.add(DummyCard.intialiseDummyCard(stage), 0, 0);
        root.add(dummyCard, 0, 1, 2, 1); // Place dummy card spanning two columns

        // Dynamic card addition
        int row = 2;
        int col = 0;
        /*for (int i = 0; i < 5; i++) {
            GridPane card = ProductCard.createModelCard(
                    "CustomerChurnPredictor",
                    "89.5%",
                    "customer_train.csv",
                    "customer_test.csv",
                    "This model predicts customer churn based on usage patterns, demographics, and previous interactions. It uses a logistic regression algorithm for binary classification."
            );
            // Add the card to the grid
            root.add(card, col, row);

            // Update column and row indices for the next card
            col++;
            if (col > 2) { // Adjust this value based on how many columns you want
                col = 0;
                row++;
            }
        }*/

        initializeProductCards(root);

        // Create a ScrollPane and add the GridPane to it
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Set up the scene and add it to the stage
        Scene scene = new Scene(scrollPane, 800, 600);
        UIModuleProcessing.addScene("Project overview", scene, stage);
    }

    public static void initializeProductCards(GridPane root) {
        // API URL
        String urlStr = UrlValues.GET_ALL_PRODUCTS.getUrl();
        String authHeader = "Basic YWRtaW46YWRtaW4="; // Authorization header

        new Thread(() -> {
            try {
                // Open connection to the API
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", authHeader);
                connection.setRequestProperty("Content-Type", "application/json");

                // Check response code
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                        // Parse the JSON response
                        JsonObject responseObject = JsonParser.parseReader(reader).getAsJsonObject();
                        JsonArray products = responseObject.getAsJsonArray("products");

                        // Initialize column and row indices for the grid

                        int row = 2;
                        int col = 0;

                        for (int i = 0; i < products.size(); i++) {
                            JsonObject product = products.get(i).getAsJsonObject();

                            // Extract product details
                            String modelName = product.get("name").getAsString();
                            String accuracy = product.get("accuracy").getAsString() + "%";
                            String trainFile = product.get("trainModelPath").getAsString();
                            String testFile = product.get("testModelPath").getAsString();
                            String description = product.get("description").getAsString();
                            String id = product.get("id").getAsString();
//                            String imageUrl = product.get("imageUrl").getAsString();

                            // Create a product card using the details
                            GridPane card = ProductCard.createModelCard(
                                    id,
                                    modelName,
                                    accuracy,
                                    trainFile,
                                    testFile,
                                    description
                            );

                            // Update the UI on the JavaFX Application Thread
                            final int colIndex = col;
                            final int rowIndex = row;
                            Platform.runLater(() -> root.add(card, colIndex, rowIndex));

                            // Update column and row indices for the next card
                            col++;
                            if (col > 2) { // Adjust the number of columns per row as needed
                                col = 0;
                                row++;
                            }
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

    public static void main(String[] args) {
        launch();
    }
}
