# Abalone Game

## Description
This project is an implementation of the classic board game Abalone. It is developed using Java, featuring a robust design that includes classes for the game board, cells, moves, and game logic. The game is played on a hexagonal board with a specific set of rules that govern the movement of marbles and their interactions.

## Features
- **Game Board Representation**: A hexagonal board layout with an efficient mapping of cells and their neighbors.
- **Move Validation**: Logic to validate the moves based on the game rules, including checking for valid destinations and move types (single, inline, sidestep).
- **Directional Logic**: Each cell knows its neighbors and the relative directions to them.
- **Marble Sorting**: Functionality to sort marbles based on their positions for consistent move processing.
- **Game State Management**: Tracks the state of each cell (empty, player 1, player 2) and updates it throughout the game.

## How to Play
- Each player starts with an equal number of marbles on opposite sides of the board.
- Players take turns moving their marbles in one of three ways: single, inline, or sidestep.
- The goal is to push your opponent's marbles off the board. The first player to push off six of the opponent's marbles wins.
