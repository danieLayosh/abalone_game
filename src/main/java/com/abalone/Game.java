package com.abalone;

public class Game {
    private GameBoard board;
    private int whitePlayer;
    private int blackPlayer;
    private int startingPlayer;
    private int currentPlayer;

    public Game(int whitePlayer, int blackPlayer, int startingPlayer) {
        this.board = new GameBoard();
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.startingPlayer = startingPlayer;
        this.currentPlayer = startingPlayer;
    }

    
}
