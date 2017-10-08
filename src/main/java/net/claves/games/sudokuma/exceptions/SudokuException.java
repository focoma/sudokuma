package net.claves.games.sudokuma.exceptions;

import net.claves.games.sudokuma.SudokuGrid;

public class SudokuException extends RuntimeException {
    private SudokuGrid sudokuGrid;

    public SudokuException(SudokuGrid sudokuGrid, String message) {
        super(message);
        this.sudokuGrid = sudokuGrid;
    }

    public SudokuGrid getSudokuGrid() {
        return sudokuGrid;
    }
}
