package com.abalone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.abalone.enums.Direction;
import com.abalone.enums.MoveType;

public class Computer {
    private int player;
    private GameBoard gameBoard;
    private Map<Cell, Map<Cell, Direction>> board;
    private List<Cell> myMarbles; // List to store your marbles
    private List<Cell> opponentsMarbles; // List to store opponents marbles
    private List<Cell> cellsToMoveTo;
    private ArrayList<Move> moves;

    public Computer(GameBoard gameBoard, int player) {
        this.gameBoard = gameBoard;
        this.board = gameBoard.getBoard();
        this.player = player;

        this.cellsToMoveTo = new ArrayList<>(board.keySet());
        this.moves = new ArrayList<>();
        this.myMarbles = new ArrayList<>();
        this.opponentsMarbles = new ArrayList<>();

        cellsToMoveTo(); // Filter cells to move to
        updateMarblesList(); // Update the list of your marbles
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
    private void updateMarblesList() {
        myMarbles.clear(); // Clear existing marbles
        opponentsMarbles.clear(); // Clear existing marbles
        for (Cell cell : board.keySet()) {
            if (cell.getState() == player) {
                myMarbles.add(cell); // Add the cell if it belongs to the player
            } else if (cell.getState() != 0) {
                opponentsMarbles.add(cell);
            }
        }
    }

    // Method to generate all valid moves
    private void generateValidMoves() {
        moves.clear();
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

        double bestEvaluation = 0;
        Move bestMove = moves.get(0);
        for (Move move : moves) {
            double evaluation = evaluatesBoardState(move);
            if (evaluation > bestEvaluation) {
                bestMove = move;
                bestEvaluation = evaluation;
            }
        }

        
        System.out.println(bestMove.toString());

        // gameBoard.printBoard();

        // System.out.println("before");
        // // for (Cell cell : bestMove.getMarblesUsed().keySet()) {
        // // System.out.print(cell.formatCoordinate() + " - ");
        // // }
        // System.out.println();

        // bestMove.executeMove();

        // gameBoard.printBoard();

        // System.out.println("after");
        // System.out.println();

        // bestMove.undoMove();

        // gameBoard.printBoard();

        // System.out.println("afterafter");
        // // for (Cell cell : bestMove.getMarblesUsed().keySet()) {
        // // System.out.print(cell.formatCoordinate() + " - ");
        // // }
        // System.out.println();

        // ArrayList<Move> moves2 = new ArrayList<>();
        // for (Move move : moves) {
        // if (move.getMoveType() == MoveType.INLINE || move.getMoveType() ==
        // MoveType.OUT_OF_THE_BOARD) {
        // moves2.add(move);
        // }
        // }
        // Choose a random move from the list of possible moves
        // Random random = new Random();
        // int randomIndex = random.nextInt(moves2.size());
        // bestMove = moves2.get(randomIndex);

        return bestMove;
    }

    private double evaluatesBoardState(Move move) {
        // System.out.println(move.toString());

        // System.out.println("before executeMove");
        // gameBoard.printBoard();
        // System.out.println();

        move.executeMove();// executeMove to evalute the new board state.

        double distanceScore = evaluateDistanceScore();// evaluates score from the distances

        // System.out.println("after executeMove");
        // gameBoard.printBoard();
        // System.out.println();

        move.undoMove();// undo the move to get it back before checking another move.

        // System.out.println("after undoMove");
        // gameBoard.printBoard();
        // System.out.println();

        return distanceScore;
    }

    private double evaluateDistanceScore() {
        updateMarblesList(); // update the player marbles list.
        double MydistanceScore = 0;
        for (Cell cell : myMarbles) {
            // System.out.println(cell.formatCoordinate() + " <-> " + cell.getScore());
            MydistanceScore += cell.getScore();// sum player's marbles score, according to the distance from the center.
        }
        // System.out.println("-----------OPPONENTES MARBLES-------------------------");
        double opponentsDistanceScore = 0;
        for (Cell cell : opponentsMarbles) {
            // System.out.println(cell.formatCoordinate() + " <-> " + cell.getScore());
            opponentsDistanceScore += cell.getScore();
        }

        // System.out.println("------------------------------------------------------");
        // System.out.println("this move evaluate Distance Score is: " +
        // (MydistanceScore - opponentsDistanceScore));
        // System.out.println("------------------------------------------------------");

        return MydistanceScore - opponentsDistanceScore;
    }

}
