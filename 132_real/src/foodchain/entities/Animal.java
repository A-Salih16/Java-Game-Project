package foodchain.entities;

import foodchain.model.Pos;
import foodchain.model.Role;

/**
 * Represents a living entity in the game (Apex, Predator, or Prey).
 * Manages dynamic properties like score and ability cooldowns.
 */
public class Animal extends Entity {
    private final Role role;
    private int score=0;
    private int abilityCooldown=0;
   
    /**
     * Creates a new animal with a specific role and position.
     * @param name The display name of the animal.
     * @param role The role in the food chain.
     * @param pos The initial position on the board.
     */
    public Animal(String name, Role role, Pos pos) {
        super(name,pos);
        this.role=role;
    }
 
    /**
     * Increases (or decreases) the animal's current score.
     * @param a The amount to add to the score.
     */
    public void addScore(int a) { score+=a; }
    
    /**
     * Checks if the special ability is ready to be used.
     * @return True if cooldown is 0.
     */
    public boolean isAbilityAvailable() { return abilityCooldown==0; }
    
    public Role getRole() { return role; }
    public int getScore() { return score; }
    public int getAbilityCooldown() { return abilityCooldown; }

    /**
     * Updates the cooldown timer for the special ability.
     * Prevents negative cooldown values.
     * @param abilityCooldown The new cooldown value (in turns).
     */
    public void setAbilityCooldown(int abilityCooldown) {
        if(abilityCooldown<0) this.abilityCooldown=0;
        else this.abilityCooldown=abilityCooldown;
    }
}