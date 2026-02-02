package foodchain.model;

/**
 * Defines the available board dimensions.
 */
public enum GridSize {
    SMALL(10),
    MEDIUM(15),
    LARGE(20);

    private final int size;

    GridSize(int size) {
        this.size=size;
    }

    public int getSize() {return size;}
    
    /**
     * Converts an integer size back to the corresponding Enum constant.
     * @param n The integer size to look up.
     * @return The matching GridSize.
     * @throws IllegalArgumentException if the size is not supported.
     */
    public static GridSize fromSize(int n) {
        if (n==10) return SMALL;
        if (n==15) return MEDIUM;
        if (n==20) return LARGE;
        throw new IllegalArgumentException("Unsupported grid size: "+n);
    }
}