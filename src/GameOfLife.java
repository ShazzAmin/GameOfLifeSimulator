/**
 * A simulation of Conway's Game of Life.
 *
 * @author Shazz Amin
 * @version 1.0 2015-04-23
 */
public class GameOfLife
{
    // class fields
    /**
     * The default cell configuration.
     */
    public static final boolean[][] DEFAULT_CELL_CONFIGURATION =
        new boolean[][]
        {
            {false, false, false},
            {false, false, false},
            {false, false, false}
        };
    private static final int CELLS_NEEDED_TO_COME_ALIVE = 3;
    private static final int CELLS_NEEDED_TO_STAY_ALIVE_1 = 2;
    private static final int CELLS_NEEDED_TO_STAY_ALIVE_2 = 3;

    // instance fields
    private int currentGeneration;
    private boolean[][] cells;
    private int height;
    private int width;

    /*
     * constructors
     */

    /**
     * Creates a <code>GameOfLife</code> with the default cell configuration
     * (see <code>DEFAULT_CELL_CONFIGURATION</code> constant).
     */
    public GameOfLife()
    {
        currentGeneration = 1;
        cells = DEFAULT_CELL_CONFIGURATION;
        height = DEFAULT_CELL_CONFIGURATION.length;
        width = DEFAULT_CELL_CONFIGURATION[0].length;
    }

    /**
     * Creates a <code>GameOfLife</code> with specified cell configuration.
     *
     * @param cells the configuration of the cells in the first generation;
     * must be an array of rows (i.e. the second level array represents a row)
     * every row must be the same size as each other (i.e. the configuration
	 * must be a rectangle). <code>true</code> represents an alive cell,
	 * <code>false</code> represents a dead cell
     */
    public GameOfLife(boolean[][] cells)
    {
        for (int y = 1; y < cells.length; y++)
        {
            if (cells[y].length != cells[0].length)
            {
                cells = DEFAULT_CELL_CONFIGURATION;
                break;
            }
        }

        currentGeneration = 1;
        this.cells = cells;
        height = cells.length;
        width = cells[0].length;
    }

    /**
     * Creates a <code>GameOfLife</code> with specified cell configuration.
     *
     * @param cells the configuration of the cells in the first generation;
     * must be an array of rows (i.e. the second level array represents a row)
     * every row must be the same size as each other (i.e. the configuration
     * must be a rectangle). <code>true</code> represents an alive cell,
     * <code>false</code> represents a dead cell
     * @param currentGeneration the generation which this
     * <code>GameOfLife</code> is currently on; must be greater than 0
     */
    public GameOfLife(boolean[][] cells, int currentGeneration)
    {
        for (int y = 1; y < cells.length; y++)
        {
            if (cells[y].length != cells[0].length)
            {
                cells = DEFAULT_CELL_CONFIGURATION;
                break;
            }
        }

        this.currentGeneration = currentGeneration > 0 ? currentGeneration : 1;
        this.cells = cells;
        height = cells.length;
        width = cells[0].length;
    }

    /*
     * accessors
     */

    /**
     * Returns the generation which this <code>GameOfLife</code> is currently
     * on.
     *
     * @return the generation which this <code>GameOfLife</code> is currently on
     */
    public int getCurrentGeneration()
    {
        return currentGeneration;
    }

    /**
     * Returns the current cell configuration.
     *
     * @return the current cell configuration
     */
    public boolean[][] getCells()
    {
        return cells;
    }

    /**
     * Returns the height of the grid.
     *
     * @return the height of the grid
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * Returns the width of the grid.
     *
     * @return the width of the grid
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * Returns whether a cell is alive or not at the specified coordinate.
     *
     * @param x the x-coordinate of the cell that is being checked
     * @param y the y-coordinate of the cell that is being checked
     * @return <code>true</code> if the cell is alive, <code>false</code> if
     * the cell is dead or the coordinate is out of bounds
     */
    public boolean isCellAlive(int x, int y)
    {
        if (isCoordinateInBounds(x, y))
            return cells[y][x];
        else
            return false;
    }

    /**
     * Returns a string representation of this <code>GameOfLife</code>.
     *
     * @return a string representation of this <code>GameOfLife</code>
     */
    public String toString()
    {
        return
            getClass().getName()
            + "["
            + "currentGeneration: " + currentGeneration
            + ", cells: " + cells
            + ", height: " + height
            + ", width: " + width
            + "]";
    }

    /*
     * mutator
     */

    /**
     * Sets a cell at the specified coordinate to the specified state.
     *
     * @param x the x-coordinate of the cell whose state is being set
     * @param y the y-coordinate of the cell whose state is being set
     * @return <code>true</code> if the coordinate is within bounds,
     * <code>false</code> otherwise
     */
    public boolean setCellState(int x, int y, boolean state)
    {
        if (isCoordinateInBounds(x, y))
        {
            cells[y][x] = state;
            return true;
        }
        else
        {
            return false;
        }
    }

    /*
     * methods
     */

    /**
     * Returns whether the current generation is stable (i.e.
     * the next generation will be exactly the same) or not.
     *
     * @return <code>true</code> if the current generation is stable,
     * <code>false</code> if the current generation is not stable
     */
    public boolean isSimulationStable()
    {
        boolean[][] nextGeneration = getNextGeneration();

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                if (cells[y][x] != nextGeneration[y][x])
                    return false;
            }
        }

        return true;
    }

    /**
     * Simulates the next generation by applying the ruleset.
     */
    public void simulateNextGeneration()
    {
        cells = getNextGeneration();
        currentGeneration++;
    }

    private int countAliveNeighbours(int x, int y)
    {
        int aliveNeighbours = 0;

        for (int yOffset = -1; yOffset <= 1; yOffset++)
            for (int xOffset = -1; xOffset <= 1; xOffset++)
                if
                (
                    !(xOffset == 0 && yOffset == 0) &&
                    isCoordinateInBounds(x + xOffset, y + yOffset) &&
                    cells[y + yOffset][x + xOffset]
                )
                    aliveNeighbours++;

        return aliveNeighbours;
    }

    private boolean[][] getNextGeneration()
    {
        boolean[][] nextGeneration = new boolean[height][width];

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int aliveNeighbours = countAliveNeighbours(x, y);
                if (cells[y][x])
                {
                    if
                    (
                        aliveNeighbours == CELLS_NEEDED_TO_STAY_ALIVE_1 ||
                        aliveNeighbours == CELLS_NEEDED_TO_STAY_ALIVE_2
                    )
                        nextGeneration[y][x] = true;
                    else
                        nextGeneration[y][x] = false;
                }
                else
                {
                    if (aliveNeighbours == CELLS_NEEDED_TO_COME_ALIVE)
                        nextGeneration[y][x] = true;
                    else
                        nextGeneration[y][x] = false;
                }
            }
        }

        return nextGeneration;
    }

    private boolean isCoordinateInBounds(int x, int y)
    {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return false;

        return true;
    }
}
