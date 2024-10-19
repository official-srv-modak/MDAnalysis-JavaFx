module com.modakdev.mdanalysis {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.jsobject;
    requires com.google.gson;


    opens com.modakdev.mdanalysis to javafx.fxml;
    exports com.modakdev.mdanalysis;
    exports com.modakdev.mdanalysis.model;
    opens com.modakdev.mdanalysis.model to javafx.fxml;
    exports com.modakdev.mdanalysis.values;
    opens com.modakdev.mdanalysis.values to javafx.fxml;
    exports com.modakdev.mdanalysis.libraries;
    opens com.modakdev.mdanalysis.libraries to javafx.fxml;
}