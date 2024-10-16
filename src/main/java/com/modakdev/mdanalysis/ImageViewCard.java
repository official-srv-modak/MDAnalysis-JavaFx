package com.modakdev.mdanalysis;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageViewCard {
    public static HBox initialise(String imageUrl, String... values) {
        // Create the image view
        ImageView imageView = new ImageView();
        if (values.length > 0) {
            loadImageFromCurl(imageView, values[0], imageUrl);
        }

        imageView.setFitWidth(320);
        imageView.setFitHeight(240);

        // Add click event to open the image in a new window when clicked
        imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> openZoomedImage(imageView.getImage()));

        // Create the title and description
        Label titleLabel = new Label("Card Title");
        Text descriptionText = new Text("This is a sample description for the card.");

        // Create a VBox for the title and description
        VBox textContainer = new VBox(5); // 5 is the spacing between elements
        textContainer.getChildren().addAll(titleLabel, descriptionText);

        // Create an HBox for the card layout
        HBox card = new HBox(10); // 10 is the spacing between the image and text
        card.getChildren().addAll(imageView, textContainer);

        // Set some padding and styling for the card
        card.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-border-width: 1; "
                + "-fx-border-radius: 5; -fx-background-color: white; -fx-background-radius: 5;");

        return card;
    }

    public static void loadImageFromCurl(ImageView imageView, String trainFileName, String apiUrl) {
        new Thread(() -> {
            try {
                // API endpoint URL
                URL url = new URL(apiUrl);

                // Open connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // JSON body as a string
                String jsonInputString = String.format(
                        "{\"model_name\": \"Banking\", \"train_file_name\": \"%s\"}", trainFileName
                );

                // Write the JSON input to the connection output stream
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Check response code
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the input stream from the connection
                    try (InputStream inputStream = connection.getInputStream()) {
                        Image image = new Image(inputStream);
                        // Set the image on the JavaFX Application Thread
                        Platform.runLater(() -> imageView.setImage(image));
                    }
                } else {
                    System.err.println("Request failed. Response code: " + responseCode);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void openZoomedImage(Image image) {
        if (image == null) return;

        // Create a new stage for the zoomed image
        Stage zoomStage = new Stage();
        zoomStage.initModality(Modality.APPLICATION_MODAL);
        zoomStage.setTitle("Zoomed Image");

        // Create an ImageView with the zoomed image
        ImageView zoomedImageView = new ImageView(image);
        zoomedImageView.setPreserveRatio(true);
        zoomedImageView.setFitWidth(800); // Set a larger width for the zoomed image
        zoomedImageView.setFitHeight(600); // Set a larger height for the zoomed image

        // Enable pinch-to-zoom and scroll zooming
        enableZoom(zoomedImageView);

        // Set up a layout for the zoomed image view
        StackPane zoomPane = new StackPane(zoomedImageView);
        zoomPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);"); // Dark background

        Scene scene = new Scene(zoomPane, 800, 600);
        zoomStage.setScene(scene);
        zoomStage.show();
    }

    private static void enableZoom(ImageView imageView) {
        // Add scroll event handler for zooming with mouse wheel
        imageView.addEventHandler(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            double scaleFactor = (delta > 0) ? 1.1 : 0.9;
            imageView.setScaleX(imageView.getScaleX() * scaleFactor);
            imageView.setScaleY(imageView.getScaleY() * scaleFactor);
            event.consume();
        });

        // Add pinch zoom support (touch gestures)
        imageView.addEventHandler(ZoomEvent.ZOOM, event -> {
            double zoomFactor = event.getZoomFactor();
            imageView.setScaleX(imageView.getScaleX() * zoomFactor);
            imageView.setScaleY(imageView.getScaleY() * zoomFactor);
            event.consume();
        });

        // Add drag event handling to move the image
        imageView.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                imageView.setUserData(new double[]{event.getSceneX(), event.getSceneY()}); // Store initial position
            }
        });

        imageView.setOnMouseDragged(event -> {
            double[] initialPosition = (double[]) imageView.getUserData();
            if (initialPosition != null) {
                double deltaX = event.getSceneX() - initialPosition[0];
                double deltaY = event.getSceneY() - initialPosition[1];
                imageView.setTranslateX(imageView.getTranslateX() + deltaX);
                imageView.setTranslateY(imageView.getTranslateY() + deltaY);
                // Update initial position for next drag event
                imageView.setUserData(new double[]{event.getSceneX(), event.getSceneY()});
            }
        });
    }
}
