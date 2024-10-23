package com.modakdev.mdanalysis.model;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.modakdev.mdanalysis.libraries.UIModuleProcessing.*;
import static com.modakdev.mdanalysis.values.UrlValues.*;

public class ImageViewCard {
    private static Thread chatResponseThread; // Thread for handling chat response
//    private static TextFlow descriptionTextArea; // Reference to the TextArea
    private static TextArea descriptionTextArea; // Reference to the TextArea
    private static  Button stopButton;

    public static HBox initialise(String imageUrl, String... values) {
        // Create the image view
        // value[0] - corr_mat url
        // value[1] - query
        ImageView imageView = new ImageView();
        if (values.length > 0) {
            loadImageFromCurl(imageView, values[0], imageUrl);
        }

        imageView.setFitWidth(600);
        imageView.setFitHeight(500);

        // Add click event to open the image in a new window when clicked
        imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> openZoomedImage(imageView.getImage()));

        // Create the title
        Label titleLabel = new Label("AI Recommendations");

        stopButton = new Button("Cancel Analysis");


        // Create a TextArea for the description
        descriptionTextArea = new TextArea();
        descriptionTextArea.setEditable(false); // Make it read-only
        descriptionTextArea.setWrapText(true); // Enable wrapping
//
        // Set styles for the TextArea
        descriptionTextArea.setStyle(
                AI_CHAT_STYLE
        );


        // Load the chat response into the TextArea
        if(values.length > 1 && values[1] != null && !values[1].isBlank() && values[2]!=null && !values[2].isBlank())
            loadChatResponse(values[1], ANALYSIS_CHAT_URL_FLASK.getUrl(), descriptionTextArea, stopButton, values[2]);
        else
            loadChatResponse("write a code to print # in pyramid and explain me", ANALYSIS_CHAT_URL_FLASK.getUrl(), descriptionTextArea, stopButton,"Analysing...");

        // Add a click event to the TextArea to open it in a larger scrollable window
        descriptionTextArea.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> openDescriptionWindow());
        // Add a scroll event handler to allow scrolling the TextArea
