package MySweep;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.Insets;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;

public class ScoresWindow extends JFrame {
    private final ScoreEntry thisBoard;
    private JToggleButton clickableToggle = new JToggleButton("click?");
    private boolean clickable;
    private JFrame ParentFrame;//we need this reference in case we have to close it
    private boolean FileIssue = false;//<-- used to stop it from becoming a button when you use the toggle button if file issue. Probably not needed anymore since I split stuff out into IO and Entry but I'd have to change stuff
    private final Dimension defaultwindowsize = new Dimension(280, 500);
    private JLabel ColumnHeadingLabel1, ColumnHeadingLabel2, ColumnHeadingLabel3, TitleLabel;//these are initialized globally to allow toggle dark mode to access them.
    private JButton Back;
    private JPanel BoardPanel, LivesPanel, TimePanel;//these ones are globally initialized to allow leaderboardText() to be called anywhere in the file
    private JLabel[] BoardLabel, LivesLabel, TimeLabel;//                                                              ^leaderboardText() defined at end of file.
    private JButton[] BoardButton;//<-- these are here so we can swap them without re-running set leaderboard text function
    private boolean isControlDown;
    private boolean isShiftDown;
    private boolean isDeleteMode;//<-- controls toggle button background color WHILE SELECTED (see constructor)
    private final Color PURPLE = new Color(58, 0, 82);
    private final Color LIGHTPRPL = new Color(215, 196, 255);
    private static final Icon DefaultButtonIcon = (new JButton()).getIcon();
    //-----------------------action listeners for leaderboardText(...). Yes, you can globally declare action listeners too------------------
    private ActionListener BoardButtonListener = new ActionListener(){
        public void actionPerformed(ActionEvent evt) {
            if(clickableToggle.isSelected()==true){
                if(!(isControlDown&&isShiftDown))BoardButtonPressedAction(((JButton) evt.getSource()), ParentFrame);
                if(isControlDown&&isShiftDown)BoardButtonDeleteAction((JButton) evt.getSource());
            }
        }
    };
    private KeyAdapter keyAdapter = new KeyAdapter() {//<-- key listener. CTRL+SHIFT+Click (or enter) to delete!
        public void keyPressed(KeyEvent evt) {
            if(evt.getKeyCode() == KeyEvent.VK_CONTROL){// Check if control key is pressed
                isControlDown = true;
            }
            if(evt.getKeyCode() == KeyEvent.VK_SHIFT){// Check if shift key is pressed
                isShiftDown = true;
            }
            if(isControlDown&&isShiftDown){//update display to show we are in delete mode
                isDeleteMode=true;
                clickableToggle.setText("delete?");
            }
            // Check if the Enter key is pressed
            if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
                // get focused component source
                Component CurrComp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if(CurrComp == clickableToggle){//<- this one is a JToggleButton
                    clickableToggle.doClick();
                }else ((JButton)CurrComp).doClick();
            }
        }
        public void keyReleased(KeyEvent evt) {//if you release a key, let the program know
            if(evt.getKeyCode() == KeyEvent.VK_CONTROL){
                isControlDown = false;
            }
            if(evt.getKeyCode() == KeyEvent.VK_SHIFT){
                isShiftDown = false;
            }
            if(!isControlDown||!isShiftDown){
                isDeleteMode=false;
                clickableToggle.setText("click?");
            }
        }
    };
    //The 2 things you can do other than look
    //------------------------Quickstart a new game
    private void BoardButtonPressedAction(JButton BoardButtonPressed, JFrame ParentFrame){
        ScoreEntry buttonEntry=((ScoreEntry)BoardButtonPressed.getClientProperty("BoardTarget"));
        if(buttonEntry.isValid()){
            ParentFrame.dispose();//<-- close the old window
            EventQueue.invokeLater(new Runnable() {
                public void run() {//open a pre populated opening window. Hit enter, or change something first
                    new OpeningWindow(Integer.toString(buttonEntry.getX()),Integer.toString(buttonEntry.getY()),Integer.toString(buttonEntry.getBombCount()),Integer.toString(buttonEntry.getLives())).setVisible(true);
                }
            });
            ScoresWindow.this.dispose();//<-- close the score window
        }
    }//-------------------------or delete a score.
    private void BoardButtonDeleteAction(JButton BoardButtonPressed){
        ScoresFileIO.deleteScoreEntry(((ScoreEntry)BoardButtonPressed.getClientProperty("BoardTarget")));//<-- delete this button's score from file
        BoardPanel.removeAll();//remove items in the GridLayout panels which are our columns because we are going to re-add from file.
        LivesPanel.removeAll();
        TimePanel.removeAll();
        leaderboardText();//<-- Update the text for main scores display
        revalidate();
    }
    //------------------------------------Constructor------------Constructor-------------Constructor---------------------------------
    public ScoresWindow(int Fieldx, int  Fieldy, int bombCount, int lives, JFrame ParentFrame) {
        thisBoard = new ScoreEntry(Fieldx, Fieldy, bombCount, lives, 0, 0);//<-- this is how it knows what score to highlight
        clickableToggle.setSelected(!(ParentFrame instanceof MainGameWindow));//<-- make button reflect default state of clickable
        this.clickable = clickableToggle.isSelected();//<-- get default state of clickable for our global variable
        this.ParentFrame=ParentFrame;//<-- we need this to close it later if new board is chosen
        initComponents();
    }
    public ScoresWindow(JFrame ParentFrame) {
        thisBoard = new ScoreEntry();
        clickableToggle.setSelected(!(ParentFrame instanceof MainGameWindow));//<-- make button reflect default state of clickable
        this.clickable = clickableToggle.isSelected();//<-- get default state of clickable for our global variable
        this.ParentFrame=ParentFrame;//<-- we need this to close it later if new board is chosen
        initComponents();
    }
    void toggleDarkMode(){
        repaint();
        setDarkMode();
    }
    //---------------------------this is the function to set the buttons and labels for the scores in the window.---------------------------------------
    //---------------------------leaderboardText()---------------leaderboardText()------------------leaderboardText()-------------leaderboardText()--------
    //----------------------------------------------leaderboardText()-----------Reads files, creates components based on contents-----------------
    private void leaderboardText(){
        //creates components with info from string array recieved from scoresFileManager
        String SHL="<u>"; //StartHighLight variable for easily changing tags
        String EHL="</u>";//EndHighLight
        String Shtml="<html>";
        String Ehtml="</html>";
        ScoreEntry[] entries = ScoresFileIO.readLeaderboard();//<-- read scores file to ScoreEntry array
        if(entries==null){//<-- no file was present
            FileIssue=true;
            BoardLabel = new JLabel[1];
            BoardButton = new JButton[1];
            LivesLabel = new JLabel[1];
            TimeLabel = new JLabel[1];
            BoardLabel[0] = new JLabel("File");
            BoardButton[0] = new JButton("File");
            LivesLabel[0] = new JLabel("not");
            TimeLabel[0] = new JLabel("found.");
            BoardPanel.add(BoardLabel[0]);
            LivesPanel.add(LivesLabel[0]);
            TimePanel.add(TimeLabel[0]);
        }else if(entries.length==0){//<-- file was present but empty
            FileIssue=true;
            BoardButton = new JButton[1];
            BoardLabel = new JLabel[1];
            LivesLabel = new JLabel[1];
            TimeLabel = new JLabel[1];
            BoardLabel[0] = new JLabel("File");
            BoardButton[0] = new JButton("File");
            LivesLabel[0] = new JLabel("is");
            TimeLabel[0] = new JLabel("empty.");
            BoardPanel.add(BoardLabel[0]);
            LivesPanel.add(LivesLabel[0]);
            TimePanel.add(TimeLabel[0]);
        }else{//     <----------------------------- score file with entries was found
            String[] BoardText = new String[entries.length];
            String[] LivesText = new String[entries.length];
            String[] TimeText = new String[entries.length];
            BoardButton = new JButton[entries.length];
            for(int c=0; c<entries.length; c++){//initialize board buttons
                BoardButton[c] = new JButton();
                BoardButton[c].putClientProperty("BoardTarget", "");//<-- we are about to overwrite this in a second anyway but if we dont initialize we might glitch our highlighting
            }//Board String will be its own property so that we can change how it displays as we wish
            for(int c=0; c<entries.length; c++){//<-- for entries returned from read
                if(entries[c].isValid()){//Is it valid?
                    String finalBoardDisplayString = entries[c].getX()+"x"+entries[c].getY()+" B:"+entries[c].getBombCount()+" L:"+entries[c].getLives();
                    if(entries[c].equals(thisBoard)){//<-- This is us! add to start
                        if(entries.length>1 && c!=0){
                            for(int add=entries.length-1; add>0; add--){//<-- move all values up 1 first so we dont overwrite when we place at start
                                BoardButton[add].putClientProperty("BoardTarget",BoardButton[add-1].getClientProperty("BoardTarget"));
                                BoardButton[add].setText(BoardButton[add-1].getText());  //^^^^this is what not initializing it would glitch
                                BoardText[add]=BoardText[add-1];                         //it would try to get a property that does not exist.
                                LivesText[add]=LivesText[add-1];
                                TimeText[add]=TimeText[add-1];
                            }
                        }//now place our board at start
                        BoardButton[0].putClientProperty("BoardTarget", entries[c]);
                        BoardButton[0].setText(Shtml+SHL+finalBoardDisplayString+EHL+Ehtml);
                        BoardText[0] = Shtml+SHL+finalBoardDisplayString+EHL+Ehtml;
                        LivesText[0] = Shtml+SHL+((entries[c].getRemainingLives()==0)?"DIED AT":entries[c].getRemainingLives())+EHL+Ehtml;
                        TimeText[0] = Shtml+SHL+Long.toString(entries[c].getTime()/1000)+EHL+Ehtml;
                    } else{//add scores that arent us to end
                        BoardButton[c].putClientProperty("BoardTarget", entries[c]);
                        BoardButton[c].setText(Shtml+finalBoardDisplayString+Ehtml);
                        BoardText[c] = Shtml+finalBoardDisplayString+Ehtml;
                        LivesText[c] = Shtml+((entries[c].getRemainingLives()==0)?"DIED AT":entries[c].getRemainingLives())+Ehtml;
                        TimeText[c] = Shtml+Long.toString(entries[c].getTime()/1000)+Ehtml;
                    }
                } else {//invalid entry?
                    BoardButton[c].putClientProperty("BoardTarget", new ScoreEntry());//<--assign an empty one in case we try to do isValid and it crashes somehow
                    BoardButton[c].setText("entry");
                    BoardText[c] = "entry";
                    LivesText[c] = "is";
                    TimeText[c] = "invalid";
                }
            }
            BoardLabel = new JLabel[entries.length];//initialize all the labels and button properties
            LivesLabel = new JLabel[entries.length];//that we didnt need to add during read.
            TimeLabel = new JLabel[entries.length];
            for(int i=0; i<entries.length; i++){
                BoardButton[i].setMargin(new Insets(-1, 0, -1, 0));
                BoardButton[i].setBorderPainted(false);
                BoardButton[i].addActionListener(BoardButtonListener);
                BoardButton[i].addKeyListener(keyAdapter);
                BoardLabel[i] = new JLabel(BoardText[i]);
                BoardLabel[i].setHorizontalAlignment(SwingConstants.CENTER);
                LivesLabel[i] = new JLabel(LivesText[i]);
                LivesLabel[i].setBorder(new EmptyBorder(0, 0, 0, 0));
                TimeLabel[i] = new JLabel(TimeText[i]);
                TimeLabel[i].setBorder(new EmptyBorder(0, 0, 0, 10));
                LivesPanel.add(LivesLabel[i]);//<-- add LivesLabel[i] to panel
                TimePanel.add(TimeLabel[i]);//<----  add TimeLabel[i] to panel
                if(clickable){//<--    add correct board button/label to panel
                    BoardPanel.add(BoardButton[i]);
                }else{
                    BoardPanel.add(BoardLabel[i]);
                }
            }
        }
        setDarkMode();
    }
    //----------------setDarkMode()----------setDarkMode()-----------------
    private void setDarkMode(){
        if(MineSweeper.isDarkMode()){
            Back.setForeground(Color.WHITE);
            Back.setBackground(Color.BLACK);
            clickableToggle.setForeground(Color.WHITE);
            clickableToggle.setBackground(Color.BLACK);
            ColumnHeadingLabel1.setForeground(Color.WHITE);
            ColumnHeadingLabel2.setForeground(Color.WHITE);
            ColumnHeadingLabel3.setForeground(Color.WHITE);
            TitleLabel.setForeground(Color.GREEN);
            for(int i=0;i<BoardLabel.length;i++){
                BoardButton[i].setForeground(Color.WHITE);
                BoardButton[i].setBackground(Color.BLACK);
                BoardLabel[i].setForeground(Color.WHITE);
                LivesLabel[i].setForeground(Color.WHITE);
                TimeLabel[i].setForeground(Color.WHITE);
            }
        }else{
            Back.setForeground(Color.BLACK);
            Back.setBackground(null);
            Back.setIcon(DefaultButtonIcon);
            clickableToggle.setForeground(Color.BLACK);
            clickableToggle.setBackground(null);
            clickableToggle.setIcon(DefaultButtonIcon);
            ColumnHeadingLabel1.setForeground(Color.BLACK);
            ColumnHeadingLabel2.setForeground(Color.BLACK);
            ColumnHeadingLabel3.setForeground(Color.BLACK);
            TitleLabel.setForeground(Color.BLACK);
            for(int i=0;i<BoardLabel.length;i++){
                BoardButton[i].setForeground(Color.BLACK);
                BoardButton[i].setBackground(null);
                BoardButton[i].setIcon(DefaultButtonIcon);
                BoardLabel[i].setForeground(Color.BLACK);
                LivesLabel[i].setForeground(Color.BLACK);
                TimeLabel[i].setForeground(Color.BLACK);
            }
        }
    }
    //------------------initComponents()-----------------------initComponents()---------------------------initComponents()------------------------
    private void initComponents() {
        clickableToggle.setUI(new MetalToggleButtonUI() {//<-- allows me to change the color of a toggle button that is selected
            @Override
            protected Color getSelectColor() {
                return (isDeleteMode)?Color.RED:((MineSweeper.isDarkMode())?PURPLE:super.getSelectColor());//<-- "super" allows us to refer to the class we extended
            }                                             // that way, if we override a function we can trigger the default functionality if we wish.
        });
        //------------------------------------------Initialize our nested gridbaglayout panels
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); 
        getContentPane().setPreferredSize(new Dimension(defaultwindowsize));
        setIconImage(MineSweeper.MineIcon);
        JPanel containerGridBag = new JPanel(new GridBagLayout()){
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor((MineSweeper.isDarkMode())?PURPLE:LIGHTPRPL);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        JPanel HeadingPanel = new JPanel(new GridBagLayout()){
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor((MineSweeper.isDarkMode())?PURPLE:LIGHTPRPL);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        JPanel ScoresPanel = new JPanel(new GridBagLayout()){
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor((MineSweeper.isDarkMode())?PURPLE:LIGHTPRPL);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        GridBagConstraints containerConstraints = new GridBagConstraints();
        GridBagConstraints HeadingConstraints = new GridBagConstraints();
        GridBagConstraints ScoresConstraints = new GridBagConstraints();
        JScrollPane scrollPane = new JScrollPane(ScoresPanel);//<-- add just the scores panel to scroll pane to scroll without losing back and toggle button
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setBackground(PURPLE);
        scrollPane.getHorizontalScrollBar().setBackground(PURPLE);
        scrollPane.setBackground(PURPLE);
        getContentPane().add(containerGridBag);//<-- add panel to frame

        containerConstraints.fill = GridBagConstraints.BOTH;//add our HeadingPanel and scrollPane into the containerGridBag panel
        containerConstraints.gridx = 0;
        containerConstraints.gridy = 0;
        containerConstraints.gridwidth = 1;
        containerConstraints.gridheight = 1;
        containerConstraints.weighty = 0.0;
        containerGridBag.add(HeadingPanel, containerConstraints);
        containerConstraints.gridy = 1;
        containerConstraints.gridwidth = GridBagConstraints.REMAINDER;
        containerConstraints.gridheight = GridBagConstraints.REMAINDER;
        containerConstraints.weightx = 1.0;
        containerConstraints.weighty = 1.0;
        containerGridBag.add(scrollPane, containerConstraints);

        //-----------------------------------------------Heading Panel----------------------------------------------------------

        TitleLabel = new JLabel();              //initialize HeadingPanel items
        TitleLabel.setFont(new Font("Tahoma", 0, 36));
        TitleLabel.setText("High Scores!");
        TitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        Back = new JButton("Back");
        clickableToggle.addKeyListener(keyAdapter);
        Back.addKeyListener(keyAdapter);
        Back.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                ScoresWindow.this.dispose();
            }
        });
        clickableToggle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clickable = clickableToggle.isSelected();
                getContentPane().setPreferredSize(ScoresWindow.this.getContentPane().getSize());
                if(!FileIssue){
                    BoardPanel.removeAll();//remove so we can add new ones without weirdness
                    if(clickable){//add the correct components to BoardPanel
                        for(int i=0;i<BoardButton.length;i++)BoardPanel.add(BoardButton[i]);
                    }else for(int i=0;i<BoardLabel.length;i++)BoardPanel.add(BoardLabel[i]);
                    ScoresWindow.this.pack();//make it display right
                    ScoresWindow.this.setVisible(true);
                    ScoresWindow.this.getContentPane().revalidate();
                }
            }
        });

        HeadingConstraints.gridx = 0;
        HeadingConstraints.gridy = 0;
        HeadingConstraints.gridwidth = 1;
        HeadingConstraints.gridheight = 2;
        HeadingConstraints.weighty = 1.0;
        HeadingConstraints.fill = GridBagConstraints.BOTH;                //layout HeadingPanel
        HeadingPanel.add(Back, HeadingConstraints);
        HeadingConstraints.weighty = 0.0;
        HeadingConstraints.gridy = 2;
        HeadingConstraints.gridheight = 1;
        HeadingPanel.add(clickableToggle, HeadingConstraints);
        HeadingConstraints.gridy = 0;
        HeadingConstraints.gridx = 1;
        HeadingConstraints.gridheight = 3;
        HeadingConstraints.gridwidth = GridBagConstraints.REMAINDER;
        HeadingConstraints.weightx = 1.0;
        HeadingPanel.add(TitleLabel, HeadingConstraints);

        //-----------------------------------------------Scores Panel-------------------------------------------------

        ColumnHeadingLabel1 = new JLabel("<html><u>Board:</u></html>");         //initialize row 1 of ScoresPanel
        JLabel ColumnHeadingSpacer = new JLabel(" ");
        ColumnHeadingLabel2 = new JLabel("<html><u>Lives Left:</u></html>");    //scores panel lives heading
        JLabel ColumnHeadingSpacer2 = new JLabel(" ");
        ColumnHeadingLabel3 = new JLabel("<html><u>time:</u></html>");          //scores panel time heading
        ColumnHeadingLabel1.setBorder(new EmptyBorder(5, 10, 0, 0));
        ColumnHeadingSpacer.setBorder(new EmptyBorder(5, 10, 0, 10));
        ColumnHeadingLabel2.setBorder(new EmptyBorder(5, 0, 0, 0));
        ColumnHeadingSpacer2.setBorder(new EmptyBorder(5, 5, 0, 5));
        ColumnHeadingLabel3.setBorder(new EmptyBorder(5, 0, 0, 10));
        ColumnHeadingLabel1.setVerticalAlignment(SwingConstants.NORTH);
        ColumnHeadingSpacer.setVerticalAlignment(SwingConstants.NORTH);
        ColumnHeadingLabel2.setVerticalAlignment(SwingConstants.NORTH);
        ColumnHeadingSpacer2.setVerticalAlignment(SwingConstants.NORTH);
        ColumnHeadingLabel3.setVerticalAlignment(SwingConstants.NORTH);

        ScoresConstraints.fill = GridBagConstraints.BOTH;
        ScoresConstraints.gridx = 0;
        ScoresConstraints.gridy = 0;
        ScoresConstraints.gridwidth = 1;
        ScoresConstraints.gridheight = 1;
        ScoresPanel.add(ColumnHeadingLabel1, ScoresConstraints);
        ScoresConstraints.gridx = 1;
        ScoresPanel.add(ColumnHeadingSpacer, ScoresConstraints); //layout row 1 of ScoresPanel
        ScoresConstraints.gridx = 2;
        ScoresPanel.add(ColumnHeadingLabel2, ScoresConstraints);
        ScoresConstraints.gridx = 3;
        ScoresPanel.add(ColumnHeadingSpacer2, ScoresConstraints);
        ScoresConstraints.gridx = 4;
        ScoresPanel.add(ColumnHeadingLabel3, ScoresConstraints);

        BoardPanel = new JPanel(){
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor((MineSweeper.isDarkMode())?PURPLE:LIGHTPRPL);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };                           //set up main scores display properties
        BoardPanel.setLayout(new GridLayout(0, 1)); 
        JLabel BoardSpacer = new JLabel(" ");
        BoardSpacer.setBorder(new EmptyBorder(10, 10, 10, 10));
        LivesPanel = new JPanel(){
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor((MineSweeper.isDarkMode())?PURPLE:LIGHTPRPL);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        LivesPanel.setLayout(new GridLayout(0, 1));//each one of these is going to be a column of the display after leaderboardText adds the entries
        JLabel BoardSpacer2 = new JLabel(" ");
        BoardSpacer2.setBorder(new EmptyBorder(10, 5, 10, 5));
        TimePanel = new JPanel(){
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor((MineSweeper.isDarkMode())?PURPLE:LIGHTPRPL);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        TimePanel.setLayout(new GridLayout(0, 1));
        JLabel BoardSpacer3 = new JLabel(" ");

        leaderboardText();//set text for main scores display


        ScoresConstraints.fill = GridBagConstraints.BOTH;
        ScoresConstraints.gridx = 0;
        ScoresConstraints.gridy = 1;
        ScoresConstraints.gridwidth = 1;
        ScoresConstraints.gridheight = 1;
        ScoresPanel.add(BoardPanel, ScoresConstraints);
        ScoresConstraints.gridx = 1;
        ScoresPanel.add(BoardSpacer, ScoresConstraints);             //layout main scores display
        ScoresConstraints.gridx = 2;
        ScoresPanel.add(LivesPanel, ScoresConstraints);
        ScoresConstraints.gridx = 3;
        ScoresPanel.add(BoardSpacer2, ScoresConstraints);
        ScoresConstraints.gridx = 4;
        ScoresPanel.add(TimePanel, ScoresConstraints);
        ScoresConstraints.gridy = 2;
        ScoresConstraints.gridx = GridBagConstraints.REMAINDER;
        ScoresConstraints.weighty = 1.0;
        ScoresPanel.add(BoardSpacer3, ScoresConstraints);

        pack();
        getContentPane().setVisible(true);
    }
}