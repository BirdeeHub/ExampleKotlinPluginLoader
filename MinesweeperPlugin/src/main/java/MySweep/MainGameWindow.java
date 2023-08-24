package MySweep;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.util.TimerTask;
import java.util.Timer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JScrollBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
//This is the main game board display window. contains action listeners and displays control buttons, and a Grid instance, which is the game board.
public class MainGameWindow extends javax.swing.JFrame {
    //--------------Initialize-----------------------------
    private final int Fieldx, Fieldy, bombCount, lives;//you may wonder why I have so many classes. This is because, well...
    private final Color PURPLE = new Color(58, 0, 82);//its java what did you expect. so you create a new minefield to store mutable game state.
    private final Color GREEN = new Color(0, 255, 0);//Then the file got long so I put the grid display in grid and then split the rest of the window here.
    private final Dimension DefaultWindowSize = new Dimension(830, 830);//also i needed a private cellbutton class for border painting, etc...
    private JLabel timeDisplay = new JLabel();
    private final Timer displayTimer = new Timer();// Create Timer Displayer
    private final TimerTask timeDisplayTask = new TimerTask() {
        public void run() {
            long time = grid.getTime();
            if(time == -1){
                timeDisplay.setText("");
            }else{ //if you want to add a time format you can do that here.
                timeDisplay.setText(Long.toString(time/1000));
            }//we passed the value through grid.getTime() to get the correct minefield's timer without needing to find it from here
        }
    };
    private JLabel GameOverDisplay = new JLabel();
    private JLabel BombsFoundDisplay = new JLabel();
    private JLabel livesLostDisplay = new JLabel();//our 3 display labels and functions to set them.
    private final String highScoreMessage = "Record Time!";//to change text of messages in menubar display, edit from here
    private final String newBoardSizeAndWonMessage = "New Board Cleared!";
    private final String wonAndNotHighScoreMessage = "Cleared!";//compiler will treat it the same as having it actually in setGameOverDisplay()
    private final String diedButNewBoardMessage = "1st Board Death";//so having these here is just for readability
    private final String diedAndNotNewBoardMessage = "Exploded...";
    private void setBombsFoundDisplay(){
        BombsFoundDisplay.setText("M:" + Integer.toString(grid.getBombsFound()) + "/" + Integer.toString(bombCount));
    }
    private void setLivesLostDisplay(){
        livesLostDisplay.setText("L:" + Integer.toString(grid.getLivesLeft()) + "/" + Integer.toString(lives));
    }
    private void setGameOverDisplay(){
        int[] GOIndex = new int[2];//GOIndex[0] is message index, GOIndex[1] is won value.
        GOIndex = grid.getGameOverIndex();//if not in game over state, these will both be -1.
        if(GOIndex[1]==1){ GameOverDisplay.setText((GOIndex[0]==2)?highScoreMessage:((GOIndex[0]==1)?newBoardSizeAndWonMessage:wonAndNotHighScoreMessage));
        }else if(GOIndex[1]==0){ GameOverDisplay.setText((GOIndex[0]==1)?diedButNewBoardMessage:diedAndNotNewBoardMessage);
        }else GameOverDisplay.setText("");
    }
    private Grid grid;//<-- Game Board Class (action listeners not included)
    //things for listeners that needed to persistance across buttons or are used in different scopes
    private JScrollPane scrollPane;
    private JToggleButton markToggle = new JToggleButton("Mark");
    private JToggleButton chordToggle = new JToggleButton("Chord");
    private boolean LMB = false;
    private boolean RMB = false;
    private JButton currentButton = null;
    //used in both initComponents and toggleDarkMode
    private static final Icon DefaultButtonIcon = (new JButton()).getIcon();
    private JButton NewGame = new JButton("New Game");
    private JButton Reset = new JButton("Reset");
    private JButton HowToPlay = new JButton("Help");
    private JToggleButton toggleQuestionMarking = new JToggleButton("?'s?");
    private JButton ScoreBoard = new JButton("HiSc");
    void toggleDarkMode(){
        grid.toggleDarkMode();
        this.setDarkMode();
    }
//---------------------MainGameWindow CONSTRUCTOR----------------------MainGameWindow CONSTRUCTOR----------------------------MainGameWindow CONSTRUCTOR------------------------------
    public MainGameWindow(int w, int h, int bombNum, int lives) {
        Fieldx = w;
        Fieldy = h;
        this.lives = lives;
        bombCount = bombNum;
        grid = new Grid(Fieldx, Fieldy, bombCount, lives);//<-- Generate game board
        displayTimer.scheduleAtFixedRate(timeDisplayTask, 0, 50);//this just displays the time in Minefield answers.
        scrollPane = new JScrollPane(grid);
        addGridActionListeners();//<-- I could have done this in Grid but it was nice to keep the listeners in 1 place, and it would have been harder
        initComponentsAndMiscListeners();
    }
    private void addGridActionListeners(){//i would have needed to pass in the toggle buttons for click action, and scrolls for zoom. this is better.
        grid.addCellListener(new MouseAdapter() {//add our listener to each cell.
            @Override
            public void mouseEntered(MouseEvent e){//allows the 1.5 click trick by actually getting the component the mouse is over rather than just
                currentButton=(JButton)e.getSource();// getting the component that fired the mousePressed and released actions which will always be the same
            }//                                         ^ this is because each listener is associated with a particular button, and contains both methods
            @Override
            public void mousePressed(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)){
                    LMB = true;
                    if(currentButton!=null){
                        if(chordToggle.isSelected()){
                            grid.doClickType(currentButton, 2);
                        }else if(markToggle.isSelected()){
                            grid.doClickType(currentButton, 1);//mark
                        }else if(RMB){
                            grid.doClickType(currentButton, 2);//Chord
                        }else{
                            grid.doClickType(currentButton, 0);//regular
                        }
                    }
                }
                if(SwingUtilities.isRightMouseButton(e)){//same thing but for right click, so no need for clickType 0 or marktoggle check
                    RMB = true;
                    if(currentButton!=null){
                        if(LMB){
                            grid.doClickType(currentButton, 2);
                        }else{
                            grid.doClickType(currentButton, 1);
                        }
                    }
                }
                setBombsFoundDisplay();//<-- update display text with changes
                setLivesLostDisplay();
                setGameOverDisplay();
            }
            @Override
            public void mouseReleased(MouseEvent e) {//<-- not holding the mouse anymore
                if(SwingUtilities.isLeftMouseButton(e)){
                    LMB = false;
                }
                if(SwingUtilities.isRightMouseButton(e)){
                    RMB = false;
                }
            }
        });
        grid.addMouseWheelListener(new MouseWheelListener() {//zoom and scroll (only active over grid)
            JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
            JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
            int rotationAmount = 0;
            boolean zoomInProgress = false;
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if(e.isControlDown()){
                    if(zoomInProgress){rotationAmount+=e.getWheelRotation();//<--save for when previous action is done
                    }else{//initialize stuff we need to know for recentering before changing zoom
                        zoomInProgress = true;
                        rotationAmount += e.getWheelRotation();
                        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
                        Point mouseLocation = pointerInfo.getLocation();
                        SwingUtilities.convertPointFromScreen(mouseLocation, grid);//<-- find mouse relative to grid at function call
                        int mouseX1 = mouseLocation.x;
                        int mouseY1 = mouseLocation.y;
                        getContentPane().setPreferredSize(MainGameWindow.this.getContentPane().getSize());//<-- stop it from reverting to old size
                        //get wheel and cell size info and do zoom
                        SwingUtilities.invokeLater(() -> {//<-- invoke later to avoid issues with many simultaneous scroll inputs,
                            int[] gridSizesOldNew = grid.doZoom(-rotationAmount, mouseX1, mouseY1);//<-- does zoom, gives old and new button grid sizes
                            rotationAmount=0;//reset rotation amount
                            grid.setCellFontSize();//set font size after zoom so that its not dependent on rotationAmount
                            //make sure it keeps the spot the mouse is over in more or less the same place on the board.
                            int mouseX2 = (gridSizesOldNew[2]*mouseX1)/gridSizesOldNew[0];//if i calculate these inside doZoom and then call
                            int mouseY2 = (gridSizesOldNew[3]*mouseY1)/gridSizesOldNew[1];//revalidate it works weird. Outside works great. Idk.
                            int scrollAmountX = (mouseX2 - mouseX1);
                            int scrollAmountY = (mouseY2 - mouseY1);
                            int newScrollValueX = horizontalScrollBar.getValue() + scrollAmountX;
                            int newScrollValueY = verticalScrollBar.getValue() + scrollAmountY;
                            revalidate();//i got rid of the need to pack() by creating newScrollValueX & Y BEFORE i revalidate apparently
                            horizontalScrollBar.setValue(newScrollValueX);
                            verticalScrollBar.setValue(newScrollValueY);
                            zoomInProgress = false;
                        });
                    }
                } else {//if no control key, do scroll
                    verticalScrollBar.setUnitIncrement(20);
                    verticalScrollBar.setValue(verticalScrollBar.getValue() + (e.getUnitsToScroll() * verticalScrollBar.getUnitIncrement()));
                }
            }
        });
    }
    private void initComponentsAndMiscListeners() {//-------------------initComponents() on window below-----------------------------------------------------------------------------
        //--------------init
        JMenuBar menuBar = new JMenuBar();
        JPanel menuPanel = new JPanel(new GridBagLayout());
        GridBagConstraints menuBagConstraints = new GridBagConstraints();
        Font ScoreAreaFontSize = new Font("Tahoma", 0, 20);
        MetalToggleButtonUI toggleButtonSelectedColor = new MetalToggleButtonUI() {
            @Override
            protected Color getSelectColor() {
                return (MineSweeper.isDarkMode())?PURPLE:super.getSelectColor();
            }
        };
        //------------------------set stuff
        setBombsFoundDisplay();
        setLivesLostDisplay();
        setGameOverDisplay();
        setDarkMode();
        toggleQuestionMarking.setUI(toggleButtonSelectedColor);
        markToggle.setUI(toggleButtonSelectedColor);
        chordToggle.setUI(toggleButtonSelectedColor);
        BombsFoundDisplay.setHorizontalAlignment(SwingConstants.CENTER);
        livesLostDisplay.setHorizontalAlignment(SwingConstants.CENTER);
        GameOverDisplay.setHorizontalAlignment(SwingConstants.CENTER);
        timeDisplay.setHorizontalAlignment(SwingConstants.CENTER);
        BombsFoundDisplay.setFont(ScoreAreaFontSize);
        livesLostDisplay.setFont(ScoreAreaFontSize);
        GameOverDisplay.setFont(ScoreAreaFontSize);
        timeDisplay.setFont(ScoreAreaFontSize);
        timeDisplay.setForeground(GREEN);
        BombsFoundDisplay.setForeground(GREEN);
        livesLostDisplay.setForeground(GREEN);
        GameOverDisplay.setForeground(GREEN);
        timeDisplay.setBackground(PURPLE);
        livesLostDisplay.setBackground(PURPLE);
        BombsFoundDisplay.setBackground(PURPLE);
        GameOverDisplay.setBackground(PURPLE);
        timeDisplay.setOpaque(true);
        livesLostDisplay.setOpaque(true);
        BombsFoundDisplay.setOpaque(true);
        GameOverDisplay.setOpaque(true);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setBackground(PURPLE);
        scrollPane.getHorizontalScrollBar().setBackground(PURPLE);
        scrollPane.setBackground(PURPLE);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setPreferredSize(DefaultWindowSize);
        setIconImage(MineSweeper.MineIcon);
        //---------------------component adding and layout managing
        menuBagConstraints.gridx =0;
        menuBagConstraints.gridy =0;
        menuBagConstraints.gridwidth =1;
        menuBagConstraints.gridheight =1;//           theyre all in a menu bar.
        menuBagConstraints.weightx = 0.0;
        menuBagConstraints.fill = GridBagConstraints.BOTH;
        menuPanel.add(markToggle, menuBagConstraints);
        menuBagConstraints.gridx =1;
        menuPanel.add(chordToggle, menuBagConstraints);
        menuBagConstraints.gridx =2;
        menuPanel.add(toggleQuestionMarking, menuBagConstraints);
        menuBagConstraints.gridx =3;
        menuBagConstraints.weightx = 0.25;
        menuPanel.add(BombsFoundDisplay, menuBagConstraints);
        menuBagConstraints.gridx =4;
        menuPanel.add(livesLostDisplay, menuBagConstraints);
        menuBagConstraints.gridx =5;
        menuBagConstraints.weightx = 1.0;
        menuPanel.add(timeDisplay, menuBagConstraints);
        menuBagConstraints.weightx = 0.75;
        menuBagConstraints.gridx =6;
        menuPanel.add(GameOverDisplay, menuBagConstraints);
        menuBagConstraints.gridx =7;
        menuBagConstraints.weightx = 0.0;
        menuPanel.add(ScoreBoard, menuBagConstraints);
        menuBagConstraints.gridx =8;
        menuPanel.add(HowToPlay, menuBagConstraints);
        menuBagConstraints.gridx =9;
        menuPanel.add(Reset, menuBagConstraints);
        menuBagConstraints.gridx =10;
        menuPanel.add(NewGame, menuBagConstraints);
        menuBar.add(menuPanel);
        setJMenuBar(menuBar);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        //System.out.println("Start packing.");//<-- if you need proof of the below, uncomment this and start a 300x300 with 6000 bombs game.
        pack();//<-- This pack() call is the slowest, heaviest thing in the entire program. But we need to call it to use layout managers...
        getContentPane().revalidate();//^For 300x300 (90,000 cells) execution reaches here in under 1s, and the rest after it is even faster.
        grid.setCellFontSize();//       ^pretty sure to make it faster would need a different language unless there is a better pack function somewhere?

        //------------------misc action listeners----------------------misc action listeners-------------misc action listeners-------------misc action listeners------
        Reset.addActionListener(new ActionListener() {//reset button
            long clickmemory = System.currentTimeMillis();//<-- this is for protection from spamming
            public void actionPerformed(ActionEvent evt) {
                getContentPane().setPreferredSize(MainGameWindow.this.getContentPane().getSize());//<-- stop it from reverting to old size
                if((System.currentTimeMillis()-clickmemory)>1000){//<-- this means 1s has passed since last click
                    clickmemory = System.currentTimeMillis();//<-- if so, update clickmemory
                    grid.ResetBoard();
                    grid.resetZoom(MainGameWindow.this.getContentPane().getSize());
                    setBombsFoundDisplay();
                    setLivesLostDisplay();
                    setGameOverDisplay();
                    getContentPane().revalidate();
                }else clickmemory = System.currentTimeMillis();//if you spammed, update clickmemory
            }
        });
        toggleQuestionMarking.addActionListener(new ActionListener(){//----toggles the ? option for marking cells on and off
            public void actionPerformed(ActionEvent e){
                grid.toggleQuestionMarks();
            }
        });
        NewGame.addActionListener(new ActionListener() {//new game
            public void actionPerformed(ActionEvent evt) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        new OpeningWindow().setVisible(true);
                    }
                });
                MainGameWindow.this.dispose();
            }
        });
        HowToPlay.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        new InstructionsWindow().setVisible(true);
                    }
                });
            }
        });
        ScoreBoard.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        new ScoresWindow(Fieldx, Fieldy, bombCount, lives, MainGameWindow.this).setVisible(true);
                    }//clickable is false at start so you dont cancel ur game on accident.
                });
            }
        });
        KeyAdapter keyAdapter = new KeyAdapter() {//enter key functionality
            public void keyPressed(KeyEvent evt) {
                // Check if the Enter key is pressed
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    // get focused component source
                    Component CurrComp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                    if(CurrComp instanceof JToggleButton){
                        ((JToggleButton)CurrComp).doClick();
                    }else ((JButton)CurrComp).doClick();
                }
            }
        };
        ScoreBoard.addKeyListener(keyAdapter);
        NewGame.addKeyListener(keyAdapter);
        Reset.addKeyListener(keyAdapter);
        HowToPlay.addKeyListener(keyAdapter);
        markToggle.addKeyListener(keyAdapter);
        toggleQuestionMarking.addKeyListener(keyAdapter);
        chordToggle.addKeyListener(keyAdapter);

        getContentPane().setVisible(true);
    }
    private void setDarkMode(){
        if(MineSweeper.isDarkMode()){
            markToggle.setForeground(Color.WHITE);
            markToggle.setBackground(Color.BLACK);
            chordToggle.setForeground(Color.WHITE);
            chordToggle.setBackground(Color.BLACK);
            toggleQuestionMarking.setForeground(Color.WHITE);
            toggleQuestionMarking.setBackground(Color.BLACK);
            NewGame.setForeground(Color.WHITE);
            NewGame.setBackground(Color.BLACK);
            Reset.setForeground(Color.WHITE);
            Reset.setBackground(Color.BLACK);
            HowToPlay.setForeground(Color.WHITE);
            HowToPlay.setBackground(Color.BLACK);
            ScoreBoard.setForeground(Color.WHITE);
            ScoreBoard.setBackground(Color.BLACK);
        }else{
            markToggle.setForeground(Color.BLACK);
            markToggle.setBackground(null);
            markToggle.setIcon(DefaultButtonIcon);
            chordToggle.setForeground(Color.BLACK);
            chordToggle.setBackground(null);
            chordToggle.setIcon(DefaultButtonIcon);
            toggleQuestionMarking.setForeground(Color.BLACK);
            toggleQuestionMarking.setBackground(null);
            toggleQuestionMarking.setIcon(DefaultButtonIcon);
            NewGame.setForeground(Color.BLACK);
            NewGame.setBackground(null);
            NewGame.setIcon(DefaultButtonIcon);
            Reset.setForeground(Color.BLACK);
            Reset.setBackground(null);
            Reset.setIcon(DefaultButtonIcon);
            HowToPlay.setForeground(Color.BLACK);
            HowToPlay.setBackground(null);
            HowToPlay.setIcon(DefaultButtonIcon);
            ScoreBoard.setForeground(Color.BLACK);
            ScoreBoard.setBackground(null);
            ScoreBoard.setIcon(DefaultButtonIcon);
        }
    }
}