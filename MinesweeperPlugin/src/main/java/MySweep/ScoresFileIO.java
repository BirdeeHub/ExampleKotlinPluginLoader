package MySweep;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

class ScoresFileIO{//reads from file, creates scoreEntry instances based on the file contents. ScoresWindow uses read and delete, maingame uses update. write is private
    private static final String scoresFileNameWindows = System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "minesweeperScores" + File.separator + "MineSweeperScores.txt";
    private static final String scoresFileNameOther = System.getProperty("user.home") + File.separator + ".minesweeper" + File.separator + "MineSweeperScores.txt";
    private static final String scoresFileName = (System.getProperty("os.name").toLowerCase().contains("win"))?scoresFileNameWindows:scoresFileNameOther;
    //private static final String scoresFileName = System.getProperty("java.io.tmpdir") + File.separator + "MineSweeperScores.txt";
    //Its all static so we do not need a constructor.
    //----------------------------------WRITE------------------------------------------------------WRITE-------------------------------------------
    private static void writeLeaderboard(ScoreEntry[] allEntries, boolean append){// writes from Score Entries to file
        StringBuilder scoresFileString = new StringBuilder();// StringBuilder to store and create string from entries
        if(append)scoresFileString.append(" ");//<-- make sure theres at least one space to separate it from the other scores
        for(int i = 0; i < allEntries.length; i++){//<-- for all the entries
            scoresFileString.append(allEntries[i].toString()).append(" ");//<-- string builders have a good append function. arrays dont.
        }
        //-------------------------write string----------------------write string-------------
        try {
            Files.createDirectories(Path.of(scoresFileName).getParent()); //<-- Create the directory.
        } catch (IOException e) {System.out.println(e.getClass()+" @ "+scoresFileName);}
        try{
            Files.createFile(Path.of(scoresFileName));//<-- Create the file if not created.
        }catch(IOException e){if(!(e instanceof FileAlreadyExistsException))System.out.println(e.getClass()+" @ "+scoresFileName);}

        try (FileWriter out2 = new FileWriter(scoresFileName, append)) {//<-- filewriters can overwrite or append a string to a file
            out2.write(scoresFileString.toString());//<-- overwrite the file with new contents, or append as specified.
        }catch(IOException e){System.out.println(e.getClass()+" @ "+scoresFileName);}
    }
    //-----------------------------------READ-------------------------------------READ----------------------------------------------------------------
    public static ScoreEntry[] readLeaderboard(){ //reads from file by word to Score Entries
        ArrayList<ScoreEntry> fileEntriesBuilder = new ArrayList<>();//<-- Array lists also have a good append function.
        ScoreEntry[] fileEntries;
        try(Scanner in = new Scanner(new File(scoresFileName))) {//scanner class reads words from a file to strings (separated by whitespace) 
            while (in.hasNext()) {//<-- while theres still something in the file
                ScoreEntry currentEntry = new ScoreEntry(in.next());//<-- get next word (a string separated by whitespace)
                if(currentEntry.isValid())fileEntriesBuilder.add(currentEntry);//<-- only read out valid scores
            }
            fileEntries = fileEntriesBuilder.toArray(new ScoreEntry[0]);//<-- return our array of entries
        }catch(FileNotFoundException e){
            fileEntries=null; 
            System.out.println(e.getClass()+" @ "+scoresFileName);
        }
        return fileEntries;
    }
    //---------------------------------Everything below here uses only ScoreEntries to do its work---and uses read and write---------------
    //-----------------------------Everything below here uses only ScoreEntries to do its work------------deleteScoreEntry----------------------
    public static void deleteScoreEntry(ScoreEntry thisEntry){//<-- reads score file, overwrites with the same thing but without specified entry
        ScoreEntry[] deletries = readLeaderboard();// <-- read
        ArrayList<ScoreEntry> newFileBuilder = new ArrayList<>();
        if(deletries!=null){
            int c = 0;
            while(c<deletries.length){
                if(thisEntry.isValid()){//<-- only write back valid entries
                    if(!(deletries[c].equals(thisEntry) && thisEntry.getRemainingLives()==deletries[c].getRemainingLives() && thisEntry.getTime()==deletries[c].getTime())){
                        newFileBuilder.add(deletries[c]);//only add back entries that arent the exact entry in thisEntry (equals() only finds if same board)
                    }
                }
                c++;
            }
            deletries = newFileBuilder.toArray(new ScoreEntry[0]);
            writeLeaderboard(deletries, false);// <-- overwrite with new
        }
    }//-------------------------------------------------------------------------update score entry-------------------------------------------
    public static int updateScoreEntry(boolean won, long time, int cellsExploded, int Fieldx, int Fieldy, int bombCount, int lives){
        //Writes new scores to score file, returns highscore/new_board/normal index for assigning win/loss message
        int RemainingLives= Math.max(0, lives-cellsExploded);
        ScoreEntry thisEntry = new ScoreEntry(Fieldx,Fieldy,bombCount,lives,RemainingLives,time);
        if(thisEntry.isValid()){
            ScoreEntry[] entries = readLeaderboard();//<-- get our score entries
            if(entries == null){//<-- file not found
                entries = new ScoreEntry[1];
                entries[0] = thisEntry;
                writeLeaderboard(entries, false);
                return 1;//<-- score not found index
            }else{
                if(0==entries.length){//<-- file found but empty. Writing.
                    entries = new ScoreEntry[1];
                    entries[0] = thisEntry;
                    writeLeaderboard(entries, false);
                    return 1;
                }else{//<---------------------- file found and not empty
                    boolean thisScoreFound=false;
                    boolean isHighscore=false;
                    for(int c = 0;c<entries.length;c++){//loop through entries in file
                        if(entries[c].isValid() && entries[c].equals(thisEntry)){//<-- board identifier matches
                            thisScoreFound=true;
                            if(won && entries[c].getTime()>time){
                                entries[c]=thisEntry;//                         ^did you beat the time?
                                isHighscore=true;
                            }else if(won && entries[c].getRemainingLives()>RemainingLives && entries[c].getTime()==time){
                                entries[c]=thisEntry;//                         ^is it same time but more lives?
                                isHighscore=true;
                            }else if(won && entries[c].getRemainingLives()<1){//was the entry created by dying on a new board configuration?
                                entries[c]=thisEntry;
                                isHighscore=true;
                            }
                        }
                    }
                    if(!thisScoreFound){//none were a match. New Board Size
                        ScoreEntry[] newEntries = new ScoreEntry[1];
                        newEntries[0] = thisEntry;
                        writeLeaderboard(newEntries, true);
                        return 1;
                    }
                    if(isHighscore){//Was a high score! save edited version of file
                        writeLeaderboard(entries, false);
                        return 2;
                    }
                }
            }
        }
        return 0;//<-- board size was found, score was not better.
    }
}