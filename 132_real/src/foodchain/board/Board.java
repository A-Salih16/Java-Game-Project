package foodchain.board;

import foodchain.model.Pos;

/**
 * Represents the game board as a 2D grid.
 * Stores the content of each cell.
 */
public class Board {
    private final int size;
    private final CellContent[][] grid;
    
    /**
     * Initializes an empty board with the specified size.
     * @param size The dimension of the square grid.
     */
    public Board(int size) {
        this.size=size;
        this.grid=new CellContent[size][size];
        for(int r=0;r<size;r++) {
            for(int c=0;c<size;c++) {
                grid[r][c]=CellContent.EMPTY;
            }
        }
    }

    /**
     * Checks if a position is within the board limits.
     * @param p The position to check.
     * @return True if valid, false otherwise.
     */
    public boolean inBounds(Pos p) {
        return p.getRow()>=0 && p.getRow()<size && p.getCol()>=0 && p.getCol()<size;
    }
    
    /**
     * Checks if a specific cell is empty.
     * @param p The position to check.
     * @return True if the cell contains CellContent.EMPTY.
     */
    public boolean isEmpty(Pos p) {
        requireInBounds(p);
        return grid[p.getRow()][p.getCol()]==CellContent.EMPTY;
    }

    private void requireInBounds(Pos p) {
        if(!inBounds(p)) {
            throw new IndexOutOfBoundsException("Out of bounds: "+p+" size="+size);
        }
    }

    /**
     * Sets the content of a specific cell.
     * @param p The position to update.
     * @param cc The new content for the cell.
     */
    public void set(Pos p, CellContent cc) {
        requireInBounds(p);
        grid[p.getRow()][p.getCol()]=cc;
    }

    /**
     * Retrieves the content of a specific cell.
     * @param p The position to query.
     * @return The content at the given position.
     */
    public CellContent get(Pos p) {
        requireInBounds(p);
        return grid[p.getRow()][p.getCol()];
    }
    
    public int getSize() { return size; }
}