package com.abalone;

import java.io.IOException;

// import javafx.scene.web.WebView;
// import javafx.scene.web.WebEngine;
// import javafx.scene.layout.StackPane;
// import javafx.stage.Modality;


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

            // Setting up the scene and stage
            Scene scene = new Scene(root);
            Stage gameStage = new Stage();
            guiController.setStage(gameStage);
            gameStage.setScene(scene);
            gameStage.setTitle("Game");
            gameStage.show();

            this.stage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // public void showRulesVideo() {
    //     // URL or file path to your video
    //         String videoUrl = "path_to_your_video.mp4"; // Adjust this to the path of your video file or URL

    //         // Create a Media object
    //         Media media = new Media(videoUrl);

    //         // Create a MediaPlayer object
    //         MediaPlayer mediaPlayer = new MediaPlayer(media);

    //         // Create a MediaView and set the MediaPlayer
    //         MediaView mediaView = new MediaView(mediaPlayer);

    //         // Make sure the video fits within the window
    //         mediaView.setFitWidth(800); // Set to desired width
    //         mediaView.setFitHeight(450); // Set to desired height
    //         mediaView.setPreserveRatio(true);

    //     // Create a new Stage (window)
    //     Stage videoStage = new Stage();
    //     videoStage.initModality(Modality.APPLICATION_MODAL); // Block input events to other windows
    //     videoStage.setTitle("Game Rules");

    //     // Create a layout and add the MediaView to it
    //     StackPane layout = new StackPane();
    //     layout.getChildren().add(mediaView);

    //     // Create a scene and set it on the stage
    //     Scene scene = new Scene(layout, 800, 450); // Match MediaView size
    //     videoStage.setScene(scene);

    //     // Show the stage
    //     videoStage.show();

    //     // Start playing the video
    //     mediaPlayer.play();

    //     // Optional: Stop the video when the window is closed
    //     videoStage.setOnCloseRequest(event -> mediaPlayer.stop());
    // }
}
