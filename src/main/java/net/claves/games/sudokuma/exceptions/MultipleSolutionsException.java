package net.claves.games.sudokuma.exceptions;

import net.claves.games.sudokuma.SudokuGrid;

import java.util.Set;

public class MultipleSolutionsException extends SudokuException {
    private Set<SudokuGrid> solutions;
    public MultipleSolutionsException(SudokuGrid sudokuGrid, Set<SudokuGrid> solutions) {
        super(sudokuGrid, "At least two solutions were found! You may study the first two solutions via MultipleSolutionsException#getSolutions()");
        this.solutions = solutions;
    }

    public Set<SudokuGrid> getSolutions() {
        return solutions;
    }
}
