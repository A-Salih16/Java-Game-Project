package foodchain.gui;

import foodchain.core.GameConfig;
import foodchain.model.Era;
import foodchain.model.GridSize;



import javax.swing.*;
import java.awt.*;

/**
 * The initial screen for configuring game settings.
 * Allows selection of Era, Grid Size, and Rounds.
 */
public class StartPanel extends JPanel {

    private final JComboBox<Era> eraBox=new JComboBox<>(Era.values());
    private final JComboBox<GridSize> sizeBox=new JComboBox<>(GridSize.values());
    private final JSpinner roundsSpinner=new JSpinner(new SpinnerNumberModel(10,10,100,1));

    public StartPanel(GameFrame frame) {
        setLayout(new BorderLayout(10,10));

        JLabel title=new JLabel("FoodChain - Start",SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI",Font.BOLD,32));
        title.setBorder(BorderFactory.createEmptyBorder(20,0,20,0));
        add(title,BorderLayout.NORTH);

        JPanel form=new JPanel(new GridLayout(4,2,10,20));
        form.setBorder(BorderFactory.createEmptyBorder(20,100,20,100));

        form.add(new JLabel("Select Era:"));
        form.add(eraBox);

        form.add(new JLabel("Grid Size:"));
        form.add(sizeBox);

        form.add(new JLabel("Total Rounds:"));
        form.add(roundsSpinner);

        add(form,BorderLayout.CENTER);

        JButton startBtn=new JButton("START GAME");
        startBtn.setFont(new Font("Segoe UI",Font.BOLD,18));
        startBtn.setBackground(new Color(60,179,113));
        startBtn.setForeground(Color.WHITE);
        
        startBtn.addActionListener(e->{
            try {
                Era era=(Era)eraBox.getSelectedItem();
                GridSize gs=(GridSize)sizeBox.getSelectedItem();
                int totalRounds=(Integer)roundsSpinner.getValue();
      

                GameConfig cfg=new GameConfig(era,gs,totalRounds);
                frame.startNewGame(cfg);
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setBorder(BorderFactory.createEmptyBorder(0,0,40,0));
        bottom.add(startBtn);
        add(bottom,BorderLayout.SOUTH);
    }
}