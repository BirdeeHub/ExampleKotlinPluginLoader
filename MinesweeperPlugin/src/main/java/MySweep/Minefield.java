package MySweep;
import java.util.TimerTask;
import java.util.Timer;

class Minefield{//a data class for Grid. contains and manages all mutable game-state variables, it is more disposable than grid to allow easy reset of timer and board
    private boolean[][] cell, chkd, mrk, exp, qstn;    //<-- initialization of variables. these will contain 5 different 2D arrays. (or, arrays of arrays)
    private int[][] adj;//<-- this is also a 2D array but of numbers rather than true or false values
    private final int Fieldx, Fieldy, bombCount;//<-- size of field and number of bombs
    private int totalExploded = 0;
    private int totalChecked = 0;
    private int totalMarked = 0;
    private boolean firstClick = true;
    private boolean GameOver = false;
    private long time;  // Create Timer, which is started in doFirstClick();
    private long startTime;
    private final Timer currentTimeTimer = new Timer();
    private TimerTask timeTask = new TimerTask() {
        public void run() {
            time = System.currentTimeMillis()-startTime;//<-- dont add a time format here. This gets saved to scores file
        }//                                                                           It may not compare for highscore correctly if you that
    };
    //----------Constructor---------------------------------
    Minefield(int w, int h, int bombCount){
        Fieldx=w; 
        Fieldy=h; 
        this.bombCount=bombCount;
        cell = new boolean[Fieldx][Fieldy];//<-- create the array instances but everything is null right now
        adj = new int[Fieldx][Fieldy];//<-- the values in the arrays still have not been added.
        chkd  = new boolean[Fieldx][Fieldy];//<-- but the array instances are ready to recieve them now.
        mrk  = new boolean[Fieldx][Fieldy];
        exp  = new boolean[Fieldx][Fieldy];
        qstn = new boolean[Fieldx][Fieldy];
    }
    //----------initBoardForFirstClickAt-----------MANDATORY---------------initBoardForFirstClickAt------------------initBoardForFirstClickAt-------------
    void initBoardForFirstClickAt(int a, int b){//THIS MUST BE CALLED BEFORE REFERENCING MINEFIELD CLASS IF YOU WANT IT TO WORK
        //if you dont call this, everything will return as null.
        //This cannot place a bomb that would cause AdjCount(a,b) to become >0 unless that is impossible
        //the big multi-line if statements exist to make sure that it does not loop forever if there are not enough available squares to place bombs.
        totalMarked = 0;//initialize variables
        totalChecked = 0;
        totalExploded = 0;
        GameOver = false;
        for(int j=0;j<Fieldx;j++){//<-- with 2 nested for loops you can step through each entry of a 2D array
            for(int k=0;k<Fieldy;k++){
                chkd[j][k]=false;//<-- initialize each of these to false
                mrk[j][k]=false;//<-- otherwise they would be null and I might get an error somewhere.
                exp[j][k]=false;
                cell[j][k]=false;
                qstn[j][k]=false;
            }
        }
        if(Fieldx*Fieldy<=bombCount){//bombs>=cells. set all as bombs then proceed with the inevitable.
            for(int j=0;j<Fieldx;j++)for(int k=0;k<Fieldy;k++){//<-- this is 2 nested for loops as well... sorry... but better to know it well.. i do it too much
                cell[j][k]=true;
            }
        } else {//<-- this else means if not dead on arrival
            int i=0;//If you had googled operators you would know that && and || are logical operators for comparisons that return booleans and that whitespace doesnt matter
            if((8<(Fieldx*Fieldy-bombCount))||//IF field has 9 non-bomb cells, OR
            ((5<(Fieldx*Fieldy-bombCount))&&(a==0||a==(Fieldx-1)||b==0||b==(Fieldy-1)))||//clicked on an edge and 6 non-bomb cells, OR
            ((3<(Fieldx*Fieldy-bombCount))&&((a==0 && b==(Fieldy-1))||(b==0 && a==(Fieldx-1))||(a==0 && b==0)||(b==(Fieldy-1) && a==(Fieldx-1))))){
                while(i<bombCount){                                                    //^^ clicked a corner and 4 non-bomb cells
                    int Randx=(int)Math.round(Math.random()*(Fieldx-1));//place a bomb in a random cell unless occupied or is too close 
                    int Randy=(int)Math.round(Math.random()*(Fieldy-1));//repeatedly until no bombs are left to distribute
                    if((cell[Randx][Randy]==false)&&(((Randx <(a-1))||(Randx >(a+1)))||((Randy <(b-1))||(Randy >(b+1))))){//<-- if not too close to us
                        cell[Randx][Randy]=true;//<-- place the bomb in the cell
                        i++;//<-- increment i. when i >= bombCount we placed all our bombs.
                    }
                }
            }else {//not enough room for adjc==0. square just can't be a bomb.
                while(i<bombCount){//<-- while we still have bombs to place
                    int Randx=(int)Math.round(Math.random()*(Fieldx-1));//place a bomb in a random cell unless occupied or is our cell 
                    int Randy=(int)Math.round(Math.random()*(Fieldy-1));//repeatedly until no bombs are left to distribute
                    if((cell[Randx][Randy]==false)&&(Randx!=a && Randy!=b)){//<-- is not our cell or occupied already?
                        cell[Randx][Randy]=true;
                        i++;
                    }
                }
            }
        }
        for(int j=0;j<Fieldx;j++)for(int k=0;k<Fieldy;k++)adj[j][k]=adjc(j,k);//<-- call the adjc(a,b) function on each cell to initialize all adj counts
    }
    private int adjc(int a, int b){//when called, initialize adjacent bomb counts for 1 cell. Helper for initBoardForFirstClickAt(x,y).
        int adjCount = 0;
        for(int i=a-1;i<=a+1;i++){//a-1 to a+1
            for(int j=b-1;j<=b+1;j++){//b-1 to b+1
                if(i<0||j<0||i>=Fieldx||j>=Fieldy||(i==a && j==b)) continue;//<--  if not inside the grid, continue so we dont check an out of bounds array index
                if(cell[i][j]) adjCount++;//<-- if cell has bomb, increase adj count
            }
        }
        return adjCount;//<-- return number of adjacent bombs
    }
    //-----------------------Data Access functions------------------------------------------------------------------------------
    //I grouped them by the 2D array they reference.

