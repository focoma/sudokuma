package net.claves.games.sudokuma.solvers;

import net.claves.games.Grid;
import net.claves.games.Position;
import net.claves.games.sudokuma.SudokuSolver;
import net.claves.games.sudokuma.SudokuGrid;
import net.claves.games.sudokuma.exceptions.MultipleSolutionsException;
import net.claves.games.sudokuma.exceptions.UnsolvableSudokuException;

import java.util.*;

public class SudokuSolverImpl implements SudokuSolver {
    private SudokuGrid sudokuGrid;
    private Set<SudokuGrid> solutions;

    private int size;
    private boolean changed;

    @Override
    public SudokuGrid solve(SudokuGrid sudokuGrid) {
        this.sudokuGrid = sudokuGrid;
        this.size = sudokuGrid.getSize();
        this.solutions = new HashSet<>();

        doSolve();

        if (!sudokuGrid.solved()) {
            throw new UnsolvableSudokuException(sudokuGrid, "Cannot find a valid solution!");
        }

        return sudokuGrid;
    }

    private void doSolve() {
        attemptDeterministicSolution();
        if (sudokuGrid.solved()) {
            solutions.add(sudokuGrid.copy());
        } else {
            for (Grid.Item item : sudokuGrid) {
                if (item instanceof SudokuGrid.VariableItem) {
                    SudokuGrid.VariableItem variableItem = (SudokuGrid.VariableItem) item;
                    if (!variableItem.solved()) {
                        Set<Integer> possibilities = new HashSet<>(variableItem.getPossibilities());
                        for (Integer guess : possibilities) {
                            variableItem.solve(guess);
                            try {
                                solutions.add(new SudokuSolverImpl().solve(sudokuGrid.copy()));
                            } catch (UnsolvableSudokuException ignored) {
                            }

                            if (solutions.size() > 1) {
                                throw new MultipleSolutionsException(sudokuGrid, solutions);
                            }
                            variableItem.setPossibilities(new HashSet<>(possibilities));
                            variableItem.setValue(null);
                        }
                    }
                }
            }
        }
    }

    private void attemptDeterministicSolution() {
        do {
            changed = false;
            for (Grid.Item item : sudokuGrid) {
                solveItem(item);
            }
            for (int index = 0; index < size; index++) {
                trimNeighborPossibilitiesUsingRegion(sudokuGrid.getRegion(index));
            }
        } while (changed);
    }

    private boolean solveVariable(SudokuGrid.VariableItem variableItem) {
        int rowIndex = variableItem.getRowIndex();
        int columnIndex = variableItem.getColumnIndex();

        trimPossibilitiesUsingNeighbors(variableItem, sudokuGrid.getRow(rowIndex));
        trimPossibilitiesUsingNeighbors(variableItem, sudokuGrid.getColumn(columnIndex));
        if (sudokuGrid.hasRegions()) {
            trimPossibilitiesUsingNeighbors(variableItem, sudokuGrid.getRegion(variableItem.getPosition()));
        }
        return variableItem.solved();
    }

    private void trimPossibilitiesUsingNeighbors(SudokuGrid.VariableItem variableItem, Grid.Item[] array) {
        trimPossibilitiesUsingNeighborValues(variableItem, array);
        trimPossibilitiesUsingNeighborPossibilities(variableItem, array);
    }

    private void trimPossibilitiesUsingNeighborPossibilities(SudokuGrid.VariableItem variableItem, Grid.Item[] array) {
        if (!variableItem.solved()) {
            Set<Integer> possibilities = new HashSet<>(variableItem.getPossibilities());
            for (Grid.Item<Integer> item : array) {
                if (!variableItem.equals(item)) {
                    if (item instanceof SudokuGrid.VariableItem) {
                        possibilities.removeAll(((SudokuGrid.VariableItem) item).getPossibilities());
                    } else {
                        possibilities.remove(item.getValue());
                    }
                    if (possibilities.isEmpty()) {
                        return;
                    }
                }
            }
            if (possibilities.size() == 1) {
                variableItem.solve(possibilities.iterator().next());
                fireGridChanged(variableItem);
            }
        }
    }

    private void trimPossibilitiesUsingNeighborValues(SudokuGrid.VariableItem variableItem, Grid.Item[] array) {
        if (!variableItem.solved()) {
            for (Grid.Item<Integer> item : array) {
                if (!item.equals(variableItem) && variableItem.removePossibility(item.getValue())) {
                    fireGridChanged(variableItem);
                }
            }
        }
    }

