package foodchain.core;

import foodchain.model.Era;
import foodchain.model.GridSize;
import foodchain.model.Role;

/**
 * Holds initial configuration settings for the game.
 */
public class GameConfig {
    private final Era era;
    private final GridSize gridSize;
    private final int totalRounds;

    /**
     * Creates a new game configuration.
     * @param era The time period of the game (Past, Present, Future).
     * @param gridSize The size of the board.
     * @param totalRounds Total number of rounds to play.
     * @param playerRole The role controlled by the human player.
     */
    public GameConfig(Era era, GridSize gridSize, int totalRounds) {
        this.era=era;
        this.gridSize=gridSize;
        if(totalRounds<=0) {
            throw new IllegalArgumentException("totalRounds must be > 0");
        }

        this.totalRounds=totalRounds;
        
    }
    public Era getEra() { return era; }
    public GridSize getGridSize() { return gridSize; }
    public int getTotalRounds() { return totalRounds; }
}