//        descriptionTextArea.setOnScroll(event -> {
//            descriptionTextArea.setScrollTop(descriptionTextArea.getScrollTop() - event.getDeltaY());
//        });

        // Create a VBox for the title and description
        VBox textContainer = new VBox(5); // 5 is the spacing between elements
        textContainer.getChildren().addAll(titleLabel, stopButton, descriptionTextArea);

        // Set the preferred width and height to 50% of the parent container
        descriptionTextArea.setPrefWidth(640);
        descriptionTextArea.setPrefHeight(480);

        // Set the minimum width and height to avoid collapsing
        descriptionTextArea.setMinWidth(640); // Minimum width to ensure it doesn't collapse too much
        descriptionTextArea.setMinHeight(480); // Minimum height to ensure sufficient space


        // Create an HBox for the card layout
        HBox card = new HBox(10); // 10 is the spacing between the image and text
        card.getChildren().addAll(imageView, textContainer);

        // Set textContainer width to 50% of the parent container
        textContainer.setStyle("-fx-pref-width: 50%;");

        /*// Set some padding and styling for the card
        card.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-border-width: 1; "
                + "-fx-border-radius: 5; -fx-background-color: white; -fx-background-radius: 5;");*/

        return card;
    }

    private static void openDescriptionWindow() {
        // Create a new stage for the description
        Stage descriptionStage = new Stage();
        descriptionStage.initModality(Modality.APPLICATION_MODAL);
        descriptionStage.setTitle("AI recommendations");

        // Create a TextArea for the description
        TextArea descriptionArea = new TextArea(descriptionTextArea.getText());
        descriptionArea.setEditable(false); // Make it read-only
        descriptionArea.setWrapText(true); // Enable wrapping
        descriptionArea.setStyle(AI_CHAT_STYLE);

        /*// Set styles for the TextArea
        descriptionArea.setStyle(
                "-fx-font-family: 'Courier New'; " + // Monospaced font
                        "-fx-font-size: 12px; " + // Font size
                        "-fx-background-color: #f7f7f7; " + // Light background color
                        "-fx-border-color: #ccc; " + // Border color
                        "-fx-border-width: 1; " + // Border width
                        "-fx-padding: 5;" + // Padding
                        "-fx-text-fill: #333;" // Text color
        );*/

        // Create a ScrollPane to make the TextArea scrollable
        ScrollPane scrollPane = new ScrollPane(descriptionArea);
        scrollPane.setFitToWidth(true); // Allow the scroll pane to fit to the width of the stage
        scrollPane.setFitToHeight(true); // Allow the scroll pane to fit to the height of the stage

        // Set vertical and horizontal scrollbar policies
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // Always show vertical scrollbar
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Show horizontal scrollbar as needed

        // Set a preferred height for the ScrollPane to allow scrolling
        scrollPane.setPrefHeight(400); // Set a preferred height to trigger scrolling if necessary

        // Set up the scene with the scroll pane
        Scene scene = new Scene(scrollPane, 800, 600); // Set preferred size
        descriptionStage.setScene(scene);

        // Start updating the new window with streaming data
        startUpdatingDescriptionArea(descriptionArea);

        // Show the description window
        descriptionStage.show();
    }


    private static void startUpdatingDescriptionArea(TextArea descriptionArea) {
        // Start a new thread to update the TextArea in the new window
        chatResponseThread = new Thread(() -> {
            try {
                while (true) { // Keep the thread running
                    Platform.runLater(() -> {
                        // Get the new text from the source TextArea
                        String newText = descriptionTextArea.getText();
                        double scrollPos = descriptionArea.getScrollTop(); // Save current scroll position

                        // Only update if the new text is different to avoid unnecessary updates
                        if (!descriptionArea.getText().equals(newText)) {
                            descriptionArea.setText(newText); // Set new text
                            descriptionArea.setScrollTop(scrollPos); // Restore scroll position
                        }
                    });

                    // Pause for a short time before checking for new data
                    Thread.sleep(1); // Adjust this value as needed
                }
            } catch (InterruptedException e) {
                // Thread was interrupted, handle accordingly
                Thread.currentThread().interrupt();
            }
        });
        chatResponseThread.setDaemon(true); // Make the thread a daemon so it doesn't block app exit
        chatResponseThread.start(); // Start the thread
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
        // Set initial scale limits
        double minScale = 1.0; // Minimum zoom level (no zoom out beyond original size)
        double maxScale = 5.0; // Maximum zoom level (e.g., 5x original size)

        // Set initial scale to 1 (default)
        imageView.setScaleX(1);
        imageView.setScaleY(1);

        imageView.setOnScroll((ScrollEvent event) -> {
            double scaleFactor = 1.1;
            if (event.getDeltaY() < 0) {
                scaleFactor = 1 / scaleFactor;
            }

            // Calculate new scale values
            double newScaleX = imageView.getScaleX() * scaleFactor;
            double newScaleY = imageView.getScaleY() * scaleFactor;

            // Apply limits
            if (newScaleX >= minScale && newScaleX <= maxScale) {
                imageView.setScaleX(newScaleX);
                imageView.setScaleY(newScaleY);
            }
        });

        imageView.setOnZoom((ZoomEvent event) -> {
            double newScaleX = imageView.getScaleX() * event.getZoomFactor();
            double newScaleY = imageView.getScaleY() * event.getZoomFactor();

            // Apply limits
            if (newScaleX >= minScale && newScaleX <= maxScale) {
                imageView.setScaleX(newScaleX);
                imageView.setScaleY(newScaleY);
            }
        });

        // Mouse drag event for moving the image
        imageView.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                imageView.setUserData(new double[]{event.getSceneX(), event.getSceneY()}); // Store the initial position
            }
        });

        imageView.setOnMouseDragged(event -> {
            double[] initialPosition = (double[]) imageView.getUserData();
            if (initialPosition != null) {
                double deltaX = event.getSceneX() - initialPosition[0];
                double deltaY = event.getSceneY() - initialPosition[1];
                imageView.setTranslateX(imageView.getTranslateX() + deltaX);
                imageView.setTranslateY(imageView.getTranslateY() + deltaY);
                imageView.setUserData(new double[]{event.getSceneX(), event.getSceneY()}); // Update position
            }
        });
    }
}
