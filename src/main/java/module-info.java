module com.abalone {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.media;
    
    opens com.abalone to javafx.fxml;

    exports com.abalone;
    exports com.abalone.enums;
}
