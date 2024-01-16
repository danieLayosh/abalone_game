package com.abalone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.abalone.enums.Direction;

public class Computer {
    private int player;
    private Map<Cell, Map<Cell, Direction>> board;
    private List<Cell> myMarbles; // List to store your marbles
    private List<Cell> cellsToMoveTo;
    private ArrayList<Move> moves;

    public Computer(GameBoard gameBoard, int player) {
        this.board = gameBoard.getBoard();
        this.player = player;

        this.cellsToMoveTo = new ArrayList<>(board.keySet());
        this.moves = new ArrayList<>();
        this.myMarbles = new ArrayList<>(); // Initialize the list of your marbles

        cellsToMoveTo(); // Filter cells to move to
        updateMyMarbles(); // Update the list of your marbles
        // printDebugInfo(); // Add this for debugging

        generateValidMoves(); // Generate all valid moves
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
        myMarbles.clear(); // Clear existing marbles
        for (Cell cell : board.keySet()) {
            if (cell.getState() == player) {
                myMarbles.add(cell); // Add the cell if it belongs to the player
            }
        }
    }

    // Method to generate all valid moves
    private void generateValidMoves() {
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

    public void printAllMoves() {
        if (moves.isEmpty()) {
            System.out.println("No moves available.");
            return;
        }
        for (Move move : moves) {
            System.out.println(move.toString());
        }
    }

    /*
     * private void printDebugInfo() {
     * // Print cellsToMoveTo for debugging
     * System.out.println("Cells To Move To: " + cellsToMoveTo.stream()
     * .map(Cell::formatCoordinate)
     * .collect(Collectors.joining(", ")));
     * // Print board cells and their states
     * System.out.println("Board Cells:");
     * board.keySet().forEach(cell -> System.out.println(cell.formatCoordinate() +
     * ": State " + cell.getState()));
     * }
     */

    public Move computerTurn() {
        if (moves.isEmpty()) {
            // No valid moves available, return null or handle this case as needed
            return null;
        }

        int bestEvaluation = 0;
        Move bestMove = moves.get(0);
        for (Move move : moves) {
            int evaluation = evaluatesBoardState(move);
            if (evaluation > bestEvaluation) {
                bestMove = move;
                bestEvaluation = evaluation;
            }
        }

        // Choose a random move from the list of possible moves
        Random random = new Random();
        int randomIndex = random.nextInt(moves.size());
        bestMove = moves.get(randomIndex);
        return bestMove;
    }

    private int evaluatesBoardState(Move move) {
        int distaceScore = evaluateDistanceScoe(); 
        
        return 0;
    }

    private int evaluateDistanceScoe() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'evaluateDistanceScoe'");
    }

}
