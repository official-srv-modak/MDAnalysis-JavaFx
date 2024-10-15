package com.modakdev.mdanalysis;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class DummyCard {
    public static VBox intialiseDummyCard(Stage stage){
        AnchorPane root = new AnchorPane();

        // Create a VBox for the card layout
        VBox card = new VBox(15); // Spacing between elements is 15
        card.setStyle("-fx-padding: 20; -fx-background-color: #ffffff; "
                + "-fx-border-color: #cccccc; -fx-border-width: 1; "
                + "-fx-border-radius: 10; -fx-background-radius: 10;");

        // Set max width to match the parent width and add some margin
        card.setMaxWidth(800); // You can adjust this to set a maximum width
        card.setPrefWidth(Double.MAX_VALUE); // To allow it to expand within the margins

        // Create Labels and Text for the card details
        Label companyTitle = new Label("ABC Company");
        companyTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-background-color: #000000; -fx-text-fill: white; -fx-padding: 10;");

        Label projectName = new Label("Project Name: PredictIQ: Intelligent Customer Segmentation");
        projectName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label numberOfModels = new Label("Number of Models: 5");
        numberOfModels.setStyle("-fx-font-size: 16px;");

        Text projectDescription = new Text("Project Description: The PredictIQ project focuses on developing a comprehensive machine learning solution for customer segmentation in the retail industry. The goal is to create accurate customer profiles based on purchasing behavior, demographics, and online engagement, enabling targeted marketing strategies and improving customer retention. The project leverages advanced machine learning models to analyze vast datasets, predict customer preferences, and recommend personalized experiences. PredictIQ aims to help retailers optimize their marketing campaigns and increase conversion rates by understanding customers at a deeper level.");
        projectDescription.setWrappingWidth(750); // Set wrapping width to fit within the card

        Label projectOwners = new Label("Project Owners: John Doe, Jane Doe");
        projectOwners.setStyle("-fx-font-size: 14px; -fx-font-style: italic;");

        // Add all elements to the card
        card.getChildren().addAll(companyTitle, projectName, numberOfModels, projectDescription, projectOwners);

        // Add the card to the root container (AnchorPane)
        root.getChildren().add(card);
        AnchorPane.setTopAnchor(card, 50.0);
        AnchorPane.setLeftAnchor(card, 50.0);
        AnchorPane.setRightAnchor(card, 50.0);

        // Create the scene and set it in the stage
        return card;

    }
}
