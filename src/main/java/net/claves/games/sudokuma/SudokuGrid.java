package net.claves.games.sudokuma;

import net.claves.games.Grid;
import net.claves.games.Position;
import net.claves.games.PositionsGenerator;
import net.claves.games.sudokuma.solvers.SudokuSolverImpl;
import net.claves.games.sudokuma.validators.GivenCountValidator;
import net.claves.games.sudokuma.validators.LegalValueManager;
import net.claves.games.sudokuma.validators.UniqueItemsValidator;

import java.util.*;

public class SudokuGrid extends Grid<Integer> {
    private Item[][] itemsByReqion;

    private int regionSize;

    private List<SudokuValidator> validators;
    private LegalValueManager legalValueManager;

    private SudokuSolver solver;

    public SudokuGrid(int size) {
        super(size);

        List<SudokuValidator> validators = new ArrayList<>();
        validators.add(new GivenCountValidator());
        validators.add(new UniqueItemsValidator());
        legalValueManager = new LegalValueManager(size);
        validators.add(legalValueManager);
        setValidators(validators);

        setSolver(new SudokuSolverImpl());

        int sqrt = (int) Math.sqrt(getSize());
        if (sqrt * sqrt == getSize()) {
            regionSize = sqrt;
        }
        if (hasRegions()) {
            itemsByReqion = new Item[getSize()][getSize()];
        }
    }

    public List<SudokuValidator> getValidators() {
        return validators;
    }

    public void setValidators(List<SudokuValidator> validators) {
        this.validators = validators;
    }

    public SudokuSolver getSolver() {
        return solver;
    }

