package com.abalone;

// import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
// import java.util.List;
import java.util.Map;

import com.abalone.enums.Direction;
// import com.abalone.enums.MoveType;

public class GameBoard {
    // Adjacency map to represent the board, each cell has a list of neighbors
    private Map<Cell, Map<Cell, Direction>> board;

    public final static int MAX_ROW_LENGTH = 9; // Maximum number of cells in a row

    // 2D Arrays to represent the six possible directions (neighbors) in a hexagonal
    // grid for the lower, upper and middle rows
    private final int[][] directionsUpperPart = new int[][] { { 0, 1 }, { 0, -1 }, { -1, 0 }, { -1, -1 }, { 1, 1 },
            { 1, 0 } };
    private final int[][] directionsLowerPart = new int[][] { { 0, 1 }, { 0, -1 }, { -1, 1 }, { -1, 0 }, { 1, 0 },
            { 1, -1 } };
    private final int[][] directionsMiddleRow = new int[][] { { 0, 1 }, { 0, -1 }, { -1, 0 }, { -1, -1 }, { 1, 0 },
            { 1, -1 } };

    // Constructor for GameBoard
    public GameBoard() {
        this.board = new HashMap<>();
        initializeBoard();
    }

    // Constructor for GameBoard
    public GameBoard(Map<Cell, Map<Cell, Direction>> board) {
        this.board = board;
    }

    // Initializes the board by creating cells and establishing their adjacencies
    private void initializeBoard() {
        // Iterate through all possible positions on the board
        for (int x = 0; x < MAX_ROW_LENGTH; x++) {
            for (int y = 0; y < MAX_ROW_LENGTH; y++) {
                // Check if the position is valid on the hexagonal board
                if (isValidPosition(x, y)) {
                    int state = determineInitialState(x, y);
                    Cell cell = new Cell(x, y, state);
                    // Initialize neighborsMap for the cell
                    cell.setNeighborsMap(new HashMap<>());
                    board.put(cell, cell.getNeighborsMap()); // Use a HashMap for neighbors
                }
            }
        }

        // Establish adjacencies for each cell
        for (Cell cell : board.keySet()) {
            Map<Cell, Direction> neighborsWithDirection = findNeighbors(cell);
            // board.put(cell, neighborsWithDirection);
            cell.setNeighborsMap(neighborsWithDirection);
        }
        // sets cells scores
        cellsScore();
    }

    private void cellsScore() {
        Direction dir[] = { Direction.DOWNLEFT, Direction.LEFT, Direction.UPLEFT, Direction.UPRIGHT, Direction.RIGHT,
                Direction.DOWNRIGHT };

        double score = 4;
        Cell currnt = getCellAt(4, 4);
        currnt.setScore(score);
        score--;
        Cell temp = currnt;
        for (int i = 1; i <= 4; i++) {
            temp = temp.getNeighborInDirection(Direction.RIGHT);
            for (int d = 0; d < 6; d++) {
                for (int h = 0; h < i; h++) {
                    temp = temp.getNeighborInDirection(dir[d]);
                    temp.setScore(score);
                    // if (i > 1 && h != i - 1)
                    // temp.setScore(score - 1);
                }
            }
            score -= 2;
        }
    }

    // Finds and returns a list of neighboring cells for a given cell
    private Map<Cell, Direction> findNeighbors(Cell cell) {

        Map<Cell, Direction> neighbors = new HashMap<>();
        int x = cell.getX();
        int y = cell.getY();

        // determines the suitable directions array, according to the row
        int[][] directions = x <= 3 ? directionsUpperPart : x >= 5 ? directionsLowerPart : directionsMiddleRow;
        Direction[] dirEnums = new Direction[] { Direction.RIGHT, Direction.LEFT, Direction.UPRIGHT, Direction.UPLEFT,
                Direction.DOWNRIGHT, Direction.DOWNLEFT };

        for (int i = 0; i < directions.length; i++) {
            int newX = x + directions[i][0];
            int newY = y + directions[i][1];
            Cell neighbor = getCellAt(newX, newY);
            if (neighbor != null) {
                neighbors.put(neighbor, dirEnums[i]);
            }
        }
        return neighbors;
    }

    // Returns the Cell object at a given set of coordinates
    public Cell getCellAt(int x, int y) {
        // Iterate through all cells in the board
        for (Cell cell : board.keySet()) {
            if (cell.getX() == x && cell.getY() == y) {
                return cell; // Return the matching cell
            }
        }
        return null; // Return null if no matching cell is found
    }

    // Determines if a given position is valid on the hexagonal board
    private boolean isValidPosition(int x, int y) {
        switch (x) {
            case 0:
            case 8:
                return y >= 0 && y <= 4; // 5 cells on the first and last rows
            case 1:
            case 7:
                return y >= 0 && y <= 5; // 6 cells on the second and second-to-last rows
            case 2:
            case 6:
                return y >= 0 && y <= 6; // 7 cells on the third and third-to-last rows
            case 3:
            case 5:
                return y >= 0 && y <= 7; // 8 cells on the fourth and fourth-to-last rows
            case 4:
                return y >= 0 && y <= 8; // 9 cells on the middle row
            default:
                return false;
        }
    }

    // Determines the initial state of each cell based on its position
    private int determineInitialState(int x, int y) {
        // Black pieces in the initial setup
        if (x == 0 || x == 1 || (x == 2 && y >= 2 && y <= 4))
            return 2;
        // White pieces in the initial setup
        else if (x == 8 || x == 7 || (x == 6 && y >= 2 && y <= 4))
            return 1;

        // Empty spaces
        return 0;
    }

    public static String getCellId(int x, int y) {
        return "bt" + x + "_" + y;
    }

    public Collection<Cell> getCells() {
        return board.keySet();
    }

    public Map<Cell, Map<Cell, Direction>> getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof GameBoard))
            return false;
        GameBoard other = (GameBoard) obj;

        if (this.board.size() != other.board.size())
            return false;

        for (Cell cell : this.board.keySet()) {
            Map<Cell, Direction> thisNeighbors = this.board.get(cell);
            Map<Cell, Direction> otherNeighbors = other.board.get(cell);

            if (otherNeighbors == null || !thisNeighbors.equals(otherNeighbors)) {
                return false;
            }
        }

        return true;
    }

    // @Override
    // public int hashCode() {
    // int result = 1;
    // for (Map.Entry<Cell, Map<Cell, Direction>> entry : board.entrySet()) {
    // int cellHash = entry.getKey().hashCode(); // Correctly obtain hash code of
    // the key
    // int neighborsHash = entry.getValue().hashCode(); // Obtain hash code of the
    // value (Map)
    // result = 31 * result + cellHash + neighborsHash; // Combine hash codes
    // appropriately
    // }
    // return result;
    // }

    @Override
    public int hashCode() {
        int result = 17; // Starting with a non-zero constant.
        for (Map.Entry<Cell, Map<Cell, Direction>> entry : board.entrySet()) {
            Cell cell = entry.getKey();
            Map<Cell, Direction> neighbors = entry.getValue();
            
            int cellHash = cell.hashCode(); // Hash code of the cell itself, assuming Cell's hashCode includes its state.
            int neighborsHash = neighbors.hashCode(); // Hash code of the neighbors map.
            
            // Optionally, directly include the cell's state in the hash code calculation for clarity.
            int cellState = cell.getState(); // Assuming Cell has a getState() method returning its state.
            
            result = 31 * result + cellHash;
            result = 31 * result + neighborsHash;
            result = 31 * result + cellState; // Include the cell's state in the hash code calculation.
        }
        return result;
    }

}