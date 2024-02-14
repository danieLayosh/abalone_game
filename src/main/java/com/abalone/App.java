package com.abalone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("start", stage), 840, 680 );
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml, Stage stage) throws IOException {
        scene.setRoot(loadFXML(fxml, stage));
    }

    private static Parent loadFXML(String fxml, Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/" + fxml + ".fxml"));
        loader.setControllerFactory(param -> {
            if (param == StartController.class) {
                StartController controller = new StartController();
                controller.setStage(stage); // Set the stage here
                return controller;
            }
            // Default behavior for controller creation:
            try {
                return param.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e); // Handle this exception
            }
        });
        return loader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }

}