package com.modakdev.mdanalysis;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

public class ProductCard {
    public static GridPane createModelCard(String modelName, String accuracy, String trainSet, String testSet, String description) {
        // Create a GridPane
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #000000; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Title label for model name
        Label titleLabel = new Label("Model Name: " + modelName);
        titleLabel.setFont(new Font("Arial", 18));
        titleLabel.setStyle("-fx-font-weight: bold;");
        grid.add(titleLabel, 0, 0, 2, 1);

        // Accuracy label
        Label accuracyLabel = new Label("Accuracy: " + accuracy);
        accuracyLabel.setFont(new Font("Arial", 14));
        grid.add(accuracyLabel, 0, 1);

        // Train set label
        Label trainSetLabel = new Label("Train Set: " + trainSet);
        trainSetLabel.setFont(new Font("Arial", 14));
        grid.add(trainSetLabel, 0, 2);

        // Test set label
        Label testSetLabel = new Label("Test Set: " + testSet);
        testSetLabel.setFont(new Font("Arial", 14));
        grid.add(testSetLabel, 0, 3);

        // Description area
        TextArea descriptionArea = new TextArea(description);
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setEditable(false);
        grid.add(descriptionArea, 0, 4, 2, 1);

        return grid;
    }
}
