package MySweep;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseListener;
import java.util.Stack;
import java.awt.Graphics;
import javax.swing.SwingConstants;
import javax.swing.Icon;
import java.awt.geom.AffineTransform;
import java.awt.Component;
import java.awt.RenderingHints;
import java.awt.Graphics2D;
import javax.swing.ImageIcon;

//This class controls the behavior of the game board. Contains cell display and action logic, and uses Minefield to keep track of the game state
public class Grid extends JPanel {
    //--------------Initialize-Colors--------Many are the same color, but now theyre easily changed?
    private static final Icon DefaultButtonIcon = (new JButton()).getIcon();//<-- used for non darkmode
    private static final Color LightModeTextColor = new Color(0);//<-- LightMode button foreground (the color of the text)
    private static final Color DarkModeTextColor = new Color(255, 255, 255);//<-- DarkMode button foreground (the color of the text)
    private static final Color BLACK = new Color(0);//<-- default button background color in dark mode
    private static final Color GRASS = new Color(31, 133, 28);//<-- grass
    private static final Color MAGENTA = new Color(255,0,255);//<-- unmarked status color on win
    private static final Color GREEN = new Color(0,255,0);//<-- marked status color on loss
    private static final Color RED = new Color(255,0,0);//<-- in game exploded bomb background
    private static final Color ChGO_RED = new Color(255,0,0);//<-- game over indicator on chord number foreground
    private static final Color CYAN = new Color(0,255,200);//<-- exploded bomb foreground
    private static final Color QSTNMARKCOLOR = new Color(133, 95, 227);//<-- question mark color
    private static final Color MARKCOLOR = new Color(255,0,0);//<-- color of marks
    private static final Color BORDERYELLOW = new Color(255, 255, 0);//<-- border colors 2 bombs
    private static final Color BORDERORANGE = new Color(255, 160, 0);//<-- 3
    private static final Color BORDERORANGERED = new Color(255,95,0);//<-- 4
    private static final Color BORDERRED = new Color(255,0,0);//<-- higher
    private static final Color defaultBorderColor = new Color(126, 126, 126);//<-- default border color
    private static final Insets CellInset = new Insets(-20, -20, -20, -20);//<-- leave this alone unless you want dots instead of numbers. It sets text margins
    private static final ScalableIcon GOEXPLODEICON = new ScalableIcon(MineSweeper.ExplosionIcon);
    private static final ScalableIcon GOREVEALEDICON = new ScalableIcon(MineSweeper.MineIcon);
    //-------------logic initializing-----------------------------logic initializing--------------logic initializing---------------------------------logic initializing-----
    private final int Fieldx, Fieldy, bombCount, lives;//<-- the size of board and how many bombs and lives

    private Minefield answers;//<-- this one is the data class for game logic. Once you read up through the constructor of this Grid class, read that file.

    //MainGameWindow gets these from functions defined here later
    private int GameOverMessageIndex = -1;//<-- -1 is the "off" state of this variable (later set in game over function)
    private int wonValue = -1;//<-- -1 is the "off" state. also set in game over function
    private int BombsFound = 0;//<-- just the number of how many marks you have placed. 
    private int livesLeft = 0;//<-- just the number of how many lives you have left.
    private boolean cancelQuestionMarks = true;//<-- boolean for toggling ? marks on bombs

