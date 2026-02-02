package foodchain.gui;

import foodchain.core.GameConfig;
import foodchain.core.GameEngine;
import foodchain.io.GameStateSerializer;
import foodchain.io.InvalidSaveFormatException;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.awt.CardLayout;

/**
 * Main application window for the FoodChain game.
 * Manages screen navigation (Start vs Game) and global menus.
 */
public class GameFrame extends JFrame {

    private final CardLayout cards=new CardLayout();
    private final JPanel root=new JPanel(cards);

    private final StartPanel startPanel;
    private final GamePanel gamePanel;

    private GameEngine engine;
    private final Path SAVE_FILE=Path.of("data", "save.txt");

    /**
     * Initializes the main frame and setup panels.
     */
    public GameFrame() {
        super("FoodChain Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);

        setJMenuBar(buildMenuBar());

        startPanel=new StartPanel(this);
        gamePanel=new GamePanel(this);

        root.add(startPanel, "START");
        root.add(gamePanel, "GAME");

        setContentPane(root);
        showStart();
    }

    public GameEngine getEngine() { return engine; }

    private JMenuBar buildMenuBar() {
        JMenuBar bar=new JMenuBar();
        JMenu file=new JMenu("Menu");

        JMenuItem newGame=new JMenuItem("New Game");
        newGame.addActionListener(e->showStart());

        JMenuItem save=new JMenuItem("Save");
        save.addActionListener(e->saveWithChooser());

        JMenuItem load=new JMenuItem("Load");
        load.addActionListener(e->loadWithChooser());

        JMenuItem exit=new JMenuItem("Exit");
        exit.addActionListener(e->System.exit(0));

        file.add(newGame);
        file.addSeparator();
        file.add(save);
        file.add(load);
        file.addSeparator();
        file.add(exit);

        bar.add(file);
        return bar;
    }

    /**
     * Saves the current game state to a file.
     * Shows a popup dialog on success or failure.
     */
    public void saveWithChooser() {
        if(engine==null) {
            JOptionPane.showMessageDialog(this, "No running game to save.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if(SAVE_FILE.getParent()!=null) Files.createDirectories(SAVE_FILE.getParent());
            GameStateSerializer.save(SAVE_FILE, engine);
            JOptionPane.showMessageDialog(this, "Game saved to data/save.txt!");
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, "Save Failed: "+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads a game state from a file.
     * Restores the game engine and UI.
     */
    public void loadWithChooser() {
        if(!Files.exists(SAVE_FILE)) {
            JOptionPane.showMessageDialog(this, "No save file found at data/save.txt", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            GameEngine loaded=new GameEngine();
            GameStateSerializer.load(SAVE_FILE, loaded);
            this.engine=loaded;
            gamePanel.refreshFromEngine(engine);
            showGame();
            JOptionPane.showMessageDialog(this, "Game loaded from data/save.txt!");
        } catch(InvalidSaveFormatException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Save File", JOptionPane.ERROR_MESSAGE);
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, "Load Failed: "+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Starts a new game session with provided configuration.
     * @param cfg The game configuration (Era, Size, etc.).
     * @throws java.io.IOException if assets fail to load.
     */
    public void startNewGame(GameConfig cfg) throws java.io.IOException {
        engine=new GameEngine();
        engine.startGame(cfg);
        gamePanel.refreshFromEngine(engine);
        showGame();
    }

    public void showStart() { cards.show(root, "START"); }
    public void showGame()  { cards.show(root, "GAME"); }
}