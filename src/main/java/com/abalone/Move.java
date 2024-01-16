package com.abalone;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.abalone.enums.Direction;
import com.abalone.enums.MoveType;

public class Move {
    private List<Cell> marbles;
    private Direction marblesDirection;
    private Direction directionToDest;
    private Cell dest;
    private int player;
    private MoveType moveType;
    private int sizeInLine;

    private Map<Cell, Integer> marblesUsed;

    public Move(List<Cell> marbles, Cell dest, int player) {
        this.marbles = marbles;
        this.dest = dest;
        this.player = player;
        this.marblesDirection = null;
        this.moveType = null;
        this.directionToDest = null;
        this.sizeInLine = marbles.size();
    }

    public List<Cell> getMarbles() {
        return marbles;
    }

    public Cell getDestCell() {
        return dest;
    }

    public int getPlayer() {
        return player;
    }

    public boolean isValid() {
        sortMarbles(); // Ensure marbles are sorted before validation

        if (marbles.size() < 1 || marbles.size() > 3) {
            System.out.println("Invalid number of marbles selected.");
            return false;
            // throw new IllegalStateException("Invalid number of marbles selected.");
        }

        if (!marblesBelongToPlayer()) {
            System.out.println("One or more of the marbles do not belong to the player.");
            return false;
            // throw new IllegalStateException("One or more of the marbles do not belong to
            // the player.");
        }

        if (!areMarblesInlineAndAdjacent()) {
            System.out.println("The marbles are not in an inline formation and/or not adjacent.");
            return false;
            // throw new IllegalStateException("The marbles are not in an inline formation
            // and/or not adjacent.");
        }

        if (determineMoveType(marbles, dest) == null) {
            System.out.println("Invalid move type for the selected marbles and destination.");
            return false;
            // throw new IllegalStateException("Invalid move type for the selected marbles
            // and destination.");
        }

        if (!isPathValid()) {
            System.out.println("Invalid path, the selected marbles can not move in this direction.");
            return false;
            // throw new IllegalStateException("Invalid path, the selected marbles can not
            // move in this direction.");
        }

        return true;
    }

    /**
     * Checks if the marbles can move in the direction.
     * 
     * @return true if all marbles can move to the right place, false otherwise.
     */
    private boolean isPathValid() {
        switch (moveType) {
            case SINGLE:
                return singleMove();
            case INLINE:
                return inlineMove();
            case SIDESTEP:
                return sideStepMove();
            default:
                return false;
        }
    }

    public MoveType getMoveType() {
        return moveType;
    }

    /**
     * Checks if all the marbles can move to the dest direction
     * 
     * @return true if the marbles can move to there direction
     */
    private boolean sideStepMove() {
        for (Cell cell : marbles) {
            Cell cellInDirection = cell.getNeighborInDirection(directionToDest);
            if (cellInDirection == null || cellInDirection.getState() != 0) {
                return false;
            }
        }
        System.out.println("sideStepMoveValid");
        return true;
    }

    private boolean inlineMove() {
        // The dest cell is empty, the marbles can move in this direction.
        if (dest.getState() == 0) {
            System.out.println("Inline to empty space");
            return true;
        }

        // The dest cell is your cell, you can not go in this direction.
        if (dest.getState() == player)
            return false;

        // The dest is enemy cell, we need to check if he has more marbles inline him
        return SumoMove();
    }

    private boolean SumoMove() {
        int ourForce = marbles.size();
        int theirForce = 1;
        // Start checking from the cell next to the destination in the direction of the
        // move
        Cell currentCell = dest.getNeighborInDirection(directionToDest);

        while (currentCell != null) { // Ensure we don't run into a null reference
            if (currentCell.getState() == 0) {
                break; // Empty cell, can potentially push
            } else if (currentCell.getState() == this.player) {
                return false; // Our own marble, can't push
            } else {
                theirForce++; // Opponent's marble, count force
            }

            if (theirForce >= ourForce) {
                return false; // Can't push, their force is too strong
            }

            // Move to the next cell in the direction
            currentCell = currentCell.getNeighborInDirection(directionToDest);
        }

        // We can push if we have greater force
        if (ourForce > theirForce) {
            System.out.println("sumoMove");
            return true;
        }
        return ourForce > theirForce;
    }

    /**
     * Checks if the single marble can move to the destination cell.
     * 
     * @return true if the marble can move to the destination cell.
     */
    private boolean singleMove() {
        return dest.getState() == 0;
    }

