package foodchain.core;

import foodchain.model.Role;

/**
 * Manages the flow of turns and rounds in the game.
 */
public class TurnManager {
    private static final Role[] ORDER={Role.PREY, Role.PREDATOR, Role.APEX};

    private int idx=0;         
    private int round=1;
    private final int totalRounds;
    private boolean gameOver=false;

    /**
     * Creates a TurnManager with a specific round limit.
     * @param totalRounds The maximum number of rounds to play.
     */
    public TurnManager(int totalRounds) {
        if(totalRounds<=0) throw new IllegalArgumentException("totalRounds must be > 0");
        this.totalRounds=totalRounds;
    }
    
    /**
     * Creates a TurnManager loading from a specific state.
     * @param totalRounds Total rounds.
     * @param currentTurn The role whose turn it currently is.
     * @param round The current round number.
     */
    public TurnManager(int totalRounds, Role currentTurn, int round) {
        if(totalRounds<=0) throw new IllegalArgumentException("totalRounds must be > 0");
        if(round<=0) throw new IllegalArgumentException("round must be > 0");
        this.totalRounds=totalRounds;
        this.round=round;

        if(currentTurn==Role.PREY) idx=0;
        else if(currentTurn==Role.PREDATOR) idx=1;
        else if(currentTurn==Role.APEX) idx=2;
        else throw new IllegalArgumentException("Invalid turn: "+currentTurn);
    }
    
    /**
     * Checks if the game has ended.
     * @return True if game over, false otherwise.
     */
    public boolean isGameOver() {
        return gameOver;
    }
    
    /**
     * Advances to the next turn or round.
     * @return True if the round has ended, false if just the turn changed.
     */
    public boolean endTurn() {
        if(idx<2) {
            idx+=1;
            return false;
        }
        idx=0;
        if(round>=totalRounds) {   
            gameOver=true;
            return true;              
        }
        round+=1;                    
        return true;
    }
    
    public Role getCurrentTurn() { return ORDER[idx]; }
    public int getRound() { return round; }
    public int getTotalRounds() { return totalRounds; }
}