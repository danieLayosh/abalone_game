module com.example {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.abalone to javafx.fxml;
    exports com.abalone;
    exports com.abalone.enums;
}
