package foodchain.gui;

import foodchain.ai.ApexAI;
import foodchain.ai.PreyAI;
import foodchain.core.GameEngine;
import foodchain.model.Era;
import foodchain.model.Pos;
import foodchain.model.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * The primary game interface containing the board and status panels.
 * Manages the AI turn timer and player interaction flow.
 */
public class GamePanel extends JPanel {

    private final GameFrame frame;
    private final BoardPanel boardPanel=new BoardPanel();
    private final StatusPanel statusPanel=new StatusPanel();

    private GameEngine boundEngine;
    private ApexAI apexAI;
    private PreyAI preyAI;

    private Timer aiTimer; 

    public GamePanel(GameFrame frame) {
        this.frame=frame;
        setLayout(new BorderLayout(10,10));
        setBorder(new EmptyBorder(10,10,10,10));

        add(boardPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.EAST);
        add(buildBottomBar(), BorderLayout.SOUTH);

        boardPanel.setOnCellClick(this::handleCellClick);

        aiTimer=new Timer(750,e->processNextAITurn());
        aiTimer.setRepeats(true);
    }

    private JPanel buildBottomBar() {
        JButton newGame=new JButton("New Game");
        newGame.addActionListener(e->{
            stopTimer();
            frame.showStart();
        });

        JButton saveBtn=new JButton("Save");
        saveBtn.addActionListener(e->frame.saveWithChooser());

        JButton loadBtn=new JButton("Load");
        loadBtn.addActionListener(e->{
            stopTimer(); 
            frame.loadWithChooser();
        });

        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(loadBtn);
        bottom.add(saveBtn);
        bottom.add(newGame);
        return bottom;
    }

    private void stopTimer() {
        if(aiTimer.isRunning()) aiTimer.stop();
    }

    /**
     * Updates the UI to reflect a new or loaded game engine.
     * Re-initializes AI controllers and timers.
     * @param engine The GameEngine instance to display.
     */
    public void refreshFromEngine(GameEngine engine) {
        if(engine==null) return;

        boolean newEngine=(engine!=boundEngine);
        if(newEngine) {
            stopTimer(); 
            boundEngine=engine;
            apexAI=new ApexAI(engine);
            preyAI=new PreyAI(engine);

            if(engine.getTurnManager().getCurrentTurn()!=Role.PREDATOR) {
                boardPanel.setEnabledAll(false); 
                aiTimer.start();
            }
        }

        statusPanel.update(engine);
        boardPanel.render(engine.getState());

        if(engine.isGameOver()) {
            boardPanel.disableAll();
            stopTimer();
        } else if(!aiTimer.isRunning()) {
            highlightForCurrentTurn(engine);
        } else {
            boardPanel.disableAll(); 
        }
    }

    /**
     * Handles clicks on the board by the human player.
     * Executes the move if valid and passes turn to AI.
     * @param to The target position.
     */
    private void handleCellClick(Pos to) {
        if(aiTimer.isRunning()) return;

        GameEngine engine=frame.getEngine();
        if(engine==null || engine.isGameOver()) return;
        if(engine.getTurnManager().getCurrentTurn()!=Role.PREDATOR) return;

        var st=engine.getState();
        boolean ok=engine.move(Role.PREDATOR, to);

        if(!ok) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        refreshFromEngine(engine);

        if(!engine.isGameOver()) {
            boardPanel.setEnabledAll(false); 
            aiTimer.start(); 
        } else {
            showGameOverMessage(engine);
        }
    }

    /**
     * Triggered by the timer to execute AI moves (Apex and Prey).
     */
    private void processNextAITurn() {
        GameEngine engine=frame.getEngine();
        if(engine==null || engine.isGameOver()) {
            stopTimer();
            if(engine!=null) showGameOverMessage(engine);
            return;
        }

        Role turn=engine.getTurnManager().getCurrentTurn();

        if(turn==Role.PREDATOR) {
            stopTimer(); 
            refreshFromEngine(engine); 
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        boolean moved=false;
        if(turn==Role.PREY) {
            moved=preyAI.playTurn();
        } else if(turn==Role.APEX) {
            moved=apexAI.playTurn();
        }

        if(!moved) {
            engine.getTurnManager().endTurn();
        }

        refreshFromEngine(engine);
        if(engine.isGameOver()) {
            stopTimer();
            showGameOverMessage(engine);
        }
    }

    private void showGameOverMessage(GameEngine engine) {
        JOptionPane.showMessageDialog(this, engine.getWinnerText(), "Game Over", JOptionPane.INFORMATION_MESSAGE);
        boardPanel.disableAll();
    }

    private void highlightForCurrentTurn(GameEngine engine) {
        Role turn=engine.getTurnManager().getCurrentTurn();
        
        if(turn!=Role.PREDATOR) {
            return;
        }

        var board=engine.getState().getBoard();
        int n=board.getSize();
        boolean[][] walk=new boolean[n][n];
        boolean[][] ability=new boolean[n][n];
        Pos playerPos=engine.getState().getPredator().getPos();

        for(int r=0;r<n;r++) {
            for(int c=0;c<n;c++) {
                Pos p=new Pos(r, c);
                GameEngine.MoveKind k=engine.getMoveKind(turn, p);
                if(k==GameEngine.MoveKind.WALK) walk[r][c]=true;
                else if(k==GameEngine.MoveKind.ABILITY) ability[r][c]=true;
            }
        }

        boolean enableClicks=!engine.isGameOver();
        boardPanel.highlight(turn, walk, ability, enableClicks, playerPos);
    }
}