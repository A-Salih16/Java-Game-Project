package foodchain.ai;
import foodchain.core.GameEngine;
import foodchain.core.GameState;
import foodchain.core.TurnManager;
import foodchain.model.Pos;
import foodchain.model.Role;

/**
 * Artificial Intelligence for the Apex player.
 * Calculates optimal moves to catch prey or predators.
 */
public class ApexAI {
    private final GameEngine engine;

    /**
     * Creates an AI controller for the Apex role.
     * @param engine The main game engine instance.
     */
    public ApexAI(GameEngine engine) {
        this.engine=engine;
    }
    
    /**
     * Executes the turn for the Apex player.
     * Checks valid moves and selects the best scoring position.
     * @return True if a move was successfully made, false otherwise.
     */
    public boolean playTurn() {
        TurnManager tm=engine.getTurnManager();
        if(tm.isGameOver()) return false;
        if(tm.getCurrentTurn()!=Role.APEX) return false;
        Pos to=pickBestApexMove();
        if(to==null) { engine.getTurnManager().endTurn(); return false; }
        return engine.move(Role.APEX, to);
    }

    /**
     * Evaluates all possible moves on the board.
     * Prioritizes moves that are closer to targets (Prey/Predator).
     * @return The best position to move to, or null if no move is possible.
     */
    private Pos pickBestApexMove() {
        GameState st=engine.getState();
        int n=st.getBoard().getSize();
        Pos preyPos=st.getPrey().getPos();
        Pos predPos=st.getPredator().getPos();
        
        Pos best=null;
        int bestScore=Integer.MIN_VALUE;

        for(int r=0;r<n;r++) for(int c=0;c<n;c++) {
            Pos to=new Pos(r,c);
            if(!engine.canMove(Role.APEX, to)) continue;

            int d1=cheb(to,preyPos);
            int d2=cheb(to,predPos);
            int score=-Math.min(d1, d2);
            if(to.equals(preyPos)||to.equals(predPos)) score+=1000;

            if(score>bestScore) {
                bestScore=score;
                best=to;
            }
        }
        return best;
    }

    private int cheb(Pos a, Pos b) {
        return Math.max(Math.abs(a.getRow()-b.getRow()), Math.abs(a.getCol()-b.getCol()));
    }
}