import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SudokuTest
{
    @Test
    public void testCreateNewGridFromArray()
    {
        Integer[][] values = new Integer[9][9];
        values[0] = new Integer[] {1, 2, null, 4, null, 6, 7, null, 9};
        values[8] = new Integer[] {5, null, null, 9, null, 8, 3, null};
        SudokuGrid sudokuGrid = SudokuGrid.newInstance(values);

        assertEquals(9, sudokuGrid.getItem(8, 3));
    }
}