module com.example.lab01_ui_javafx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.desktop;

    opens com.example.lab01_ui_javafx to javafx.fxml;
    exports com.example.lab01_ui_javafx;
}