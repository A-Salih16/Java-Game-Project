package foodchain.core;

import java.io.IOException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;

import foodchain.ai.ApexAI;
import foodchain.ai.PreyAI;
import foodchain.board.Board;
import foodchain.board.CellContent;
import foodchain.entities.Animal;
import foodchain.entities.Food;
import foodchain.io.FoodChainLoader;
import foodchain.io.GameLogger;
import foodchain.model.*;

/**
 * Core engine that controls the game logic, movement rules, and scoring.
 */
public class GameEngine {

    private final SecureRandom rng=new SecureRandom();
    private GameState state;
    private TurnManager tm;
    private final GameLogger logger=new GameLogger(Path.of("data/log.txt"));

    public TurnManager getTurnManager() { return tm; }
    public GameState getState() { return state; }
    public boolean isGameOver() { return tm.isGameOver(); }

    /**
     * Initializes and starts a new game based on the configuration.
     * @param config The game settings.
     * @return The initial GameState.
     * @throws IOException If loading data fails.
     */
    public GameState startGame(GameConfig config) throws IOException {
        tm=new TurnManager(config.getTotalRounds());
 

        Board board=new Board(config.getGridSize().getSize());
        state=new GameState(config.getEra(), board, config.getTotalRounds());

        Set<Pos> used=new HashSet<>();
        Pos preyPos=pickRandomEmpty(board, used);
        Pos predatorPos=pickRandomEmpty(board, used);
        Pos apexPos=pickRandomEmpty(board, used);
        Pos foodPos=pickRandomEmpty(board, used);

        List<FoodChain> options=FoodChainLoader.load(config.getEra());
        FoodChain chosen=options.get(rng.nextInt(options.size()));

        Animal prey=new Animal(chosen.getPreyName(), Role.PREY, preyPos);
        Animal predator=new Animal(chosen.getPredatorName(), Role.PREDATOR, predatorPos);
        Animal apex=new Animal(chosen.getApexName(), Role.APEX, apexPos);
        Food food=new Food(chosen.getFoodName(), foodPos);

        state.initEntities(prey, predator, apex, food);
        logger.log("GAME START era="+config.getEra()+" chain="+chosen);
        logger.log("ROUND BEGIN round="+tm.getRound());

        return state;
    }

    /**
     * Attempts to move a piece to a target position.
     * Handles special moves like the Predator dash.
     * @param role The role attempting to move.
     * @param to The destination position.
     * @return True if the move was successful.
     */
    public boolean move(Role role, Pos to) {
        if(role==Role.PREDATOR && state.getEra()==Era.PRESENT) {
            Pos from=state.getPredator().getPos();
            if(cheb(from, to)==2 && isAdjacent(from, state.getApex().getPos())) {
                Pos mid=findPresentDashMid(from, to);
                if(mid!=null) return dashPredator(mid, to);
            }
        }
        return moveInternal(role, to, true);
    }

    /**
     * Executes the specific dash ability for the Predator.
     * @param mid The intermediate position jumped over.
     * @param to The final destination.
     * @return True if successful.
     */
    public boolean dashPredator(Pos mid, Pos to) {
        if(tm.isGameOver()) return false;
        if(state.getEra()!=Era.PRESENT) return false;
        if(tm.getCurrentTurn()!=Role.PREDATOR) return false;
        if(!isAdjacent(state.getPredator().getPos(), state.getApex().getPos())) return false;
        if(!moveInternal(Role.PREDATOR, mid, false)) return false;
        return moveInternal(Role.PREDATOR, to, true);
    }

    /**
     * Checks if a move is valid without executing it.
     * @param role The role moving.
     * @param to The target position.
     * @return True if the move is allowed.
     */
    public boolean canMove(Role role, Pos to) {
        return getMoveKind(role, to)!=MoveKind.NONE;
    }

    /**
     * Checks if a role can enter a cell with specific content.
     * @param mover The role attempting to enter.
     * @param target The content of the target cell.
     * @return True if entry is allowed according to food chain rules.
     */
    public boolean canEnter(Role mover, CellContent target) {
        if(target==CellContent.EMPTY) return true;
        if(target==CellContent.FOOD) return mover==Role.PREY;
        if(target==CellContent.PREY) return mover==Role.PREDATOR || mover==Role.APEX;
        if(target==CellContent.PREDATOR) return mover==Role.APEX;
        return false;
    }

    public enum MoveKind { NONE, WALK, ABILITY, SKIP }

