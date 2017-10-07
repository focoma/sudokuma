import net.claves.games.sudokuma.SudokuGrid;
import net.claves.games.sudokuma.exceptions.UnsolvableSudokuException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class SudokuTest {
    @Test
    public void testParseArrayGrid() {
        Integer[][] values = new Integer[9][9];
        values[0] = new Integer[]{1, 2, null, 4, null, 6, 7, null, 9};
        values[8] = new Integer[]{5, null, null, 9, null, 8, 3, null};
        SudokuGrid sudokuGrid = SudokuGrid.newInstance(values);

        assertEquals(new Integer(9), sudokuGrid.get(8, 3).getValue());
    }

    @Test
    public void testGenerateNewGrid() {
        SudokuGrid sudokuGrid = SudokuGrid.newInstance();

        assertTrue(sudokuGrid.isValid());
    }

    @Test(expected = UnsolvableSudokuException.class)
    public void testNoValidValueForItem() {
        SudokuGrid sudokuGrid = SudokuGrid.newInstance(
                new Integer[][] {
                        {1, null, null, null},
                        {2, null, null, null},
                        {null, 3, null, null},
                        {4, null, null, null}
                }
        );
        sudokuGrid.solve();
    }

    @Test
    public void testSolveSizeFour() {
        SudokuGrid unsolved = SudokuGrid.newInstance(
                new Integer[][] {
                        {null, 2, 3, 4},
                        {4, 3, null, 1},
                        {3, null, 4, 2},
                        {2, 4, 1, null}
                }
        );
        SudokuGrid solved = unsolved.solve();

        assertEquals(SudokuGrid.newInstance(
                new Integer[][] {
                        {1, 2, 3, 4},
                        {4, 3, 2, 1},
                        {3, 1, 4, 2},
                        {2, 4, 1, 3}
                }
        ), solved);
    }

    @Test
    public void testSolveSizeNine() {
        SudokuGrid unsolved = SudokuGrid.newInstance(
                new Integer[][] {
                        {0, 0, 0, 0, 6, 0, 9, 5, 7},
                        {0, 0, 8, 1, 9, 4, 3, 0, 0},
                        {0, 0, 0, 0, 3, 7, 0, 0, 0},
                        {0, 0, 3, 0, 4, 8, 0, 0, 5},
                        {4, 5, 7, 6, 2, 0, 8, 3, 9},
                        {8, 0, 6, 0, 0, 0, 0, 4, 0},
                        {3, 4, 0, 2, 7, 5, 0, 9, 8},
                        {0, 0, 9, 0, 8, 0, 0, 1, 0},
                        {0, 8, 0, 0, 0, 9, 0, 7, 0}
                }
        );
        SudokuGrid solved = unsolved.solve();

        assertEquals(SudokuGrid.newInstance(
                new Integer[][] {
                        {1, 3, 4, 8, 6, 2, 9, 5, 7},
                        {5, 7, 8, 1, 9, 4, 3, 2, 6},
                        {9, 6, 2, 5, 3, 7, 1, 8, 4},
                        {2, 1, 3, 9, 4, 8, 7, 6, 5},
                        {4, 5, 7, 6, 2, 1, 8, 3, 9},
                        {8, 9, 6, 7, 5, 3, 2, 4, 1},
                        {3, 4, 1, 2, 7, 5, 6, 9, 8},
                        {7, 2, 9, 4, 8, 6, 5, 1, 3},
                        {6, 8, 5, 3, 1, 9, 4, 7, 2}
                }
        ), solved);
    }

    @Test
    public void testEquality() {
        assertEquals(SudokuGrid.newInstance(
                new Integer[][] {
                    {null, 2, 3, 4},
                    {4, 3, null, 1},
                    {3, null, 4, 2},
                    {2, 4, 1, null}
                }
            ),
            SudokuGrid.newInstance(
                new Integer[][] {
                    {null, 2, 3, 4},
                    {4, 3, null, 1},
                    {3, null, 4, 2},
                    {2, 4, 1, null}
                }
            ));

        assertNotEquals(SudokuGrid.newInstance(
                new Integer[][] {
                        {null, 2, null, 4},
                        {4, 3, null, 1},
                        {3, null, 4, 2},
                        {2, 4, 1, null}
                }
                ),
                SudokuGrid.newInstance(
                        new Integer[][] {
                                {null, 2, 3, 4},
                                {4, 3, null, 1},
                                {3, null, 4, 2},
                                {2, 4, 1, null}
                        }
                ));
    }
}