package com.abalone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.abalone.enums.Direction;

public class Computer {
    private GameBoard gameBoard;
    private int player;
    private Map<Cell, Map<Cell, Direction>> board;
    private List<Cell> myMarbles; // List to store your marbles
    private List<Cell> cellsToMoveTo;
    private ArrayList<Move> moves;

    public Computer(GameBoard gameBoard, int player) {
        this.gameBoard = gameBoard;
        this.board = gameBoard.getBoard();
        this.cellsToMoveTo = new ArrayList<>(board.keySet());
        this.player = player;
        this.moves = new ArrayList<>();
        this.myMarbles = new ArrayList<>(); // Initialize the list of your marbles
        cellsToMoveTo();
        updateMyMarbles(); // Update the list of your marbles
        generateValidMoves(); // get all the valid moves
    }

    private void cellsToMoveTo() {
        cellsToMoveTo.removeIf(cell -> !isNeighborOfPlayerCell(cell));
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

    public void printPotentialCells() {
        for (Cell cell : cellsToMoveTo) {
            System.out.print(cell.formatCoordinate() + ": " + cell.getScore() + ", ");
        }
    }

    // Update the list of your marbles
    private void updateMyMarbles() {
        myMarbles = cellsToMoveTo.stream()
                .filter(cell -> cell.getState() == player)
                .collect(Collectors.toList());
    }

    // Method to generate all valid moves
    public void generateValidMoves() {
        for (int i = 0; i < myMarbles.size(); i++) {
            for (int j = i; j < myMarbles.size(); j++) {
                for (int k = j; k < myMarbles.size(); k++) {
                    List<Cell> marblesToMove = new ArrayList<>();
                    marblesToMove.add(myMarbles.get(i));
                    if (j != i)
                        marblesToMove.add(myMarbles.get(j));
                    if (k != j && k != i)
                        marblesToMove.add(myMarbles.get(k));

                    for (Cell destination : cellsToMoveTo) {
                        Move potentialMove = new Move(marblesToMove, destination, player);
                        if (potentialMove.isValid() && !moves.contains(potentialMove)) {
                            moves.add(potentialMove);
                        }
                    }
                }
            }
        }
    }

}
