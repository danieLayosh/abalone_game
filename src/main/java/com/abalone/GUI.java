package com.abalone;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.abalone.enums.Direction;
import com.abalone.enums.MoveType;

import javafx.animation.FadeTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GUI {
    private Stage stage;
    private GameBoard gameBoard = new GameBoard();;
    private int player;
    private List<Cell> marbles;
    private IntegerProperty blue_score;
    private IntegerProperty red_score;
    private Stack<Move> LastTwoMove;
    private double startX, startY;
    private List<Move> moveHistory = new ArrayList<>();

    public GUI() {
        this.marbles = new ArrayList<>(); // Initialize the list here
        this.player = 1;
        this.blue_score = new SimpleIntegerProperty(0);
        this.red_score = new SimpleIntegerProperty(0);
        this.LastTwoMove = new Stack<>();
    }

    @FXML
    private HBox blueHBox;

    @FXML
    private HBox redHBox;

    @FXML
    private Label playerTurn;

    @FXML
    private Label redPoint;

    @FXML
    private Label bluePoint;

    @FXML
    private Button undoBt;

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

    public void initialize() {
        redPoint.textProperty().bind(red_score.asString());
        bluePoint.textProperty().bind(blue_score.asString());
        undoBt.setOnAction(event -> undoMove());

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

                    // Event handler for mouse entering and exiting the button area
                    cellButton.setOnMouseEntered(event -> hoverOn(cell));
                    cellButton.setOnMouseExited(event -> endHover(cell));

                    cellButton.setOnAction(event -> turn(cell)); // Player VS Compuer
                    // cellButton.setOnAction(event -> computerPlay()); // computer vs computer

                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                System.err.println("Error linking cell to button: " + e.getMessage());
                // Handle exception or log error
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
        }
        updateBoard();
    }

    private void handleScoreUpdate(Move move) {
        if (move != null && move.getMoveType() == MoveType.OUT_OF_THE_BOARD) {
            // Adjust the score and update display
            if (move.getPlayer() == 1) {
                red_score.set(red_score.get() - 1);
                updateScoreDisplay(red_score.get(), redHBox, "/blackAndWhite/black_ball.png");
            } else {
                blue_score.set(blue_score.get() - 1);
                updateScoreDisplay(blue_score.get(), blueHBox, "/blackAndWhite/white_ball.png");
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
                            getClass().getResourceAsStream(("/blackAndWhite/arrows/" + playerColor + "_DownRight.png")));
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
        // Image image = new Image("abalone0.gif");
        Image image = new Image(getClass().getResourceAsStream("/blackAndWhite/hole.png"));

        switch (cell.getState()) {
            case 1:
                // image = new Image("abalone1.gif");
                image = new Image(getClass().getResourceAsStream("/blackAndWhite/white_ball.png"));
                break;
            case 2:
                // image = new Image("abalone2.gif");
                image = new Image(getClass().getResourceAsStream("/blackAndWhite/black_ball.png"));
                break;
            default:
                break;
        }
        ImageView imageView = new ImageView(image);

        imageView.setFitWidth(35);
        imageView.setPreserveRatio(true);
        cell.getBt().setGraphic(imageView);

    }

    public void updateBoard() {
        for (Cell cell : gameBoard.getCells()) {
            updateCellGUI(cell);
        }

        // for (Cell cell : move.getMarblesUsed().keySet()) {
        // updateCellGUI(cell);
        // }
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
                            if (inlineWithJump(marbles.get(0), marbles.get(1))) {
                                cellPushed(cell);
                            } else {
                                updateCellGUI(cell);
                                showTemporaryMessage("The marble is not an inline neighbor.");
                            }
                        }
                    }
                } else {
                    showTemporaryMessage("To much marbles, Choose again please.");
                    marbles.clear();
                    updateBoard();
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

    /**
     * @param cell
     */
    private void executeTheTurn(Move move) {
        if (move != null && move.isValid()) {

            moveHistory.add(move);
            if (isLoopingSequenceDetected()) {
                endGameDueToLoop_TIE();
                // restartGame(); // For testing
                return;
            }

            move.executeMove();

            updateBoard();// For testing turn off

            LastTwoMove.add(move);// For the undo move button

            if (move.getMoveType() == MoveType.OUT_OF_THE_BOARD) {
                if (player == 1) {
                    red_score.set(red_score.get() + 1);
                    updateScoreDisplay(red_score.get(), redHBox, "abalone2.gif");
                } else {
                    blue_score.set(blue_score.get() + 1);
                    updateScoreDisplay(blue_score.get(), blueHBox, "abalone1.gif");
                }

                if (red_score.get() == 6 || blue_score.get() == 6) {
                    endGame();
                    // endGameCPTesting(); // for testing
                }
            }

            changePlayer();
            marbles.clear();
            // computerVsComputerTesting();// for testing
        }
    }

    private void updateScoreDisplay(int score, HBox hbox, String imageFile) {
        hbox.getChildren().clear(); // Clear existing images
        for (int i = 0; i < score; i++) {
            ImageView imageView = new ImageView(new Image(imageFile));
            imageView.setFitHeight(35); // Set height
            imageView.setPreserveRatio(true);
            hbox.getChildren().add(imageView); // Add new image for each point in score
        }
    }

    private void computerVsComputerTesting() {
        computerPlay();// for the loop
        endGameCPTesting();
    }

    private void endGame() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        if (player == 1) {
            alert.setHeaderText("THE RED PLAYER WON!!!!");
        } else {
            alert.setHeaderText("THE BLUE PLAYER WON!!!!");
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
    }

    private void endGameCPTesting() {
        if (player == 1) {
            System.out.println("THE RED PLAYER WON!!!!");
        } else {
            System.out.println("THE BLUE PLAYER WON!!!!");
        }
        restartGame();

    }

    private void restartGame() {
        this.player = 2;
        this.blue_score = new SimpleIntegerProperty(0);
        this.red_score = new SimpleIntegerProperty(0);
        this.gameBoard = new GameBoard();
        this.marbles.clear();
        this.redHBox.getChildren().clear();
        this.blueHBox.getChildren().clear();
        this.LastTwoMove.clear();
        this.moveHistory.clear();
        this.blueHBox.getChildren().clear();
        this.redHBox.getChildren().clear();
        this.bluePoint.textProperty().bind(blue_score.asString());
        this.redPoint.textProperty().bind(red_score.asString());
        initialize();
    }

    private void changePlayer() {
        player = (player == 1 ? 2 : 1);
        if (player == 2) {
            // computerPlay();
            playerTurn.setText("Blue Turn");
            playerTurn.setTextFill(Color.BLUE);
        }
        if (player == 1) {
            playerTurn.setText("Red Turn");
            playerTurn.setTextFill(Color.RED);
        }
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
        colorAdjust.setBrightness(-0.25); // Adjust this value between -1 and 0 to control darkness
        imageView.setEffect(colorAdjust);
        cell.getBt().setGraphic(imageView);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private boolean isLoopingSequenceDetected() {
        int historySize = moveHistory.size();
        if (historySize < 6) { // Minimum size for a loop, can be adjusted
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

    private void endGameDueToLoop_TIE() {
        // Handle the end game logic for a loop scenario
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Game ended due to repetitive loop - TIE");
        alert.setContentText("The players have entered into a repetitive loop of moves. The game is over.");
        alert.showAndWait();
        // Additional logic to restart or close the game
    }

}
