package net.claves.games.sudokuma;

import net.claves.games.Position;
import net.claves.games.PositionsGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SudokuGrid {
    private List<List<GridItem>> itemsByRow;
    private List<List<GridItem>> itemsByColumn;
//    private List<List<GridItem>> itemsBySquare;

    private SudokuGrid() {

    }

    public static SudokuGrid newInstance() {
        SudokuGrid sudokuGrid = new SudokuGrid();
        sudokuGrid.generate();

        return sudokuGrid;
    }

    public static SudokuGrid newInstance(Integer[][] grid) {
        SudokuGrid sudokuGrid = new SudokuGrid();
        sudokuGrid.parseArrayGrid(grid);

        return sudokuGrid;
    }

    private void parseArrayGrid(Integer[][] grid) {
        if (grid.length != 9 || grid[0].length != 9) {
            throw new UnsupportedOperationException("Sudokuma currently supports 9x9 grids only.");
        }

        itemsByRow = new ArrayList<>();
        itemsByColumn = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < grid.length; rowIndex++) {
            List<GridItem> row = new ArrayList<>();
            itemsByRow.add(row);
            for (int columnIndex = 0; columnIndex < grid[rowIndex].length; columnIndex++) {
                Integer value = grid[rowIndex][columnIndex];
                if (value != null && (value < 1 || value > 9)) {
                    throw new IllegalArgumentException("Values can only be from 1 to 9.");
                }

                List<GridItem> column;
                if (rowIndex == 0) {
                    column = new ArrayList<>();
                    itemsByColumn.add(column);
                } else {
                    column = itemsByColumn.get(columnIndex);
                }

                GridItem gridItem = new GridItem(value);

                row.add(gridItem);
                column.add(gridItem);
            }
        }
    }

    private void generate() {
        itemsByRow = new ArrayList<>();
        itemsByColumn = new ArrayList<>();

        for (int rowIndex = 0; rowIndex < 9; rowIndex++) {
            List<GridItem> row = new ArrayList<>();
            itemsByRow.add(row);
            for (int columnIndex = 0; columnIndex < 9; columnIndex++) {
                List<GridItem> column;
                if (rowIndex == 0) {
                    column = new ArrayList<>();
                    itemsByColumn.add(column);
                } else {
                    column = itemsByColumn.get(columnIndex);
                }

                GridItem gridItem = new GridItem(generateValueFor(new Position(rowIndex, columnIndex)));

                row.add(gridItem);
                column.add(gridItem);
            }
        }

        Collection<Position> positionsToPopulate = getPositionsGenerator().generate();
        for (int rowIndex = 0; rowIndex < 9; rowIndex++) {
            for (int columnIndex = 0; columnIndex < 9; columnIndex++) {
                GridItem gridItem = getGridItem(rowIndex, columnIndex);
                if (positionsToPopulate.contains(new Position(rowIndex, columnIndex))) {
                    gridItem.setFinal();
                } else {
                    gridItem.setValue(null);
                }
            }
        }
    }

    protected PositionsGenerator getPositionsGenerator() {
        return new SudokuInitialPositionsGenerator();
    }

    protected Integer generateValueFor(Position position) {
        List<Integer> validValuesForPosition = getValidValuesFor(position);
        if (validValuesForPosition.isEmpty()) {
            return null;
        }

        return validValuesForPosition.get((((int) (Math.random() * 1000)) % validValuesForPosition.size()));
    }

    protected List<Integer> getValidValuesFor(Position position) {
        List<Integer> validValues = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
        for (GridItem gridItem : itemsByRow.get(position.x)) {
            validValues.remove(gridItem.getValue());
        }
        for (GridItem gridItem : itemsByColumn.get(position.y)) {
            validValues.remove(gridItem.getValue());
        }

        return validValues;
    }

    public int getItem(int row, int column) {
        return itemsByRow.get(row).get(column).value;
    }

    public GridItem getGridItem(int row, int column) {
        return itemsByRow.get(row).get(column);
    }

    public boolean isValid() {
        for (List<GridItem> row : itemsByRow) {
            if (!itemsAreUnique(row)) {
                return false;
            }
        }

        for (List<GridItem> column : itemsByColumn) {
            if (!itemsAreUnique(column)) {
                return false;
            }
        }

        return hasEnoughGivens();
    }

    public boolean hasEnoughGivens() {
        int givenCount = 0;
        for (List<GridItem> row : itemsByRow) {
            for (GridItem gridItem : row) {
                if (gridItem.getValue() != null) {
                    givenCount++;
                }
            }
        }

        return givenCount > 16;
    }

    private boolean itemsAreUnique(List<GridItem> row) {
        List<Integer> processedValues = new ArrayList<>();
        for (GridItem gridItem : row) {
            if (gridItem.getValue() != null && processedValues.contains(gridItem.getValue())) {
                return false;
            }
        }

        return true;
    }

    private class GridItem {
        private Integer value;
        private boolean isFinal;

        public GridItem(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) throws IllegalAccessError {
            if (isFinal) {
                throw new IllegalAccessError();
            }
            this.value = value;
        }

        void setFinal() {
            this.isFinal = true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            GridItem gridItem = (GridItem) o;

            return value != null && value.equals(gridItem.getValue());
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }

}