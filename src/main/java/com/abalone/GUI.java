package com.abalone;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class GUI {
    private Map<String, Button> buttonsMap = new HashMap<>();
    private GameBoard gameBoard = new GameBoard();
    @FXML
    private Button 
            bt0_0, bt0_1, bt0_2, bt0_3, bt0_4,
            bt1_0, bt1_1, bt1_2, bt1_3, bt1_4, bt1_5,
            bt2_0, bt2_1, bt2_2, bt2_3, bt2_4, bt2_5, bt2_6,
            bt3_0, bt3_1, bt3_2, bt3_3, bt3_4, bt3_5, bt3_6, bt3_7,
            bt4_0, bt4_1, bt4_2, bt4_3, bt4_4, bt4_5, bt4_6, bt4_7, bt4_8,
            bt5_0, bt5_1, bt5_2, bt5_3, bt5_4, bt5_5, bt5_6, bt5_7,
            bt6_0, bt6_1, bt6_2, bt6_3, bt6_4, bt6_5, bt6_6,
            bt7_0, bt7_1, bt7_2, bt7_3, bt7_4, bt7_5,
            bt8_0, bt8_1, bt8_2, bt8_3, bt8_4;

    public void initialize() {

        // Iterate over all cells in the game board and link them to buttons
        for (Cell cell : gameBoard.getCells()) {
            try {
                String cellId = "bt" + cell.getX() + "_" + cell.getY();
                Field field = getClass().getDeclaredField(cellId);
                field.setAccessible(true);
                Button cellButton = (Button) field.get(this);
                if (cellButton != null) {
                    cell.setBt(cellButton);
                    buttonsMap.put(cellId, cellButton);

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

                    imageView.setFitHeight(40); // Set the height of the image
                    imageView.setFitWidth(40); // Set the width of the image
                    imageView.setPreserveRatio(true);
                    cellButton.setGraphic(imageView);

                    cellButton.setOnAction(event -> handleCellClick(cell));
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                System.err.println("Error linking cell to button: " + e.getMessage());
                // Handle exception or log error
            }
        }
    }

    private void handleCellClick(Cell cell) {
        // Implement your logic here, for example:
        System.out.println("Clicked on cell " + cell.formatCoordinate());
        
    }



}
