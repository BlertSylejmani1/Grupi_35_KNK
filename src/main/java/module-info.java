module com.smartinventory {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.prefs;
    requires java.sql;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires com.github.librepdf.openpdf;

    opens com.smartinventory to javafx.fxml;
    opens com.smartinventory.controller to javafx.fxml;
    opens com.smartinventory.model to javafx.base;

    exports com.smartinventory;
}
