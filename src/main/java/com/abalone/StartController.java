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
    private int whitePlayerType;
    private int blackPlayerType;
    private int startingPlayer;

    @FXML
    private ToggleGroup Player1;

    @FXML
    private ToggleGroup Player2;

    @FXML
    private Button btStartNewGame;

    @FXML
    private RadioButton rdC1;

    @FXML
    private RadioButton rdC2;

    @FXML
    private RadioButton rdP1;

    @FXML
    private RadioButton rdP2;

    @FXML
    private RadioButton rdStartBlack;

    @FXML
    private RadioButton rdStartWhite;

    @FXML
    private ToggleGroup starts;

    @FXML
    private VBox mainVbox;

    public void initialize() {
        mainVbox.setStyle("-fx-background-image: url('startingBackground.png');");

    }

    @FXML
    void startNewGame(ActionEvent event) {
        // Determine the selection for Player 1
        boolean player1IsHuman = rdP1.isSelected();
        boolean player1IsComputer = rdC1.isSelected();

        // Determine the selection for Player 2
        boolean player2IsHuman = rdP2.isSelected();
        boolean player2IsComputer = rdC2.isSelected();

        // Determine who starts first
        boolean startWithWhite = rdStartWhite.isSelected();
        boolean startWithBlack = rdStartBlack.isSelected();

        // Now use these boolean values to proceed with the game setup
        if (player1IsHuman) {
            whitePlayerType = 1;
        } else if (player1IsComputer) {
            whitePlayerType = 2;
        }

        if (player2IsHuman) {
            blackPlayerType = 1;
        } else if (player2IsComputer) {
            blackPlayerType = 2;
        }

        if (startWithWhite) {
            startingPlayer = 1;
        } else if (startWithBlack) {
            startingPlayer = 2;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/primary.fxml"));
            Parent root = loader.load();

            // Get the GUI controller and set the parameters
            GUI guiController = loader.getController();
            // guiController.setPlayerTypes(whitePlayerType, blackPlayerType);
            // guiController.setStartingPlayer(startingPlayer);
            // guiController.initialize(whitePlayerType, blackPlayerType, startingPlayer);

            // Setting up the scene and stage
            Scene scene = new Scene(root);
            Stage gameStage = new Stage();
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
