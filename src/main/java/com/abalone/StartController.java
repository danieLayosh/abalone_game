package com.abalone;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class StartController {
    private Stage stage;
    private int startingPlayer;
    private int gameMode;
    private int startingPlayerType;

    @FXML
    private Button btStartNewGame;

    @FXML
    private VBox mainVbox;

    @FXML
    private ToggleGroup whitePlayer;

    @FXML
    private RadioButton whiteComputer;

    @FXML
    private RadioButton whiteHuman;

    @FXML
    private ToggleGroup blackPlayer;

    @FXML
    private RadioButton blackComputer;

    @FXML
    private RadioButton blackHuman;

    @FXML
    private ToggleGroup starts;

    @FXML
    private RadioButton whiteStart;

    @FXML
    private RadioButton blackStart;

    public void initialize() {
        mainVbox.setStyle("-fx-background-image: url('startingBackground.png');");
        whitePlayer.selectToggle(whiteHuman);
        blackPlayer.selectToggle(blackComputer);
        starts.selectToggle(whiteStart);
    }

    @FXML
    void startNewGame(ActionEvent event) {
        // Determine the selection for Player 1
        boolean whiteIsHuman = whiteHuman.isSelected();
        boolean whiteIsComputer = whiteComputer.isSelected();

        // Determine the selection for Player 2
        boolean blackIsHuman = blackHuman.isSelected();
        boolean blackIsComputer = blackComputer.isSelected();

        // Determine who starts first
        boolean whiteStarts = whiteStart.isSelected();
        boolean blackStarts = blackStart.isSelected();

        if (whiteStarts) {
            startingPlayer = 1;
        } else if (blackStarts) {
            startingPlayer = 2;
        }

        if (whiteIsHuman && blackIsHuman) {
            gameMode = 3;
        } else if (whiteIsHuman && blackIsComputer || blackIsHuman && whiteIsComputer) {
            gameMode = 1;
        } else if (whiteIsComputer && blackIsComputer) {
            gameMode = 2;
        }

        if (startingPlayer == 1 && whiteIsComputer) {
            startingPlayerType = 2;
        } else if (startingPlayer == 1 && whiteIsHuman) {
            startingPlayerType = 1;
        } else if (startingPlayer == 2 && blackIsComputer) {
            startingPlayerType = 2;
        } else if (startingPlayer == 2 && blackIsHuman) {
            startingPlayerType = 1;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/primary.fxml"));
            Parent root = loader.load();

            // Get the GUI controller and set the parameters
            GUI guiController = loader.getController();
            guiController.setGameMode(gameMode);
            guiController.setPlayer(startingPlayer);
            guiController.setStartingPlayerType(startingPlayerType);

            guiController.initializeGame();
            // guiController.gameModeSettings();

            // Setting up the scene and stage
            Scene scene = new Scene(root);
            Stage gameStage = new Stage();
            guiController.setStage(gameStage);
            gameStage.setScene(scene);
            gameStage.setTitle("Game");
            gameStage.show();

            // Optional: close the current (start) stage, if desired
            this.stage.close();

        } catch (IOException e) {
            e.printStackTrace();
            // Consider displaying an error message to the user
        }

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