    private void fireGridChanged(SudokuGrid.VariableItem variableItem) {
        changed = true;
        Set<Integer> possibilities = variableItem.getPossibilities();
        if (possibilities.isEmpty()) {
            Position position = variableItem.getPosition();
            throw new UnsolvableSudokuException(sudokuGrid.copy(), "Item " + position + " has no valid possible value.");
        }

        if (!variableItem.solved() && possibilities.size() == 1) {
            variableItem.setValue(possibilities.iterator().next());
        }

        solveNeighbors(variableItem.getPosition());
    }

    private void solveNeighbors(Position position) {
        Grid.Item[] row = sudokuGrid.getRow(position.x);
        Grid.Item[] column = sudokuGrid.getColumn(position.y);
        Grid.Item[] region = null;
        if (sudokuGrid.hasRegions()) {
            region = sudokuGrid.getRegion(position);
        }
        for (int index = 0; index < size; index++) {
            solveItem(row[index]);
            solveItem(column[index]);
            if (sudokuGrid.hasRegions()) {
                solveItem(region[index]);
            }
        }
    }

    private void trimNeighborPossibilitiesUsingRegion(Grid.Item[] region) {
        Map<Integer, Set<Position>> positionsMap = new HashMap<>();
        Position disqualifiedFlag = new Position(-1, -1);
        Position rowFlag = new Position(1, -1);
        Position columnFlag = new Position(-1, 1);
        for (Grid.Item item : region) {
            if (item instanceof SudokuGrid.VariableItem) {
                for (Integer possibility : ((SudokuGrid.VariableItem) item).getPossibilities()) {
                    Set<Position> positionSet = positionsMap.get(possibility);
                    if (positionSet == null) {
                        positionSet = new HashSet<>();
                        positionSet.add(item.getPosition());
                        positionsMap.put(possibility, positionSet);
                    } else if (!positionSet.contains(disqualifiedFlag)) {
                        Iterator<Position> positionSetIterator = positionSet.iterator();
                        Position position = item.getPosition();
                        if (positionSet.size() == 1) {
                            Position previousPosition = positionSetIterator.next();
                            if (position.x == previousPosition.x) {
                                positionSet.add(rowFlag);
                            } else if (position.y == previousPosition.y) {
                                positionSet.add(columnFlag);
                            } else {
                                positionSet.add(disqualifiedFlag);
                            }
                        } else {
                            Position previousPosition = positionSetIterator.next();
                            if (previousPosition.x == -1 || previousPosition.y == -1) {
                                previousPosition = positionSetIterator.next();
                            }

                            if (positionSet.contains(rowFlag) && previousPosition.x != position.x) {
                                positionSet.add(disqualifiedFlag);
                            } else if (positionSet.contains(columnFlag) && previousPosition.y != position.y) {
                                positionSet.add(disqualifiedFlag);
                            }
                        }
                    }
                }
            }
        }

        for (Map.Entry<Integer, Set<Position>> entry : positionsMap.entrySet()) {
            Set<Position> positionSet = entry.getValue();
            Iterator<Position> positionSetIterator = positionSet.iterator();
            if (!positionSet.contains(disqualifiedFlag)) {
                Position position = positionSetIterator.next();
                if (position.x == -1 || position.y == -1) {
                    position = positionSetIterator.next();
                }

                Grid.Item[] itemsToUpdate;
                if (positionSet.contains(rowFlag)) {
                    itemsToUpdate = sudokuGrid.getRow(position.x);
                } else if (positionSet.contains(columnFlag)) {
                    itemsToUpdate = sudokuGrid.getColumn(position.y);
                } else {
                    continue;
                }

                for (Grid.Item item : itemsToUpdate) {
                    if (item instanceof SudokuGrid.VariableItem && !sudokuGrid.getRegion(item.getPosition()).equals(region)) {
                        SudokuGrid.VariableItem variableItem = (SudokuGrid.VariableItem) item;
                        if (!variableItem.solved() && variableItem.removePossibility(entry.getKey())) {
                            fireGridChanged(variableItem);
                        }
                    }
                }
            }
        }
    }

    private boolean solveItem(Grid.Item item) {
        if (item instanceof SudokuGrid.VariableItem) {
            return solveVariable((SudokuGrid.VariableItem) item);
        }
        return true;
    }
}
