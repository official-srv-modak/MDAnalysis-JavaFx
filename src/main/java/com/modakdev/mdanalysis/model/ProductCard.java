package com.modakdev.mdanalysis.model;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import static com.modakdev.mdanalysis.model.ProductPageGenerator.getProducts;

public class ProductCard {

    public static GridPane createModelCard(Stage primaryStage, String id, String modelName, String accuracy, String trainSet, String testSet, String description) {
        // Create a GridPane
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #000000; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Set up column constraints to push the button to the right
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS); // Allow the second column to grow

        grid.getColumnConstraints().addAll(col1, col2);

        // ID label
        Label idLabel = new Label("ID: " + id);
        idLabel.setFont(new Font("Arial", 14));
        grid.add(idLabel, 0, 0); // Adding ID at the top

        // Title label for model name
        Label titleLabel = new Label("Model Name: " + modelName);
        titleLabel.setFont(new Font("Arial", 18));
        titleLabel.setStyle("-fx-font-weight: bold;");
        grid.add(titleLabel, 0, 1, 2, 1);

        // View Model button
        Button viewModelButton = new Button("View Model");
        viewModelButton.setStyle("-fx-font-size: 12px;");
        viewModelButton.setMinWidth(100);  // Set minimum width to prevent truncation
        viewModelButton.setPrefWidth(120); // Set preferred width for better appearance
        grid.add(viewModelButton, 1, 0); // Adding button at the top right
        GridPane.setHalignment(viewModelButton, javafx.geometry.HPos.RIGHT); // Align button to the right

        viewModelButton.setOnAction(actionEvent -> {
            getProducts(primaryStage, id);
        });

        // Accuracy label
        Label accuracyLabel = new Label("Accuracy: " + accuracy);
        accuracyLabel.setFont(new Font("Arial", 14));
        grid.add(accuracyLabel, 0, 2);

        // Train set label
        Label trainSetLabel = new Label("Train Set: " + trainSet);
        trainSetLabel.setFont(new Font("Arial", 14));
        grid.add(trainSetLabel, 0, 3);

        // Test set label
        Label testSetLabel = new Label("Test Set: " + testSet);
        testSetLabel.setFont(new Font("Arial", 14));
        grid.add(testSetLabel, 0, 4);

        // Description area
        TextArea descriptionArea = new TextArea(description);
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setEditable(false);
        grid.add(descriptionArea, 0, 5, 2, 1);

        return grid;
    }
}
