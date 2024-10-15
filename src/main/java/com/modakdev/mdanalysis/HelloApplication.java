package com.modakdev.mdanalysis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

import static com.modakdev.mdanalysis.NewProductScene.initializeAddProductScene;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Create the root container (AnchorPane)
        // Create the root container (AnchorPane)
        AnchorPane root = new AnchorPane();

        // Initialize the dummy card
        VBox card = DummyCard.intialiseDummyCard(stage);

        // Create a button to add a product
        Button addButton = new Button("Add Product");
        addButton.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px;");

        // Position the button at the top right of the parent
        AnchorPane.setTopAnchor(addButton, 10.0);
        AnchorPane.setRightAnchor(addButton, 10.0);


        addButton.setOnAction(e -> initializeAddProductScene(stage));


        // Add the button to the root container
        root.getChildren().addAll(card, addButton);

        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("Project Overview");
        stage.setScene(scene);
        stage.setMaximized(true); // Set the window maximized
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}