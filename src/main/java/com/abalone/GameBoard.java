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
                    //     temp.setScore(score - 1);
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

    public void printBoardScore() {
        // Iterate through each row
        for (int x = 0; x < MAX_ROW_LENGTH; x++) {
            // Print leading spaces for hexagonal alignment
            for (int space = 0; space < MAX_ROW_LENGTH - getRowLength(x); space++) {
                System.out.print(" ");
            }

            // Print cells in the row
            for (int y = 0; y < getRowLength(x); y++) {
                Cell cell = getCellAt(x, y);
                // System.out.print(cell.getState() + " ");
                System.out.print("[" + cell.getScore() + "]");
            }

            // Move to the next line after printing each row
            System.out.println();
        }
    }

    public void printBoard() {
        // Iterate through each row
        for (int x = 0; x < MAX_ROW_LENGTH; x++) {
            // Print leading spaces for hexagonal alignment
            for (int space = 0; space < MAX_ROW_LENGTH - getRowLength(x); space++) {
                System.out.print(" ");
            }

            // Print cells in the row
            for (int y = 0; y < getRowLength(x); y++) {
                Cell cell = getCellAt(x, y);
                System.out.print(cell.getState() + " ");
            }

            // Move to the next line after printing each row
            System.out.println();
        }
    }

    public void printBoardBorders() {
        // Iterate through each row
        for (int x = 0; x < MAX_ROW_LENGTH; x++) {
            // Print leading spaces for hexagonal alignment
            for (int space = 0; space < MAX_ROW_LENGTH - getRowLength(x); space++) {
                System.out.print(" ");
            }

            // Print cells in the row
            for (int y = 0; y < getRowLength(x); y++) {
                Cell cell = getCellAt(x, y);
                System.out.print(((cell.getIsborder() == true) ? 1 : 0) + " ");
            }

            // Move to the next line after printing each row
            System.out.println();
        }
    }

    public static int getRowLength(int x) {
        if (x < 4) {
            return 5 + x; // Rows 0 to 3 increase in length
        } else if (x < 5) {
            return MAX_ROW_LENGTH; // Middle row has the maximum length
        } else {
            return 13 - x; // Rows 5 to 8 decrease in length
        }
    }

    public void printBoardWithCoordinates() {
        // Iterate through each row
        for (int x = 0; x < MAX_ROW_LENGTH; x++) {
            // Print leading spaces for hexagonal alignment
            for (int space = 0; space < MAX_ROW_LENGTH - getRowLength(x); space++) {
                System.out.print("   "); // Adjust the number of spaces based on your coordinate format
            }

            // Print cells in the row along with their coordinates
            for (int y = 0; y < getRowLength(x); y++) {
                String coordinate = String.format("(%d,%d)", x, y);
                System.out.print(coordinate + " ");
            }

            // Move to the next line after printing each row
            System.out.println();
        }
    }

    public void testAndPrintNeighborFinding() {
        int[][] testCoordinates = new int[][] { { 0, 0 }, { 4, 4 }, { 8, 4 }, { 2, 3 }, { 6, 3 }, { 1, 3 } };

        for (int[] coord : testCoordinates) {
            Cell testCell = getCellAt(coord[0], coord[1]);
            if (testCell != null) {
                Map<Cell, Direction> neighborsWithDirection = testCell.getNeighborsMap();
                System.out.println("Neighbors of Cell " + formatCoordinate(coord[0], coord[1]) + ":");
                for (Map.Entry<Cell, Direction> entry : neighborsWithDirection.entrySet()) {
                    Cell neighbor = entry.getKey();
                    Direction direction = entry.getValue();
                    System.out.println(" Neighbor at " + formatCoordinate(neighbor.getX(), neighbor.getY())
                            + " in direction " + direction);
                }
            }
        }
    }

    private String formatCoordinate(int x, int y) {
        return "(" + x + "," + y + ")";
    }

}