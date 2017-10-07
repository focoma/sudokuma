package net.claves.games.sudokuma.validators;

import net.claves.games.Grid;
import net.claves.games.sudokuma.SudokuGrid;
import net.claves.games.sudokuma.SudokuValidator;

import java.util.HashSet;
import java.util.Set;

public class LegalValueManager implements SudokuValidator {
    private Set<Integer> legalValues;
    private int sudokuSize;

    public LegalValueManager(int sudokuSize) {
        this.sudokuSize = sudokuSize;
        this.legalValues = new HashSet<>();
        for (int i = 1; i <= sudokuSize; i++) {
            legalValues.add(i);
        }
    }

    public boolean isValueLegal(Integer value) {
//        return legalValues.contains(value);
        return value == null || value >= 1 && value <= sudokuSize;
    }

    public Set<Integer> getLegalValues() {
        return new HashSet<>(legalValues);
    }

    @Override
    public boolean isValid(SudokuGrid sudokuGrid) {
        int size = sudokuGrid.getSize();
        for (int row = 0; row < size; row++) {
            for (Grid.Item<Integer> item : sudokuGrid.getRow(row)) {
                Integer value = item.getValue();
                if (!isValueLegal(value)) {
                    return false;
                }
            }
        }
        return true;
    }
}
