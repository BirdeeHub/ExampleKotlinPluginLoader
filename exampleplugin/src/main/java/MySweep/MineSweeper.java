package MySweep;

import javax.swing.ImageIcon;
import javax.swing.UIManager;
import java.awt.EventQueue;
import java.awt.Image;
//import java.io.FileInputStream;
//import java.nio.file.Paths;
import java.awt.Frame;
class MineSweeper {
    public static Image MineIcon = new ImageIcon(MineSweeper.class.getResource("/Icons/MineSweeperIcon.png")).getImage();
    public static Image ExplosionIcon = new ImageIcon(MineSweeper.class.getResource("/Icons/GameOverExplosion.png")).getImage();
    private static boolean DarkMode = true;//<-- we only set this in toggleDarkMode so it is private so we dont forget and mess that up somewhere
    public static boolean isDarkMode(){return DarkMode;}//<-- for other classes to get darkmode status using MineSweeper.isDarkMode()
    public static void toggleDarkMode() {//<-- the toggle button in InstructionsWindow calls this.
        Frame[] frames = Frame.getFrames();//<-- this is how you get all classes that extend Frame in a program. JFrames extend Frame
        DarkMode = !DarkMode;//<-- toggle our DarkModeVariable
        for (Frame frame : frames) {//<-- a fancy for loop. "for each frame in frames array"
            if(frame instanceof MainGameWindow){//<-- check if it is a specific type you can cast it as the correct window
                ((MainGameWindow)frame).toggleDarkMode();//<-- cast as correct window, and run the function from that instance of the class.
            }else if(frame instanceof InstructionsWindow){
                ((InstructionsWindow)frame).toggleDarkMode();
            }else if(frame instanceof OpeningWindow){
                ((OpeningWindow)frame).toggleDarkMode();
            }else if(frame instanceof ScoresWindow){
                ((ScoresWindow)frame).toggleDarkMode();
            }
        }
    }
    public static void StartMineSweeperMain(String[] args){
        try {//(I found out that if you dont do this thing some Swing library stuff breaks on mac)
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }catch (Exception e) {}
        
        int width, height, bombCount, lives;
        if(args.length == 4){
            try{
                width = (int)(Integer.parseInt(args[0]));
                height = (int)(Integer.parseInt(args[1]));
                bombCount = (int)(Integer.parseInt(args[2]));
                lives = (int)(Integer.parseInt(args[3]));
                if(width > 0 && height > 0 && bombCount > 0 && lives > 0){
                    EventQueue.invokeLater(new Runnable(){public void run(){new MainGameWindow(width, height, bombCount, lives).setVisible(true);}});
                }else{
                    System.out.println("integer arguments only: width, height, BombCount, lives (where all are > 0)");
                    EventQueue.invokeLater(new Runnable(){public void run(){new OpeningWindow().setVisible(true);}});
                }
            }catch(NumberFormatException e){
                System.out.println("integer arguments only: width, height, BombCount, lives (where all are > 0)");
                EventQueue.invokeLater(new Runnable(){public void run(){new OpeningWindow().setVisible(true);}});
            }
        }else if(args.length == 5){
            if(args[0].equals("o")||args[0].equals("m")){
                try{
                    width = (int)(Integer.parseInt(args[1]));
                    height = (int)(Integer.parseInt(args[2]));
                    bombCount = (int)(Integer.parseInt(args[3]));
                    lives = (int)(Integer.parseInt(args[4]));
                    if(width > 0 && height > 0 && bombCount > 0 && lives > 0){
                        if(args[0].equals("m")){
                            EventQueue.invokeLater(new Runnable(){public void run(){
                                new MainGameWindow(width, height, bombCount, lives).setVisible(true);
                            }});
                        }
                        if(args[0].equals("o")){//Opening window can take strings
                            EventQueue.invokeLater(new Runnable(){public void run(){new OpeningWindow(args[1],args[2], args[3], args[4]).setVisible(true);}});
                        }
                    }else{
                        System.out.println("<m or o>, width, height, BombCount, lives");
                        EventQueue.invokeLater(new Runnable(){public void run(){new OpeningWindow().setVisible(true);}});
                    }
                }catch(NumberFormatException e){
                    System.out.println("<m or o>, width, height, BombCount, lives");
                    EventQueue.invokeLater(new Runnable(){public void run(){new OpeningWindow().setVisible(true);}});
                }
            }else{
                System.out.println("<m or o>, width, height, BombCount, lives");
                EventQueue.invokeLater(new Runnable(){public void run(){new OpeningWindow().setVisible(true);}});
            }
        }else{
            EventQueue.invokeLater(new Runnable(){public void run(){new OpeningWindow().setVisible(true);}});
        }
    }
    /** These are our possible command line arguments (all > 0)
     * @param args [String <o or m>], int width, int height, int BombCount, int lives
     */
    public static void main(String[] args) {
        StartMineSweeperMain(args);
    }
}