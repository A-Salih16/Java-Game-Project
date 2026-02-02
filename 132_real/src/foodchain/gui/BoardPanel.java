package foodchain.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import foodchain.core.GameState;
import foodchain.model.Pos;
import foodchain.model.Role;

/**
 * Visual representation of the game grid.
 * Handles rendering of cells, tokens, and click events.
 */
public class BoardPanel extends JPanel {

    private int size=10;
    private JButton[][] cells;
    private Consumer<Pos> onCellClick=p->{};
    private final Map<String, Image> imageCache=new HashMap<>();

    /**
     * Initializes the board panel with a default size.
     */
    public BoardPanel() {
        setBackground(Color.WHITE);
        rebuild(10);
    }

    /**
     * Registers a callback for cell click events.
     * @param onCellClick The action to perform when a cell is clicked.
     */
    public void setOnCellClick(Consumer<Pos> onCellClick) {
        this.onCellClick=(onCellClick==null) ? (p->{}) : onCellClick;
    }

    /**
     * Generates a circular token icon with the entity image inside.
     * @param name The name of the asset to load.
     * @param cellSize The dimension of the cell.
     * @param circleColor The background color of the token.
     * @return An ImageIcon ready for the button, or null if image fails.
     */
    private ImageIcon createTokenIcon(String name, int cellSize, Color circleColor) {
        Image originalImg=loadRawImage(name); 
        if(originalImg==null) return null;

        BufferedImage token=new BufferedImage(cellSize,cellSize,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2=token.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int padding=5; 
        int circleSize=cellSize-(padding*2);
        
        if(circleColor!=null) {
            g2.setColor(circleColor);
            g2.fillOval(padding,padding,circleSize,circleSize);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawOval(padding,padding,circleSize,circleSize);
        }

        int imgSize=(int)(circleSize*0.85); 
        int centerOffset=(cellSize-imgSize)/2;

        g2.drawImage(originalImg,centerOffset,centerOffset,imgSize,imgSize,null);
        g2.dispose();

        return new ImageIcon(token);
    }

    private Image loadRawImage(String name) {
        if(imageCache.containsKey(name)) return imageCache.get(name);
        URL url=getClass().getResource("/assets/"+name+".png");
        if(url==null) {
            System.err.println("ICON NOT FOUND: "+name);
            imageCache.put(name, null);
            return null;
        }
        Image img=new ImageIcon(url).getImage();
        imageCache.put(name, img);
        return img;
    }

    /**
     * Recreates the grid layout based on a new board size.
     * @param newSize The number of rows/cols.
     */
    public void rebuild(int newSize) {
        this.size=newSize;
        removeAll();
        setLayout(new GridLayout(size,size)); 

        cells=new JButton[size][size];

        for(int r=0;r<size;r++) {
            for(int c=0;c<size;c++) {
                JButton b=new JButton();
                b.setFocusPainted(false);
                b.setContentAreaFilled(true); 
                b.setOpaque(true);
                b.setBackground(Color.WHITE);
                b.setBorder(BorderFactory.createLineBorder(new Color(245,245,245))); 

                int rr=r, cc=c;
                b.addActionListener(e->onCellClick.accept(new Pos(rr,cc)));

                cells[r][c]=b;
                add(b);
            }
        }
        revalidate();
        repaint();
    }

    /**
     * Updates the board visuals based on the current game state.
     * @param st The current GameState.
     */
    public void render(GameState st) {
        var board=st.getBoard();
        if(board.getSize()!=size) rebuild(board.getSize());

        int cellSize=cells[0][0].getWidth();
        if(cellSize<=0) cellSize=64; 

        for(int r=0;r<size;r++) {
            for(int c=0;c<size;c++) {
                cells[r][c].setIcon(null);
                cells[r][c].setDisabledIcon(null);
                cells[r][c].setText("");
                cells[r][c].setBackground(Color.WHITE);
            }
        }

        putToken(st.getPrey().getPos(), st.getPrey().getName(), new Color(205,225,255), "Y", cellSize);
        putToken(st.getPredator().getPos(), st.getPredator().getName(), new Color(255,235,200), "P", cellSize);
        putToken(st.getApex().getPos(), st.getApex().getName(), new Color(255,215,215), "A", cellSize);
        putToken(st.getFood().getPos(), st.getFood().getName(), new Color(230,230,230), "F", cellSize);
    }

    private void putToken(Pos p, String imgName, Color circleColor, String fallbackText, int size) {
        if(p==null) return;
        if(p.getRow()<0 || p.getRow()>=size || p.getCol()<0 || p.getCol()>=size) return;

        JButton b=cells[p.getRow()][p.getCol()];
        ImageIcon icon=createTokenIcon(imgName, size, circleColor);
        
        if(icon!=null) {
            b.setIcon(icon);
            b.setDisabledIcon(icon);
            b.setText("");
        } else {
            b.setText(fallbackText);
            b.setForeground(circleColor.darker().darker());
            b.setFont(new Font("Arial",Font.BOLD,16));
        }
    }

    public void setEnabledAll(boolean enabled) {
        for(int r=0;r<size;r++)
            for(int c=0;c<size;c++)
                cells[r][c].setEnabled(enabled);
    }

    /**
     * Highlights cells to show valid moves or the current player.
     * @param turn Current active role.
     * @param walk Grid of valid walk moves.
     * @param ability Grid of valid ability moves.
     * @param enableClicks Whether the user can interact with highlighted cells.
     * @param playerPos Position of the current player.
     */
    public void highlight(Role turn, boolean[][] walk, boolean[][] ability, boolean enableClicks, Pos playerPos) {
        for(int r=0;r<size;r++) {
            for(int c=0;c<size;c++) {
                cells[r][c].setBackground(Color.WHITE); 

                boolean w=(walk!=null && walk[r][c]);
                boolean a=(ability!=null && ability[r][c]);
                boolean isSelf=(playerPos!=null && r==playerPos.getRow() && c==playerPos.getCol());

                if(isSelf) {
                    cells[r][c].setBackground(new Color(180,220,255)); 
                } else if(a) {
                    cells[r][c].setBackground(new Color(255,200,200));
                } else if(w) {
                    cells[r][c].setBackground(new Color(180,220,255)); 
                }

                if(enableClicks) {
                    cells[r][c].setEnabled(w || a || isSelf);
                } else {
                    cells[r][c].setEnabled(false);
                }
            }
        }
    }
    
    public void disableAll() {
         for(int r=0;r<size;r++) {
            for(int c=0;c<size;c++) {
                cells[r][c].setEnabled(false);
                cells[r][c].setBackground(Color.WHITE); 
            }
        }
    }
}