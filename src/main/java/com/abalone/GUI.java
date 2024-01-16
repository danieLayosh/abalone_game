package com.abalone;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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

public class GUI {
    private GameBoard gameBoard = new GameBoard();;
    private int player;
    private List<Cell> marbles;
    private Cell dest;
    private IntegerProperty blue_score;
    private IntegerProperty red_score;

    public GUI() {
        this.marbles = new ArrayList<>(); // Initialize the list here
        this.dest = null;
        this.player = 1;
        this.blue_score = new SimpleIntegerProperty(0);
        this.red_score = new SimpleIntegerProperty(0);
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

                    cellButton.setOnAction(event -> turn(cell));
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                System.err.println("Error linking cell to button: " + e.getMessage());
                // Handle exception or log error
            }
        }
    }

    private void hoverOn(Cell cell) {
        // System.out.println("Mouse entered button");
        if (cell.getState() == player) {
            if (marbles.isEmpty()) {
                cell.getBt().setStyle("-fx-background-color: #6699ff; -fx-text-fill: white;");
            } else {
                marbles.add(cell);
                Move move = new Move(marbles, cell, player);
                if (move.areMarblesInlineAndAdjacent()) {
                    cell.getBt().setStyle("-fx-background-color: #6699ff; -fx-text-fill: white;");
                }
                marbles.remove(cell);
            }
        } else {
            Move move = new Move(marbles, cell, player);
            if (move.isValid()) {
                // show direction on the marbles
                Direction direction = move.getDirectionToDest();
                String playerColor = (player == 1) ? "red" : "blue";
                Image image = null;
                switch (direction) {
                    case LEFT:
                        image = new Image(playerColor + "Left.gif");
                        setMarblesImageDirection(image);
                        break;
                    case RIGHT:
                        image = new Image(playerColor + "Right.gif");
                        setMarblesImageDirection(image);
                        break;
                    case UPLEFT:
                        image = new Image(playerColor + "UpLeft.gif");
                        setMarblesImageDirection(image);
                        break;
                    case UPRIGHT:
                        image = new Image(playerColor + "UpRight.gif");
                        setMarblesImageDirection(image);
                        break;
                    case DOWNLEFT:
                        image = new Image(playerColor + "DownLeft.gif");
                        setMarblesImageDirection(image);
                        break;
                    case DOWNRIGHT:
                        image = new Image(playerColor + "DownRight.gif");
                        setMarblesImageDirection(image);
                        break;
                    default:
                        break;
                }
            }
        }

    }

    private void setMarblesImageDirection(Image image) {
        for (Cell cell : marbles) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(35);
            imageView.setPreserveRatio(true);
            cell.getBt().setGraphic(imageView);
        }
    }

    private void endHover(Cell cell) {
        // System.out.println("Mouse exited button");
        cell.getBt().setStyle("");
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
    }

    private void computerPlay() {
        Computer computer = new Computer(gameBoard, player);
        computer.printAllMoves();
        Move move = computer.computerTurn();
        move.executeMove();

        if (move.getMoveType() == MoveType.OUT_OF_THE_BOARD) {
            red_score.set(red_score.get() + 1);
            Image image = new Image("abalone2.gif");
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(35); // Set height
            imageView.setPreserveRatio(true);
            redHBox.getChildren().add(imageView);
        }

        if (red_score.get() == 6 || blue_score.get() == 6) {
            endGame();
        }

        updateBoard();
        changePlayer();
        marbles.clear();
        System.out.println("Move executed.");

        // move.undoMove();
        // System.out.println("Move undo" + move.toString());
        // updateBoard();
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
                    System.out.println("The marble is not an inline neighbor");
                }
            } else {
                if (marbles.size() < 3) {
                    marbles.add(cell);
                    Move move = new Move(marbles, cell, player);
                    if (move.areMarblesInlineAndAdjacent()) {
                        cellPushed(cell);
                    } else {
                        marbles.remove(cell);
                        System.out.println("The marble is not an inline neighbor");
                    }
                } else {
                    System.out.println("To much marbles");
                    for (Cell cell2 : marbles) {
                        updateCellGUI(cell2);
                    }
                    marbles.clear();
                    marbles.add(cell);
                    cellPushed(cell);
                    System.out.println("Choose again please");
                }
            }
        } else {
            if (cell.getState() == 0 || cell.getState() == (player == 1 ? 2 : 1) && !marbles.isEmpty()) {
                dest = cell;
                Move move = new Move(marbles, cell, player);
                if (move.isValid()) {
                    System.out.println("Move to " + move.getDirectionToDest() + " direction is valid.");
                    MoveType moveType = move.getMoveType();
                    System.out.println("The MoveType is: " + moveType);

                    move.executeMove();
                    updateBoard();
                    if (move.getMoveType() == MoveType.OUT_OF_THE_BOARD) {
                        if (player == 1) {
                            red_score.set(red_score.get() + 1);
                            Image image = new Image("abalone2.gif");
                            ImageView imageView = new ImageView(image);
                            imageView.setFitHeight(35); // Set height
                            imageView.setPreserveRatio(true);
                            redHBox.getChildren().add(imageView);
                        } else {
                            blue_score.set(blue_score.get() + 1);
                            Image image = new Image("abalone1.gif");
                            ImageView imageView = new ImageView(image);
                            imageView.setFitHeight(35); // Set height
                            imageView.setPreserveRatio(true);
                            blueHBox.getChildren().add(imageView);
                        }
                        if (red_score.get() == 6 || blue_score.get() == 6) {
                            endGame();
                        }
                    }
                    for (Cell cell2 : marbles) {
                        updateCellGUI(cell2);
                    }
                    changePlayer();
                    marbles.clear();
                    System.out.println("Move executed.");
                }
            } else {
                for (Cell cell2 : marbles)
                    updateCellGUI(cell2);

                marbles.clear();
                System.out.println("Move is invalid.");
            }
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
        colorAdjust.setBrightness(-0.5); // Adjust this value between -1 and 0 to control darkness
        imageView.setEffect(colorAdjust);
        cell.getBt().setGraphic(imageView);
    }

}
