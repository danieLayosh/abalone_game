package com.abalone.stats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.abalone.Move;

public class LastGameStats {
    private int white; // white player
    private int black; // black player
    private String whiteType; // human or computer
    private String blackType; // human or computer

    private String winner; // "WHITE", "BLACK", "DRAW - No winner"
    private String gameTime; // time of the game in format "HH:MM:SS"

    private int totalTurns; // total moves in the game
    private int whiteTurnsCount; // moves made by white
    private int blackTurnsCount; // moves made by black

    private ArrayList<Move> whiteExecutedMoves; // moves made by white
    private ArrayList<Move> blackExecutedMoves; // moves made by black

    private int whiteBestMoves; // if human best moves for white
    private int blackBestMoves; // if human best moves for black

    private ArrayList<ArrayList<Move>> whiteAllPossibleMovesInEachTurn; // all possible moves for white
    private ArrayList<ArrayList<Move>> blackAllPossibleMovesInEachTurn; // all possible moves for black

    private ArrayList<String> whiteTurnsResults;
    private ArrayList<String> blackTurnsResults;

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
        this.whiteExecutedMoves = new ArrayList<Move>();
        this.blackExecutedMoves = new ArrayList<Move>();
        this.whiteAllPossibleMovesInEachTurn = new ArrayList<ArrayList<Move>>();
        this.blackAllPossibleMovesInEachTurn = new ArrayList<ArrayList<Move>>();
        this.whiteTurnsResults = new ArrayList<String>();
        this.blackTurnsResults = new ArrayList<String>();
        this.whiteBestMoves = 0;
        this.blackBestMoves = 0;
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

    public int getWhiteTurnsCount() {
        return whiteTurnsCount;
    }

    public int getBlackTurnsCount() {
        return blackTurnsCount;
    }

    public void addPlayerMove(int player, Move move) {
        if (player == 1) {
            whiteExecutedMoves.add(move);
            this.whiteTurnsCount++;
        } else {
            blackExecutedMoves.add(move);
            this.blackTurnsCount++;
        }
        this.totalTurns++;
    }

    public ArrayList<Move> getBlackMoves() {
        return blackExecutedMoves;
    }

    public ArrayList<Move> getWhiteMoves() {
        return whiteExecutedMoves;
    }

    public ArrayList<ArrayList<Move>> getWhiteAllPossibleMoves() {
        return whiteAllPossibleMovesInEachTurn;
    }

    public ArrayList<ArrayList<Move>> getBlackAllPossibleMoves() {
        return blackAllPossibleMovesInEachTurn;
    }

    public void addPlayerAllPossibleMoves(int player, ArrayList<Move> moves) {
        if (player == 1) {
            this.whiteAllPossibleMovesInEachTurn.add(moves);
        } else {
            this.blackAllPossibleMovesInEachTurn.add(moves);
        }
    }

    public void playerDidBestMove(int player) {
        if (player == 1) {
            whiteBestMoves++;
        } else {
            blackBestMoves++;
        }
    }

    private void calculateStatsForColor(String playerColor, String playerType,
            ArrayList<ArrayList<Move>> movesInEachTurn,
            ArrayList<String> turnsResults,
            int bestMoves) {
        if (movesInEachTurn.size() < 2) {
            turnsResults.add("Not enough data to calculate statistics.");
            return;
        }

        turnsResults.add("Statistics for " + playerColor + " player:");

        double totalPercent = 0;
        int comparisons = 0;

        for (int i = 0; i < movesInEachTurn.size() - 1; i++) {
            ArrayList<Move> currentList = movesInEachTurn.get(i);
            ArrayList<Move> nextList = movesInEachTurn.get(i + 1);

            int equalMovesCount = 0;
            for (Move move : currentList) {
                if (nextList.contains(move)) {
                    equalMovesCount++;
                }
            }

            double averageListSize = (currentList.size() + nextList.size()) / 2.0;
            double percent = (equalMovesCount / averageListSize) * 100;

            String result = String.format(
                    "Equal moves between turns %d and %d: %d out of an average of %.0f moves (%.2f%%)",
                    i, i + 1, equalMovesCount, averageListSize, percent);
            turnsResults.add(result);

            totalPercent += percent;
            comparisons++;
        }

        double avgPercent = totalPercent / comparisons;
        turnsResults.add(String.format("Average percentage of equal moves between adjacent turns: %.2f%%", avgPercent));

        turnsResults.add("The " + playerColor + " - " + playerType + ": " + bestMoves + " best moves, from "
                + (totalTurns / 2) + " moves");
        turnsResults.add(playerColor + " score: " + (bestMoves * 100 / (totalTurns / 2)) + " out of 100\n");
    }

