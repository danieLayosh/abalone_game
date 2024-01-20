package com.abalone;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class StartController {
    private Stage stage;

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
            // Code for Player 1 as a human player
        } else if (player1IsComputer) {
            // Code for Player 1 as a computer
        }

        if (player2IsHuman) {
            // Code for Player 2 as a human player
        } else if (player2IsComputer) {
            // Code for Player 2 as a computer
        }

        if (startWithWhite) {
            // Code for starting with white
        } else if (startWithBlack) {
            // Code for starting with black
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