    //--------------------------CellButton();-----------------------------------------CellButton();-------------------------------------------------
    private class CellButton extends JButton {//<-- these are our game board buttons. This class was created to allow the 
        private int borderWeight = 1;         //^setting of color and thickness of cell borders, now it knows where it is.
        private Color borderColor = defaultBorderColor;//<--default color not defined inside cellbutton class because that would mean creating a new color for each button, which is expensive.
        private int x, y;//<-- it knows where it is
        private boolean dynamicWidth=false;//<-- and if its border width should adjust based on size
        public CellButton() {//<------------- Constructor
            this.setHorizontalAlignment(SwingConstants.CENTER);
            this.setVerticalAlignment(SwingConstants.CENTER);//Initialize our cell button properties other than font size (which is done after packing layout)
            this.setFocusable(false);//<-- made unfocusable because i didnt like hitting tab for 3 years
            this.setMargin(CellInset);//<-- Inset defined outside of class so it is not multiplied times whatever (same as default border color)
        }
        //functions!
        public void setBorderColor(Color borderColor, int borderWeight) {
            this.borderColor = borderColor;//<-- "this.something" allows us to refer to 'something' belonging to 'this' instance of the class
            this.borderWeight = borderWeight;
        }
        public void setDynamicBorderWidth(boolean value){
            dynamicWidth = value;
        }
        public void setXY(int x, int y){
            this.x = x;//<-- this used to be added via .putClientProperty("xValue", x)
            this.y = y;
        }
        public int getXcoord(){return x;}//<-- these functions get the x and y
        public int getYcoord(){return y;}
        @Override
        protected void paintBorder(Graphics g) {//<-- override paintBorder so that I can change border color and thickness
            if(borderColor!=null){
                g.setColor(borderColor);//dont worry too much about what a Graphics class is. It makes graphics. And has functions to do it.
                int top;                //it gets passed in, we use it to draw stuff.
                if(!dynamicWidth){top = borderWeight;
                }else top = borderWeight*(1+(this.getSize().height/23));
                for(int i=0;i<top;i++){
                    g.drawRect(i, i, getWidth() - i*2 - 1, getHeight() - i*2 -1);//<-- like draw rectangles of 1 pixel width
                }//in a loop because 1 pixel width, so i need many.
            }
        }
    }
    private static class ScalableIcon implements Icon {//<-- implement the Icon interface, which contains 3 functions for us to implement.
        private ImageIcon originalIcon;//<-- getting original icon for sizing purposes
        public ScalableIcon(Image originalImage) {//<-- Constructor 
            this.originalIcon = new ImageIcon(originalImage);//<-- create an ImageIcon out of the icon. It is not easy to get the height of the Image class.
        }
        public int getIconWidth() {return 0;}//<-- it only matters that these are smaller than the buttons. If it isnt, the button will get bigger.
        public int getIconHeight() {return 0;}//<-- We are implementing an interface. Other java classes use these functions. One of those, is JButton.
        public void paintIcon(Component c, Graphics g, int x, int y) {//<-- This will then make the icon the size of the button after.
            int width = c.getWidth();
            int height = c.getHeight();
            Graphics2D g2d = (Graphics2D) g.create();//<-- i hate these because i have to look for stuff like
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);//<-- RenderingHints
            AffineTransform transform = new AffineTransform();//<-- or the stretch but with matricies that I couldnt remember the name of
            transform.scale((double) width / originalIcon.getIconWidth(), (double) height / originalIcon.getIconHeight());
            g2d.drawImage(originalIcon.getImage(), transform, null);
            g2d.dispose();
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------------
    //---------------------GRID CONSTRUCTOR----------------------GRID CONSTRUCTOR----------------------------GRID CONSTRUCTOR------------------------------
    public Grid(int w, int h, int bombNum, int lives) {//INIT
        Fieldx = w;
        Fieldy = h;                                      //THE
        this.lives = lives;//<-- "this." allows us to say, the one belonging to this instance of the class
        bombCount = bombNum;                               //GRID
        BombsFound = 0;
        livesLeft = lives;
        answers = new Minefield(Fieldx, Fieldy, bombCount);//<-- constructor understood? go to MineField.java
        Grid.this.setLayout(new GridLayout(Fieldy, Fieldx));//<-- this one uses a GridLayout(rows, columns). constructor input is not x by y. it is rows by columns
        Grid.this.setOpaque(false);
        for(int i = 0; i < Fieldx; i++){//<-- this is how you step through x and y at once. you can also use 2 while loops but it is less convenient.
            for(int j = 0; j < Fieldy; j++){//<-- the second loop
                Grid.this.add(new CellButton());//<-- add the buttons to the grid, which has GridLayout
            }
        }
        for(int i = 0; i < Fieldx; i++){//<-- first for loop
            for(int j = 0; j < Fieldy; j++){//<-- second inner for loop
                getButtonAt(i,j).setXY(i,j);//<-- add our coordinates to our buttons.
            }//at the end 0,0 will be in the top left. like the first word on a page. doesnt matter to us though. we just need to know that its a grid and the x and y
        }
        if(MineSweeper.isDarkMode())for(int i = 0; i < Fieldx; i++)for(int j = 0; j < Fieldy; j++)getButtonAt(i,j).setBackground(BLACK);//<-- i do this more than i should... You are able to leave brackets off of things like if statements and loops if its just 1 function call.
    }//--------------------getButtonAt(int x, int y) Became necessary after getting rid of 2d array to reference cells by location-------------------------
    private CellButton getButtonAt(int x, int y) {//<-- all you need to know about this right now is that it gets button at x, y
        return (CellButton) Grid.this.getComponent(y * Fieldx + x); //<-- this works because of integer division. Explained in fillZeroes.
    }                                                           //read on. when you get there and read it, this will make sense.
    //-----------------------------------function for adding mouse Listener to cells in Grid-------------------------------------------------
    void addCellListener(MouseListener mouseListener){//----------addCellListener()--------------------------------------------------
        for (int i = 0; i < Fieldx; i++) {//<-- step through x and y
            for (int j = 0; j < Fieldy; j++) {
                getButtonAt(i,j).addMouseListener(mouseListener);//<-- add the listener to each.
            }//The short time where your click wont immediately update the visuals yet after board becomes visible on large board sizes
        }//                               ^ isnt due to this function its just because its java (tm) and still packing.
    }//----------------------------------------------------------------------------------------------------------------------------------------

