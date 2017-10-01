package net.claves.games.sudokuma.validators;

import net.claves.games.Grid;
import net.claves.games.sudokuma.SudokuGrid;
import net.claves.games.sudokuma.Validator;

public class GivenCountValidator implements Validator {

    @Override
    public boolean isValid(SudokuGrid sudokuGrid) {
        int givenCount = 0;
        int size = sudokuGrid.getSize();

        for (int row = 0; row < size; row++) {
            for (Grid.Item item : sudokuGrid.getRow(row)) {
                if (item instanceof SudokuGrid.GivenItem) {
                    givenCount++;
                }
            }
        }

        return givenCount > (int)(size / 0.5625);
    }
}