    private boolean marblesBelongToPlayer() {
        for (Cell marble : this.marbles) {
            if (marble.getState() != this.player) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the marbles are in a straight line.
     * 
     * @return true if all marbles are in line, false otherwise.
     */
    public boolean areMarblesInlineAndAdjacent() {
        if (marbles.size() == 1) {
            return true; // Single marble
        }

        Direction initialDirection = marbles.get(0).getDirectionOfNeighbor(marbles.get(1));
        if (initialDirection == null) {
            System.out.println("The first two Marbles are not neighbors.");
            return false;
            // throw new IllegalStateException("The first two Marbles are not neighbors.");
        }

        for (int i = 0; i < marbles.size() - 1; i++) {
            Cell currentMarble = marbles.get(i);
            Cell nextMarble = marbles.get(i + 1);
            Direction direction = currentMarble.getDirectionOfNeighbor(nextMarble);

            if (direction != initialDirection || direction == null) {
                System.out.println("Marbles are not neighbors: " + currentMarble.formatCoordinate() + " + "
                        + nextMarble.formatCoordinate());
                return false;
            }

        }
        marblesDirection = initialDirection;
        return true;// All marbles are in a straight line
    }

    // Method to determine the MoveType based on selected marbles and destination
    // cell
    public MoveType determineMoveType(List<Cell> selectedMarbles, Cell dest) {
        // if single marble and dest marble neighbors
        directionToDest = selectedMarbles.get(0).getDirectionOfNeighbor(dest);
        if (directionToDest == null) {
            directionToDest = selectedMarbles.get(marbles.size() - 1).getDirectionOfNeighbor(dest);
        }

        if (selectedMarbles.size() == 1) {
            Direction direction = marbles.get(0).getDirectionOfNeighbor(dest);
            if (direction != null) {
                moveType = MoveType.SINGLE;
                return MoveType.SINGLE; // The marble and the destination marble are not neighbors
            } else {
                System.out.println("The destination cell is not a neighbor");
                return null;
                // throw new IllegalStateException("The destination cell is not a neighbor");
            }
        }

        if (selectedMarbles.get(0).getNeighborsMap().containsKey(dest)) {
            if (selectedMarbles.get(0).getNeighborsMap().get(dest)
                    .compareTo(oppositeDirection(marblesDirection)) == 0) {
                moveType = MoveType.INLINE;
                return MoveType.INLINE;
            }
        }

        if (selectedMarbles.get(selectedMarbles.size() - 1).getNeighborsMap().containsKey(dest)) {
            if (selectedMarbles.get(selectedMarbles.size() - 1).getNeighborsMap().get(dest)
                    .compareTo(marblesDirection) == 0) {
                moveType = MoveType.INLINE;
                return MoveType.INLINE;
            }
        }

        // if its perpendicular
        if (isPerpendicular(directionToDest, marblesDirection)) {
            moveType = MoveType.SIDESTEP;
            return MoveType.SIDESTEP;
        }

        return null;
    }

    // Helper method to check if two directions are perpendicular
    private boolean isPerpendicular(Direction directionToDest, Direction columnDirection) {
        // Group the directions into pairs
        if ((directionToDest == Direction.UPRIGHT
                && (columnDirection == Direction.UPLEFT || columnDirection == Direction.LEFT
                        || columnDirection == Direction.RIGHT || columnDirection == Direction.DOWNRIGHT))
                ||
                (directionToDest == Direction.DOWNLEFT
                        && (columnDirection == Direction.UPLEFT || columnDirection == Direction.LEFT
                                || columnDirection == Direction.RIGHT || columnDirection == Direction.DOWNRIGHT))
                ||
                (directionToDest == Direction.UPLEFT
                        && (columnDirection == Direction.UPRIGHT || columnDirection == Direction.LEFT
                                || columnDirection == Direction.RIGHT || columnDirection == Direction.DOWNLEFT))
                ||
                (directionToDest == Direction.DOWNRIGHT
                        && (columnDirection == Direction.UPRIGHT || columnDirection == Direction.LEFT
                                || columnDirection == Direction.RIGHT || columnDirection == Direction.DOWNLEFT))
                ||
                (directionToDest == Direction.LEFT
                        && (columnDirection == Direction.UPRIGHT || columnDirection == Direction.UPLEFT
                                || columnDirection == Direction.DOWNRIGHT || columnDirection == Direction.DOWNLEFT))
                ||
                (directionToDest == Direction.RIGHT
                        && (columnDirection == Direction.UPRIGHT || columnDirection == Direction.UPLEFT
                                || columnDirection == Direction.DOWNRIGHT || columnDirection == Direction.DOWNLEFT))) {
            return true;
        }

        return false;
    }

    public Direction oppositeDirection(Direction direction) {
        switch (direction) {
            case UPLEFT:
                return Direction.DOWNRIGHT;
            case UPRIGHT:
                return Direction.DOWNLEFT;
            case RIGHT:
                return Direction.LEFT;
            case DOWNRIGHT:
                return Direction.UPLEFT;
            case DOWNLEFT:
                return Direction.UPRIGHT;
            default:
                System.out.println("Invalid direction: " + direction);
                return null;
            // throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }

    /**
     * Sorts the list of marbles so that the cell with the smallest x is first.
     * If two cells have the same x, the one with the smallest y is first.
     */
    private void sortMarbles() {
        Collections.sort(marbles, new Comparator<Cell>() {
            @Override
            public int compare(Cell c1, Cell c2) {
                if (c1.getX() != c2.getX()) {
                    return Integer.compare(c1.getX(), c2.getX());
                } else {
                    return Integer.compare(c1.getY(), c2.getY());
                }
            }
        });
    }

    public Direction getDirectionToDest() {
        return directionToDest;
    }

    public void executeMove() {
        if (!isValid()) {
            System.out.println("Attempted to execute an invalid move.");
            return;
            // throw new IllegalStateException("Attempted to execute an invalid move.");
        }

        // Handle different types of moves
        switch (moveType) {
            case SINGLE:
                marblesUsed.put(marbles.get(0), player);
                marblesUsed.put(dest, 0);
                marbles.get(0).setState(0);
                dest.setState(player);
            case INLINE:
                executeInlineOrSingleMove();
                break;
            case SIDESTEP:
                executeSideStepMove();
                break;
            default:
                System.out.println("Attempted to execute an invalid move.");
                return;
            // throw new IllegalStateException("Unknown move type.");
        }

    }

    private void executeInlineOrSingleMove() {
        if (dest.getState() == 0) {

            boolean reverseOrder = directionToDest == Direction.DOWNLEFT ||
                    directionToDest == Direction.DOWNRIGHT || directionToDest == Direction.RIGHT;

            int start = reverseOrder ? marbles.size() - 1 : 0;
            int end = reverseOrder ? -1 : marbles.size();
            int step = reverseOrder ? -1 : 1;

            for (int i = start; reverseOrder ? i > end : i < end; i += step) {
                Cell marble = marbles.get(i);
                Cell nextCell = marble.getNeighborInDirection(directionToDest);
                if (nextCell != null && nextCell.getState() == 0) {
                    nextCell.setState(player); // Move marble to the next cell
                    marble.setState(0); // Empty the original cell
                } else {
                    System.out.println("Cannot move if path is blocked and cannot push");
                    // If the path is blocked, the move cannot be completed.
                    break;
                }
            }
        } else {
            if (dest.getState() != player) {
                executeSumoMove();
            }
        }
    }

    private void executeSumoMove() {
        if (!SumoMove()) {
            System.out.println("Cannot execute Sumo move: push not possible.");
            return;
        }

        pushOpponentMarbles();
        executeInlineOrSingleMove();
    }

    private void pushOpponentMarbles() {
        Cell firstOpponentMarble = null;
        Cell secondOpponentMarble = null;

        // Find the first and possibly second opponent marbles in the line of push
        Cell currentCell = dest;
        while (currentCell != null && currentCell.getState() == (player == 1 ? 2 : 1)) {
            if (firstOpponentMarble == null) {
                firstOpponentMarble = currentCell;
            } else if (secondOpponentMarble == null) {
                secondOpponentMarble = currentCell;
            }
            currentCell = currentCell.getNeighborInDirection(directionToDest);
        }

        // Push the opponent marbles
        if (secondOpponentMarble != null) {
            pushMarble(secondOpponentMarble);
        }
        if (firstOpponentMarble != null) {
            pushMarble(firstOpponentMarble);
        }

    }

    private void pushMarble(Cell marble) {
        Cell nextCell = marble.getNeighborInDirection(directionToDest);
        if (nextCell == null) { // Push marble off the board
            marble.setState(0);
            moveType = moveType.OUT_OF_THE_BOARD;
        } else if (nextCell.getState() == 0) { // Push marble to next cell
            nextCell.setState(marble.getState());
            marble.setState(0);
        }
    }

    private void executeSideStepMove() {
        Cell currentCell, nextCell;
        for (Cell marble : marbles) {
            currentCell = marble;
            nextCell = currentCell.getNeighborInDirection(directionToDest);

            if (nextCell != null && nextCell.getState() == 0) {
                nextCell.setState(currentCell.getState()); // Move the marble to the next cell
                currentCell.setState(0); // Set the current cell to empty
            }
        }
    }

    public int getSizeInLine() {
        return sizeInLine;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Move with marbles: ");
        for (Cell marble : marbles) {
            sb.append(marble.formatCoordinate()).append(", ");
        }
        sb.append("Destination: ").append(dest.formatCoordinate());
        sb.append(", Player: ").append(player);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Move move = (Move) o;
        return player == move.player &&
                sizeInLine == move.sizeInLine &&
                Objects.equals(marbles, move.marbles) &&
                marblesDirection == move.marblesDirection &&
                directionToDest == move.directionToDest &&
                Objects.equals(dest, move.dest) &&
                moveType == move.moveType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(marbles, marblesDirection, directionToDest, dest, player, moveType, sizeInLine);
    }

    public void undoMove() {

    }

}