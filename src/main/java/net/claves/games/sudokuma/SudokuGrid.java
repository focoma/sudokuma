package net.claves.games.sudokuma;

import net.claves.games.Position;
import net.claves.games.PositionsGenerator;

import java.util.*;

public class SudokuGrid {
    private GridItem[][] itemsByRow;
    private GridItem[][] itemsByColumn;
    private GridItem[][] itemsByReqion;
    private Map<Position, GridItem> gridMap;
    private int size;
    private int regionSize;

    public SudokuGrid(int size) {
        if (size < 1)
        {
            throw new IllegalArgumentException("Invalid size.");
        }
        this.size = size;
        int sqrt = (int)Math.sqrt(size);
        if (sqrt * sqrt == size)
        {
            regionSize = sqrt;
        }
        itemsByRow = new GridItem[size][size];
        itemsByColumn = new GridItem[size][size];
        if (regionSize > 0) {
            itemsByReqion = new GridItem[size][size];
        }

        gridMap = new HashMap<Position, GridItem>() {
            @Override
            public GridItem put(Position position, GridItem gridItem) {
                if (position.x >= size || position.y >= size || position.x < 0 || position.y < 0)
                {
                    throw new IllegalArgumentException("Invalid position.");
                }
                itemsByRow[position.x][position.y] = gridItem;
                itemsByColumn[position.y][position.x] = gridItem;
                if (regionSize > 0)
                {
                    getRegion(position)
                            [(position.x % regionSize)*regionSize + (position.y % regionSize)] = gridItem;
                }

                return super.put(position, gridItem);
            }

            @Override
            public GridItem remove(Object key) {
                Position position = (Position)key;
                GridItem blank = new VariableItem();
                put(position, blank);
                return blank;
            }
        };
    }

    private GridItem[] getRegion(Position position) {
        return itemsByReqion
                [(position.x / regionSize)*regionSize + (position.y / regionSize)];
    }

    public static SudokuGrid newInstance() {
        SudokuGrid sudokuGrid = new SudokuGrid(9);
        sudokuGrid.generate();

        return sudokuGrid;
    }

    public static SudokuGrid newInstance(Integer[][] squareGrid) {
        int size = squareGrid.length;
        if (size != squareGrid[0].length)
        {
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

                gridMap.put(new Position(rowIndex, columnIndex), new GivenItem(value));
            }
        }
    }

    private void generate() {
        Collection<Position> givenPositions = getPositionsGenerator().generate();
        for (int rowIndex = 0; rowIndex < 9; rowIndex++) {
            for (int columnIndex = 0; columnIndex < 9; columnIndex++) {
                Integer randomValue = generateValueFor(new Position(rowIndex, columnIndex));
                GridItem gridItem;
                if (givenPositions.contains(new Position(rowIndex, columnIndex))) {
                    gridItem = new GivenItem(randomValue);
                }
                else {
                    gridItem = new VariableItem();
                    gridItem.setValue(randomValue);
                }

                gridMap.put(new Position(rowIndex, columnIndex), gridItem);
            }
        }

        for (int rowIndex = 0; rowIndex < 9; rowIndex++) {
            for (int columnIndex = 0; columnIndex < 9; columnIndex++) {
                GridItem gridItem = getGridItem(rowIndex, columnIndex);
                if (!givenPositions.contains(new Position(rowIndex, columnIndex))) {
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
        for (GridItem gridItem : itemsByRow[position.x]) {
            if (gridItem != null) {
                validValues.remove(gridItem.getValue());
            }
        }
        for (GridItem gridItem : itemsByColumn[position.y]) {
            if (gridItem != null) {
                validValues.remove(gridItem.getValue());
            }
        }
        if (regionSize > 0) {
            for (GridItem gridItem : getRegion(position)) {
                if (gridItem != null) {
                    validValues.remove(gridItem.getValue());
                }
            }
        }

        return validValues;
    }

    public int getItem(int row, int column) {
        return getGridItem(row, column).value;
    }

    public GridItem getGridItem(int row, int column) {
        return itemsByRow[row][column];
    }

    public boolean isValid() {
        for (GridItem[] row : itemsByRow) {
            if (!itemsAreUnique(row)) {
                return false;
            }
        }

        for (GridItem[] column : itemsByColumn) {
            if (!itemsAreUnique(column)) {
                return false;
            }
        }

        return hasEnoughGivens();
    }

    public boolean hasEnoughGivens() {
        int givenCount = 0;
        for (GridItem[] row : itemsByRow) {
            for (GridItem gridItem : row) {
                if (gridItem.getValue() != null) {
                    givenCount++;
                }
            }
        }

        return givenCount > 16;
    }

    private boolean itemsAreUnique(GridItem[] row) {
        List<Integer> processedValues = new ArrayList<>();
        for (GridItem gridItem : row) {
            if (gridItem.getValue() != null && processedValues.contains(gridItem.getValue())) {
                return false;
            }
            processedValues.add(gridItem.getValue());
        }

        return true;
    }

    private abstract class GridItem {
        private Integer value;

        public GridItem(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
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

    private class GivenItem extends GridItem
    {
        public GivenItem(Integer value)
        {
            super(value);
        }
    }

    private class VariableItem extends GridItem
    {
        private Set<Integer> possibilities;

        public VariableItem()
        {
            super(null);
            possibilities = new HashSet<>();
            for (int i = 1; i <= size; i++)
            {
                possibilities.add(i);
            }
        }

        public void removePossibility(Integer possibility)
        {
            possibilities.remove(possibility);
        }
    }
}