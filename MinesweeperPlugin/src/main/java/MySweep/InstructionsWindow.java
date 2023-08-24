package MySweep;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.KeyboardFocusManager;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JToggleButton;

public class InstructionsWindow extends javax.swing.JFrame {
    //initialize variables
    private final Color PURPLE = new Color(58, 0, 82);
    private final Color LIGHTPRPL = new Color(215, 196, 255);
    private final String InstructionsText = "<html>Begin by choosing numbers for width by height, then bombs, then lives. 25x25 42-60 bombs, 1-3 lives is a good starting point, but choose whatever you want. Keep in mind it takes a little bit to pull up the window if you choose something over 200x200. (CTRL+Scroll to zoom... fair warning, 200x200 is 40,000 cells so its gonna take you a while.)<br><br>"
    +"If you click a bomb, you lose a life, lose all your lives to lose the game. Click a cell to reveal if it is a bomb. If your cell is not a bomb, it will instead display how many bombs are in the 8 surrounding cells, and if there are none, it will fill out all bombless cells surrounding the cell you clicked until no cell marked with a zero is adjacent to another cell that could be marked with a zero.<br>"
    +"The goal is to reveal every cell that is not a bomb. You may mark cells that contain bombs by use of the mark bombs button, or by right-clicking a cell. You cannot interact with marked cells other than to unmark them so as to prevent accidental death. If you mark twice it will instead receive a question mark, which is similar but interacts differently with chords.<br><br>"
    +"There is another method of interaction called a chord. If you use both mouse buttons when you click an already revealed numbered cell it clicks all surrounding squares but not an X mark, and if you marked too many it penalizes you for each extra mark by exploding a bomb each. However, this means it will reveal many more squares from just 1 action so it can be advantageous. It will explode question marks but not reveal if not a bomb.<br><br>"
    +"I have made a modification from the original ruleset in that you will always reveal a square with no adjacent bombs on your first click of the game instead of just requiring the cell itself to not be a bomb (unless your board size and #ofBombs prohibit this, in which case it will revert to the original ruleset).<br><br>"
    +"CTRL+SHIFT+Click on a score to delete it. Personal leaderboards will be saved in %userprofile%\\AppData\\Roaming\\minesweeperScores on windows, and ~/.minesweeper on linux or mac. If you alter the leaderboard manually and do not follow the format, scores will be unreadable by the program. format: x:y:bombCount:lives-livesLeft-time Additionally, if Leaderboard.txt is deleted or moved your scores can't be read until you move it back, but a new Leaderboard.txt file will be created to record future games.</html>";
    private static final Icon DefaultButtonIcon = (new JButton()).getIcon();
    private JButton Back = new JButton("Back");
    private JToggleButton DMToggleButton = new JToggleButton();
    private JLabel TitleLabel = new JLabel();
    private JLabel instructions = new JLabel();
    void toggleDarkMode(){
        setDarkMode();
    }
    //constructors
    public InstructionsWindow() {
        initComponents();
    }
    //init components
    private void initComponents() {
        MetalToggleButtonUI toggleButtonSelectedColor = new MetalToggleButtonUI() {
            @Override
            protected Color getSelectColor() {
                return (MineSweeper.isDarkMode())?PURPLE:super.getSelectColor();
            }
        };
        DMToggleButton.setUI(toggleButtonSelectedColor);
        //listeners
        if(MineSweeper.isDarkMode())DMToggleButton.doClick();//<--sync toggle button status with dark mode status
        Back.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                ((JFrame) SwingUtilities.getWindowAncestor(((JButton) e.getSource()))).dispose();
            }
        });
        DMToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                MineSweeper.toggleDarkMode();
            }
        });
        KeyAdapter keyAdapter = new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                // Check if the Enter key is pressed
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    // get focused component source
                    Component CurrComp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                    if(CurrComp instanceof JToggleButton){
                        ((JToggleButton)CurrComp).doClick();
                    }else{
                        ((JButton)CurrComp).doClick();
                    }
                }
            }
        };
        Back.addKeyListener(keyAdapter);
        DMToggleButton.addKeyListener(keyAdapter);

        TitleLabel.setFont(new Font("Tahoma", 0, 36));
        TitleLabel.setText("Instructions");
        TitleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        instructions.setVerticalAlignment(SwingConstants.NORTH);
        instructions.setBorder(new EmptyBorder(10, 10, 10, 10));
        instructions.setText(InstructionsText);

        setDarkMode();

        setIconImage(MineSweeper.MineIcon);
        JPanel containerPanel = new JPanel(new GridBagLayout()){
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor((MineSweeper.isDarkMode())?PURPLE:LIGHTPRPL);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setPreferredSize(new Dimension(650,530));
        getContentPane().add(containerPanel);//<-- setLayout(GridBagLayout)
        GridBagConstraints containerConstraints = new GridBagConstraints();//<-- you create a constraints object

        containerConstraints.gridx = 0;//set some values for x and y and whatever of the constraints
        containerConstraints.gridy = 0;
        containerConstraints.gridwidth = 1;
        containerConstraints.gridheight = 1;
        containerConstraints.fill = GridBagConstraints.BOTH;
        containerPanel.add(Back, containerConstraints);//<-- then add your component to the pane, but with containerConstraints as a 2nd argument

        containerConstraints.weightx = 1.0;
        containerConstraints.gridwidth = 1;
        containerConstraints.gridx = 1;
        containerPanel.add(TitleLabel, containerConstraints);
        containerConstraints.weightx = 0.0;

        containerConstraints.gridx = 2;
        containerPanel.add(DMToggleButton, containerConstraints);

        containerConstraints.gridx = 0;
        containerConstraints.gridy = 1;
        containerConstraints.gridwidth =3;
        containerConstraints.weightx = 1.0;
        containerConstraints.weighty = 1.0;
        containerPanel.add(instructions, containerConstraints);

        pack();//<-- when you are all done adding stuff you pack() it, making sure everything is laid out as you put it on the screen.
        getContentPane().setVisible(true);//<-- and then you make it visible
    }
    private void setDarkMode(){
        if(MineSweeper.isDarkMode()){
            Back.setBackground(Color.BLACK);
            Back.setForeground(Color.WHITE);
            DMToggleButton.setBackground(Color.BLACK);
            DMToggleButton.setForeground(Color.WHITE);
            DMToggleButton.setText("<html>Dark<br>Mode</html>");
            TitleLabel.setForeground(Color.GREEN);
            instructions.setForeground(Color.WHITE);
        }else{
            Back.setBackground(null);
            Back.setIcon(DefaultButtonIcon);
            Back.setForeground(Color.BLACK);
            DMToggleButton.setBackground(null);
            DMToggleButton.setIcon(DefaultButtonIcon);
            DMToggleButton.setForeground(Color.BLACK);
            DMToggleButton.setText("<html>Light<br>Mode</html>");
            TitleLabel.setForeground(Color.BLACK);
            instructions.setForeground(Color.BLACK);
        }
    }
}