    /**
     * Determines the type of move (Walk, Ability, etc.).
     * @param role The role moving.
     * @param to The target position.
     * @return The MoveKind enum representing the move type.
     */
    public MoveKind getMoveKind(Role role, Pos to) {
        if(tm==null || state==null || tm.isGameOver()) return MoveKind.NONE;
        if(role!=tm.getCurrentTurn()) return MoveKind.NONE;

        Animal mover=getAnimal(role);
        Pos from=mover.getPos();

        if(from.equals(to)) {
            return MoveKind.SKIP;
        }

        Board board=state.getBoard();
        if(!board.inBounds(to)) return MoveKind.NONE;

        int d=cheb(from, to);
        if(d==0) return MoveKind.NONE;

        CellContent target=board.get(to);

        if(d==1) {
            if(!canEnter(role, target)) return MoveKind.NONE;
            return MoveKind.WALK;
        }

        if(mover.getAbilityCooldown()>0) return MoveKind.NONE;
        if(!isAbilityMoveOk(state.getEra(), role, from, to)) return MoveKind.NONE;

        if(state.getEra()==Era.FUTURE && role==Role.PREY && target==CellContent.FOOD) {
            return MoveKind.NONE;
        }

        if(!canEnter(role, target)) return MoveKind.NONE;

        return MoveKind.ABILITY;
    }

    private boolean moveInternal(Role moverRole, Pos to, boolean endTurn) {
        if(tm.isGameOver()) return false;
        if(moverRole!=tm.getCurrentTurn()) return false;

        MoveKind kind=getMoveKind(moverRole, to);
        if(kind==MoveKind.NONE) return false;

        if(kind==MoveKind.SKIP) {
            logger.log("SKIP TURN role="+moverRole);
            if(endTurn) {
                boolean roundEnded=tm.endTurn();
                if(roundEnded) cooldownTick();
                if(roundEnded) {
                    logger.log("ROUND END");
                    if(!tm.isGameOver()) logger.log("ROUND BEGIN round="+tm.getRound());
                }
                if(tm.isGameOver()) logger.log("GAME OVER "+getWinnerText());
            }
            return true;
        }

        Animal mover=getAnimal(moverRole);
        Pos from=mover.getPos();
        CellContent target=state.getBoard().get(to);
        Board board=state.getBoard();
        board.set(from, CellContent.EMPTY);

        int deltaApex=0, deltaPred=0, deltaPrey=0;

        if(target==CellContent.EMPTY) {
            mover.moveTo(to);
            board.set(to, toCell(moverRole));
        } else if(moverRole==Role.PREY && target==CellContent.FOOD) {
            mover.addScore(3); deltaPrey+=3;
            mover.moveTo(to);
            board.set(to, CellContent.PREY);
            respawnFood();
        } else if(moverRole==Role.PREDATOR && target==CellContent.PREY) {
            mover.addScore(3); deltaPred+=3;
            mover.moveTo(to);
            board.set(to, CellContent.PREDATOR);
            state.getPrey().addScore(-1); deltaPrey-=1;
            respawnAnimal(state.getPrey());
        } else if(moverRole==Role.APEX && (target==CellContent.PREDATOR || target==CellContent.PREY)) {
            mover.addScore(1); deltaApex+=1;
            mover.moveTo(to);
            board.set(to, CellContent.APEX);
            if(target==CellContent.PREDATOR) {
                state.getPredator().addScore(-1); deltaPred-=1;
                respawnAnimal(state.getPredator());
            } else {
                state.getPrey().addScore(-1); deltaPrey-=1;
                respawnAnimal(state.getPrey());
            }
        }

        logger.log("MOVE role="+moverRole+" from="+from+" to="+to+" target="+target);
        if(deltaApex!=0) logger.log("SCORE role=APEX delta="+formatDelta(deltaApex));
        if(deltaPred!=0) logger.log("SCORE role=PREDATOR delta="+formatDelta(deltaPred));
        if(deltaPrey!=0) logger.log("SCORE role=PREY delta="+formatDelta(deltaPrey));

        if(endTurn) {
            boolean roundEnded=tm.endTurn();
            if(roundEnded) cooldownTick();
            if(kind==MoveKind.ABILITY) {
                int cd=abilityCooldownFor(state.getEra(), moverRole);
                if(cd>0) {
                    mover.setAbilityCooldown(cd);
                    logger.log("COOLDOWN role="+moverRole+" set="+cd);
                }
            }
            if(roundEnded) {
                logger.log("ROUND END");
                if(!tm.isGameOver()) logger.log("ROUND BEGIN round="+tm.getRound());
            }
            if(tm.isGameOver()) logger.log("GAME OVER "+getWinnerText());
        }
        return true;
    }

