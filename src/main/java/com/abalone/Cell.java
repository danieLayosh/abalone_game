package com.abalone;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.abalone.enums.Direction;

import javafx.scene.control.Button;

public class Cell {
    private int x, y; // Coordinates on the board
    private int State; // 0 for empty, 1 for player 1 and 2 for player 2
    private Map<Cell, Direction> neighborsMap;
    private Button bt;
    private double score;
    private boolean isborder;

    public Cell(int x, int y, int State) {
        this.x = x;
        this.y = y;
        this.State = State;
        this.isborder = isBorder();
    }

    private boolean isBorder() {
        if (x == 0 || x == 8) {
            return true;
        } else {
            if (y == 0) {
                return true;
            } else {
                return getRowLength(x) == y + 1;
            }
        }
    }

    public static int getRowLength(int x) {
        if (x < 4) {
            return 5 + x; // Rows 0 to 3 increase in length
        } else if (x < 5) {
            return 9; // Middle row has the maximum length
        } else {
            return 13 - x; // Rows 5 to 8 decrease in length
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getState() {
        return State;
    }

    public void setState(int State) {
        this.State = State;

    }

    public Map<Cell, Direction> getNeighborsMap() {
        return neighborsMap;
    }

    public Cell getNeighborInDirection(Direction dir) {
        if (neighborsMap == null) {
            return null;
        }

        for (Map.Entry<Cell, Direction> entry : neighborsMap.entrySet()) {
            if (entry.getValue() == dir) {
                return entry.getKey();
            }
        }
        return null; // Return null if no neighbor is found in the given direction
    }

    /**
     * Finds the direction of the specified neighbor cell relative to this cell.
     * 
     * @param neighbor The cell for which to find the direction.
     * @return The direction of the neighbor cell, or null if the cell is not a
     *         neighbor.
     */
    public Direction getDirectionOfNeighbor(Cell neighbor) {
        if (neighborsMap == null) {
            return null; // neighborsMap not initialized
        }
        return neighborsMap.getOrDefault(neighbor, null);
    }

    public void setNeighborsMap(Map<Cell, Direction> neighborsMap) {
        this.neighborsMap = neighborsMap;
    }

    public String formatCoordinate() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Cell cell = (Cell) obj;
        return x == cell.x && y == cell.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public Button getBt() {
        return bt;
    }

    public void setBt(Button bt) {
        this.bt = bt;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    // Method to get neighbor cells
    public Set<Cell> getNeighbors() {
        if (neighborsMap == null) {
            return new HashSet<>(); // Return an empty set if neighborsMap is null
        }
        return new HashSet<>(neighborsMap.keySet());
    }

    public boolean getIsborder() {
        return isborder;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cell: ").append(formatCoordinate());
        sb.append(", State: ").append(getState());
        sb.append(", isBorder: ").append(isborder);
        sb.append(" ");
        return sb.toString();
    }

}
