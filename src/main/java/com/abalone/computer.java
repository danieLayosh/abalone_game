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
    private List<Cell> myCellsToMoveTo;
    private List<Cell> opponentsCellsToMoveTo;
    private ArrayList<Move> moves;
    private ArrayList<Move> movesOppo;
    private final int BIG_SCORE = 100;

    public Computer(GameBoard gameBoard, int player) {
        this.gameBoard = gameBoard;
        this.board = gameBoard.getBoard();
        this.player = player;

        this.myCellsToMoveTo = new ArrayList<>(board.keySet());
        this.opponentsCellsToMoveTo = new ArrayList<>(board.keySet());
        this.moves = new ArrayList<>();
        this.movesOppo = new ArrayList<>();
        this.myMarbles = new ArrayList<>();
        this.opponentsMarbles = new ArrayList<>();

        getAllPotentialMovesForBoth();
    }

    private void cellsToMoveTo() {
        myCellsToMoveTo = new ArrayList<>(board.keySet());
        opponentsCellsToMoveTo = new ArrayList<>(board.keySet());
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

    /**
     * Updates both players own marbles list and cellsToMoveTo lists and then
     * calculte all possibol moves for each one
     */
    private void getAllPotentialMovesForBoth() {
        cellsToMoveTo(); // Filter cells to move to
        updateMarblesList(); // Update the list of your marbles

        getAllPotentialMovesForPlayer(player);
        getAllPotentialMovesForPlayer(player == 2 ? 1 : 2);
    }

    /**
     * Need to update his marbles and CellToMoveTo lists!!!!
     * 
     * @param ply
     *            Calcultes all moves for a player, updates his moves list
     */
    private void getAllPotentialMovesForPlayer(int ply) {
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
    }

    public Move computerTurn() {
        if (moves.isEmpty()) {
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
            }
        }

        // Choose a random move from the list of possible moves
        Random random = new Random();
        int randomIndex = 0;
        if (bestMoves.size() != 0) {
            randomIndex = random.nextInt(bestMoves.size());
        }
        bestMove = bestMoves.get(randomIndex);
        printMoveDetails(bestMove, bestMoves, bestEvaluation);

        return bestMove;
    }

    private void printMoveDetails(Move bestMove, List<Move> bestMoves, double bestEvaluation) {
        System.out.println("Total moves: " + moves.size());
        System.out.println("Bests moves: " + bestMoves.size());
        System.out.println(" ");
        System.out.println("The best move is: " + bestMove.toString() + " The score is: " + bestEvaluation);
        System.out.println(" ");
    }

    private double evaluatesBoardState(Move move) {
        move.executeMove();// executeMove to evalute the new board state.

        updateMarblesList(); // update the player marbles list.

        double gravityCenterScore = gravityCenter();// evaluates score from the distances

        double pushedOffScore = pushedOff(move);

        double keepPackedScore = keepPacked();

        double marblesGroupScore = evaluateGroupScore();

        double marblesInDangerScore = marblesInDanger();

        move.undoMove();// undo the move to get it back before checking another move.

        // if (player == 2) {
            return gravityCenterScore + pushedOffScore + marblesGroupScore + marblesInDangerScore;
        // } else {
        //     return gravityCenterScore + pushedOffScore + keepPackedScore;
        // }
    }

    private double marblesInDanger() {
        Double score = 0.0;
        for (Cell cell : myMarbles) {
            if (cell.getIsborder()) {
                if (canBePushed(cell)) {
                    score -= BIG_SCORE;
                }
            }
        }
        return score;
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

    private double pushedOff(Move move) {
        double pushCounter = 0.0;

        if (move.getMoveType() == MoveType.OUT_OF_THE_BOARD) {
            // Initial score for pushing a marble out
            pushCounter = 30;

            // Check if this move results in a win
            if (14 - opponentsMarbles.size() == 5) { // Check if pushing this marble results in six marbles being out
                pushCounter = BIG_SCORE; // Assign a high score for a winning move
            } else if (canBePushed(move.getDestCell())) {
                // If it's not a winning move, but the player's marble can be pushed in return
                if (myMarbles.size() - opponentsMarbles.size() == 0) {
                    pushCounter = 10;
                } else if (myMarbles.size() - opponentsMarbles.size() > 0) {
                    pushCounter = 0;
                } else {
                    pushCounter = 30;
                }
            }
        }

        return pushCounter;
    }

    private boolean canBePushed(Cell cell) {
        for (Cell neighbor : cell.getNeighbors()) {
            if (canPushDirection(cell, neighbor)) {
                return true;
            }
        }
        return false;
    }

    private boolean canPushDirection(Cell cell, Cell neighbor) {
        int opponentState = (player == 1) ? 2 : 1;
        if (neighbor.getState() == opponentState) {
            Direction direction = cell.getDirectionOfNeighbor(neighbor);
            if (cell.getNeighborInDirection(Move.oppositeDirection(direction)) == null) {
                return isConsecutiveOpponent(neighbor, direction, opponentState);
            }
        } else {
            int opponentNeighborCount = 0;
            Cell opponentNeighbor = neighbor.getNeighborInDirection(cell.getDirectionOfNeighbor(neighbor));
            if (opponentNeighbor == null) {
                return false;
            }
            if (neighbor.getState() == player && opponentNeighbor.getState() == opponentState) {
                opponentNeighborCount++;
                opponentNeighbor = neighbor.getNeighborInDirection(cell.getDirectionOfNeighbor(neighbor));
                if (opponentNeighbor == null) {
                    return false;
                }
                if (opponentNeighbor.getState() == opponentState) {
                    opponentNeighborCount++;
                    opponentNeighbor = neighbor.getNeighborInDirection(cell.getDirectionOfNeighbor(neighbor));
                    if (opponentNeighbor == null) {
                        return false;
                    }
                    if (opponentNeighbor.getState() == opponentState) {
                        opponentNeighborCount++;
                    }
                }
            }
            if (opponentNeighborCount == 3 && cell.getIsborder()) {
                return true;
            }
        }
        return false;
    }

    private boolean isConsecutiveOpponent(Cell startCell, Direction direction, int opponentState) {
        Cell currentCell = startCell.getNeighborInDirection(direction);
        boolean foundOpponent = false;

        while (currentCell != null && currentCell.getState() == opponentState) {
            foundOpponent = true; // At least one opponent cell in the sequence.
            currentCell = currentCell.getNeighborInDirection(direction);
        }

        return foundOpponent;
    }

    private double evaluateGroupScore() {
        double Score = 0.0;

        // Evaluate group score Top Left To Bottom Right diagnols
        Score += mainDiagnols();

        // Evaluate group score Top Right To Bottom Left diagnols
        Score += secondariesDiagnols();

        // Evaluate group score Left To Right Rows
        Score += LeftToRight();

        return Score;
    }

    private double LeftToRight() {
        double LeftToRightSore = 0.0;
        int playerInOrder, opponentInOrder;
        for (int x = 0; x < 9; x++) {
            int rowLength = getRowLength(x);
            playerInOrder = 0;
            opponentInOrder = 0;
            for (int y = 0; y < rowLength; y++) {
                Cell cell = gameBoard.getCellAt(x, y);
                if (cell.getState() == player) {
                    opponentInOrder = 0;
                    if (playerInOrder == 0) {
                        playerInOrder++;
                    } else {
                        if (playerInOrder == 1) {
                            playerInOrder++;
                            LeftToRightSore++;
                        } else if (playerInOrder == 2) {
                            playerInOrder++;
                            LeftToRightSore++;
                        }
                    }
                } else {
                    if (cell.getState() != 0) {
                        playerInOrder = 0;
                        if (opponentInOrder == 0) {
                            opponentInOrder++;
                        } else {
                            if (opponentInOrder == 1) {
                                opponentInOrder++;
                                LeftToRightSore--;
                            } else if (opponentInOrder == 2) {
                                opponentInOrder++;
                                LeftToRightSore--;
                            }
                        }
                    } else {
                        opponentInOrder = 0;
                        playerInOrder = 0;
                    }
                }
            }
        }
        return LeftToRightSore;
    }

    private double mainDiagnols() {
        double mainDiagnolScore = 0.0;

        for (int d = 0; d < 5; d++) {
            Cell cell = gameBoard.getCellAt(d, 0);
            mainDiagnolScore += evaluateDiagnol(cell, Direction.DOWNRIGHT);
        }
        for (int d = 1; d < 5; d++) {
            Cell cell = gameBoard.getCellAt(0, d);
            mainDiagnolScore += evaluateDiagnol(cell, Direction.DOWNRIGHT);
        }

        return mainDiagnolScore;
    }

    private double secondariesDiagnols() {
        double secondariesDiagnolsScore = 0.0;

        for (int d = 4; d < 9; d++) {
            Cell cell = gameBoard.getCellAt(d, 0);
            secondariesDiagnolsScore += evaluateDiagnol(cell, Direction.UPRIGHT);
        }
        for (int d = 1; d < 5; d++) {
            Cell cell = gameBoard.getCellAt(8, d);
            secondariesDiagnolsScore += evaluateDiagnol(cell, Direction.UPRIGHT);
        }

        return secondariesDiagnolsScore;
    }

    private double evaluateDiagnol(Cell cell, Direction direction) {
        double evaluatedLeftToRightDiagnol = 0.0;
        int playerInOrder = 0;
        int opponentInOrder = 0;
        while (cell != null) {
            if (cell.getState() == player) {
                opponentInOrder = 0;
                if (playerInOrder == 0) {
                    playerInOrder++;
                } else {
                    if (playerInOrder == 1) {
                        playerInOrder++;
                        evaluatedLeftToRightDiagnol++;
                    } else if (playerInOrder == 2) {
                        playerInOrder++;
                        evaluatedLeftToRightDiagnol++;
                    }
                }
            } else {
                if (cell.getState() != 0) {
                    playerInOrder = 0;
                    if (opponentInOrder == 0) {
                        opponentInOrder++;
                    } else {
                        if (opponentInOrder == 1) {
                            opponentInOrder++;
                            evaluatedLeftToRightDiagnol--;
                        } else if (opponentInOrder == 2) {
                            opponentInOrder++;
                            evaluatedLeftToRightDiagnol--;
                        }
                    }
                } else {
                    opponentInOrder = 0;
                    playerInOrder = 0;
                }
            }
            cell = cell.getNeighborInDirection(direction);
        }
        return evaluatedLeftToRightDiagnol;
    }

    private static int getRowLength(int x) {
        if (x < 4) {
            return 5 + x; // Rows 0 to 3 increase in length
        } else if (x < 5) {
            return 9; // Middle row has the maximum length
        } else {
            return 13 - x; // Rows 5 to 8 decrease in length
        }
    }

}
