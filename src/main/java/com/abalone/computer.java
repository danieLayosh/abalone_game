package com.abalone;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.abalone.enums.Direction;

public class computer {
    private GameBoard gameBoard;
    private int player;
    Map<Cell, Map<Cell, Direction>> board;
    ArrayList<Cell> myCells;

    public computer(GameBoard gameBoard, int player) {
        this.gameBoard = gameBoard;
        this.board = gameBoard.getBoard();
        this.myCells = (ArrayList<Cell>) board.keySet();
        myCells();
        this.player = player;
    }

    // gets all the player marbles in a set
    private void myCells() {
        for (Cell cell : myCells) {
            if (cell.getState() != player) {
                myCells.remove(cell);
            }
        }
    }

    

}
