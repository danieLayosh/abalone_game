package com.abalone.stats;

import java.util.ArrayList;

import com.abalone.Move;

public class LastGameStats {
    private int white; // white player
    private int black; // black player
    private String whiteType; // human or computer
    private String blackType; // human or computer
    private String winner; // 0 if draw, 1 if white, 2 if black
    private String gameTime; // time of the game
    private int totalTurns; // total moves in the game
    private int whiteTurnsCount; // moves made by white
    private int blackTurnsCount; // moves made by black
    private ArrayList<Move> whiteMoves; // moves made by white
    private ArrayList<Move> blackMoves; // moves made by black
    private ArrayList<ArrayList<Move>> whiteAllPossibleMoves; // all possible moves for white
    private ArrayList<ArrayList<Move>> blackAllPossibleMoves; // all possible moves for black

    public LastGameStats(int white, int black, String whiteType, String blackType) {
        this.white = white;
        this.black = black;
        this.whiteType = whiteType;
        this.blackType = blackType;
        this.winner = "no winner";
        this.gameTime = "00:00:00";
        this.totalTurns = 0;
        this.whiteTurnsCount = 0;
        this.blackTurnsCount = 0;
        this.whiteMoves = new ArrayList<Move>();
        this.blackMoves = new ArrayList<Move>();
        this.whiteAllPossibleMoves = new ArrayList<ArrayList<Move>>();
        this.blackAllPossibleMoves = new ArrayList<ArrayList<Move>>();
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getGameTime() {
        return gameTime;
    }

    public void setGameTime(String gameTime) {
        this.gameTime = gameTime;
    }

    public int getTotalTurns() {
        return totalTurns;
    }

    public void incrementTotalTurns() {
        this.totalTurns++;
    }

    public int getWhiteTurnsCount() {
        return whiteTurnsCount;
    }

    public void incrementWhiteTurnsCount() {
        this.whiteTurnsCount++;
    }

    public int getBlackTurnsCount() {
        return blackTurnsCount;
    }

    public void incrementBlackTurnsCount() {
        this.blackTurnsCount++;
    }

    public ArrayList<Move> getWhiteMoves() {
        return whiteMoves;
    }

    public void addWhiteMove(Move move) {
        this.whiteMoves.add(move);
    }

    public void addBlackMove(Move move) {
        this.blackMoves.add(move);
    }

    public ArrayList<Move> getBlackMoves() {
        return blackMoves;
    }

    public ArrayList<ArrayList<Move>> getWhiteAllPossibleMoves() {
        return whiteAllPossibleMoves;
    }

    public void addWhiteAllPossibleMoves(ArrayList<Move> moves) {
        this.whiteAllPossibleMoves.add(moves);
    }

    public ArrayList<ArrayList<Move>> getBlackAllPossibleMoves() {
        return blackAllPossibleMoves;
    }

    public void addBlackAllPossibleMoves(ArrayList<Move> moves) {
        this.blackAllPossibleMoves.add(moves);
    }

    @Override
    public String toString() {
        return "LastGameStats{" +
                "white =" + white +
                ", black =" + black +
                ", white Type ='" + whiteType + '\'' +
                ", black Type ='" + blackType + '\'' +
                ", winner = " + winner +
                ", gameTime = '" + gameTime + '\'' +
                ", total Turns = " + totalTurns +
                ", white Turns Count = " + whiteTurnsCount +
                ", black Turns Count = " + blackTurnsCount +
                '}';
    }
}
