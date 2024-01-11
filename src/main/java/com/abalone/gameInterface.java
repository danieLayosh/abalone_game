package com.abalone;

import java.util.List;

public interface gameInterface {
    void makeMove();

    boolean isMoveValid(Move move);

    void updateGameBoard(Move move);

    List<Move> getPossibleMoves();
    
    boolean hasGameEnded();

    //void calculateAIMove();
}
