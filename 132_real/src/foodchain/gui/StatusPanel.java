package foodchain.gui;

import foodchain.core.GameEngine;
import foodchain.core.GameState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Side panel that displays game progress and player statistics.
 */
public class StatusPanel extends JPanel {

    private final JLabel roundLabel=new JLabel("Round: -",SwingConstants.LEFT);
    private final JLabel eraLabel=new JLabel("Era: -",SwingConstants.LEFT);

    private final PlayerCard apexCard;
    private final PlayerCard predatorCard;
    private final PlayerCard preyCard;

    public StatusPanel() {
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(12,12,12,12));
        setBackground(new Color(230,230,230)); 
        setPreferredSize(new Dimension(280,0)); 

        JPanel infoBox=new JPanel(new BorderLayout());
        infoBox.setBackground(Color.WHITE);
        infoBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY,1),
            new EmptyBorder(10,10,10,10)
        ));
        
        JLabel infoTitle=new JLabel("Game Information",SwingConstants.CENTER);
        infoTitle.setFont(infoTitle.getFont().deriveFont(Font.BOLD,14f));
        
        JPanel infoBody=new JPanel(new GridLayout(2,1,0,6));
        infoBody.setOpaque(false);
        infoBody.add(roundLabel);
        infoBody.add(eraLabel);

        infoBox.add(infoTitle,BorderLayout.NORTH);
        infoBox.add(infoBody,BorderLayout.CENTER);

        add(infoBox);
        add(Box.createVerticalStrut(12));

        apexCard=new PlayerCard(new Color(255,215,215)); 
        predatorCard=new PlayerCard(new Color(255,235,200)); 
        preyCard=new PlayerCard(new Color(205,225,255)); 

        add(apexCard);
        add(Box.createVerticalStrut(10));
        add(predatorCard);
        add(Box.createVerticalStrut(10));
        add(preyCard);
        add(Box.createVerticalGlue());
    }

    /**
     * Refreshes all status information from the engine.
     * @param engine The current game engine.
     */
    public void update(GameEngine engine) {
        GameState st=engine.getState();
        roundLabel.setText("Round: "+engine.getTurnManager().getRound()+" / "+engine.getTurnManager().getTotalRounds());
        eraLabel.setText("Era: "+st.getEra());

        apexCard.update("Apex Predator", st.getApex(), true);
        predatorCard.update("Predator (You)", st.getPredator(), false);
        preyCard.update("Prey", st.getPrey(), true);
    }
}