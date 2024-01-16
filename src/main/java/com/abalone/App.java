package com.abalone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("primary"), 940, 680);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        InputStream fxmlStream = App.class.getClassLoader().getResourceAsStream(fxml + ".fxml");
        if (fxmlStream == null) {
            throw new IOException("Cannot load resource " + fxml + ".fxml");
        }
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        fxmlLoader.setLocation(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load(fxmlStream);
    }

    public static void main(String[] args) {
        // launch();
        GameBoard gameBoard = new GameBoard();
        gameBoard.printBoardWithCoordinates();
        Computer computer = new Computer(gameBoard, 1);
        computer.calculateCenterDistances();
    }

}