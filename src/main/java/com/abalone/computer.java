package com.abalone;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.abalone.enums.Direction;

public class Computer {
    private GameBoard gameBoard;
    private int player;
    Map<Cell, Map<Cell, Direction>> board;
    Set<Cell> myCells;

    public Computer(GameBoard gameBoard, int player) {
        this.gameBoard = gameBoard;
        this.board = gameBoard.getBoard();
        this.myCells = board.keySet();
        // myCells();
        this.player = player;
    }

    // gets all the player marbles in a set
    // private void myCells() {
    //     Iterator<Cell> iterator = myCells.iterator();
    //     while (iterator.hasNext()) {
    //         Cell cell = iterator.next();
    //         if (cell.getState() != player) {
    //             iterator.remove(); // Use iterator's remove method
    //         }
    //     }
    // }

    public void calculateCenterDistances() {
        Cell center = new Cell(4, 4, 0); // Assuming EMPTY is a constant for an empty cell
        center.convertToCube();

        for (Cell cell : board.keySet()) {
            cell.convertToCube();
            int distance = cell.distanceFrom(center);
            System.out.println(cell.formatCoordinate() + " = " + distance);
        }
    }

}
