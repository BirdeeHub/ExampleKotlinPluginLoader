package MySweep;
public class ScoreEntry{//This exists because i was converting from string way more times than my brain wanted to do.
    private int x, y, bombCount, lives;//creates a standardized object for entries so you can change score saving formats without changing a ton of code
    private int RemainingLives;//as long as it converts to string for write and read, change it however.
    private boolean validScore = true;//JSON? sure. I didnt do that though. Also, scanner reads by spaces, so you would have to change write and read in IO
    private long time;
    //-----------Constructors-----------------------------Constructors--------------------------
    //This Constructor is used only in read ScoresFileIO
    public ScoreEntry(String Word){//format for entryWord = "x:y:bombCount:lives-RemainingLives-time"
        setEntryFromWord(Word);//     defined at end in string to score conversion section.
    }
    //--These Constructors are used anywhere else
    public ScoreEntry(){
        this.validScore=false;
    }
    public ScoreEntry(int x,int y,int bombCount,int lives,int RemainingLives,long time){
        if(x<=0||y<=0||lives<=0||bombCount<=0||time<0||RemainingLives<0){
            validScore=false;
        }else{
            this.validScore = true;
            this.x=x;
            this.y=y;
            this.bombCount=bombCount;
            this.lives=lives;
            this.RemainingLives=RemainingLives;
            this.time=time;
        }
    }//------------------------------------------Misc Public functions-------------------------------------------------
    public boolean isValid(){return validScore;}
    public int getX(){return x;}
    public int getY(){return y;}
    public int getBombCount(){return bombCount;}
    public int getLives(){return lives;}
    public int getRemainingLives(){return RemainingLives;}
    public long getTime(){return time;}
    @Override
    public boolean equals(Object o) {
        if(o instanceof ScoreEntry){
            if(((ScoreEntry)o).isValid()){
                if((((ScoreEntry)o).getX() == this.x) && (((ScoreEntry)o).getY() == this.y) &&
                    (((ScoreEntry)o).getBombCount() == this.bombCount) && (((ScoreEntry)o).getLives() == this.lives)){
                    return true;
                }else return false;
            }else return false;
        }else return false;
    }
    //-----------------------------------------String to score conversion-----------------------------------------------------------
    @Override
    public String toString(){//used only in write of ScoresFileIO
        if(validScore){
            return Integer.toString(x)+":"+Integer.toString(y)+":"+Integer.toString(bombCount)+":"+Integer.toString(lives)+"-"+Integer.toString(RemainingLives)+"-"+Long.toString(time);
        }else return "";
    }
    private boolean setEntryFromWord(String Word){//used only in the constructor of this class
        validScore = false;
        String[] currentEntry = Word.split("-");
        String[] boardStrings = currentEntry[0].split(":");
        if(currentEntry.length == 3 && boardStrings.length == 4){
            try{
                if(Integer.parseInt(boardStrings[0])<=0||Integer.parseInt(boardStrings[1])<=0||Integer.parseInt(boardStrings[2])<=0||Integer.parseInt(boardStrings[3])<=0||Integer.parseInt(currentEntry[1])<0||Long.parseLong(currentEntry[2])<0){
                }else{
                    validScore=true;
                    this.x=Integer.parseInt(boardStrings[0]);
                    this.y=Integer.parseInt(boardStrings[1]);
                    this.bombCount=Integer.parseInt(boardStrings[2]);
                    this.lives=Integer.parseInt(boardStrings[3]);
                    this.RemainingLives=Integer.parseInt(currentEntry[1]);
                    this.time=Long.parseLong(currentEntry[2]);
                }
            }catch(NumberFormatException e){System.out.println("errors in "+Word);validScore=false;}
        }
        return validScore;
    }
}