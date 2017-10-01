package net.claves.games.sudokuma.validators;

import net.claves.games.Grid;
import net.claves.games.sudokuma.SudokuGrid;
import net.claves.games.sudokuma.Validator;

import java.util.HashSet;
import java.util.Set;

public class UniqueItemsValidator implements Validator {

    @Override
    public boolean isValid(SudokuGrid sudokuGrid) {
        int size = sudokuGrid.getSize();

        for (int index = 0; index < size; index++) {
            if (!isListUnique(sudokuGrid.getRow(index))) {
                return false;
            }
            if (!isListUnique(sudokuGrid.getColumn(index))) {
                return false;
            }
            if (!isListUnique(sudokuGrid.getRegion(index))) {
                return false;
            }
        }

        return true;
    }

    private boolean isListUnique(Grid.Item[] list) {
        Set<Grid.Item> processedItems = new HashSet<>();
        for (Grid.Item item : list) {
            if (!processedItems.add(item)) {
                return false;
            }
        }
        return true;
    }
}
