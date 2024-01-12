package com.abalone;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.abalone.enums.Direction;
import com.abalone.enums.MoveType;

public class Move {
    private List<Cell> marbles;
    private Direction marblesDirection;
    private Direction directionToDest;
    private Cell dest;
    private int player;
    private MoveType moveType;

    public Move(List<Cell> marbles, Cell dest, int player) {
        this.marbles = marbles;
        this.dest = dest;
        this.player = player;
        this.marblesDirection = null;
        this.moveType = null;
        this.directionToDest = null;
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
            throw new IllegalStateException("Invalid number of marbles selected.");
        }

        if (!marblesBelongToPlayer()) {
            throw new IllegalStateException("One or more of the marbles do not belong to the player.");
        }

        if (!areMarblesInlineAndAdjacent()) {
            throw new IllegalStateException("The marbles are not in an inline formation and/or not adjacent.");
        }

        if (determineMoveType(marbles, dest) == null) {
            throw new IllegalStateException("Invalid move type for the selected marbles and destination.");
        }

        if (!isPathValid()) {
            throw new IllegalStateException("Invalid path, the selected marbles can not move in this direction.");
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

    /**
     * Checks if all the marbles can move to the dest direction
     * 
     * @return true if the marbles can move to thire direction
     */
    private boolean sideStepMove() {
        for (Cell cell : marbles) {
            Cell cellInDirection = cell.getNeighborInDirection(directionToDest);
            if (cellInDirection == null || cellInDirection.getState() != 0) {
                return false;
            }
        }
        return true;
    }

    private boolean inlineMove() {
        // The dest cell is empty, the marbles can move in this direction.
        if (dest.getState() == 0)
            return true;

        // The dest cell is your cell, you can not go in this direction.
        if (dest.getState() == player)
            return false;

        // The dest is enemy cell, we need to check if he has more marbels inline him
        return SumoMove();
    }

    private boolean SumoMove() {
        int ourForce = marbles.size();
        int theirForce = 0;
        Cell currentCell = dest;

        // Keep moving in the direction of marblesDirection and count opponent's marbles
        while (true) {
            currentCell = currentCell.getNeighborInDirection(directionToDest);

            // If we reach an empty cell or the end of the board, we can push
            if (currentCell == null || currentCell.getState() == 0) {
                return ourForce > theirForce;
            }

            // If we encounter a marble of our own, we cannot push
            if (currentCell.getState() == this.player) {
                return false;
            }

            theirForce++;

            // If the opponent's force equals or exceeds ours, we cannot push
            if (theirForce >= ourForce) {
                return false;
            }
        }
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
    private boolean areMarblesInlineAndAdjacent() {
        if (marbles.size() == 1) {
            return true; // Single marble
        }

        Direction initialDirection = marbles.get(0).getDirectionOfNeighbor(marbles.get(1));
        if (initialDirection == null) {
            throw new IllegalStateException("The first two Marbles are not neighbors.");
        }

        for (int i = 0; i < marbles.size() - 1; i++) {
            Cell currentMarble = marbles.get(i);
            Cell nextMarble = marbles.get(i + 1);
            Direction direction = currentMarble.getDirectionOfNeighbor(nextMarble);

            if (direction != initialDirection || direction == null) {
                throw new IllegalStateException("Marbles are not neighbors: " + currentMarble.formatCoordinate()
                        + " and " + nextMarble.formatCoordinate());
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
                return MoveType.SINGLE; // The marble and the destonation marble are not neighbors
            } else {
                throw new IllegalStateException("The destination cell is not a neighbor");
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

        // if its peroendicular
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
                throw new IllegalArgumentException("Invalid direction: " + direction);
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
            throw new IllegalStateException("Attempted to execute an invalid move.");
        }

        // Handle different types of moves
        switch (moveType) {
            case SINGLE:
                marbles.get(0).setState(0);
                dest.setState(player);
            case INLINE:
                executeInlineOrSingleMove();
                break;
            case SIDESTEP:
                executeSideStepMove();
                break;
            default:
                throw new IllegalStateException("Unknown move type.");
        }

        // Additional logic if needed (e.g., checking for winning conditions)
    }

    private void executeInlineOrSingleMove() {
        Cell currentCell, nextCell;
        for (int i = marbles.size() - 1; i >= 0; i--) {
            currentCell = marbles.get(i);
            nextCell = currentCell.getNeighborInDirection(directionToDest);
    
            if (nextCell != null) {
                nextCell.setState(currentCell.getState()); // Move the marble to the next cell
                currentCell.setState(0); // Set the current cell to empty
            }
            // Additional logic for pushing opponent's marbles, if necessary
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
}
