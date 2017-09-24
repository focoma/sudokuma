import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class SudokuGrid
{
    private SudokuGrid()
    {

    }

    public static SudokuGrid newInstance(Integer[][] grid)
    {
        if (grid.length != 9)
        {
            throw new NotImplementedException();
        }

        SudokuGrid sudokuGrid = new SudokuGrid();

        return sudokuGrid;
    }

    public int getItem(int pI, int pI1)
    {
        return 0;
    }

    private class Item
    {

    }
}