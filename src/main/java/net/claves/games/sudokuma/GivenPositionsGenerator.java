package net.claves.games.sudokuma;

import net.claves.games.Position;
import net.claves.games.PositionsGenerator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GivenPositionsGenerator implements PositionsGenerator {
    private int sudokuSize;
    public GivenPositionsGenerator(int sudokuSize) {
        this.sudokuSize = sudokuSize;
    }

    @Override
    public Collection<Position> generate() {
        Set<Position> positions = new HashSet<>();

        int count = getPositionCount();

        while (positions.size() < count) {
            positions.add(getRandomSudokuPosition());
        }

        return positions;
    }

    private Position getRandomSudokuPosition() {
        int x = (((int) (Math.random() * 1000)) % sudokuSize);
        int y = (((int) (Math.random() * 1000)) % sudokuSize);
        return new Position(x, y);
    }

    private int getPositionCount() {
        int minimumCount = (sudokuSize*sudokuSize - (int)(sudokuSize / 0.5625)) / 2;

        int difficulty = 31;
        return minimumCount + (((int) (Math.random() * 1000)) % (minimumCount / ((difficulty % minimumCount) + 1)));
    }
}
