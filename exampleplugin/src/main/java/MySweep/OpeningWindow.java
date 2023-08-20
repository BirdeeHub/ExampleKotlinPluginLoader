package MySweep;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.EventQueue;
import java.awt.Color;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;

public class OpeningWindow extends JFrame {//<-- its a JFrame
    private JTextField WidthField;
    private JTextField HeightField;//<-- we create the actual instances to go in these in the constructor
    private JTextField BombNumber;
    private JTextField LivesNumber;
    private JButton Start = new JButton("Start!");
    private JButton ScoreBoard = new JButton();
    private JButton HelpWindow = new JButton();
    private JLabel LifeFieldLabel = new JLabel();
    private JLabel WidthFieldLabel = new JLabel();
    private JLabel HeightFieldLabel = new JLabel();
    private JLabel BombFieldLabel = new JLabel();
    private JLabel TitleLabel = new JLabel();
    private JLabel AuthorLabel = new JLabel();
    private final Color PURPLE = new Color(58, 0, 82);
    private final Color LIGHTPRPL = new Color(215, 196, 255);
    private static final Icon DefaultButtonIcon = (new JButton()).getIcon();
    //-----------------------------------------Constructors----------------------------------------------------------
    public OpeningWindow(String initialx, String initialy, String initialbombno, String initiallives) {
        WidthField = new JTextField(initialx);
        HeightField = new JTextField(initialy);
        BombNumber = new JTextField(initialbombno);
        LivesNumber = new JTextField(initiallives);
        initComponents();
    }
    public OpeningWindow() {
        WidthField = new JTextField();
        HeightField = new JTextField();
        BombNumber = new JTextField();
        LivesNumber = new JTextField();
        initComponents();
    }
    //-------------------------------------------------Start Action Performed---called by action listener on start button-------------------
    private void StartActionPerformed() {//this function runs MainGameWindow, performs error checking and displays errors
        try{
            int width =(int)(Integer.parseInt(WidthField.getText()));
            int height =(int)(Integer.parseInt(HeightField.getText()));
            int bombCount = (int)(Integer.parseInt(BombNumber.getText()));
            int lives = (int)(Integer.parseInt(LivesNumber.getText()));
            if(width*height<=bombCount||bombCount<0||lives<1||width<1||height<1){
                if(lives<1)LifeFieldLabel.setText("no life");
                if(width<1)WidthFieldLabel.setText("invalid width");
                if(height<1)HeightFieldLabel.setText("invalid height");
                if(width*height<=bombCount)BombFieldLabel.setText("Space<Bombs");
                if(bombCount<0)BombFieldLabel.setText("Bombs<0");
                return;
            }
            EventQueue.invokeLater(new Runnable() {
                public void run() {new MainGameWindow(width,height,bombCount,lives).setVisible(true);}
            });
            OpeningWindow.this.dispose();
        }catch(NumberFormatException e){TitleLabel.setText("Invalid field(s)");}
    } 
    void toggleDarkMode(){//<-- MineSweeper.toggleDarkMode() calls this function
        setDarkMode();//<-- and it calls this, which is defined at the end of the file
        repaint();
    }
    //---------------------------------initComponents()-----called by constructor-----------------------------------------------------------------
    private void initComponents() {//<-- a private function that doesnt return anything. It does stuff though.
        //--------------------------------------add action Listeners to components
        Start.addActionListener(new ActionListener() {//<-- our start button was clicked?
            public void actionPerformed(ActionEvent evt) {
                StartActionPerformed();//<-- run the start function!
            }
        });
        ScoreBoard.addActionListener(new ActionListener() {//<-- action listeners are also interfaces with various functions you can assign
            public void actionPerformed(ActionEvent evt) {//<-- we have assigned the actionPerformed function to do a thing
                EventQueue.invokeLater(new Runnable() {//<-- and that thing is to implement the runnable interface
                    public void run() {//<-- this run() will launch the scores window. It is part of the Runnable class, but we have to define it.
                        try{
                            int width =(int)(Integer.parseInt(WidthField.getText()));//get input info so we can highlight the current one.
                            int height =(int)(Integer.parseInt(HeightField.getText()));
                            int bombCount = (int)(Integer.parseInt(BombNumber.getText()));
                            int lives = (int)(Integer.parseInt(LivesNumber.getText()));
                            new ScoresWindow(width,height,bombCount,lives,OpeningWindow.this).setVisible(true);//<-- and then run our scores window
                        }catch(NumberFormatException e){
                            new ScoresWindow(OpeningWindow.this).setVisible(true);//<-- if bad input, use the other constructor
                        }
                    }
                });
            }
        });
        HelpWindow.addActionListener(new ActionListener() {//and this one runs our Help window!
            public void actionPerformed(ActionEvent evt) {//these are anonymous interface classes. They are defined within the () of a function call.
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        new InstructionsWindow().setVisible(true);
                    }
                });//<-- see?
            }
        });//<-- 2 of them!
        KeyAdapter keyAdapter = new KeyAdapter() {//<-- this one is not defined as an anonymous class. It is called keyAdapter and it is a KeyAdapter.
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    Component CurrComp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                    if(!(CurrComp instanceof JButton)){
                        StartActionPerformed();//<-- and here is where we say to start the start if it was a text field
                    }else ((JButton)CurrComp).doClick();
                }//It gives us enter key functionality!
            }    //the tab focusable component property is on by default unless you turn it off in Swing.
        };
        Start.addKeyListener(keyAdapter);
        ScoreBoard.addKeyListener(keyAdapter);//<-- if you dont add listeners directly (anonymously) 
        WidthField.addKeyListener(keyAdapter);//<-you have to add them to the components you want later like this
        HeightField.addKeyListener(keyAdapter);
        BombNumber.addKeyListener(keyAdapter);
        LivesNumber.addKeyListener(keyAdapter);
        HelpWindow.addKeyListener(keyAdapter);

        //set font and initial text
        ScoreBoard.setFont(new Font("Tahoma", 0, 12));
        ScoreBoard.setText("HiSc");
        HelpWindow.setFont(new Font("Tahoma", 0, 12));
        HelpWindow.setText("Help");
        LifeFieldLabel.setFont(new Font("Tahoma", 0, 14));
        LifeFieldLabel.setText("#ofLives:");
        LifeFieldLabel.setHorizontalAlignment(SwingConstants.CENTER);
        WidthFieldLabel.setFont(new Font("Tahoma", 0, 14));
        WidthFieldLabel.setText("Width(in Tiles):");
        WidthFieldLabel.setHorizontalAlignment(SwingConstants.CENTER);
        HeightFieldLabel.setFont(new Font("Tahoma", 0, 14));
        HeightFieldLabel.setText("Height(in Tiles):");
        HeightFieldLabel.setHorizontalAlignment(SwingConstants.CENTER);
        BombFieldLabel.setFont(new Font("Tahoma", 0, 14));
        BombFieldLabel.setText("#ofBombs");
        BombFieldLabel.setHorizontalAlignment(SwingConstants.CENTER);
        TitleLabel.setFont(new Font("Tahoma", 0, 36));
        TitleLabel.setText("Mine Sweeper");
        AuthorLabel.setFont(new Font("Tahoma", 0, 12));
        AuthorLabel.setText("-Birdee");
        AuthorLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        setIconImage(MineSweeper.MineIcon);

        setDarkMode();
        //You can actually use this syntax when you use 'new' to modify any class rather than just interfaces.
        JPanel backgroundPanel = new JPanel(){//<-- new ClassConstructor(...){Your Stuff Here};
            @Override//<-- use @Override to modify the original function of the class (works for extends as well!)
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor((MineSweeper.isDarkMode())?PURPLE:LIGHTPRPL);//<-- this one makes our background a prettier color.
                g.fillRect(0, 0, getWidth(), getHeight());
            }//^unfortunately, I had to do this, because when using component.setBackground(color) the color gets inherited by child components if their background is null.
        };

        //--------------------------now to add our stuff to our content pane----------------
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setPreferredSize(new Dimension(300, 200));
        getContentPane().add(backgroundPanel);
        backgroundPanel.setLayout(new GridBagLayout());//<-- add the layout manager
        GridBagConstraints containerConstraints = new GridBagConstraints();//<-- you modify this, and add a thing to the pane with it

        containerConstraints.gridx =2;//set some values for x and y and whatever of the constraints
        containerConstraints.gridy =0;
        containerConstraints.gridwidth =3;
        containerConstraints.gridheight =1;
        containerConstraints.fill = GridBagConstraints.BOTH;
        backgroundPanel.add(TitleLabel, containerConstraints);//<-- then add your component to the pane, but with containerConstraints as a 2nd argument

        containerConstraints.gridx =4;//<-- you can then change the values you want to, and then repeat the process, 
        containerConstraints.gridy =1;// and TitleLabel will keep the previous setting as its position.
        containerConstraints.gridwidth =1;
        containerConstraints.gridheight =1;
        containerConstraints.fill = GridBagConstraints.BOTH;
        backgroundPanel.add(AuthorLabel, containerConstraints);//<-- and this will recieve the new values, along with whatever wasnt changed from before.

        containerConstraints.gridx =2;
        containerConstraints.gridy =2;
        containerConstraints.gridwidth =1;
        containerConstraints.gridheight =1;
        containerConstraints.fill = GridBagConstraints.BOTH;
        backgroundPanel.add(WidthFieldLabel, containerConstraints);

        containerConstraints.gridx =4;
        containerConstraints.gridy =2;
        containerConstraints.gridwidth =1;
        containerConstraints.gridheight =1;
        containerConstraints.fill = GridBagConstraints.BOTH;
        backgroundPanel.add(HeightFieldLabel, containerConstraints);

        containerConstraints.gridx =2;
        containerConstraints.gridy =3;
        containerConstraints.gridwidth =1;
        containerConstraints.gridheight =1;
        containerConstraints.fill = GridBagConstraints.BOTH;
        backgroundPanel.add(WidthField, containerConstraints);

        containerConstraints.gridx =4;
        containerConstraints.gridy =3;
        containerConstraints.gridwidth =1;
        containerConstraints.gridheight =1;
        containerConstraints.fill = GridBagConstraints.BOTH;
        backgroundPanel.add(HeightField, containerConstraints);

        containerConstraints.gridx =2;
        containerConstraints.gridy =4;
        containerConstraints.gridwidth =1;
        containerConstraints.gridheight =1;
        containerConstraints.fill = GridBagConstraints.BOTH;
        backgroundPanel.add(BombFieldLabel, containerConstraints);

        containerConstraints.gridx =4;
        containerConstraints.gridy =4;
        containerConstraints.gridwidth =1;
        containerConstraints.gridheight =1;
        containerConstraints.fill = GridBagConstraints.BOTH;
        backgroundPanel.add(LifeFieldLabel, containerConstraints);

        containerConstraints.gridx =2;
        containerConstraints.gridy =5;
        containerConstraints.gridwidth =1;
        containerConstraints.gridheight =1;
        containerConstraints.fill = GridBagConstraints.BOTH;
        backgroundPanel.add(BombNumber, containerConstraints);

        containerConstraints.gridx =4;
        containerConstraints.gridy =5;
        containerConstraints.gridwidth =1;
        containerConstraints.gridheight =1;
        containerConstraints.fill = GridBagConstraints.BOTH;
        backgroundPanel.add(LivesNumber, containerConstraints);

        containerConstraints.gridx =2;
        containerConstraints.gridy =6;
        containerConstraints.gridwidth =1;
        containerConstraints.gridheight =1;
        containerConstraints.fill = GridBagConstraints.BOTH;
        backgroundPanel.add(ScoreBoard, containerConstraints);

        containerConstraints.gridx =2;
        containerConstraints.gridy =7;
        containerConstraints.gridwidth =1;
        containerConstraints.gridheight =1;
        containerConstraints.fill = GridBagConstraints.BOTH;
        backgroundPanel.add(HelpWindow, containerConstraints);

        containerConstraints.gridx =4;
        containerConstraints.gridy =6;
        containerConstraints.gridwidth =1;
        containerConstraints.gridheight =2;
        containerConstraints.fill = GridBagConstraints.BOTH;
        backgroundPanel.add(Start, containerConstraints);

        pack();
        getContentPane().setVisible(true);
    }
    private void setDarkMode(){//sets colors appropriately based on DarkMode
        if(MineSweeper.isDarkMode()){
            Start.setForeground(Color.WHITE);
            ScoreBoard.setForeground(Color.WHITE);
            HelpWindow.setForeground(Color.WHITE);
            Start.setBackground(Color.BLACK);
            ScoreBoard.setBackground(Color.BLACK);
            HelpWindow.setBackground(Color.BLACK);
            LifeFieldLabel.setForeground(Color.WHITE);
            WidthFieldLabel.setForeground(Color.WHITE);
            HeightFieldLabel.setForeground(Color.WHITE);
            BombFieldLabel.setForeground(Color.WHITE);
            TitleLabel.setForeground(Color.GREEN);
            AuthorLabel.setForeground(Color.GREEN);
        }else{
            Start.setBackground(null);
            Start.setIcon(DefaultButtonIcon);
            Start.setForeground(Color.BLACK);
            ScoreBoard.setBackground(null);
            ScoreBoard.setIcon(DefaultButtonIcon);
            ScoreBoard.setForeground(Color.BLACK);
            HelpWindow.setBackground(null);
            HelpWindow.setIcon(DefaultButtonIcon);
            HelpWindow.setForeground(Color.BLACK);
            LifeFieldLabel.setForeground(Color.BLACK);
            WidthFieldLabel.setForeground(Color.BLACK);
            HeightFieldLabel.setForeground(Color.BLACK);
            BombFieldLabel.setForeground(Color.BLACK);
            TitleLabel.setForeground(Color.BLACK);
            AuthorLabel.setForeground(Color.BLACK);
        }
    }
}