    public void setSolver(SudokuSolver solver) {
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
    protected Item<Integer> createEmptyItem(Position position) {
        return new VariableItem(1, getSize(), position);
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
        for (int rowIndex = 0; rowIndex < getSize(); rowIndex++) {
            for (int columnIndex = 0; columnIndex < getSize(); columnIndex++) {
                Position position = new Position(rowIndex, columnIndex);
                Integer randomValue = generateValueFor(position);
                if (randomValue == null) {
                    clear();
                    rowIndex = 0;
                    columnIndex = -1;
                    continue;
                }
                put(position, new GivenItem(randomValue, position));
            }
        }
        SudokuGrid generated = copy();

        while (true) {
            Collection<Position> givenPositions = getPositionsGenerator().generate();
            for (int rowIndex = 0; rowIndex < getSize(); rowIndex++) {
                for (int columnIndex = 0; columnIndex < getSize(); columnIndex++) {
                    if (!givenPositions.contains(new Position(rowIndex, columnIndex))) {
                        remove(new Position(rowIndex, columnIndex));
                    }
                }
            }
            try {
                if (solve().solved()) {
                    return;
                }
            } catch (Exception ignored) {
            }
            // Try again
            copy(generated);
        }
    }

    private void clearVariables() {
        for (Item item : this) {
            if (item instanceof VariableItem) {
                remove(item.getPosition());
            }
        }
    }

    public void copy(SudokuGrid sudokuGrid) {
        SudokuGrid copy = sudokuGrid.copy();
        for (Item item : copy) {
            put(item.getPosition(), item);
        }
    }

    @Override
    protected void clear() {
        super.clear();
        if (hasRegions()) {
            itemsByReqion = new Item[getSize()][getSize()];
        }
    }

    protected PositionsGenerator getPositionsGenerator() {
        return new GivenPositionsGenerator(getSize());
    }

    public Integer generateValueFor(Position position) {
        List<Integer> validValuesForPosition = new ArrayList<>(getValidValuesFor(position));
        if (validValuesForPosition.isEmpty()) {
            return null;
        }

        return validValuesForPosition.get((((int) (Math.random() * getSize())) % validValuesForPosition.size()));
    }

    private Item<Integer> getGridItem(int row, int column) {
        return getRow(row)[column];
    }

    protected Set<Integer> getValidValuesFor(Position position) {
        Set<Integer> validValues = legalValueManager.getLegalValues();
        for (Item<Integer> gridItem : getRow(position.x)) {
            if (gridItem != null) {
                validValues.remove(gridItem.getValue());
            }
        }
        for (Item<Integer> gridItem : getColumn(position.y)) {
            if (gridItem != null) {
                validValues.remove(gridItem.getValue());
            }
        }
        if (hasRegions()) {
            for (Item gridItem : getRegion(position)) {
                if (gridItem != null) {
                    validValues.remove(gridItem.getValue());
                }
            }
        }

        return validValues;
    }

    public SudokuGrid copy() {
        return newInstance(getIntegerArray());
    }

    private Integer[][] getIntegerArray() {
        Integer[][] itemsArray = new Integer[getSize()][getSize()];
        for (int row = 0; row < getSize(); row ++) {
            for (int column = 0; column < getSize(); column++) {
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
                if (!legalValueManager.isValueLegal(value)) {
                    value = null;
                }

                Position position = new Position(rowIndex, columnIndex);
                put(position, (value == null) ?
                        createEmptyItem(position) :
                        new GivenItem(value, position));
            }
        }
    }

    public boolean hasRegions() {
        return regionSize > 0;
    }

    public boolean isValid() {
        for (SudokuValidator validator : validators) {
            if (!validator.isValid(this)) {
                return false;
            }
        }
        return true;
    }

    public SudokuGrid solve() {
        return solver.solve(copy());
    }

    public boolean solved() {
        for (Item item : this) {
            if (item instanceof VariableItem && !((VariableItem) item).solved()) {
                return false;
            }
        }
        return isValid();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\n");
        for (int rowIndex = 0; rowIndex < getSize(); rowIndex++) {
            stringBuilder.append("    {");
            for (int columnIndex = 0; columnIndex < getSize(); columnIndex++) {
                Item<Integer> item = get(rowIndex, columnIndex);
                if (item.getValue() == null) {
                    stringBuilder.append("0");
                } else {
                    stringBuilder.append(item.getValue());
                }
                if (columnIndex < getSize() - 1) {
                    stringBuilder.append(", ");
                }
            }
            stringBuilder.append("}");
            if (rowIndex < getSize() - 1) {
                stringBuilder.append(",");
            }
            stringBuilder.append("\n");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public String toStringWithPossibilities() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\n");
        for (int rowIndex = 0; rowIndex < getSize(); rowIndex++) {
            stringBuilder.append("    {");
            for (int columnIndex = 0; columnIndex < getSize(); columnIndex++) {
                Item<Integer> item = get(rowIndex, columnIndex);
                if (item.getValue() == null && item instanceof VariableItem) {
                    stringBuilder.append("[");
                    Set<Integer> possibilities = ((VariableItem) item).getPossibilities();
                    Iterator<Integer> possibilityIterator = possibilities.iterator();
                    for (int i = 0; i < possibilities.size(); i++) {
                        Integer possibility = possibilityIterator.next();
                        stringBuilder.append(possibility);
                        if (i < possibilities.size() - 1) {
                            stringBuilder.append("|");
                        }
                    }
                    stringBuilder.append("]");
                } else {
                    stringBuilder.append(item.getValue());
                }
                if (columnIndex < getSize() - 1) {
                    stringBuilder.append(", ");
                }
            }
            stringBuilder.append("}");
            if (rowIndex < getSize() - 1) {
                stringBuilder.append(",");
            }
            stringBuilder.append("\n");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public static class GivenItem extends Item<Integer> {
        public GivenItem(Integer value, Position position) {
            super(value, position);
        }

        @Override
        public void setValue(Integer value) {
            throw new UnsupportedOperationException("The value is final for given items.");
        }
    }

    public static class VariableItem extends Item<Integer> {
        private Set<Integer> possibilities;

        public VariableItem(int start, int end, Position position) {
            super(null, position);
            possibilities = new HashSet<>();
            for (int i = start; i <= end; i++) {
                possibilities.add(i);
            }
        }

        public boolean removePossibility(Integer possibility) {
            boolean removed = possibilities.remove(possibility);
            if (solved()) {
                setValue(possibilities.iterator().next());
            }
            return removed;
        }

        public void solve(Integer solution) {
            possibilities.retainAll(Arrays.asList(solution));
            setValue(solution);
        }

        public boolean solved() {
            return possibilities.size() == 1 && getValue() != null;
        }

        public Set<Integer> getPossibilities() {
            return possibilities;
        }

        public void setPossibilities(Set<Integer> possibilities) {
            this.possibilities = possibilities;
        }
    }
}