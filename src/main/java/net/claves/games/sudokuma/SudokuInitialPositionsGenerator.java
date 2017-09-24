package net.claves.games.sudokuma;

import net.claves.games.Position;
import net.claves.games.PositionsGenerator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SudokuInitialPositionsGenerator implements PositionsGenerator {
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
        int x = (((int) (Math.random() * 1000)) % 9);
        int y = (((int) (Math.random() * 1000)) % 9);
        return new Position(x, y);
    }

    private int getPositionCount() {
        return 32 + (((int) (Math.random() * 1000)) % 32);
    }
}