    //--------------------------------------------Misc Public Display Functions--------------------------------------------------------------------
    //---------Misc Public Display Functions-----------------------------------------Misc Public Display Functions---------------------------
    @Override
    protected void paintComponent(Graphics g) {//<--Override the paint function to make the background GRASS without making the components also GRASS??
            g.setColor(GRASS);
            g.fillRect(0, 0, getWidth(), getHeight());
    }
    long getTime(){
        return (answers.isFirstClick())?(-1):answers.getTime();//<-- i passed this through here from minefield so mainGameWindow gets the correct instance of minefield's time
    }
    int getBombsFound(){return BombsFound;}//<-- get
    int getLivesLeft(){return livesLeft;}//<-- that
    int[] getGameOverIndex(){//     <--    text (for displays in MainGameWindow)
        int[] messageIndexes = new int[2];
        messageIndexes[0]=GameOverMessageIndex;
        messageIndexes[1]=wonValue;
        return messageIndexes;
    }
    void toggleQuestionMarks(){//<-- toggles the ability to mark cells with a ? on and off (I find it annoying so it starts turned off)
        this.cancelQuestionMarks = !cancelQuestionMarks;
        if(cancelQuestionMarks == true){
            for (int x = 0; x < Fieldx; x++) {
                for (int y = 0; y < Fieldy; y++) {
                    if(answers.isQuestionable(x, y)){//if there were question marks, clear them
                        answers.clearSuspicion(x,y);//<-- tell answers that it isn't sus
                        getButtonAt(x,y).setForeground((MineSweeper.isDarkMode())?DarkModeTextColor:LightModeTextColor);
                        getButtonAt(x,y).setText("");
                    }
                }
            }
        }
    }
    void ResetBoard(){//<-- for when you want new but not like, new new. Just like, sorta new. Refreshed.
        BombsFound = 0;
        livesLeft = lives;//reset stuff for main game window displays
        GameOverMessageIndex = -1;
        wonValue = -1;
        answers = new Minefield(Fieldx, Fieldy, bombCount);//<-- get a new minefield, thus resetting the timer and everything else
        for (int i = 0; i < Fieldx; i++) {
            for (int j = 0; j < Fieldy; j++) {// and then reset appearances of cellbuttons
                getButtonAt(i,j).setForeground((MineSweeper.isDarkMode())?DarkModeTextColor:LightModeTextColor);
                getButtonAt(i,j).setBorderColor(defaultBorderColor, 1);
                getButtonAt(i, j).setDynamicBorderWidth(false);
                getButtonAt(i,j).setText("");
                if(MineSweeper.isDarkMode()){
                    getButtonAt(i,j).setBackground(BLACK);
                    getButtonAt(i,j).setIcon(null);
                }else {
                    getButtonAt(i,j).setBackground(null);
                    getButtonAt(i,j).setIcon(DefaultButtonIcon);
                }
            }
        }
        Grid.this.repaint();//<-- repaint so that it actually repaints at a time that makes sense rather than just like... later.
    }
    void setCellFontSize(){//<-- call this from main window after pack or resize to get correct component size before font size.
        int cellHeight = Grid.this.getComponent(0).getHeight();
        Font newFont = new Font("Tahoma", 0, ((cellHeight>18)?18:cellHeight - 1));
        for(int x = 0; x < Fieldx; x++)for(int y = 0; y < Fieldy; y++){
            getButtonAt(x,y).setFont(newFont);
        }
    }
    //this is called from the zoom listener in main game window
    int[] doZoom(int rotation, int mouseX1, int mouseY1){//<-- it makes the cells bigger. The main window is in a scroll pane. does not set font
        Dimension currentCellSize = Grid.this.getComponent(0).getSize();//get the first buttons size (theyre all the same)
        Dimension newCellSize = new Dimension(currentCellSize.width + rotation, currentCellSize.height + rotation);
        for (int i = 0; i < Fieldx; i++) {//apply the new sizes
            for (int j = 0; j < Fieldy; j++) {
                getButtonAt(i,j).setPreferredSize(newCellSize);
            }
        }//Returns old and new grid sizes. I need it to recenter window on mouse. MUCH faster than using pack(); to update size before recenter
        int[] gridSizesOldNew= new int[4];
        gridSizesOldNew[0] = currentCellSize.width*Fieldx;
        gridSizesOldNew[1] = currentCellSize.height*Fieldy;
        gridSizesOldNew[2] = (newCellSize.width*Fieldx);
        gridSizesOldNew[3] = (newCellSize.height*Fieldy);
        return gridSizesOldNew;
    }
    void resetZoom(Dimension windowSize){
        if(windowSize.width/Fieldx<Grid.this.getComponent(0).getWidth() && windowSize.height/Fieldy<Grid.this.getComponent(0).getHeight()){
            Dimension newCellSize= new Dimension(windowSize.width/Fieldx, windowSize.height/Fieldy);
            Font newFont = new Font("Tahoma", 0, ((newCellSize.height>18)?18:newCellSize.height - 1));
            for (int x = 0; x < Fieldx; x++){
                for (int y = 0; y < Fieldy; y++){
                    getButtonAt(x,y).setPreferredSize(newCellSize);
                    getButtonAt(x,y).setFont(newFont);
                }
            }
        }
    }
    void toggleDarkMode(){
        for (int x = 0; x < Fieldx; x++) {
            for (int y = 0; y < Fieldy; y++) {
                if(!(answers.exploded(x, y)||(answers.checked(x, y)&&answers.adjCount(x, y)==0))){
                    if(MineSweeper.isDarkMode()){getButtonAt(x,y).setBackground(BLACK);
                    }else{
                        getButtonAt(x,y).setBackground(null);
                        getButtonAt(x,y).setIcon(DefaultButtonIcon);
                    }
                    if(((MineSweeper.isDarkMode())?(getButtonAt(x,y).getForeground() == LightModeTextColor):(getButtonAt(x,y).getForeground() == DarkModeTextColor))){
                        getButtonAt(x,y).setForeground((MineSweeper.isDarkMode())?DarkModeTextColor:LightModeTextColor);
                    }
                    if(answers.isBomb(x, y)&&answers.isGameOver()){
                        if(wonValue == 0)getButtonAt(x,y).setIcon(GOEXPLODEICON);
                        if(wonValue == 1)getButtonAt(x,y).setIcon(GOREVEALEDICON);
                    }
                }
            }
        }
        Grid.this.repaint();
    }
//----GAME-LOGIC-BELOW--------GAME-LOGIC-BELOW--------GAME-LOGIC-BELOW--------GAME-LOGIC-BELOW--------GAME-LOGIC-BELOW-------GAME-LOGIC-BELOW----------
//--------Click Handler--------------Click Handler--------------Click Handler--------------Click Handler--------------Click Handler------
    public void doClickType(JButton clickedButton, int clickType) {//<-- runs the correct click type. Called from listener
        CellButton clickedCell = (CellButton)clickedButton;//<-- since listener was only added to cellbuttons, we know we can cast it as a cellbutton
        if(clickType==0){//       and cellbuttons know where they are! CellButton is a private class though. This means MainGameWindow just thinks it's a JButton
            regularClick(clickedCell);
        }else if(clickType==1){//<-- for each click type
            markCell(clickedCell);//<-- pass the CellButton to the correct click function.
        }else if(clickType==2){
            playChord(clickedCell);
        }
    }
//-----------END OF PUBLIC FUNCTIONS------------------------THESE ARE CALLED BY CLICK HANDLER---------------END OF PUBLIC FUNCTIONS-----------------------
//-----------MAIN CELL CLICKED FUNCTIONS-----------------MAIN CELL CLICKED FUNCTIONS----------MAIN CELL CLICKED FUNCTIONS--------------------------------
    private void regularClick(CellButton current){//---------regularClick()--------regularClick()--------regularClick()--------regularClick()----
        int xValue = current.getXcoord();//<-- get the cellbutton's stored location
        int yValue = current.getYcoord();
        if(!answers.isGameOver()){//<-- if game not over already
            if (answers.isFirstClick()) {//<-- if it was the first click
                answers.initBoardForFirstClickAt(xValue,yValue);//if possible, answers.adjCount(clickedX,clickedY) == 0 on the first click. also initializes field data.
                answers.doFirstClick();//<-- starts the timer
            }
            //was it a clickable cell?
            if (!answers.exploded(xValue,yValue) && !answers.checked(xValue,yValue) && !answers.marked(xValue,yValue) && !answers.isQuestionable(xValue,yValue)) {
                if (answers.isBomb(xValue, yValue)) {//<-- was it a bomb?
                    answers.explode(xValue,yValue);//<-- it was a bomb... explode it.
                    if(answers.cellsExploded()>=lives){//RIP
                        current.setText("!");
                        current.setBackground(RED);
                        current.setForeground(CYAN);
                        answers.setGameOver();//<-- Stop the clocks!!!
                        GameOver(false);//<-- starts game over display process
                    }else if(answers.cellsExploded()<lives){//You lost one of your privates.
                        current.setText(Integer.toString(answers.cellsExploded()));
                        current.setBackground(RED);
                        current.setForeground(CYAN);
                        BombsFound = answers.cellsMarked()+answers.cellsExploded();//<-- I mean, you found the bomb so....
                    }
                    livesLeft = lives-answers.cellsExploded();//<-- set the variable used for setting lives display because you lost one
                } else if (answers.adjCount(xValue, yValue) != 0) {//*whew*.... close one. Clicked a number.
                    current.setText(String.valueOf(answers.adjCount(xValue, yValue)));
                    current.setForeground((MineSweeper.isDarkMode())?DarkModeTextColor:LightModeTextColor);
                    answers.check(xValue, yValue);
                    setBorderBasedOnAdj(xValue, yValue);
                } else {                           //else you clicked a 0
                    boolean[][] prechekd=new boolean[Fieldx][Fieldy];//saving current checks so we know which ones fillzeroes flipped
                    for(int j=0;j<Fieldx;j++){
                        for(int k=0;k<Fieldy;k++){//we save this so that mark lonely bombs can know what to mark
                            prechekd[j][k]=(answers.checked(j, k));
                        }
                    }
                    fillZeroes(xValue, yValue);//<-- adjCount==0. Fill.
                    markLonelyBombs(prechekd);//<-- mark the bombs with all neighbors 1 that were completely revealed by fillzeroes
                    BombsFound = answers.cellsMarked()+answers.cellsExploded();//<-- set variable used for setting Bombs found display.
                }
            }
            //check if you won (notice we use cells checked because cells marked is just how many you right click on)
            if ((answers.cellsChecked() == Fieldx * Fieldy - bombCount)&&(answers.cellsChecked()>0)&&!answers.isGameOver()) {//Are you done yet?
                answers.setGameOver();//<-- Stop the clocks!!!
                GameOver(true);//<-- starts game over process
            }
        }
    }
    //---------------------------------------------------------------markCell()--------------------------markCell()-------------
    private void markCell(CellButton current){//----------------------markCell()--------------------------markCell()-------------
        int xValue = current.getXcoord();
        int yValue = current.getYcoord();
        if (!answers.isGameOver() && !answers.isFirstClick()) {//<-- if game is in progress
            if (!answers.exploded(xValue,yValue) && !answers.checked(xValue,yValue) && !answers.isFirstClick()){//<-- if its a markable cell
                if(!answers.marked(xValue,yValue) && !answers.isQuestionable(xValue, yValue)) {//<-- if not already marked or questionable
                    answers.mark(xValue,yValue);
                    current.setForeground(MARKCOLOR);
                    current.setText("X");
                } else if (answers.marked(xValue,yValue) && !cancelQuestionMarks) {//<-- if marked and not questionable
                    answers.unmark(xValue,yValue);
                    answers.question(xValue,yValue);
                    current.setForeground(QSTNMARKCOLOR);
                    current.setText("?");
                }else if (answers.isQuestionable(xValue,yValue) || ((answers.marked(xValue, yValue) && cancelQuestionMarks))){//<-if questionable, or if no quesion marks allowed and marked
                    answers.unmark(xValue,yValue);//<-- it already checks if it was unmarked in minefield for count so just stick it here as well to make it work regardless of ? settings
                    answers.clearSuspicion(xValue,yValue);
                    current.setForeground((MineSweeper.isDarkMode())?DarkModeTextColor:LightModeTextColor);
                    current.setText("");
                }
                BombsFound = answers.cellsMarked()+answers.cellsExploded();
            }
        }
    }//--------------------------playChord()----------playChord()--------------playChord()--------------playChord()--------------playChord()-------
    private void playChord(CellButton current){//Chord reveals all cells around a number, but you need to mark correctly or it hurts.
        int a = current.getXcoord();//     ^ did you know about this? I didnt either... I checked the minesweeper rules again...
        int b = current.getYcoord();// basically this will do the same thing as regular click but to all the neighbors of an already revealed number
        int adjMarked = 0;//           with the exception of being able to click through question marks
        if(!answers.isFirstClick() && !answers.isGameOver()){//<-- if during game (no chords on first click)
            if(answers.checked(a, b) && answers.adjCount(a, b)!=0){//<-- if it was a revealed number
                for(int i=a-1;i<=a+1;i++){//go through neighbors
                    for(int j=b-1;j<=b+1;j++){
                        if(i<0||j<0||i>=Fieldx||j>=Fieldy||(i==a && j==b)) continue;//<-- if not a valid cell, skip to next iteration
                        if(answers.marked(i, j))adjMarked++;//<-- this counts the number of marked cells adjacent for penalty if too many are marked
                        if(!answers.exploded(i, j)&&!answers.marked(i, j)){//<-- if not exploded or marked (does not include !isQuesionable(x,y))
                            if(answers.isBomb(i, j)){//<-- then check this before checking !isQuestionable(x,y)
                                answers.explode(i, j);
                                getButtonAt(i,j).setBackground(RED);
                                getButtonAt(i,j).setForeground(CYAN);
                                getButtonAt(i,j).setText(Integer.toString(answers.cellsExploded()));
                            }else if(!answers.isQuestionable(i, j)){//<-- after this else is our logic for numbered cells and 0s. Notice we checked if bomb first.
                                if (answers.adjCount(i, j) != 0) {//<-- adjCount>0?
                                    getButtonAt(i,j).setText(String.valueOf(answers.adjCount(i, j)));
                                    getButtonAt(i,j).setForeground((MineSweeper.isDarkMode())?DarkModeTextColor:LightModeTextColor);
                                    answers.check(i, j);
                                    setBorderBasedOnAdj(i, j);
                                } else {                           //<-- you hit a 0
                                    boolean[][] prechekd=new boolean[Fieldx][Fieldy];//saving current checks so we know which ones fillzeroes flipped
                                    for(int x=0;x<Fieldx;x++){
                                        for(int y=0;y<Fieldy;y++){
                                            prechekd[x][y]=(answers.checked(x, y));
                                        }
                                    }
                                    fillZeroes(i, j);//<-- adjCount=0. Fill.
                                    markLonelyBombs(prechekd);//<-- mark the bombs with all neighbors 1 that were completely revealed by fillzeroes
                                    BombsFound = answers.cellsMarked()+answers.cellsExploded();
                                }
                            }
                        }
                    }
                }
            }
            if(adjMarked>answers.adjCount(a, b)){//if you marked too many, there is a penalty
                int penalty = adjMarked-answers.adjCount(a, b);//<-- calculate the penalty
                for(int i=a-1;i<=a+1;i++){//go through neighbors
                    for(int j=b-1;j<=b+1;j++){
                        if(i<0||j<0||i>=Fieldx||j>=Fieldy||(i==a && j==b)) continue;
                        if(penalty>0&&answers.marked(i, j)&&answers.isBomb(i, j)&&!answers.exploded(i, j)){//if you marked a bomb correctly and still have penalties to give
                                answers.unmark(i, j);//unmark it
                                answers.explode(i, j);//then explode it
                                getButtonAt(i,j).setBackground(RED);
                                getButtonAt(i,j).setForeground(CYAN);
                                getButtonAt(i,j).setText(Integer.toString(answers.cellsExploded()));
                                penalty--;//decrement penalty by 1
                        }
                    }
                }
            }
            //check if you died.
            if(answers.cellsExploded()>=lives){//RIP
                current.setText("!");
                current.setForeground(ChGO_RED);
                answers.setGameOver();
                GameOver(false);//<-- starts game over process
            }else if(answers.cellsExploded()<lives){//You lost some of your privates.
                BombsFound = answers.cellsMarked()+answers.cellsExploded();
            }
            livesLeft = lives-answers.cellsExploded();//<-set the text for lives for main game window
            //check if you won
            if ((answers.cellsChecked() == Fieldx * Fieldy - bombCount) && answers.cellsChecked()>0&&!answers.isGameOver()) {
                answers.setGameOver();
                GameOver(true);//<-- starts game over process
            }
        }
    }//--------------end of main cell clicked functions----Helper functions they referenced below---------------------------------------------------------
    //------------------------------------------fillZeroes()-------------------------------------------------
    private void fillZeroes(int xValue, int yValue) {//-----------fillZeroes()------------------------------------------------------------------
        Stack<Integer> stack = new Stack<>();//<-- a stack must be used instead of recursion because large board sizes cause stack overflow.
        stack.push(yValue * Fieldx + xValue);
        while (!stack.isEmpty()) {
            int position = stack.pop();
            int y = position / Fieldx;//<-- y*Fieldx/Fieldx
            int x = position % Fieldx;//<-- remainder==x
            if (!answers.checked(x, y)) {//<-- since this will unfortunately branch and call multiple times per 0 cell because it checks all neighbors each time
                answers.check(x, y);//<--check this button                    ^^^make sure we dont re-call our loops on already checked squares
                ((CellButton)Grid.this.getComponent(position)).setText(String.valueOf(answers.adjCount(x, y)));//<--set these things direct from position 
                ((CellButton)Grid.this.getComponent(position)).setBackground(GRASS);       //because otherwise you do the multiplication and division twice unnecessarily
                ((CellButton)Grid.this.getComponent(position)).setBorderColor(null, 1);
                ((CellButton)Grid.this.getComponent(position)).setForeground(GRASS);//<-- this is how getButtonAt gets the location.
                for (int i = x - 1; i <= x + 1; i++) {//check neighbors
                    for (int j = y - 1; j <= y + 1; j++) {
                        if (i < 0 || j < 0 || i >= Fieldx || j >= Fieldy) {//exclude invalid cells
                            continue;//continue to next iteration if invalid cell
                        } else if (!answers.checked(i, j)) {
                            if (answers.adjCount(i, j) != 0) {//not 0, dont fill from it
                                answers.check(i, j);//check it and fill it instead.
                                setBorderBasedOnAdj(i, j);
                                getButtonAt(i,j).setText(String.valueOf(answers.adjCount(i, j)));
                                getButtonAt(i,j).setForeground((MineSweeper.isDarkMode())?DarkModeTextColor:LightModeTextColor);
                            } else {
                                stack.push(j * Fieldx + i);//<-- a neighbor is 0! Stack it up! In a recursive solution, we would call fillzeroes again, rather than using a loop.
                            }//but that would keep the contents of this call of the function in memory until it ran all the way through... 
                        }//   this way, we just put a number on the stack and take it off when we read it.
                    }
                }
            }
        }
        Grid.this.repaint();//repaint so that it actually repaints at a time that makes sense rather than just like... later.
    }
    //-----------------------------------------------------markLonelyBombs()----------------------------------------------------------------
    private void markLonelyBombs(boolean[][] prechekd){//I didnt want to click on all the lone bombs on big boards in order to
        for (int j = 0; j < Fieldx; j++) {             //make the count of marked bombs actually useful
            for (int k = 0; k < Fieldy; k++) {//<-- go through the board
                if(answers.isBomb(j, k)){//<-- check for "is bomb"
                    boolean dqed = true;//<--initialize stuff we will use to count stuff
                    int totalneighbors = 0;
                    int meetsConditions = 0;
                    for(int l=j-1;l<=j+1;l++){//check neighbors
                        for(int m=k-1;m<=k+1;m++){
                            if(!(l<0||m<0||l>=Fieldx||m>=Fieldy||(l==j && m==k))){//<-- it was valid cell
                                totalneighbors++;      //<-- get number of neighbors
                                if(answers.adjCount(l, m)==1 && answers.checked(l, m) && !prechekd[l][m])meetsConditions++; 
                            }//                   ^^^ condition would mean fillzeroes just revealed it and adj count was 1 so add 1
                        }
                    }
                    if(totalneighbors==meetsConditions)dqed=false;//fillzeroes revealed all neighbors and all were 1. It was a lonely bomb.
                    if(!dqed){//<-- if it was a lonely bomb
                        answers.mark(j,k); //If so, mark the lonely bomb.
                        getButtonAt(j,k).setForeground(MARKCOLOR);
                        getButtonAt(j,k).setText("X");
                    }
                }
            }
        };
    }//---------------------------------------setBorderBasedOnAdj()------------------------------------------------------------------------------
    private void setBorderBasedOnAdj(int x, int y){//it does setBorderBasedOnAdj
        if( answers.adjCount(x, y)<=1 ){ 
            getButtonAt(x,y).setBorderColor(defaultBorderColor, 1); 
        }else if(answers.adjCount(x, y)<=2 ){
            getButtonAt(x, y).setDynamicBorderWidth(true);
            getButtonAt(x,y).setBorderColor(BORDERYELLOW, 2); 
        }else if(answers.adjCount(x, y)<=3 ){
            getButtonAt(x, y).setDynamicBorderWidth(true);
            getButtonAt(x,y).setBorderColor(BORDERORANGE, 2); 
        }else if(answers.adjCount(x, y)<=5 ){
            getButtonAt(x, y).setDynamicBorderWidth(true);
            getButtonAt(x,y).setBorderColor(BORDERORANGERED, 2); 
        }else if(answers.adjCount(x, y)<=8 ){
            getButtonAt(x, y).setDynamicBorderWidth(true);
            getButtonAt(x,y).setBorderColor(BORDERRED, 2); 
        }
    }
    //---------------------------------------GameOver()-----------------------------------------------------------------------------------------
    private void GameOver(boolean won) {//reveals bombs on board with icon and border and stuff then passes the work to ScoresFileIO
        for (int i = 0; i < Fieldx; i++) {//            reveal bombs on board
            for (int j = 0; j < Fieldy; j++) {
                if (answers.isBomb(i, j) && !answers.exploded(i, j)) {//<-- if it should be revealed
                    if (won == false){//<-- should they explode?
                        if(answers.marked(i,j)){
                            getButtonAt(i,j).setDynamicBorderWidth(false);
                            getButtonAt(i,j).setBorderColor(GREEN, 2);
                        }else{getButtonAt(i,j).setText("");}
                        getButtonAt(i,j).setIcon(GOEXPLODEICON);
                        getButtonAt(i,j).revalidate();
                    }else{//<-- or did you find them?
                        if(!answers.marked(i,j)){
                            getButtonAt(i,j).setDynamicBorderWidth(false);
                            getButtonAt(i,j).setBorderColor(MAGENTA, 2);
                        }else{getButtonAt(i,j).setText("");}
                        getButtonAt(i,j).setIcon(GOREVEALEDICON);
                        getButtonAt(i,j).revalidate();
                    }
                    getButtonAt(i,j).setText("");//<-- any remove text from the button
                }
            }
        }
        int MessageIndex = 0; //update leaderboard then update win or loss message based on highscore status
        MessageIndex = ScoresFileIO.updateScoreEntry(won, answers.getTime(), answers.cellsExploded(), Fieldx, Fieldy, bombCount, lives);
        GameOverMessageIndex = MessageIndex;//these are used by main game window to set the message that says "Exploded..."" or whatever at the end
        wonValue=(won)?1:0;
    }
}

