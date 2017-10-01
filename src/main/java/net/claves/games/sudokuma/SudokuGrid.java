package net.claves.games.sudokuma;

import net.claves.games.Grid;
import net.claves.games.Position;
import net.claves.games.PositionsGenerator;
import net.claves.games.sudokuma.validators.GivenCountValidator;
import net.claves.games.sudokuma.validators.UniqueItemsValidator;

import java.util.*;

public class SudokuGrid extends Grid<Integer> {
    private Item[][] itemsByReqion;

    private int regionSize;

    private List<Validator> validators;
    private List<Integer> validValues;

    private Solver solver;

    public SudokuGrid(int size) {
        super(size);

        validators = new ArrayList<>();
        validators.add(new GivenCountValidator());
        validators.add(new UniqueItemsValidator());

        validValues = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            validValues.add(i);
        }

        int sqrt = (int) Math.sqrt(size);
        if (sqrt * sqrt == size) {
            regionSize = sqrt;
        }
        if (regionSize > 0) {
            itemsByReqion = new Item[size][size];
        }
    }

    public List<Validator> getValidators() {
        return validators;
    }

    public void setValidators(List<Validator> validators) {
        this.validators = validators;
    }

    public Solver getSolver() {
        return solver;
    }

    public void setSolver(Solver solver) {
        this.solver = solver;
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
        return new VariableItem(1, size);
    }

    public Item[] getRegion(Position position) {
        return itemsByReqion
                [(position.x / regionSize) * regionSize + (position.y / regionSize)];
    }

    public Item[] getRegion(int region) {
        return itemsByReqion[region];
    }

    public static SudokuGrid newInstance() {
        SudokuGrid sudokuGrid = new SudokuGrid(9);
        sudokuGrid.generate();

        return sudokuGrid;
    }

    private void generate() {
        Collection<Position> givenPositions = getPositionsGenerator().generate();
        for (int rowIndex = 0; rowIndex < size; rowIndex++) {
            for (int columnIndex = 0; columnIndex < size; columnIndex++) {
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

        for (int rowIndex = 0; rowIndex < size; rowIndex++) {
            for (int columnIndex = 0; columnIndex < size; columnIndex++) {
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

    @Override
    public Object clone() {
        return newInstance(getIntegerArray());
    }

    private Integer[][] getIntegerArray() {
        Integer[][] itemsArray = new Integer[size][size];
        for (int row = 0; row < size; row ++) {
            for (int column = 0; column < size; column++) {
                itemsArray[row][column] = get(row, column).getValue();
            }
        }
        return itemsArray;
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

    public boolean hasRegions() {
        return regionSize > 0;
    }

    public boolean isValid() {
        for (Validator validator : validators) {
            if (!validator.isValid(this)) {
                return false;
            }
        }
        return true;
    }

    public SudokuGrid solve() {
        return solver.solve((SudokuGrid) clone());
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