    private boolean isAbilityMoveOk(Era era, Role role, Pos from, Pos to) {
        int dr=Math.abs(to.getRow()-from.getRow());
        int dc=Math.abs(to.getCol()-from.getCol());
        int d=cheb(from, to);

        boolean isLinearOrDiagonal=(dr==0 || dc==0 || dr==dc);

        switch(era) {
            case PAST:
                if(role==Role.APEX) return d==2 && ((dr==0 || dc==0) || (dr==2 && dc==2));
                if(role==Role.PREDATOR) return d==2 && (dr==0 || dc==0);
                if(role==Role.PREY) return d==2;
                return false;

            case PRESENT:
                if(role==Role.APEX) return d>=2 && d<=3;
                if(role==Role.PREY) return d==2;
                
                if(role==Role.PREDATOR) {
                    return d==2 && (dr==0 || dc==0) && isAdjacent(from, state.getApex().getPos());
                }
                return false;

            case FUTURE:
                if(role==Role.APEX) return d>=2 && d<=3;
                
                if(role==Role.PREDATOR) {
                    return d==2 && isLinearOrDiagonal;
                }
                
                if(role==Role.PREY) return d==3;
                return false;
        }
        return false;
    }

    private boolean isAdjacent(Pos a, Pos b) { return cheb(a, b)==1; }
    
    private Pos findPresentDashMid(Pos from, Pos to) {
        Board board=state.getBoard();
        for(int dr=-1; dr<=1; dr++) {
            for(int dc=-1; dc<=1; dc++) {
                if(dr==0 && dc==0) continue;
                Pos mid=from.add(dr, dc);
                if(board.inBounds(mid) && canEnter(Role.PREDATOR, board.get(mid))) {
                     if(cheb(mid, to)==1 && canEnter(Role.PREDATOR, board.get(to))) return mid;
                }
            }
        }
        return null;
    }

    private void cooldownTick() { tick(state.getApex()); tick(state.getPredator()); tick(state.getPrey()); }
    private void tick(Animal a) { if(a.getAbilityCooldown()>0) a.setAbilityCooldown(a.getAbilityCooldown()-1); }
    
    /**
     * Executes AI moves if it is their turn.
     * @param apexAI The AI controller for Apex.
     * @param preyAI The AI controller for Prey.
     */
    public void playAITurns(ApexAI apexAI, PreyAI preyAI) {
        if(tm.isGameOver()) return;
        if(tm.getCurrentTurn()==Role.APEX) apexAI.playTurn();
        else if(tm.getCurrentTurn()==Role.PREY) preyAI.playTurn();
    }

    public void loadFrom(GameState loadedState, TurnManager loadedTurnManager) {
        this.state=loadedState; this.tm=loadedTurnManager;
    }

    private Pos pickRandomEmpty(Board board, Set<Pos> used) {
        while(true) {
            Pos p=new Pos(rng.nextInt(board.getSize()), rng.nextInt(board.getSize()));
            if(board.isEmpty(p) && !used.contains(p)) { used.add(p); return p; }
        }
    }

    private Animal getAnimal(Role role) {
        if(role==Role.PREY) return state.getPrey();
        if(role==Role.PREDATOR) return state.getPredator();
        return state.getApex();
    }

    private CellContent toCell(Role role) { return CellContent.valueOf(role.name()); }
    private int cheb(Pos a, Pos b) { return Math.max(Math.abs(a.getRow()-b.getRow()), Math.abs(a.getCol()-b.getCol())); }
    
    private void respawnFood() {
        Pos newPos=pickRandomEmpty(state.getBoard(), new HashSet<>());
        state.getFood().moveTo(newPos);
        state.getBoard().set(newPos, CellContent.FOOD);
    }

    private void respawnAnimal(Animal a) {
        Pos newPos=pickRandomEmpty(state.getBoard(), new HashSet<>());
        a.moveTo(newPos);
        state.getBoard().set(newPos, toCell(a.getRole()));
    }

    private int abilityCooldownFor(Era era, Role role) {
        if(era==Era.PAST) return 2; 
        if(era==Era.PRESENT) { if(role==Role.APEX) return 3; if(role==Role.PREY) return 3; if(role==Role.PREDATOR) return 0; }
        if(era==Era.FUTURE) { if(role==Role.APEX) return 3; if(role==Role.PREDATOR) return 2; if(role==Role.PREY) return 2; }
        return 0;
    }

    private String formatDelta(int d) { return (d>0 ? "+" : "")+d; }

    /**
     * Generates a string describing the winner and scores.
     * @return The game result text.
     */
    public String getWinnerText() {
        int prey=state.getPrey().getScore();
        int pred=state.getPredator().getScore();
        int apex=state.getApex().getScore();
        
        int max=Math.max(apex, Math.max(prey, pred));
        
        List<String> winners=new ArrayList<>();
        if(prey==max) winners.add("PREY");
        if(pred==max) winners.add("PREDATOR");
        if(apex==max) winners.add("APEX");
        
        String winnerStr=String.join(" & ", winners);
        String scores="scores: prey="+prey+" predator="+pred+" apex="+apex;
        
        return winnerStr+" wins | "+scores;
    }
}