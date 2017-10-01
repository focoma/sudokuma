package net.claves.games.sudokuma;

import net.claves.games.Grid;
import net.claves.games.Position;
import net.claves.games.PositionsGenerator;

import java.util.*;

public class SudokuGrid extends Grid<Integer> {
    private Item[][] itemsByReqion;

    private int regionSize;

    private List<Validator> validators;

    public SudokuGrid(int size) {
        super(size);

        int sqrt = (int) Math.sqrt(size);
        if (sqrt * sqrt == size) {
            regionSize = sqrt;
        }
        if (regionSize > 0) {
            itemsByReqion = new Item[size][size];
        }
    }

    @Override
    public Item put(Position position, Item gridItem) {
        super.put(position, gridItem);
        if (regionSize > 0) {
            getRegion(position)
                    [(position.x % regionSize) * regionSize + (position.y % regionSize)] = gridItem;
        }

        return gridItem;
    }

    @Override
    protected Item<Integer> getEmptyItem() {
        return new VariableItem(1, 9);
    }

    private Item[] getRegion(Position position) {
        return itemsByReqion
                [(position.x / regionSize) * regionSize + (position.y / regionSize)];
    }

    public static SudokuGrid newInstance() {
        SudokuGrid sudokuGrid = new SudokuGrid(9);
        sudokuGrid.generate();

        return sudokuGrid;
    }

    private void generate() {
        Collection<Position> givenPositions = getPositionsGenerator().generate();
        for (int rowIndex = 0; rowIndex < 9; rowIndex++) {
            for (int columnIndex = 0; columnIndex < 9; columnIndex++) {
                Integer randomValue = generateValueFor(new Position(rowIndex, columnIndex));
                Item<Integer> gridItem;
                if (givenPositions.contains(new Position(rowIndex, columnIndex))) {
                    gridItem = new GivenItem(randomValue);
                } else {
                    gridItem = getEmptyItem();
                    gridItem.setValue(randomValue);
                }

                put(new Position(rowIndex, columnIndex), gridItem);
            }
        }

        for (int rowIndex = 0; rowIndex < 9; rowIndex++) {
            for (int columnIndex = 0; columnIndex < 9; columnIndex++) {
                if (!givenPositions.contains(new Position(rowIndex, columnIndex))) {
                    remove(new Position(rowIndex, columnIndex));
                }
            }
        }
    }

    protected PositionsGenerator getPositionsGenerator() {
        return new GivenPositionsGenerator(size);
    }

    protected Integer generateValueFor(Position position) {
        List<Integer> validValuesForPosition = getValidValuesFor(position);
        if (validValuesForPosition.isEmpty()) {
            return null;
        }

        return validValuesForPosition.get((((int) (Math.random() * 1000)) % validValuesForPosition.size()));
    }

    private Item<Integer> getGridItem(int row, int column) {
        return itemsByRow[row][column];
    }

    protected List<Integer> getValidValuesFor(Position position) {
        List<Integer> validValues = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
        for (Item<Integer> gridItem : itemsByRow[position.x]) {
            if (gridItem != null) {
                validValues.remove(gridItem.getValue());
            }
        }
        for (Item<Integer> gridItem : itemsByColumn[position.y]) {
            if (gridItem != null) {
                validValues.remove(gridItem.getValue());
            }
        }
        if (regionSize > 0) {
            for (Item gridItem : getRegion(position)) {
                if (gridItem != null) {
                    validValues.remove(gridItem.getValue());
                }
            }
        }

        return validValues;
    }

    public static SudokuGrid newInstance(Integer[][] squareGrid) {
        int size = squareGrid.length;
        if (size != squareGrid[0].length) {
            throw new IllegalArgumentException("The grid must be a square.");
        }
        SudokuGrid sudokuGrid = new SudokuGrid(size);
        sudokuGrid.parseArrayGrid(squareGrid);

        return sudokuGrid;
    }

    private void parseArrayGrid(Integer[][] grid) {
        for (int rowIndex = 0; rowIndex < grid.length; rowIndex++) {
            for (int columnIndex = 0; columnIndex < grid[rowIndex].length; columnIndex++) {
                Integer value = grid[rowIndex][columnIndex];
                if (value != null && (value < 1 || value > size)) {
                    throw new IllegalArgumentException("Values can only be from 1 to " + size + ".");
                }

                put(new Position(rowIndex, columnIndex), new GivenItem(value));
            }
        }
    }

    public Integer getItem(int row, int column) {
        return getGridItem(row, column).getValue();
    }

    public boolean isValid() {
        for (Item[] row : itemsByRow) {
            if (!itemsAreUnique(row)) {
                return false;
            }
        }

        for (Item[] column : itemsByColumn) {
            if (!itemsAreUnique(column)) {
                return false;
            }
        }

        return true;
    }

    private boolean itemsAreUnique(Item<Integer>[] row) {
        List<Integer> processedValues = new ArrayList<>();
        for (Item<Integer> gridItem : row) {
            if (gridItem.getValue() != null && processedValues.contains(gridItem.getValue())) {
                return false;
            }
            processedValues.add(gridItem.getValue());
        }

        return true;
    }

    public static class GivenItem extends Item<Integer> {
        public GivenItem(Integer value) {
            super(value);
        }

        @Override
        public void setValue(Integer value) {
            throw new UnsupportedOperationException("The value is final for given items.");
        }
    }

    public static class VariableItem extends Item<Integer> {
        private Set<Integer> possibilities;

        public VariableItem(int start, int end) {
            super(null);
            possibilities = new HashSet<>();
            for (int i = start; i <= end; i++) {
                possibilities.add(i);
            }
        }

        public void removePossibility(Integer possibility) {
            possibilities.remove(possibility);
        }

        public Set<Integer> getPossibilities() {
            return possibilities;
        }

        public void setPossibilities(Set<Integer> possibilities) {
            this.possibilities = possibilities;
        }
    }
}