package foodchain.ai;

import foodchain.core.GameEngine;
import foodchain.core.GameState;
import foodchain.core.TurnManager;
import foodchain.model.Pos;
import foodchain.model.Role;

/**
 * Artificial Intelligence for the Prey player.
 * Focuses on survival and finding food.
 */
public class PreyAI {
    private final GameEngine engine;

    /**
     * Creates an AI controller for the Prey role.
     * @param engine The main game engine instance.
     */
    public PreyAI(GameEngine engine) {
        this.engine=engine;
    }
    
    private int cheb(Pos a, Pos b) {
        return Math.max(Math.abs(a.getRow()-b.getRow()), Math.abs(a.getCol()-b.getCol()));
    }

    /**
     * Executes the turn for the Prey player.
     * Calculates danger from predators and distance to food.
     * @return True if a move was successfully made.
     */
    public boolean playTurn() {
        TurnManager tm=engine.getTurnManager();
        if(tm==null||tm.isGameOver()) return false;
        if(tm.getCurrentTurn()!=Role.PREY) return false;

        Pos best=pickBestPreyMove();
        if(best==null) {
            tm.endTurn(); 
            return true;
        }
        return engine.move(Role.PREY, best);
    }

    /**
     * Scans the board to find the safest and most rewarding move.
     * Avoids Apex and Predator while trying to reach Food.
     * @return The optimal position to move to.
     */
    private Pos pickBestPreyMove() {
        GameState st=engine.getState();
        int n=st.getBoard().getSize();

        Pos food=st.getFood().getPos();
        Pos predator=st.getPredator().getPos();
        Pos apex=st.getApex().getPos();

        Pos best=null;
        int bestScore=Integer.MIN_VALUE;

        for(int r=0;r<n;r++) {
            for(int c=0;c<n;c++) {
                Pos to=new Pos(r, c);
            
                if(!engine.canMove(Role.PREY, to)) continue;

                int distFood=cheb(to, food);
                int distPred=cheb(to, predator);
                int distApex=cheb(to, apex);
                
                int score=0;
                score+=200-20*distFood;
                score+=6*distPred+4*distApex;

                if(distPred<=1) score-=200;
                if(distApex<=1) score-=200;
                
                if(to.equals(food)) score+=1000;

                if(score>bestScore) {
                    bestScore=score;
                    best=to;
                }
            }
        }
        return best;
    }
}