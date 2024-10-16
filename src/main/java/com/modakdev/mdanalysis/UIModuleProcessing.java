package com.modakdev.mdanalysis;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;


import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javafx.scene.layout.Region.USE_PREF_SIZE;


public abstract class UIModuleProcessing {

    static BackStackInfo backStackInfo = new BackStackInfo();

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    public static boolean validateFields(Node... nodes) {
        for (Node node : nodes) {
            if (node instanceof TextInputControl) {
                TextInputControl inputControl = (TextInputControl) node;
                if (inputControl==null || inputControl.getText() == null || inputControl.getText().isEmpty()) {
                    return false;
                }
            } else if (node instanceof ComboBoxBase) {
                ComboBoxBase<?> comboBox = (ComboBoxBase<?>) node;
                if (comboBox.getValue() == null) {
                    return false;
                }
            }
        }
        return true;
    }



    public static void getSuccessBox(String message, String ... val)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(String.format(message, val));
        alert.getButtonTypes().clear();
        alert.getButtonTypes().add(ButtonType.OK);
        alert.showAndWait();
    }

    public static void getConfirmationBox(String message, Runnable onConfirmation, String ... val) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(String.format(message, val));

        ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.NO);

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeYes) {
            // If user clicks Yes, execute the provided action
            if (onConfirmation != null) {
                onConfirmation.run();
            }
        }
    }

    public static String getDirectoryPicker(Stage primaryStage)
    {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory to export the file");

        File selectedDirectory = directoryChooser.showDialog(primaryStage);

        if (selectedDirectory != null) {
            System.out.println("Selected Directory: " + selectedDirectory.getAbsolutePath());
        } else {
            System.out.println("No directory selected.");
        }
        if(selectedDirectory!=null)
            return selectedDirectory.getAbsolutePath();
        else
            return "";
    }


    public static void addScene(String title, Scene scene, Stage primaryStage){
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
        backStackInfo.titleStack.add(title);
        backStackInfo.sceneStack.add(scene);

    }

    public static Scene popScene()
    {
        backStackInfo.titleStack.pop();
        return backStackInfo.sceneStack.pop();
    }

    public static void goBack(Stage primaryStage)
    {
        backStackInfo.sceneStack.pop();
        backStackInfo.titleStack.pop();

        Scene scene = backStackInfo.sceneStack.pop();
        String title = backStackInfo.titleStack.pop();

        addScene(title, scene, primaryStage);

    }

    public static Label increaseFontSize(Label textField, double fontSize) {
        textField.setStyle("-fx-font-size: " + fontSize + "pt;"); // Set font size
        return textField;
    }

}
