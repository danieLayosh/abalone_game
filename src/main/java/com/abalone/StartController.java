package com.abalone;

import java.io.IOException;
import java.net.URL;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class StartController {
    private Stage stage;
    private int startingPlayer;
    private int gameMode;
    private int startingPlayerType;
    private boolean isVideoPlaying = false;

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

    @FXML
    private Button infoBt;

    public void initialize() {
        mainVbox.setStyle("-fx-background-image: url('startingBackground.png');");
        whitePlayer.selectToggle(whiteHuman);
        blackPlayer.selectToggle(blackComputer);
        starts.selectToggle(whiteStart);
        infoBt.setOnAction(event -> {
            if (isVideoPlaying == false) {
                Platform.runLater(() -> {
                    showVideoOnStart();
                });
            } else {
                System.out.println("Video is already playing");
            }
        });
    }

    private void showVideoOnStart() {
        try {
            // Obtain the URL to the resource
            URL resourceUrl = getClass().getResource("/How To Play Abalone.mp4");
            if (resourceUrl != null) {
                // Convert the URL to a URI string
                String videoUri = resourceUrl.toURI().toString();
                Media media = new Media(videoUri);
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                MediaView mediaView = new MediaView(mediaPlayer);

                mediaView.setPreserveRatio(true);
                mediaView.setFitHeight(gameMode == 3 ? 740 : 480);
                mediaPlayer.setAutoPlay(true);
                isVideoPlaying = true;

                StackPane root = new StackPane();
                root.getChildren().add(mediaView);

                Scene videoScene = new Scene(root, 740, 480); // Adjust the size as needed

                Stage videoStage = new Stage();
                videoStage.setScene(videoScene);
                videoStage.setTitle("Video");
                videoStage.show();

                // Handle mouse click to toggle play and pause
                mediaView.setOnMouseClicked(event -> {
                    if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                        mediaPlayer.pause();
                    } else {
                        mediaPlayer.play();
                    }
                });

                // Handle keyboard events for control
                videoScene.setOnKeyPressed(event -> {
                    switch (event.getCode()) {
                        case SPACE:
                            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                                mediaPlayer.pause();
                            } else {
                                mediaPlayer.play();
                            }
                            break;
                        case RIGHT:
                            mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(5)));
                            break;
                        case LEFT:
                            mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(5)));
                            break;
                        case ESCAPE:
                            videoStage.close();
                            mediaPlayer.stop();
                            isVideoPlaying = false;
                            break;
                        default:
                            break;
                    }
                });

                // Optional: Close the video window when the video finishes
                mediaPlayer.setOnEndOfMedia(() -> {
                    videoStage.close();
                    mediaPlayer.stop();
                    isVideoPlaying = false;
                });

                btStartNewGame.setOnAction(event -> {
                    videoStage.close();
                    mediaPlayer.stop();
                    isVideoPlaying = false;
                    startNewGame(event);
                });

                videoStage.setOnCloseRequest(event -> {
                    mediaPlayer.stop();
                    isVideoPlaying = false;
                });

            } else {
                System.out.println("Resource URL is null. Check the path to your video file.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading video: " + e.getMessage());
        }
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
        } else if (whiteIsHuman && blackIsComputer || blackIsHuman &&
                whiteIsComputer) {
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
}
