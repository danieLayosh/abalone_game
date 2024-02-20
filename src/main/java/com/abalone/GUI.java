package com.abalone;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.abalone.enums.Direction;
import com.abalone.enums.MoveType;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GUI {
    private Stage stage;
    private int gameMode;
    private GameBoard gameBoard;
    private int player;
    private int startPlayer;
    private int startingPlayerType;
    private List<Cell> marbles;
    private IntegerProperty black_score;
    private IntegerProperty white_score;
    private Stack<Move> LastTwoMove;
    private double startX, startY;
    private List<Move> moveHistory = new ArrayList<>();
    private boolean isAnimationRunning = false;
    private SimpleDoubleProperty animationSpeed;
    private ExecutorService executorService;
    private AtomicBoolean gameActive;

    private Timeline gameTimer;
    private Duration timeLimit = Duration.hours(2); // 2 hours limit
    private SimpleStringProperty timeString = new SimpleStringProperty("00:00:00");
    private IntegerProperty elapsedTimeInSeconds = new SimpleIntegerProperty(0); // Track elapsed time in seconds

    public GUI() {
        this.gameBoard = new GameBoard();
        this.marbles = new ArrayList<>(); // Initialize the list here
        this.black_score = new SimpleIntegerProperty(0);
        this.white_score = new SimpleIntegerProperty(0);
        this.LastTwoMove = new Stack<>();
        this.gameMode = -1;
        this.player = -1;
        this.startPlayer = -1;
        this.animationSpeed = new SimpleDoubleProperty(0.7);
        this.executorService = Executors.newSingleThreadExecutor();
        this.gameActive = new AtomicBoolean(false);
    }

    @FXML
    private HBox blackHBox;

    @FXML
    private HBox whiteHBox;

    @FXML
    private Label playerTurn;

    @FXML
    private Label whitePoint;

    @FXML
    private Label blackPoint;

    @FXML
    private Button undoBt;

    @FXML
    private AnchorPane AnchorPaneID;

    @FXML
    private Label timerLable;

    @FXML
    private ScrollBar scrollBar;

    @FXML
    private Button bt0_0, bt0_1, bt0_2, bt0_3, bt0_4,
            bt1_0, bt1_1, bt1_2, bt1_3, bt1_4, bt1_5,
            bt2_0, bt2_1, bt2_2, bt2_3, bt2_4, bt2_5, bt2_6,
            bt3_0, bt3_1, bt3_2, bt3_3, bt3_4, bt3_5, bt3_6, bt3_7,
            bt4_0, bt4_1, bt4_2, bt4_3, bt4_4, bt4_5, bt4_6, bt4_7, bt4_8,
            bt5_0, bt5_1, bt5_2, bt5_3, bt5_4, bt5_5, bt5_6, bt5_7,
            bt6_0, bt6_1, bt6_2, bt6_3, bt6_4, bt6_5, bt6_6,
            bt7_0, bt7_1, bt7_2, bt7_3, bt7_4, bt7_5,
            bt8_0, bt8_1, bt8_2, bt8_3, bt8_4;

    public void initializeGame() {
        whitePoint.textProperty().bind(white_score.asString());
        blackPoint.textProperty().bind(black_score.asString());
        timerLable.textProperty().bind(timeString);
        scrollBar.valueProperty().bindBidirectional(animationSpeed);

        // undoBt.setOnAction(event -> undoMove());
        undoBt.setText("RESTART");
        undoBt.setOnAction(event -> restartGame());

        // Iterate over all cells in the game board and link them to buttons
        for (Cell cell : gameBoard.getCells()) {
            try {
                String cellId = "bt" + cell.getX() + "_" + cell.getY();
                Field field = getClass().getDeclaredField(cellId);
                field.setAccessible(true);
                Button cellButton = (Button) field.get(this);
                if (cellButton != null) {
                    cell.setBt(cellButton);
                    updateCellGUI(cell);
                    addDragFunctionality(cell.getBt());

                    if (gameMode != 2) {
                        cellButton.setOnAction(event -> turn(cell)); // Player VS Compuer
                        // Event handler for mouse entering and exiting the button area
                        cellButton.setOnMouseEntered(event -> hoverOn(cell));
                        cellButton.setOnMouseExited(event -> endHover(cell));
                    } else if (gameMode == 2) {
                        cellButton.setOnAction(event -> computerPlay()); // computer vs computer
                    }

                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                System.err.println("Error linking cell to button: " + e.getMessage());
            }
        }
        // Setup and start the game timer
        setupGameTimer();
        gameModeSettings();
    }

    private void setupGameTimer() {
        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateGameTime()));
        gameTimer.setCycleCount(Timeline.INDEFINITE);
        gameTimer.playFromStart();
    }

    private void updateGameTime() {
        elapsedTimeInSeconds.set(elapsedTimeInSeconds.get() + 1); // Increment elapsed time
        int elapsedHours = elapsedTimeInSeconds.get() / 3600;
        int elapsedMinutes = (elapsedTimeInSeconds.get() % 3600) / 60;
        int elapsedSeconds = elapsedTimeInSeconds.get() % 60;

        // Update the time string to reflect the new time
        timeString.set(String.format("%02d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds));

        // Convert total elapsed time in seconds to Duration for comparison
        Duration duration = Duration.seconds(elapsedTimeInSeconds.get());
        if (duration.greaterThanOrEqualTo(timeLimit)) {
            gameTimer.stop();
            endGame(0);
        }
    }

    public void gameModeSettings() {
        updatePlayerTurnUI();

        // computer vs computer
        if (gameMode == 2) {
            gameActive.set(true);
            System.out.println("Computer vs Computer");
            executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                while (gameActive.get()) {
                    if (gameActive.get() == false) {
                        System.out.println("gameActive.get() == false");
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!isAnimationRunning) {
                        Platform.runLater(() -> {
                            computerPlay();
                        });
                    }
                }
            });

        } else {
            // human vs computer
            if (gameMode == 1) {
                if (startingPlayerType == 2) {
                    System.out.println("player is 2, computer play first");
                    computerPlay();
                }
                System.out.println("Human vs Computer");
                playerTurn.setText(null);
                playerTurn.setStyle("-fx-background-color: black;");
                playerTurn.setVisible(true);
            } else {
                // human vs human
                if (gameMode == 3) {
                    System.out.println("Human vs Human");
                }
            }
        }
    }

    private void addDragFunctionality(Button button) {
        button.setOnMousePressed(event -> {
            startX = event.getSceneX();
            startY = event.getSceneY();
        });

        button.setOnMouseReleased(event -> {
            double endX = event.getSceneX();
            double endY = event.getSceneY();
            Direction direction = determineDirection(startX, startY, endX, endY);
            if (direction != null) {
                handleDragDirection(button, direction);
            }
            startX = startY = 0;
        });
    }

    private Direction determineDirection(double startX, double startY, double endX, double endY) {
        double deltaX = endX - startX;
        double deltaY = endY - startY;
        double threshold = 10.0; // Adjust as needed

        if (Math.hypot(deltaX, deltaY) < threshold) {
            return null; // No significant movement
        }

        // Normalize the deltas to determine the primary direction of movement
        double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));

        if (angle < 0) {
            angle += 360;
        }

        // Determine direction based on angle
        if ((angle > 330) || (angle <= 30)) {
            return Direction.RIGHT;
        } else if ((angle > 30) && (angle <= 90)) {
            return Direction.DOWNRIGHT;
        } else if ((angle > 90) && (angle <= 150)) {
            return Direction.DOWNLEFT;
        } else if ((angle > 150) && (angle <= 210)) {
            return Direction.LEFT;
        } else if ((angle > 210) && (angle <= 270)) {
            return Direction.UPLEFT;
        } else if ((angle > 270) && (angle <= 330)) {
            return Direction.UPRIGHT;
        } else {
            return null; // Catch any unexpected cases
        }
    }

    private void handleDragDirection(Button button, Direction direction) {
        System.out.println(direction);
        if (!marbles.isEmpty()) {
            Cell edgeCellInDirection = marbles.get(0);
            Cell nextCell = edgeCellInDirection.getNeighborInDirection(direction);
            while (nextCell != null && nextCell.getState() == player) {
                nextCell = nextCell.getNeighborInDirection(direction);
            }

            Cell dest = null;
            if (nextCell != null && nextCell.getState() != player) {
                dest = nextCell;
            }

            if (dest != null) {
                Move move = new Move(marbles, dest, player);
                move.isValid();
                MoveType moveType = move.getMoveType();
                System.out.println(dest.formatCoordinate());
                if (moveType == MoveType.SIDESTEP) {
                    showTemporaryMessage("Cant do a sideStep with draging.");
                } else {
                    turn(dest);
                }
            } else {
                showTemporaryMessage("This move is not valid. Please try again.");
            }

        } else {
            int x = button.getId().charAt(2) - '0';
            int y = button.getId().charAt(4) - '0';
            if (gameBoard.getCellAt(x, y) != null && gameBoard.getCellAt(x, y).getState() == player) {
                marbles.add(gameBoard.getCellAt(x, y));
                Move move = new Move(marbles, marbles.get(0).getNeighborInDirection(direction), player);
                if (move.isValid()) {
                    executeTheTurn(move);
                } else {
                    showTemporaryMessage("This move is not valid. Please try again.");
                }
            }
        }
    }

    private void showTemporaryMessage(String message) {
        Label label = new Label(message);
        label.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.5); -fx-text-fill: white; -fx-padding: 5px; -fx-font-size: 20px; -fx-background-radius: 5px;");

        Popup popup = new Popup();
        popup.getContent().add(label);

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), label);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.seconds(0.5)); // Message visible for 2 seconds before fading
        fadeOut.setOnFinished(event -> popup.hide());

        // Show the popup and then set its position
        popup.show(stage); // 'stage' should be your primary stage
        popup.setX(stage.getX() + stage.getWidth() / 2 - label.getWidth() / 2);
        popup.setY(stage.getY() + 100);

        popup.show(stage); // 'stage' should be your primary stage
        fadeOut.play();
    }

    private void undoMove() {
        Move computerMove = null, playerMove = null;
        if (!LastTwoMove.empty()) {
            computerMove = LastTwoMove.pop();
            if (!LastTwoMove.empty())
                playerMove = LastTwoMove.pop();
        }

        if (computerMove != null && playerMove != null) {
            handleScoreUpdate(computerMove);
            handleScoreUpdate(playerMove);

            computerMove.undoMove();
            playerMove.undoMove();

            updateBoard(computerMove);
            updateBoard(playerMove);
        }

    }

    private void handleScoreUpdate(Move move) {
        if (move != null && move.getMoveType() == MoveType.OUT_OF_THE_BOARD) {
            // Adjust the score and update display
            if (move.getPlayer() == 1) {
                white_score.set(white_score.get() - 1);
                updateScoreDisplay(white_score.get(), whiteHBox, ("/blackAndWhite/black_ball.png"));
            } else {
                black_score.set(black_score.get() - 1);
                updateScoreDisplay(black_score.get(), blackHBox, "/blackAndWhite/white_ball.png");
            }
        }
    }

    private void hoverOn(Cell cell) {
        Move move = new Move(marbles, cell, player);

        if (cell.getState() == player) {
            if (marbles.isEmpty()) {
                cell.getBt().setStyle(
                        "-fx-border-width: 4px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,100), 6, 0, 0, 0); -fx-opacity: 0.75; -fx-cursor: hand;");
            } else {
                marbles.add(cell);
                if (move.areMarblesInlineAndAdjacent()) {
                    cell.getBt().setStyle(
                            "-fx-border-width: 4px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,100), 6, 0, 0, 0); -fx-opacity: 0.75; -fx-cursor: hand;");
                    showDirectionOnMarbles(move);
                }
                marbles.remove(cell);
            }
        } else {
            if (move.isValid()) {
                cell.getBt().setStyle(
                        " -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,100), 6, 0, 0, 0); -fx-opacity: 0.75; -fx-cursor: hand;");
                showDirectionOnMarbles(move);
            }
        }
    }

    private void showDirectionOnMarbles(Move move) {
        Direction direction = move.getDirectionToDest();
        if (direction != null) { // Add null check
            String playerColor = (player == 1) ? "white" : "black";
            Image image = null;
            switch (direction) {
                case LEFT:
                    image = new Image(
                            getClass().getResourceAsStream("/blackAndWhite/arrows/" + playerColor + "_Left.png"));
                    break;
                case RIGHT:
                    image = new Image(
                            getClass().getResourceAsStream("/blackAndWhite/arrows/" + playerColor + "_Right.png"));
                    break;
                case UPLEFT:
                    image = new Image(
                            getClass().getResourceAsStream("/blackAndWhite/arrows/" + playerColor + "_UpLeft.png"));
                    break;
                case UPRIGHT:
                    image = new Image(
                            getClass().getResourceAsStream("/blackAndWhite/arrows/" + playerColor + "_UpRight.png"));
                    break;
                case DOWNLEFT:
                    image = new Image(
                            getClass().getResourceAsStream("/blackAndWhite/arrows/" + playerColor + "_DownLeft.png"));
                    break;
                case DOWNRIGHT:
                    image = new Image(
                            getClass()
                                    .getResourceAsStream(("/blackAndWhite/arrows/" + playerColor + "_DownRight.png")));
                    break;
                default:
                    break;
            }
            if (image != null) {
                setMarblesImageDirection(image);
            }
        } else {

        }
    }

    private void setMarblesImageDirection(Image image) {
        for (Cell cell : marbles) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(35);
            imageView.setPreserveRatio(true);
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(-0.25); // Adjust this value between -1 and 0 to control darkness
            imageView.setEffect(colorAdjust);
            cell.getBt().setGraphic(imageView);
        }
    }

    private void endHover(Cell cell) {
        // System.out.println("Mouse exited button");
        cell.getBt().setStyle("");

        // Clear directional indicators from all marbles
        for (Cell marble : marbles) {
            cellPushed(marble); // Removes the image set during hover
            marble.getBt().setStyle(""); // Resets any additional styling if needed
        }
    }

    public void updateCellGUI(Cell cell) {
        Image image = new Image(getClass().getResourceAsStream("/blackAndWhite/hole.png"));
        ColorAdjust colorAdjust = new ColorAdjust();

        switch (cell.getState()) {
            case 1:
                image = new Image(getClass().getResourceAsStream("/blackAndWhite/white_ball.png"));
                break;
            case 2:
                colorAdjust.setBrightness(-0.3); // Adjust this value between -1 and 0 to control darkness
                image = new Image(getClass().getResourceAsStream("/blackAndWhite/black_ball.png"));
                break;
            default:
                break;
        }
        ImageView imageView = new ImageView(image);

        imageView.setEffect(colorAdjust);
        imageView.setFitWidth(35);
        imageView.setPreserveRatio(true);
        cell.getBt().setGraphic(imageView);

    }

    public void updateBoard(Move move) {
        for (Cell cell : move.getMarblesUsed().keySet()) {
            updateCellGUI(cell);
        }
    }

    private void computerPlay() {
        Computer computer = new Computer(gameBoard, player);
        Move move = computer.computerTurn();

        executeTheTurn(move);
        System.out.println("Computer Move executed.");
    }

    public void turn(Cell cell) {
        // if the marble is already pushed remove her
        if (marbles.contains(cell)) {
            marbles.remove(cell);
            updateCellGUI(cell);
        } else if (cell != null && cell.getState() == player) {
            if (marbles.isEmpty()) {
                marbles.add(cell);
                Move move = new Move(marbles, cell, player);
                if (move.areMarblesInlineAndAdjacent()) {
                    cellPushed(cell);
                } else {
                    marbles.remove(cell);
                    updateCellGUI(cell);
                    showTemporaryMessage("The marble is not an inline neighbor.");
                }
            } else {
                if (marbles.size() < 3) {
                    marbles.add(cell);
                    Move move = new Move(marbles, cell, player);
                    if (move.areMarblesInlineAndAdjacent()) {
                        cellPushed(cell);
                    } else {
                        if (marbles.size() == 2 && inlineWithJump(marbles.get(0), marbles.get(1))) {
                            cellPushed(cell);
                        } else {
                            marbles.remove(cell);
                            updateCellGUI(cell);
                            showTemporaryMessage("The marble is not an inline neighbor.");
                        }
                    }
                } else {
                    showTemporaryMessage("To much marbles, Choose again please.");
                    for (Cell marble : marbles) {
                        updateCellGUI(marble);
                    }
                    marbles.clear();
                    marbles.add(cell);
                    cellPushed(cell);
                }
            }
        } else {
            if (cell.getState() == 0 || cell.getState() == (player == 1 ? 2 : 1) && !marbles.isEmpty()) {
                Move move = new Move(marbles, cell, player);
                if (move.isValid()) {
                    executeTheTurn(move);
                    System.out.println("Player Move executed.");
                } else
                    System.out.println("Move is invalid.");
            } else {
                marbles.clear();
                showTemporaryMessage("Move is invalid.");
            }
        }
    }

    private boolean inlineWithJump(Cell cell, Cell cell2) {
        for (Direction direction : cell.getNeighborsMap().values()) {
            if (cell.getNeighborInDirection(direction) == cell2
                    .getNeighborInDirection(Move.oppositeDirection(direction))) {
                if (cell.getNeighborInDirection(direction).getState() == player) {
                    marbles.add(cell.getNeighborInDirection(direction));
                    cellPushed(cell.getNeighborInDirection(direction));
                    return true;
                }
            }
        }
        return false;
    }

    private void executeTheTurn(Move move) {
        if (move != null && move.isValid()) {

            moveHistory.add(move);
            if (isLoopingSequenceDetected()) {
                endGame(1);
                return;
            }

            move.executeMove();

            animateMove(move);

            LastTwoMove.add(move);// For the undo move button

            if (move.getMoveType() == MoveType.OUT_OF_THE_BOARD) {
                if (player == 1) {
                    white_score.set(white_score.get() + 1);
                    updateScoreDisplay(white_score.get(), whiteHBox, "/blackAndWhite/black_ball.png");
                } else {
                    black_score.set(black_score.get() + 1);
                    updateScoreDisplay(black_score.get(), blackHBox, "/blackAndWhite/white_ball.png");
                }

                if (white_score.get() == 6 || black_score.get() == 6) {
                    endGame(0);
                }
            }

            changePlayer();
            marbles.clear();
        }
    }

    private void updateScoreDisplay(int score, HBox hbox, String imageFile) {
        Platform.runLater(() -> {
            hbox.getChildren().clear(); // Clear existing images
            for (int i = 0; i < score; i++) {
                ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(imageFile)));
                imageView.setFitHeight(35); // Set height
                imageView.setPreserveRatio(true);
                hbox.getChildren().add(imageView); // Add new image for each point in score
            }
        });
    }

    private void endGame(int whyEnded) {

        if (gameActive.get() == true) {
            System.out.println(Thread.currentThread().getName() + " - Game ended.");
            gameActive.set(false);
            System.out.println("Game ended.");
            gameTimer.stop();
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                if (whyEnded == 0) {
                    alert.setTitle("Game Over");
                    if (player == 2) {
                        alert.setHeaderText("THE WHITE PLAYER WON!!!!");
                    } else {
                        alert.setHeaderText("THE BLACK PLAYER WON!!!!");
                    }
                } else {
                    alert.setTitle("Game Over");
                    alert.setHeaderText("Game ended due to repetitive loop - TIE");
                    alert.setContentText("The players have entered into a repetitive loop of moves. The game is over.");

                    // // If using ExecutorService
                    // if (executorService != null && !executorService.isShutdown()) {
                    // executorService.shutdownNow();
                    // }

                }

                alert.setContentText("Do you want to play another game?");

                ButtonType buttonTypeYes = new ButtonType("Yes");
                ButtonType buttonTypeNo = new ButtonType("No, exit");

                alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

                alert.showAndWait().ifPresent(response -> {
                    if (response == buttonTypeYes) {
                        restartGame();
                    } else if (response == buttonTypeNo) {
                        System.exit(0); // or close the window
                    }
                });
            });
        } else {
            Platform.runLater(() -> {
                try {
                    Thread.sleep(100); // Small delay to allow ongoing animations to complete
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                }
                // endGame(whyEnded);
            });
        }
    }

    private void restartGame() {
        gameTimer.stop();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/start.fxml"));
        try {
            Parent root = loader.load();
            StartController startController = loader.getController();
            Scene scene = new Scene(root, 840, 680);
            Stage startStage = new Stage();
            startController.setStage(startStage);
            startStage.setScene(scene);
            startStage.setTitle("Game");
            startStage.show();
            System.out.println("Game restarted.");
            this.stage.close();
        } catch (IOException e) {
            System.out.println("Error loading start.fxml");
            e.printStackTrace();
        }

    }

    private void changePlayer() {
        player = (player == 1 ? 2 : 1);
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(100), // Check every 100 milliseconds
                ae -> {
                    if (!isAnimationRunning) {
                        continueChangePlayer();
                    }
                }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void continueChangePlayer() {
        if ((player == startPlayer && gameMode == 1 && startingPlayerType == 2)) {
            if (gameMode == 2 && player == 2 || gameMode == 1) {
                computerPlay();
            }
        }
        updatePlayerTurnUI();
    }

    private void updatePlayerTurnUI() {
        Platform.runLater(() -> {
            if (player == 1) {
                playerTurn.setText("White Turn");
                playerTurn.setTextFill(Color.WHITE);
                playerTurn.setStyle("-fx-background-color: #000000;");
            } else if (player == 2) {
                playerTurn.setText("Black Turn");
                playerTurn.setTextFill(Color.BLACK);
                playerTurn.setStyle("-fx-background-color: #FFFFFF;");
            }
        });
    }

    private void cellPushed(Cell cell) {
        Image image = null;
        switch (cell.getState()) {
            case 1:
                image = new Image(getClass().getResourceAsStream("/blackAndWhite/white_pushed.png"));
                break;
            case 2:
                image = new Image(getClass().getResourceAsStream("/blackAndWhite/black_pushed.png"));
                break;
        }
        ImageView imageView = new ImageView(image);

        imageView.setFitWidth(35);
        imageView.setPreserveRatio(true);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(0.25); // Adjust this value between -1 and 0 to control darkness
        imageView.setEffect(colorAdjust);
        cell.getBt().setGraphic(imageView);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private boolean isLoopingSequenceDetected() {
        int historySize = moveHistory.size();
        if (historySize < 10) { // Minimum size for a loop, can be adjusted
            return false;
        }

        // Check for a loop by analyzing the history
        for (int sequenceSize = 2; sequenceSize <= historySize / 2; sequenceSize++) {
            boolean sequenceRepeated = true;
            for (int i = 0; i < sequenceSize; i++) {
                // Compare the sequence with the sequence before it
                if (!moveHistory.get(historySize - sequenceSize - i).equals(moveHistory.get(historySize - i - 1))) {
                    sequenceRepeated = false;
                    break;
                }
            }
            if (sequenceRepeated) {
                return true;
            }
        }
        return false;
    }

    // private void endGameDueToLoop_TIE() {
    // gameTimer.stop();
    // Platform.runLater(() -> {
    // Alert alert = new Alert(AlertType.INFORMATION);
    // alert.setTitle("Game Over");
    // alert.setHeaderText("Game ended due to repetitive loop - TIE");
    // alert.setContentText("The players have entered into a repetitive loop of
    // moves. The game is over.");
    // alert.showAndWait();

    // restartGame();
    // // Additional logic to restart or close the game
    // });
    // }

    private void animateMove(Move move) {
        ParallelTransition parallelTransition = new ParallelTransition();
        isAnimationRunning = true;

        if (move.getMoveType() == MoveType.SIDESTEP) {
            Direction direction = move.getDirectionToDest();
            for (Cell cell : move.getMarbles()) {
                Cell destinationCell = cell.getNeighborInDirection(direction);
                animateMarbleMovement(cell, destinationCell, parallelTransition);

            }
        } else {

            move.undoMove();

            Direction direction = move.getDirectionToDest();
            Direction oppositeDirection = Move.oppositeDirection(direction);

            Cell destPlayerNegibor = null;
            if (direction != null)
                destPlayerNegibor = move.getDestCell().getNeighborInDirection(oppositeDirection);

            Cell FirstDestCell = move.getDestCell();
            Cell secondDestCell = FirstDestCell.getNeighborInDirection(direction);
            Cell emptyCellAfterSecondDestCell = null;
            if (secondDestCell != null && secondDestCell.getState() == ((player == 1) ? 2 : 1)) {
                emptyCellAfterSecondDestCell = secondDestCell.getNeighborInDirection(direction);
            } else {
                if (secondDestCell != null && secondDestCell.getState() == 0)
                    emptyCellAfterSecondDestCell = secondDestCell;
                else {
                    emptyCellAfterSecondDestCell = null;
                }
            }

            ArrayList<Cell> marbles = new ArrayList<>();

            if (FirstDestCell.getState() != 0) {
                if (secondDestCell != null && secondDestCell.getState() == ((player == 1) ? 2 : 1)) {
                    if (emptyCellAfterSecondDestCell != null) { // meanning its not out of the board
                        marbles.add(emptyCellAfterSecondDestCell);
                    }
                    marbles.add(secondDestCell);
                } else {
                    marbles.add(emptyCellAfterSecondDestCell);
                }
            }

            marbles.add(FirstDestCell);
            marbles.add(destPlayerNegibor);

            Cell playerNextMarble = null;
            if (oppositeDirection != null && destPlayerNegibor != null) {
                playerNextMarble = destPlayerNegibor.getNeighborInDirection(oppositeDirection);

                if (playerNextMarble != null && playerNextMarble.getState() == player
                        && move.getMarbles().contains(playerNextMarble)) {
                    marbles.add(playerNextMarble);
                }

                if (playerNextMarble != null)
                    playerNextMarble = playerNextMarble.getNeighborInDirection(oppositeDirection);
                if (playerNextMarble != null && playerNextMarble.getState() == player
                        && move.getMarbles().contains(playerNextMarble)) {
                    marbles.add(playerNextMarble);
                }
            }
            move.executeMove();

            if (marbles.size() == 2) {
                animateMarbleMovement(marbles.get(1), move.getDestCell(), parallelTransition);
            } else {

                int i = 1;
                for (Cell cell : marbles) {
                    if (i < marbles.size()) {
                        Cell startCell = marbles.get(i);
                        Cell endCell = cell;
                        if (endCell != null)
                            animateMarbleMovement(startCell, endCell, parallelTransition);
                        i++;
                    }
                }
            }
        }

        for (Cell cell : marbles) {
            if (cell != null)
                if (cell.getState() == 0)
                    updateCellGUI(cell);
        }
        parallelTransition.play();

        parallelTransition.setOnFinished(event -> {
            updateBoard(move);
            isAnimationRunning = false;
            if ((gameMode == 1 && player != startPlayer && startingPlayerType == 1) || (gameMode == 2)) {
                System.out.println(gameMode + " " + player + " " + isAnimationRunning);
                computerPlay();
                System.out.println("computerPlay");
            }
        });

    }

    private void animateMarbleMovement(Cell startCell, Cell endCell, ParallelTransition parallelTransition) {
        Button startButton = startCell.getBt();
        Button endButton = endCell.getBt();

        ImageView marbleView = new ImageView(((ImageView) startButton.getGraphic()).getImage());
        // startCell.setState(prev_state);

        marbleView.setFitWidth(startButton.getGraphic().getLayoutBounds().getWidth());
        marbleView.setFitHeight(startButton.getGraphic().getLayoutBounds().getHeight());

        // Translate button coordinates up the hierarchy to the AnchorPane
        Point2D startPointInAnchorPane = startButton.localToScene(startButton.getLayoutBounds().getWidth() / 2,
                startButton.getLayoutBounds().getHeight() / 2);
        Point2D endPointInAnchorPane = endButton.localToScene(endButton.getLayoutBounds().getWidth() / 2,
                endButton.getLayoutBounds().getHeight() / 2);

        // Translate scene coordinates back to AnchorPane local coordinates
        Point2D startPoint = AnchorPaneID.sceneToLocal(startPointInAnchorPane);
        Point2D endPoint = AnchorPaneID.sceneToLocal(endPointInAnchorPane);

        // Position the ImageView on the board
        marbleView.setX(startPoint.getX() - marbleView.getFitWidth() / 2);
        marbleView.setY(startPoint.getY() - marbleView.getFitHeight() / 2);
        AnchorPaneID.getChildren().add(marbleView);

        // Create and play the animation
        TranslateTransition transition = new TranslateTransition(Duration.seconds(animationSpeed.get()),
                marbleView);
        transition.setToX(endPoint.getX() - startPoint.getX());
        transition.setToY(endPoint.getY() - startPoint.getY());
        transition.setOnFinished(event -> {
            AnchorPaneID.getChildren().remove(marbleView);
            endButton.setGraphic(startButton.getGraphic());
        });
        int state = startCell.getState();
        startCell.setState(0);
        updateCellGUI(startCell);
        startCell.setState(state);
        state = endCell.getState();
        endCell.setState(0);
        updateCellGUI(endCell);
        endCell.setState(state);

        parallelTransition.getChildren().add(transition);
    }

    public void setPlayer(int player) {
        this.player = player;
        this.startPlayer = player;
    }

    public void setStartingPlayerType(int startingPlayerType) {
        this.startingPlayerType = startingPlayerType;
    }

    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }
}
