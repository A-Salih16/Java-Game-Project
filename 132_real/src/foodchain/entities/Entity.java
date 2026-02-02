package foodchain.entities;

import foodchain.model.Pos;

/**
 * Abstract base class for all objects on the game board.
 * Stores common properties like name and position.
 */
public abstract class Entity {
    private final String name;
    private Pos pos;
    
    /**
     * Base constructor for entities.
     * @param name Name of the entity.
     * @param pos Initial position.
     */
    protected Entity(String name, Pos pos) {
        this.name=name;
        this.pos=pos;
    }
    
    /**
     * Updates the position of the entity.
     * @param newPos The target position.
     * @throws IllegalArgumentException if newPos is null.
     */
    public void moveTo(Pos newPos) {
        if(newPos==null) throw new IllegalArgumentException("pos cannot be null");
        this.pos=newPos;
    }
    
    public String getName() { return name; }
    public Pos getPos() { return pos; }
}