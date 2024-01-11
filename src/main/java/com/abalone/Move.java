package com.abalone;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.abalone.enums.Direction;
import com.abalone.enums.MoveType;

public class Move {
    private List<Cell> marbles;
    private Direction marblesDirection;
    private Cell dest;
    private int player;
    private MoveType moveType; // You might want to include this if you need to store the move type

    public Move(List<Cell> marbles, Cell dest, int player) {
        this.marbles = marbles;
        this.dest = dest;
        this.player = player;
        this.marblesDirection = null;
        this.moveType = null;
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

        System.out.println("The MoveType is: " + determineMoveType(marbles, dest));
        return true;
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
        if (selectedMarbles.size() == 1) {
            Direction direction = marbles.get(0).getDirectionOfNeighbor(dest);
            if (direction != null) {
                return MoveType.SINGLE; // The marble and the destonation marble are not neighbors
            } else {
                throw new IllegalStateException("The destination cell is not a neighbor");
            }
        }

        // if the dest is on one of the ends of the line in both direction
        // if (dest.getNeighborInDirection(marblesDirection) != null
        //         || dest.getNeighborInDirection(oppositeDirection(marblesDirection)) != null) {
        //     return MoveType.INLINE;
        // }


        if(selectedMarbles.get(0).getNeighborsMap().containsKey(dest)){
            if(selectedMarbles.get(0).getNeighborsMap().get(dest).compareTo(oppositeDirection(marblesDirection)) == 0){
                return MoveType.INLINE;
            }
        }

        if(selectedMarbles.get(selectedMarbles.size() - 1).getNeighborsMap().containsKey(dest)){
            if(selectedMarbles.get(selectedMarbles.size() - 1).getNeighborsMap().get(dest).compareTo(marblesDirection) == 0){
                return MoveType.INLINE;
            }
        }













        // if its peroendicular
        Direction direction = selectedMarbles.get(0).getDirectionOfNeighbor(dest);
        if (direction == null) {
            direction = selectedMarbles.get(marbles.size() - 1).getDirectionOfNeighbor(dest);
        }

        if (isPerpendicular(direction, marblesDirection)) {
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
}
