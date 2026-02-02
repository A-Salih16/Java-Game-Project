package foodchain.core;

import java.util.HashSet;
import java.util.Set;

import foodchain.board.Board;
import foodchain.board.CellContent;
import foodchain.entities.Animal;
import foodchain.entities.Food;
import foodchain.model.Era;
import foodchain.model.Pos;

/**
 * Represents the current snapshot of the game.
 * Stores references to the board and all active entities.
 */
public class GameState {
    private final Era era;
    private final Board board;
    private final int totalRounds;
    private int round=1;
    
    private Animal prey;
    private Animal predator;
    private Animal apex;
    private Food food;

    /**
     * Initializes the game state.
     * @param era The current era.
     * @param board The game board.
     * @param totalRounds Maximum number of rounds.
     */
    public GameState(Era era, Board board, int totalRounds) {
        this.era=era;
        this.board=board;
        this.totalRounds=totalRounds;
    }

    public Era getEra() { return era; }
    public Board getBoard() { return board; }
    public int getTotalRounds() { return totalRounds; }
    public int getRound() { return round; }

    public Animal getPrey() { return prey; }
    public Animal getPredator() { return predator; }
    public Animal getApex() { return apex; }
    public Food getFood() { return food; }
    
    /**
     * Places entities on the board and initializes their state.
     * @param prey The Prey entity.
     * @param predator The Predator entity.
     * @param apex The Apex entity.
     * @param food The Food entity.
     */
    public void initEntities(Animal prey, Animal predator, Animal apex, Food food) {
        if(prey==null||predator==null||apex==null||food==null)
            throw new IllegalArgumentException("Null entity");

        if(!board.inBounds(prey.getPos()) || !board.inBounds(predator.getPos()) ||!board.inBounds(apex.getPos()) ||!board.inBounds(food.getPos()))
            throw new IllegalArgumentException("Entity out of bounds");

        Set<Pos> s=new HashSet<>();
        s.add(prey.getPos());
        s.add(predator.getPos());
        s.add(apex.getPos());
        s.add(food.getPos());
        if(s.size()!=4) throw new IllegalArgumentException("Overlapping entities");

        this.prey=prey;
        this.predator=predator;
        this.apex=apex;
        this.food=food;

        board.set(prey.getPos(), CellContent.PREY);
        board.set(predator.getPos(), CellContent.PREDATOR);
        board.set(apex.getPos(), CellContent.APEX);
        board.set(food.getPos(), CellContent.FOOD);
    }
}