    boolean isBomb(int a, int b){return cell[a][b];}//<-- returns value in cell[][] for the one asked about, which indicates if it is a bomb or not.

    int adjCount(int a, int b){return adj[a][b];}//<-- same thing but for adj count

    //exploding
    void explode(int a, int b){
        if(!exp[a][b])totalExploded++;//<-- cant mess up the count because it checks if it was already exploded before incrementing it.
        exp[a][b]=true;
    }
    boolean exploded(int a, int b){return exp[a][b];}
    int cellsExploded(){return totalExploded;}

    //marking
    void mark(int a, int b){
        if(!mrk[a][b])totalMarked++;
        mrk[a][b]=true;
    }
    void unmark(int a, int b){
        if(mrk[a][b])totalMarked--;
        mrk[a][b]=false;
    }
    boolean marked(int a, int b){return mrk[a][b];}
    int cellsMarked(){return totalMarked;}

    //questioning
    void question(int a, int b){qstn[a][b]=true;}
    void clearSuspicion(int a, int b){qstn[a][b]=false;}
    boolean isQuestionable(int a, int b){return qstn[a][b];}

    //checking (for every cell that is revealed and not a bomb, we will check it. When cellsChecked()==Fieldx*Fieldy-bombCount, we win.)
    void check(int a, int b){
        if(!chkd[a][b])totalChecked++;
        chkd[a][b]=true;
    }
    boolean checked(int a, int b){return chkd[a][b];}
    int cellsChecked(){return totalChecked;}

    //first click & start timer
    void doFirstClick(){
        if(firstClick){
            firstClick=false;
            startTime = System.currentTimeMillis();
            currentTimeTimer.scheduleAtFixedRate(timeTask, 0, 200);//<-- change timer precision here
        }
    }
    boolean isFirstClick(){return firstClick;}

    long getTime(){return time;}//get time

    //GameOver & stop timer (which then cannot be started again in this instance)
    void setGameOver(){
        if(!GameOver){
            GameOver=true;
            timeTask.cancel();
        }
    }
    boolean isGameOver(){return GameOver;}
}