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
        this.player = player;
        myCells();
    }

    // gets all the player marbles that are neighbors to at least one of the
    // player's cells
    private void myCells() {
        Iterator<Cell> iterator = myCells.iterator();
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            // Keep the cell if it's a neighbor of a player's cell and is either empty or
            // belongs to the opponent
            if (!isNeighborOfPlayerCell(cell)) {
                iterator.remove();
            }
        }
    }

    // Check if a cell is a neighbor of at least one of the player's cells and is
    // either empty or belongs to the opponent
    private boolean isNeighborOfPlayerCell(Cell cell) {
        for (Cell neighbor : cell.getNeighbors()) {
            if (neighbor.getState() == player) {
                // For each neighbor of the cell, check if it is empty or belongs to the
                // opponent
                return cell.getState() == 0 || cell.getState() != player;
            }
        }
        return false;
    }

}
