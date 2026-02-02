package foodchain.model;

/**
 * Represents an immutable coordinate (row, column) on the game board.
 */
public final class Pos {
    private final int r;
    private final int c;

    /**
     * Creates a new position.
     * @param row The row index.
     * @param col The column index.
     */
    public Pos(int row,int col) {
        this.r=row;
        this.c=col;
    }

    public int getRow() { return r; }
    public int getCol() { return c; }

    /**
     * Creates a new Pos by adding offsets to the current one.
     * @param i Row offset.
     * @param j Column offset.
     * @return A new Pos instance.
     */
    public Pos add(int i,int j) {
        return new Pos(r+i,c+j);
    }
    
    public String toString() {
		return "("+this.r+","+this.c+")";
    	
    }
    
    /**
     * Checks if two positions refer to the same coordinate.
     * @param other The object to compare.
     * @return True if row and col match.
     */
    public boolean equals(Object other) {
		if(this==other) return true;
		if (!(other instanceof Pos)) return false;
		Pos a=(Pos)other;
		return (a.r==this.r)&&(a.c==this.c) ;
    }
    
    public int hashCode() {
    	return 31*this.r+this.c;
    }
}