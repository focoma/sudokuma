package net.claves.games.sudokuma.exceptions;

import net.claves.games.sudokuma.SudokuGrid;

public class UnsolvableSudokuException extends SudokuException {
    public UnsolvableSudokuException(SudokuGrid sudokuGrid, String message) {
        super(sudokuGrid, message);
    }
}