    public void calcGameStats() {
        calculateStatsForColor("BLACK", blackType, blackAllPossibleMovesInEachTurn, blackTurnsResults, blackBestMoves);
        calculateStatsForColor("WHITE", whiteType, whiteAllPossibleMovesInEachTurn, whiteTurnsResults, whiteBestMoves);
    }

    public boolean downloadStatsFile() {
        // Save in Downloads folder
        Path downloadPath = saveStatsToFile(System.getProperty("user.home") + File.separator + "Downloads");

        String appStatsDir = "games_statistics/";
        Path appStatsPath = saveStatsToFile(appStatsDir);

        if (downloadPath != null && appStatsPath != null) {
            System.out.println("Statistics file saved successfully in both locations.");
            return true;
        } else {
            System.err.println("Failed to save the statistics file in one or both locations.");
            return false;
        }
    }

    private Path saveStatsToFile(String directory) {
        String baseFileName = "gameStats";
        String fileExtension = ".txt";
        Path dirPath = Paths.get(directory);
        try {
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath); // Create the directory if it doesn't exist
            }

            Path filePath = dirPath.resolve(baseFileName + fileExtension);

            // Check if the file exists and append a counter to find a unique filename
            int counter = 1;
            while (Files.exists(filePath)) {
                String newFileName = baseFileName + "_" + counter + fileExtension;
                filePath = dirPath.resolve(newFileName);
                counter++;
            }

            // Write stats to the new file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(); // This captures the current date and time
                String formattedDate = dateFormat.format(date);
                writer.write("Game Statistics - Recorded on: " + formattedDate + "\n\n");

                writer.write("Game Statistics:\n");
                writer.write("White player: " + white + " - " + whiteType + "\n");
                writer.write("Black player: " + black + " - " + blackType + "\n");
                writer.write("Winner: " + winner + "\n");
                writer.write("Game time: " + gameTime + "\n");
                writer.write("Total turns: " + totalTurns + "\n");
                writer.write("white Turns Count = " + whiteTurnsCount + "\n");
                writer.write("black Turns Count = " + blackTurnsCount + "\n");

                writer.write("White Player Statistics:\n");
                for (String result : whiteTurnsResults) {
                    writer.write(result + "\n");
                }

                writer.write("\nBlack Player Statistics:\n");
                for (String result : blackTurnsResults) {
                    writer.write(result + "\n");
                }
                System.out.println("Statistics file saved successfully: " + filePath);
                return filePath;
            } catch (IOException e) {
                System.err.println("An error occurred while writing the statistics file: " + e.getMessage());
                return null;
            }
        } catch (IOException e) {
            System.err.println("An error occurred while creating the directory: " + e.getMessage());
            return null;
        }
    }

    public Path makeTempStatsFileAndShowIt() {
        String appStatsDir = "games_statistics/";
        Path appStatsPath = saveStatsToFile(appStatsDir);
        if (appStatsPath != null) {
            System.out.println("Statistics file saved successfully in app memory.");
        } else {
            System.err.println("Failed to save the statistics file in the app memory.");
        }
        // Create a temporary file
        File tempFile;
        try {
            tempFile = File.createTempFile("gameStats", ".txt");
            tempFile.deleteOnExit(); // Ensure the file is deleted when the program exits

            // Write statistics to the temporary file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write("Game Statistics:\n");
                writer.write("White player: " + white + " - " + whiteType + "\n");
                writer.write("Black player: " + black + " - " + blackType + "\n");
                writer.write("Winner: " + winner + "\n");
                writer.write("Game time: " + gameTime + "\n");
                writer.write("Total turns: " + totalTurns + "\n");
                writer.write("white Turns Count = " + whiteTurnsCount + "\n");
                writer.write("black Turns Count = " + blackTurnsCount + "\n");

                writer.write("White Player Statistics:\n");
                for (String result : whiteTurnsResults) {
                    writer.write(result + "\n");
                }

                writer.write("\nBlack Player Statistics:\n");
                for (String result : blackTurnsResults) {
                    writer.write(result + "\n");
                }
            }

            return Paths.get(tempFile.toURI());
        } catch (IOException e) {
            System.err.println("An error occurred while handling the temporary statistics file: " + e.getMessage());
            return null;
        }
    }

}