module com.modakdev.mdanalysis {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.modakdev.mdanalysis to javafx.fxml;
    exports com.modakdev.mdanalysis;
}