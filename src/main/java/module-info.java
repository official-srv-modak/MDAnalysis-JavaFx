module com.modakdev.mdanalysis {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.jsobject;
    requires com.google.gson;


    opens com.modakdev.mdanalysis to javafx.fxml;
    exports com.modakdev.mdanalysis;
}