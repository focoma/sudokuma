package net.claves.games.sudokuma.solvers;

import net.claves.games.Grid;
import net.claves.games.Position;
import net.claves.games.sudokuma.SudokuSolver;
import net.claves.games.sudokuma.SudokuGrid;
import net.claves.games.sudokuma.exceptions.UnsolvableSudokuException;

import java.util.Set;

public class SolverImpl implements SudokuSolver {
    private SudokuGrid sudokuGrid;
    @Override
    public SudokuGrid solve(SudokuGrid sudokuGrid) {
        this.sudokuGrid = sudokuGrid;
        trimPossibilitiesBasedOnValues();

        return sudokuGrid;
    }

    private void trimPossibilitiesBasedOnValues() {
        int size = sudokuGrid.getSize();
        for (int rowIndex = 0; rowIndex < size; rowIndex++) {
            for (int columnIndex = 0; columnIndex < size; columnIndex++) {
                updateItem(sudokuGrid.get(rowIndex, columnIndex));
            }
        }
    }

    private void updateVariable(SudokuGrid.VariableItem item) {
        int rowIndex = item.getRowIndex();
        int columnIndex = item.getColumnIndex();

        trimPossibilitiesFromArrayValues(item, sudokuGrid.getRow(rowIndex));
        trimPossibilitiesFromArrayValues(item, sudokuGrid.getColumn(columnIndex));
        if (sudokuGrid.hasRegions()) {
            trimPossibilitiesFromArrayValues(item, sudokuGrid.getRegion(item.getPosition()));
        }
    }

    private void trimPossibilitiesFromArrayValues(SudokuGrid.VariableItem variableItem, Grid.Item[] array) {
        for (Grid.Item<Integer> item : array) {
            if (!item.getPosition().equals(variableItem.getPosition()) && variableItem.removePossibility(item.getValue())) {
                fireGridChanged(variableItem);
            }
        }
    }

    private void fireGridChanged(SudokuGrid.VariableItem variableItem) {
        Set<Integer> possibilities = variableItem.getPossibilities();
        if (possibilities.isEmpty()) {
            Position position = variableItem.getPosition();
            throw new UnsolvableSudokuException("Item " + position + " has no valid possible value.");
        }

        if (possibilities.size() == 1) {
            variableItem.setValue(possibilities.iterator().next());
        }

        updateNeighbors(variableItem.getPosition());
    }

    private void updateNeighbors(Position position) {
        int size = sudokuGrid.getSize();
        Grid.Item[] row = sudokuGrid.getRow(position.x);
        Grid.Item[] column = sudokuGrid.getColumn(position.y);
        Grid.Item[] region = null;
        if (sudokuGrid.hasRegions()) {
            region = sudokuGrid.getRegion(position);
        }
        for (int index = 0; index < size; index++) {
            updateItem(row[index]);
            updateItem(column[index]);
            if (sudokuGrid.hasRegions()) {
                updateItem(region[index]);
            }
        }
    }

    private void updateItem(Grid.Item item) {
        if (item instanceof SudokuGrid.VariableItem) {
            updateVariable((SudokuGrid.VariableItem) item);
        }
    }
}
