package com.abalone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
    private ArrayList<Move> bestMoves;
    private Move bestMove;
    private double bestEvaluation;
    private static final int BIG_SCORE = 100;
    private static HashMap<GameBoard, ArrayList<Move>> gameStateBestMove;
    private int turnCalcSaved;

    public void ComputerPlay(GameBoard gameBoard, int player) {
        this.gameBoard = gameBoard;
        this.board = gameBoard.getBoard();
        this.player = player;

        this.myCellsToMoveTo = new ArrayList<>(board.keySet());
        this.opponentsCellsToMoveTo = new ArrayList<>(board.keySet());
        this.moves = new ArrayList<>();
        this.movesOppo = new ArrayList<>();
        this.myMarbles = new ArrayList<>();
        this.opponentsMarbles = new ArrayList<>();
        this.bestMoves = new ArrayList<>();
        this.bestMove = null;
        this.bestEvaluation = Double.NEGATIVE_INFINITY;

        getAllPotentialMovesForBoth();
    }

    public Computer() {
        gameStateBestMove = new HashMap<>();
        turnCalcSaved = 0;
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
    private Move getAllPotentialMovesForBoth() {
        cellsToMoveTo(); // Filter cells to move to
        updateMarblesList(); // Update the list of your marbles

        if (doesGameBoardHasBestMoveAlready()) {
            System.out.println("Game board has best move already");
            turnCalcSaved++;
            return chooseRandomMoveFromBestMovesList();
        } else {
            getAllPotentialMovesForPlayer(player, myMarbles, moves, myCellsToMoveTo);
            getAllPotentialMovesForPlayer((player == 2 ? 1 : 2), opponentsMarbles, movesOppo, opponentsCellsToMoveTo);

            return computerTurn();
        }
    }

    // Check if the board state has the best move already
    private boolean doesGameBoardHasBestMoveAlready() {
        if (gameStateBestMove.containsKey(gameBoard) && gameStateBestMove.get(gameBoard).get(0).getPlayer() == player) {
            bestMoves = gameStateBestMove.get(gameBoard);
            return true;
        }
        return false;
    }

    /**
     * Calculates all moves for a player and updates his moves list.
     * This method also needs to ensure the marbles list and cellsToMoveTo list
     * are up to date to accurately reflect the current game state.
     * 
     * @param ply           The player for whom to calculate moves.
     * @param playerMarbles A reference to the list of marbles for the player.
     *                      This list should be updated elsewhere in the code
     *                      before calling this method to reflect the current
     *                      marbles.
     * @param playerMoves   The list of moves for the player which will be updated.
     * @param CellsToMoveTo A reference to the list of potential cells to move to.
     *                      This list should be updated elsewhere in the code
     *                      before calling this method to reflect current possible
     *                      destinations.
     */
    private void getAllPotentialMovesForPlayer(int ply, List<Cell> playerMarbles, List<Move> playerMoves,
            List<Cell> CellsToMoveTo) {

        Set<Move> tempMoves = new HashSet<>(); // Temp Hash to store unique moves

        for (int i = 0; i < playerMarbles.size(); i++) {
            for (int j = i; j < playerMarbles.size(); j++) {
                for (int k = j; k < playerMarbles.size(); k++) {
                    List<Cell> marblesToMove = new ArrayList<>();
                    marblesToMove.add(playerMarbles.get(i));
                    if (j != i)
                        marblesToMove.add(playerMarbles.get(j));
                    if (k != j && k != i)
                        marblesToMove.add(playerMarbles.get(k));

                    for (Cell destination : CellsToMoveTo) {
                        Move potentialMove = new Move(marblesToMove, destination, ply);
                        if (potentialMove.isValid()) {
                            tempMoves.add(potentialMove);
                        }
                    }
                }
            }
        }

        playerMoves.clear(); // Clear the original list first
        playerMoves.addAll(tempMoves); // Add all unique moves from tempMoves
    }

    // This method is called to get the best move for the computer
    public Move computerTurn() {
        if (moves.isEmpty()) {
            return null;
        }

        bestMove = moves.get(0);
        bestMoves.add(bestMove);

        // Evaluate the board state for each move
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
        gameStateBestMove.put(gameBoard, bestMoves);
        return chooseRandomMoveFromBestMovesList();
    }

    // This method is called to get the best move for the computer from the list of
    // best moves
    private Move chooseRandomMoveFromBestMovesList() {
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

    // This method is called to evaluate the board state for each move
    private double evaluatesBoardState(Move move) {
        double evaluation = 0.0;

        move.executeMove();// executeMove to evalute the new board state.

        updateMarblesList(); // update the player marbles list.

        double gravityCenterScore = gravityCenter(); // evaluates score from the distances

        double pushedOffScore = pushedOff(move); // evaluates score from the pushed off marbles

        @SuppressWarnings("unused")
        double keepPackedScore = keepPacked(); // evaluates score from the marbles packed together

        double marblesGroupScore = evaluateGroupScore(); // evaluates score from the marbles in the same group

        double marblesInDangerScore = marblesInDanger(); // evaluates score from the marbles in danger

        move.undoMove();// undo the move to get it back before checking another move.

        evaluation = gravityCenterScore + pushedOffScore + marblesGroupScore + marblesInDangerScore;

        return evaluation;
    }

    // evaluates score from the marbles in danger
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

    // evaluates score from the marbles packed together
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

    // evaluates score from the distances
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

    // evaluates score from the pushed off marbles
    private double pushedOff(Move move) {
        double pushCounter = 0.0;

        if (move.getMoveType() == MoveType.OUT_OF_THE_BOARD) {
            // Initial score for pushing a marble out
            pushCounter = BIG_SCORE; // was 30

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

    // Check if a cell can be pushed
    private boolean canBePushed(Cell cell) {
        for (Cell neighbor : cell.getNeighbors()) {
            if (canPushDirection(cell, neighbor)) {
                return true;
            }
        }
        return false;
    }

    // Check if a cell can be pushed in a certain direction
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

    // Check if there are consecutive opponent cells in a certain direction
    private boolean isConsecutiveOpponent(Cell startCell, Direction direction, int opponentState) {
        Cell currentCell = startCell.getNeighborInDirection(direction);
        boolean foundOpponent = false;

        while (currentCell != null && currentCell.getState() == opponentState) {
            foundOpponent = true; // At least one opponent cell in the sequence.
            currentCell = currentCell.getNeighborInDirection(direction);
        }

        return foundOpponent;
    }

    // evaluates score from the marbles in the same group
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

    // Evaluate group score Left To Right Rows
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

    // Evaluate group score Top Left To Bottom Right diagnols
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

    // Evaluate group score Top Right To Bottom Left diagnols
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

    // Evaluate the score of a diagnol
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

    // Get the length of a row
    private static int getRowLength(int x) {
        if (x < 4) {
            return 5 + x; // Rows 0 to 3 increase in length
        } else if (x < 5) {
            return 9; // Middle row has the maximum length
        } else {
            return 13 - x; // Rows 5 to 8 decrease in length
        }
    }

    public ArrayList<Move> getMoves() {
        return moves;
    }

    public ArrayList<Move> getBestMoves() {
        return bestMoves;
    }

    public Move getBestMove() {
        return bestMove;
    }

    public int getTurnCalcSaved() {
        return turnCalcSaved;
    }
}
