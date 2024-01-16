package com.abalone;

import java.util.Map;
import java.util.Objects;

import com.abalone.enums.Direction;

import javafx.scene.control.Button;

public class Cell {
    private int x, y, z; // Coordinates on the board
    private int State; // 0 for empty, 1 for player 1 and 2 for player 2
    private Map<Cell, Direction> neighborsMap;
    private Button bt;
    private double score;

    public Cell(int x, int y, int State) {
        this.x = x;
        this.y = y;
        this.State = State;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
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

    public void convertToCube() {
        this.z = -this.x - this.y;
    }

    public int distanceFrom(Cell other) {
        int x1 = this.x;
        int z1 = this.y;
        int y1 = -x1 - z1;

        int x2 = other.x;
        int z2 = other.y;
        int y2 = -x2 - z2;

        return (Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(z1 - z2)) / 2;
    }

}
