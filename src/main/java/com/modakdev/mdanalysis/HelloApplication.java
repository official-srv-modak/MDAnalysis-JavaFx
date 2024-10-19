package com.modakdev.mdanalysis;

import com.modakdev.mdanalysis.libraries.UIModuleProcessing;
import com.modakdev.mdanalysis.model.DummyCard;
import com.modakdev.mdanalysis.model.ProductCard;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

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
        for (int i = 0; i < 5; i++) {
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
        }

        // Create a ScrollPane and add the GridPane to it
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Set up the scene and add it to the stage
        Scene scene = new Scene(scrollPane, 800, 600);
        UIModuleProcessing.addScene("Project overview", scene, stage);
    }

    public static void main(String[] args) {
        launch();
    }
}
