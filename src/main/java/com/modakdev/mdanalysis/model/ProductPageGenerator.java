package com.modakdev.mdanalysis.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.modakdev.mdanalysis.libraries.UIModuleProcessing;
import com.modakdev.mdanalysis.values.UrlValues;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.modakdev.mdanalysis.libraries.UIModuleProcessing.getCorrelationalMatrix;
import static com.modakdev.mdanalysis.model.TestModelPage.showEncodedColumnsPage;
import static com.modakdev.mdanalysis.values.UrlValues.CORR_MAT_IMG;

public class ProductPageGenerator {
    static Button testModel;
    public static void getProducts(Stage primaryStage, String id) {
        try {
            // Fetch the product data using Gson
            JsonObject response = fetchProductData(id);

            // Create the GridPane layout for product details
            GridPane grid = new GridPane();
            grid.setPadding(new Insets(15));
            grid.setHgap(10);
            grid.setVgap(10);
            testModel = new Button("Test Model");

            // Populate the UI with product data (ignoring internal fields)
            createProductPage(primaryStage, response.getAsJsonObject("product"), grid, id);

            // Create a VBox layout for the Back button
            VBox layout = new VBox(10);
            layout.setPadding(new Insets(10));

            // Create a Back button
            Button backButton = new Button("Back");
            backButton.setOnAction(event -> {
                UIModuleProcessing.goBack(primaryStage);
            }); // Close the current window, or replace with navigation logic


            // Add the Back button to the layout
            layout.getChildren().addAll(backButton, testModel);

            // Create a ScrollPane and set the GridPane as its content
            ScrollPane scrollPane = new ScrollPane(grid);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            // Set the VBox as the root layout and add ScrollPane to it
            layout.getChildren().add(scrollPane);

            // Create the Scene with the VBox layout
            Scene scene = new Scene(layout);
            UIModuleProcessing.addScene("Model Details", scene, primaryStage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Function to create the product page UI
    public static void createProductPage(Stage primaryStage, JsonObject product, GridPane grid, String id) {
        // Create labels and fields for single text values
        addNonEditableTextField(grid, "Product ID", product.get("id").getAsString(), 0);
        addNonEditableTextField(grid, "Product Name", product.get("name").getAsString(), 1);
        addNonEditableTextField(grid, "Accuracy", product.get("accuracy").getAsString(), 2);
        addNonEditableTextField(grid, "Decision Column", product.get("decisionColumn").getAsString(), 3);
        addNonEditableTextField(grid, "Train Model Path", product.get("trainModelPath").getAsString(), 4);
        addNonEditableTextField(grid, "Test Model Path", product.get("testModelPath").getAsString(), 5);
        addNonEditableTextField(grid, "Description", product.get("description").getAsString(), 6);



        // Fetch and display the correlation matrix image
        String imageUrl = CORR_MAT_IMG.getUrl() + id;
//        ImageView imageView = new ImageView();

        String trainFileTxt = product.get("trainModelPath").getAsString().split("/")[product.get("trainModelPath").getAsString().split("/").length -1];
        String productName = product.get("name").getAsString();
        String query = "Give me a brief bullet points about the product i have sent to you in json format.";
        query += getCorrelationalMatrix(trainFileTxt, productName,"", 100);
        query += product;
        query += "Don't display the json object i passed in the response. Just give your insight and analysis";
        HBox imageViewCard = ImageViewCard.initialise(UrlValues.IMAGE_URL.getUrl(), trainFileTxt, query, "Analysing Product...");


        try {
            // Download the image from the URL
/*            Image image = fetchImageFromUrl(imageUrl);
            imageView.setImage(image);*/
        } catch (Exception e) {
            e.printStackTrace();
            // If the image can't be loaded, do not set any image and keep the ImageView empty
        }

//        imageView.setFitWidth(800);
//        imageView.setPreserveRatio(true);
        grid.add(new Label("Correlation Matrix Image:"), 0, 7);
        grid.add(imageViewCard, 1, 7);




        // Handle the encoded columns as a bullet point list
        if (product.has("encodedColumns")) {
            JsonArray encodedColumns = product.getAsJsonArray("encodedColumns");
            TextArea encodedColumnsArea = new TextArea();
            encodedColumnsArea.setEditable(false);
            encodedColumnsArea.setWrapText(true);

            // Set preferred height for the TextArea
            encodedColumnsArea.setMinHeight(300);

            StringBuilder encodedColumnsText = new StringBuilder();
            for (JsonElement column : encodedColumns) {
                encodedColumnsText.append("• ").append(column.getAsString()).append("\n");
            }
            encodedColumnsArea.setText(encodedColumnsText.toString());

            grid.add(new Label("Encoded Columns:"), 0, 8);
            grid.add(encodedColumnsArea, 1, 8);

            testModel.setOnAction(event -> {
                showEncodedColumnsPage(primaryStage, encodedColumns, String.valueOf(product), product.get("decisionColumn").getAsString(),product.get("name").getAsString(), product.get("accuracy").getAsString(), product.get("trainModelPath").getAsString(), product.get("testModelPath").getAsString());
            });
        }

    }

    public static Image fetchImageFromUrl(String imageUrl) throws Exception {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.connect();

        // Get the input stream of the image and return it as a JavaFX Image
        InputStream inputStream = connection.getInputStream();
        return new Image(inputStream);
    }

    // Helper function to add a non-editable text field to the grid
    public static void addNonEditableTextField(GridPane grid, String label, String value, int row) {
        Label lbl = new Label(label + ":");
        TextField textField = new TextField(value);
        textField.setEditable(false);
        grid.add(lbl, 0, row);
        grid.add(textField, 1, row);
    }

    // Function to fetch product data using Gson
    public static JsonObject fetchProductData(String id) throws Exception {
        String apiUrl = UrlValues.GET_PRODUCT.getUrl() + id;
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("x-request-id", "b16df981-eb65-48e1-8562-77db64e4c0ff");
        conn.setRequestProperty("Authorization", "Basic YWRtaW46YWRtaW4=");

        // Parse the response using Gson
        InputStreamReader reader = new InputStreamReader(conn.getInputStream());
        JsonElement jsonElement = JsonParser.parseReader(reader);
        return jsonElement.getAsJsonObject();
    }
}
