package com.abalone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.abalone.enums.Direction;
// import com.abalone.enums.MoveType;
import com.abalone.enums.MoveType;

public class Computer {
    private int player;
    private Map<Cell, Map<Cell, Direction>> board;
    private List<Cell> myMarbles; // List to store your marbles
    private List<Cell> opponentsMarbles; // List to store opponents marbles
    private List<Cell> myCellsToMoveTo;
    private List<Cell> opponentsCellsToMoveTo;
    private ArrayList<Move> moves;
    private ArrayList<Move> movesOppo;

    public Computer(GameBoard gameBoard, int player) {
        this.board = gameBoard.getBoard();
        this.player = player;

        this.myCellsToMoveTo = new ArrayList<>(board.keySet());
        this.opponentsCellsToMoveTo = new ArrayList<>(board.keySet());
        this.moves = new ArrayList<>();
        this.movesOppo = new ArrayList<>();
        this.myMarbles = new ArrayList<>();
        this.opponentsMarbles = new ArrayList<>();

        getAllPotentialMoves(player);
        System.out.println("getAllPotentialMoves for player: " + moves.size());

        getAllPotentialMoves(player == 2 ? 1 : 2);
        System.out.println("getAllPotentialMoves for opponent: " + movesOppo.size());
    }

    private void cellsToMoveTo() {
        myCellsToMoveTo.removeIf(cell -> !isNeighborOfPlayerCell(cell));
        opponentsCellsToMoveTo.removeIf(cell -> !isNeighborOfOpponentCell(cell));
    }

    private boolean isNeighborOfOpponentCell(Cell cell) {
        for (Cell neighbor : cell.getNeighbors()) {
            if (neighbor.getState() == ((player == 1) ? 2 : 1)) {
                // For each neighbor of the cell, check if it is empty or belongs to the
                // opponent
                return cell.getState() == 0 || cell.getState() == player;
            }
        }
        return false;
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
        for (Cell cell : myCellsToMoveTo) {
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

                    for (Cell destination : myCellsToMoveTo) {
                        Move potentialMove = new Move(marblesToMove, destination, player);
                        if (potentialMove.isValid() && !moves.contains(potentialMove)) {
                            moves.add(potentialMove);
                        }
                    }
                }
            }
        }
    }

    private void getAllPotentialMoves(int ply) {
        cellsToMoveTo(); // Filter cells to move to
        updateMarblesList(); // Update the list of your marbles

        List<Cell> marblesToConsider = (ply == player) ? myMarbles : opponentsMarbles;
        if (ply == player) {
            moves.clear();
        } else {
            movesOppo.clear();
        }

        for (int i = 0; i < marblesToConsider.size(); i++) {
            for (int j = i; j < marblesToConsider.size(); j++) {
                for (int k = j; k < marblesToConsider.size(); k++) {
                    List<Cell> marblesToMove = new ArrayList<>();
                    marblesToMove.add(marblesToConsider.get(i));
                    if (j != i)
                        marblesToMove.add(marblesToConsider.get(j));
                    if (k != j && k != i)
                        marblesToMove.add(marblesToConsider.get(k));

                    for (Cell destination : (ply == player) ? myCellsToMoveTo : opponentsCellsToMoveTo) {
                        Move potentialMove = new Move(marblesToMove, destination, ply);
                        if (potentialMove.isValid() && !((ply == player) ? moves : movesOppo).contains(potentialMove)) {
                            ((ply == player) ? moves : movesOppo).add(potentialMove);
                        }
                    }
                }
            }
        }
        // return (((ply == player) ? moves : movesOppo)); // Return a copy of the moves
        // list
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

    public Move computerTurn() {
        if (moves.isEmpty()) {
            // No valid moves available, return null or handle this case as needed
            return null;
        }

        double bestEvaluation = Double.NEGATIVE_INFINITY;
        Move bestMove = moves.get(0);
        ArrayList<Move> bestMoves = new ArrayList<>();
        bestMoves.add(bestMove);
        for (Move move : moves) {
            double evaluation = evaluatesBoardState(move);
            if (evaluation > bestEvaluation) {
                bestMoves.clear();
                bestEvaluation = evaluation;
            }
            if (evaluation == bestEvaluation) {
                bestMove = move;
                bestMoves.add(bestMove);
                System.out.println(move.toString() + " The score is: " + evaluation);
            }
        }

        // Choose a random move from the list of possible moves
        Random random = new Random();
        int randomIndex = 0;
        System.out.println("Total moves: " + moves.size());
        System.out.println("Bests moves: " + bestMoves.size());
        if (bestMoves.size() == 0) {
            randomIndex = random.nextInt(bestMoves.size());
        }
        bestMove = bestMoves.get(randomIndex);
        System.out.println(" ");
        System.out.println("The best move is: " + bestMove.toString() + " The score is: " + bestEvaluation);
        System.out.println(" ");

        return bestMove;
    }

    private double evaluatesBoardState(Move move) {
        move.executeMove();// executeMove to evalute the new board state.

        updateMarblesList(); // update the player marbles list.

        double gravityCenterScore = gravityCenter();// evaluates score from the distances

        double pushedOffScore = pushedOff(move);
        // System.out.println("Marbles pushed off --->>> " + pushedOffScore);

        double keepPackedScore = keepPacked();
        // System.out.println("keepPacked --->>> " + keepPackedScore);

        // double marblesGroupScore = evaluateGroupScore();

        move.undoMove();// undo the move to get it back before checking another move.
        return gravityCenterScore + keepPackedScore + pushedOffScore;
    }

    private double keepPacked() {
        double counter = 0;
        for (Cell marble : myMarbles) {
            for (Cell neighborCell : marble.getNeighborsMap().keySet()) {
                if (neighborCell.getState() == marble.getState())
                    counter++;
            }
        }

        for (Cell marble : opponentsMarbles) {
            for (Cell neighborCell : marble.getNeighborsMap().keySet()) {
                if (neighborCell.getState() == marble.getState())
                    counter--;
            }
        }

        return counter;
    }

    private double pushedOff(Move move) {
        // if(myMarbles.size() - opponentsMarbles.size() == 0){
        // return 1;
        // }
        double pushCounter = 0.0;
        if (move.getMoveType() == MoveType.OUT_OF_THE_BOARD) {
            pushCounter += 30;
        }

        // if (condition) {
        // ************ add the opponent OUT_OF_THE_BOARD ****************************
        // }

        return pushCounter;
        // return myMarbles.size() - opponentsMarbles.size();
    }

    private double evaluateGroupScore() {
        double MyGroupScore = 0.0;
        for (Move move : moves) {
            MyGroupScore += (move.getSizeInLine() == 2) ? 1 : ((move.getSizeInLine() == 3) ? 2 : 0);
        }

        getAllPotentialMoves(2);
        getAllPotentialMoves(1);

        double opponentsGroupScore = 0.0;
        for (Move move : moves) {
            opponentsGroupScore += (move.getSizeInLine() == 2) ? 1 : ((move.getSizeInLine() == 3) ? 2 : 0);
        }

        return MyGroupScore - opponentsGroupScore;
    }

    private double gravityCenter() {

        double MydistanceScore = 0;
        for (Cell cell : myMarbles) {
            MydistanceScore += cell.getScore();// sum player's marbles score, according to the distance from the center.
        }

        double opponentsDistanceScore = 0;
        for (Cell cell : opponentsMarbles) {
            opponentsDistanceScore += cell.getScore();
        }

        return MydistanceScore - opponentsDistanceScore;
    }

}
