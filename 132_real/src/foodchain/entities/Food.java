package foodchain.entities;

import foodchain.model.Pos;

/**
 * Represents the food object that the Prey eats.
 * Static entity with no complex behavior.
 */
public class Food extends Entity {
    public Food(String name, Pos pos) {
        super(name, pos);
    }
}