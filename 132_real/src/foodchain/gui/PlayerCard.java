package foodchain.gui;

import foodchain.entities.Animal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A UI component that displays the status of a single player (Score, Cooldown).
 */
public class PlayerCard extends JPanel {

    private final JLabel title=new JLabel("",SwingConstants.CENTER);
    private final JLabel name=new JLabel("Name:",SwingConstants.LEFT);
    private final JLabel score=new JLabel("Score:",SwingConstants.LEFT);
    private final JLabel cd=new JLabel("Ability Cooldown:",SwingConstants.LEFT);

    public PlayerCard(Color bg) {
        setLayout(new BorderLayout());
        setBackground(bg);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY,1),
            new EmptyBorder(10,10,10,10)
        ));

        title.setFont(title.getFont().deriveFont(Font.BOLD,14f));
        add(title,BorderLayout.NORTH);

        JPanel body=new JPanel(new GridLayout(3,1,0,6));
        body.setOpaque(false);
        body.add(name);
        body.add(score);
        body.add(cd);
        add(body,BorderLayout.CENTER);
    }

    /**
     * Updates the card info based on the entity's current state.
     * @param header The title for the card.
     * @param a The animal entity to display.
     * @param ai Whether this entity is controlled by AI.
     */
    public void update(String header, Animal a, boolean ai) {
        title.setText(header);
        name.setText("Name: "+a.getName()+(ai?" [AI]":" [Player]"));
        score.setText("Score: "+a.getScore());
        
        int c=a.getAbilityCooldown();
        if(c>0) {
            cd.setText("Ability Cooldown: "+c);
            cd.setForeground(Color.RED);
        } else {
            cd.setText("Ability: READY");
            cd.setForeground(new Color(0,100,0));
        }
    }
}