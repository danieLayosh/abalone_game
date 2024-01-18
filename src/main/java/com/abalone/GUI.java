package com.abalone;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.abalone.enums.Direction;
import com.abalone.enums.MoveType;

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
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

public class GUI {
    private Stage stage;
    private GameBoard gameBoard = new GameBoard();;
    private int player;
    private List<Cell> marbles;
    private IntegerProperty blue_score;
    private IntegerProperty red_score;
    private Stack<Move> LastTwoMove;

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

                    // Event handler for mouse entering and exiting the button area
                    cellButton.setOnMouseEntered(event -> hoverOn(cell));
                    cellButton.setOnMouseExited(event -> endHover(cell));

                    // cellButton.setOnAction(event -> turn(cell)); // Player VS Compuer
                    cellButton.setOnAction(event -> computerPlay()); // computer vs computer

                    // Create a KeyCodeCombination for Ctrl+Z
                    KeyCombination ctrlZ = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
                    stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                        if (ctrlZ.match(event)) {
                            // Perform your action here, for example:
                            undoMove(); // if you want to trigger the undoMove method
                        }
                    });
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                System.err.println("Error linking cell to button: " + e.getMessage());
                // Handle exception or log error
            }
        }
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
            updateBoard();
        }
    }

    private void handleScoreUpdate(Move move) {
        if (move != null && move.getMoveType() == MoveType.OUT_OF_THE_BOARD) {
            // Adjust the score and update display
            if (move.getPlayer() == 1) {
                red_score.set(red_score.get() - 1);
                updateScoreDisplay(red_score.get(), redHBox, "abalone2.gif");
            } else {
                blue_score.set(blue_score.get() - 1);
                updateScoreDisplay(blue_score.get(), blueHBox, "abalone1.gif");
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
            String playerColor = (player == 1) ? "red" : "blue";
            Image image = null;
            switch (direction) {
                case LEFT:
                    image = new Image(playerColor + "Left.gif");
                    break;
                case RIGHT:
                    image = new Image(playerColor + "Right.gif");
                    break;
                case UPLEFT:
                    image = new Image(playerColor + "UpLeft.gif");
                    break;
                case UPRIGHT:
                    image = new Image(playerColor + "UpRight.gif");
                    break;
                case DOWNLEFT:
                    image = new Image(playerColor + "DownLeft.gif");
                    break;
                case DOWNRIGHT:
                    image = new Image(playerColor + "DownRight.gif");
                    break;
                default:
                    break;
            }
            if (image != null) {
                setMarblesImageDirection(image);
            }
        } else {
            // System.out.println("--------------------------------------");
            // List<Cell> marbles = move.getMarbles();
            // Cell potintiaCell =
            // marbles.get(0).getNeighborInDirection(move.getMarblesDirection());
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
        Image image = new Image("abalone0.gif");

        switch (cell.getState()) {
            case 1:
                image = new Image("abalone1.gif");
                break;
            case 2:
                image = new Image("abalone2.gif");
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

        // gameBoard.printBoardScore();
        // gameBoard.printBoardWithCoordinates();
    }

    public void turn(Cell cell) {
        // if the marble is already pushed remove her
        if (marbles.contains(cell)) {
            marbles.remove(cell);
            updateCellGUI(cell);
        } else if (cell.getState() == player) {
            if (marbles.isEmpty()) {
                marbles.add(cell);
                Move move = new Move(marbles, cell, player);
                if (move.areMarblesInlineAndAdjacent()) {
                    cellPushed(cell);
                } else {
                    marbles.remove(cell);
                    updateCellGUI(cell);
                    System.out.println("The marble is not an inline neighbor.");
                }
            } else {
                if (marbles.size() < 3) {
                    marbles.add(cell);
                    Move move = new Move(marbles, cell, player);
                    if (move.areMarblesInlineAndAdjacent()) {
                        cellPushed(cell);
                    } else {
                        marbles.remove(cell);
                        updateCellGUI(cell);
                        System.out.println("The marble is not an inline neighbor.");
                    }
                } else {
                    System.out.println("To much marbles, Choose again please.");
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
                System.out.println("Move is invalid.");
            }
        }
    }

    /**
     * @param cell
     */
    private void executeTheTurn(Move move) {
        if (move != null && move.isValid()) {

            move.executeMove();

            updateBoard();

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
                }
            }

            changePlayer();
            marbles.clear();
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

    private void endGame() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        if (player == 1) {
            alert.setHeaderText("THE RED PLAYER WON!!!!");
        } else
            alert.setHeaderText("THE BLUE PLAYER WON!!!!");

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

    private void restartGame() {
        this.player = 2;
        this.blue_score = new SimpleIntegerProperty(0);
        this.red_score = new SimpleIntegerProperty(0);
        this.gameBoard = new GameBoard();
        this.marbles.clear();
        this.redHBox.getChildren().clear();
        this.blueHBox.getChildren().clear();

        initialize();
    }

    private void changePlayer() {
        player = (player == 1 ? 2 : 1);
        if (player == 2) {
            computerPlay();
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
                image = new Image("abalone_red_pushed.gif");
                break;
            case 2:
                image = new Image("abalone_blue_pushed.gif");
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

}
