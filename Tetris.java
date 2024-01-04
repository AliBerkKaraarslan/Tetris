//**********************************************************************************************************************************
// Tetris.java         Author:Ali Berk Karaarslan     Date:05.10.2023
//
// To change the game mode, set "gameMode" variable as "new" or "classic".
// "classic" -> Original NES mode. There is no hold and hard drop mechanics. Also it is not shows where the tetromino will fall. 
// "new" -> Current Tetris mode. Allows to hold and hard drop mechanics. Also shows where the tetromino will fall.
//
// =CONTROLS=
// Movement: Left/Right Arrow Keys         Rotate Left: Z
// Soft Drop: Down Arrow Key               Rotate Right: X/Up Arrow Key
// Hard Drop: Space                        Pause: Escape
// Hold: C
//
//**********************************************************************************************************************************

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;

public class Tetris {

    //Default Sizes
    int screenWidth = 900;
    int screenHeight = 594;
    int tetrominoSize = 20;

    //Parameters of the game
    private String gameMode = "new";  //Choose game mode. "new" or "classic"
    private int score = 0;
    private int level = 0;
    private int lines = 0;
    private double gameSpeed = Math.pow( (0.8-((level)*0.007)) , level );

    boolean gameOver = false;
    boolean gamePaused = false;
    boolean holdUsed = false;   //Only one hold operation could be used when Tetromino is falling (Resets after placed)

    //Colors of the Tetrominoes
    Color I_PieceColor = Color.CYAN;
    Color J_PieceColor = Color.BLUE;
    Color L_PieceColor = Color.ORANGE;
    Color O_PieceColor = Color.YELLOW;
    Color S_PieceColor = Color.GREEN;
    Color T_PieceColor = Color.MAGENTA;
    Color Z_PieceColor = Color.RED;

    GamePanel gamePanel;
    MainFrame frame;
    Tetromino[][] gameGrid = new Tetromino[10][24];   //Indexes corresponded to coordinate system. That means, there is 10 blocks in x-axis and 24 blocks in y-axis

    Tetromino currentTetromino;  //Stores the current falling Tetromino
    char nextTetromino = 'N';   //Stores the type of the next Tetromino. 'N' refers to null.
    char holdTetromino = 'N';   //Stores the type of the hold Tetromino. 'N' refers to null.

    ArrayList<Integer> strikes = new ArrayList<>();   //Stores the index of the removed rows when strike happens
    ArrayDeque<Tetromino> tetrominoes = new ArrayDeque<>();   //Tetrominoes Queue. Helps to show next Tetromino

    //Constructor method
    public Tetris(){
        frame = new MainFrame();
        frame.getContentPane();
    }

    //Main Frame Of The Program
    class MainFrame extends JFrame {

        public MainFrame() {
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setTitle("Tetris (By Ali Berk Karaarslan)");

            gamePanel = new GamePanel();
            add(gamePanel);
            addKeyListener(gamePanel);

            //Locates Frame into the middle of the screen
            pack();
            setLocationRelativeTo(null);

            generateRandomTetromino();  //Generating the first Tetromino of the game
            setVisible(true);
        }
    }

    //Game Panel Of The Program. Contains Painting and Control Operations.
    class GamePanel extends JPanel implements KeyListener{

        Dimension size = getSize();   //Screen size of the program

        //These coordinates contain the borders.
        
        //Coordinates of the play area
        int playAreaX1 = (screenWidth-(12*tetrominoSize))/2;
        int playAreaY1 = (screenHeight-(22*tetrominoSize))/2;
        int playAreaWidth = (12*tetrominoSize);
        int playAreaHeight = (22*tetrominoSize);

        //Coordinates of the next area
        int nextAreaWidth = 6*tetrominoSize;
        int nextAreaHeight = 6*tetrominoSize;
        int nextAreaX1 = playAreaX1 + playAreaWidth + (tetrominoSize);
        int nextAreaY1 = (playAreaY1 + (playAreaHeight-nextAreaHeight)/6);

        //Coordinates of the hold area
        int holdAreaWidth = 6*tetrominoSize;
        int holdAreaHeight = 6*tetrominoSize;
        int holdAreaX1 = playAreaX1 - holdAreaWidth - (tetrominoSize);
        int holdAreaY1 = (playAreaY1 + (playAreaHeight-holdAreaHeight)/6);

        //Labels
        JLabel nextLabel;
        Dimension nextLabelSize;

        JLabel holdLabel;
        Dimension holdLabelSize;

        JLabel scoreLabel;
        Dimension scoreLabelSize;
        JLabel scoreCountLabel;
        Dimension scoreCountLabelSize;

        JLabel levelLabel;
        Dimension levelLabelSize;
        JLabel levelCountLabel;
        Dimension levelCountLabelSize;

        JLabel linesLabel;
        Dimension linesLabelSize;
        JLabel linesCountLabel;
        Dimension linesCountLabelSize;

        JLabel strikeLabel;
        Dimension strikeLabelSize;
        JLabel strikeCountLabel;
        Dimension strikeCountLabelSize;

        JLabel gameOverLabel;
        Dimension gameOverLabelSize;
        JLabel pressEnterLabel;
        Dimension pressEnterLabelSize;

        JLabel gamePausedLabel;
        Dimension gamePausedLabelSize;

        //Constructor Method
        public GamePanel(){
            setPreferredSize(new Dimension(screenWidth,screenHeight));
            setBackground(Color.BLACK);

            setLayout(null);

            nextLabel = new JLabel("NEXT");
            scoreLabel = new JLabel("SCORE");
            scoreCountLabel = new JLabel(""+score);
            levelLabel = new JLabel("LEVEL");
            levelCountLabel = new JLabel(""+level);
            linesLabel = new JLabel("LINES");
            linesCountLabel = new JLabel(""+lines);
            strikeLabel = new JLabel("       ");
            strikeCountLabel = new JLabel("       ");
            holdLabel = new JLabel(("HOLD"));
            gameOverLabel = new JLabel("GAME OVER");
            pressEnterLabel = new JLabel("PRESS ENTER TO CONTINUE");
            gamePausedLabel = new JLabel("GAME PAUSED");

            updateLabels();

            add(nextLabel);
            add(scoreLabel);
            add(scoreCountLabel);
            add(levelLabel);
            add(levelCountLabel);
            add(linesLabel);
            add(linesCountLabel);
            add(strikeLabel);
            add(strikeCountLabel);

            if(!gameMode.equals("classic"))
                add(holdLabel);

            //Refreshes the game screen every 0.005 seconds
            Thread screenRefresher = new Thread(() -> {
                try {
                    while(true) {
                        Thread.sleep((long) (0.005 * 1000));
                        repaint();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            screenRefresher.start();
        }

        /* Updates The Strike Label Due To Given Strike Type And Score. (for ex. shows "Double +40") */
        class strikeUpdater extends Thread{

            String strike;
            int score;

            public strikeUpdater(String strike, int score){
                this.strike = strike;
                this.score = score;
                start();
            }
            public void run() {
                //Updates the Strike Label
                strikeLabel.setText(strike);
                strikeCountLabel.setText("+" + score);
                strikeLabelSize = strikeLabel.getPreferredSize();
                strikeCountLabelSize = strikeCountLabel.getPreferredSize();
                strikeLabel.setBounds((int) (holdAreaX1+(holdAreaWidth-strikeLabelSize.getWidth())/2), (int) (holdAreaY1 + holdAreaHeight + (5* tetrominoSize)), strikeLabelSize.width+ 50, strikeLabelSize.height);
                strikeCountLabel.setBounds((int) (holdAreaX1+(holdAreaWidth-strikeCountLabelSize.getWidth())/2), (int) (holdAreaY1 + holdAreaHeight + (6.5* tetrominoSize)), strikeCountLabelSize.width+ 50, strikeCountLabelSize.height);

                strikeLabel.paintImmediately(strikeLabel.getVisibleRect());
                strikeCountLabel.paintImmediately(strikeCountLabel.getVisibleRect());

                //Waits 0.7 second before removed from screen
                try {
                    Thread.sleep((long) (0.7 * 1000));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                //Clears the Strike Label
                strikeLabel.setText("");
                strikeCountLabel.setText("");
                strikeLabel.paintImmediately(strikeLabel.getVisibleRect());
                strikeCountLabel.paintImmediately(strikeCountLabel.getVisibleRect());
            }
        }

        //Updates All The Labels
        public void updateLabels(){

            nextLabel.setFont(new Font("Monospaced", Font.BOLD, (12*tetrominoSize)/10));
            nextLabel.setForeground(Color.WHITE);
            nextLabelSize = nextLabel.getPreferredSize();
            nextLabel.setBounds((int) (nextAreaX1+(nextAreaWidth-nextLabelSize.getWidth())/2), (int) (nextAreaY1-(1.5* tetrominoSize)), nextLabelSize.width + 50, nextLabelSize.height);

            holdLabel.setFont(new Font("Monospaced", Font.BOLD, (12*tetrominoSize)/10));
            holdLabel.setForeground(Color.WHITE);
            holdLabelSize = holdLabel.getPreferredSize();
            holdLabel.setBounds((int) (holdAreaX1+(holdAreaWidth-holdLabelSize.getWidth())/2), (int) (holdAreaY1-(1.5* tetrominoSize)), holdLabelSize.width + 50, holdLabelSize.height);

            scoreLabel.setFont(new Font("Monospaced", Font.BOLD, (12*tetrominoSize)/10));
            scoreLabel.setForeground(Color.WHITE);
            scoreLabelSize = scoreLabel.getPreferredSize();
            scoreLabel.setBounds((int) (nextAreaX1+(nextAreaWidth-scoreLabelSize.getWidth())/2), (int) (nextAreaY1+nextAreaHeight + (tetrominoSize)), scoreLabelSize.width + 50, scoreLabelSize.height);

            scoreCountLabel.setText(""+score);
            scoreCountLabel.setFont(new Font("Monospaced", Font.BOLD, (11*tetrominoSize)/10));
            scoreCountLabel.setForeground(Color.WHITE);
            scoreCountLabelSize = scoreCountLabel.getPreferredSize();
            scoreCountLabel.setBounds((int) (nextAreaX1+(nextAreaWidth-scoreCountLabelSize.getWidth())/2), (int) (nextAreaY1+nextAreaHeight + (2.5*tetrominoSize)), scoreCountLabelSize.width + 50, scoreCountLabelSize.height);
            scoreCountLabel.paintImmediately(scoreCountLabel.getVisibleRect());

            levelLabel.setFont(new Font("Monospaced", Font.BOLD, (12*tetrominoSize)/10));
            levelLabel.setForeground(Color.WHITE);
            levelLabelSize = levelLabel.getPreferredSize();
            levelLabel.setBounds((int) (nextAreaX1+(nextAreaWidth-levelLabelSize.getWidth())/2), (int) (nextAreaY1 + nextAreaHeight + (5* tetrominoSize)), levelLabelSize.width+ 50, levelLabelSize.height);

            levelCountLabel.setText(""+level);
            levelCountLabel.setFont(new Font("Monospaced", Font.BOLD, (11*tetrominoSize)/10));
            levelCountLabel.setForeground(Color.WHITE);
            levelCountLabelSize = levelCountLabel.getPreferredSize();
            levelCountLabel.setBounds((int) (nextAreaX1+(nextAreaWidth-levelCountLabelSize.getWidth())/2), (int) (nextAreaY1 + nextAreaHeight + (6.5* tetrominoSize)), levelCountLabelSize.width+ 50, levelCountLabelSize.height);
            levelCountLabel.paintImmediately(levelCountLabel.getVisibleRect());

            linesLabel.setFont(new Font("Monospaced", Font.BOLD, (12*tetrominoSize)/10));
            linesLabel.setForeground(Color.WHITE);
            linesLabelSize = linesLabel.getPreferredSize();
            linesLabel.setBounds((int) (nextAreaX1+(nextAreaWidth-linesLabelSize.getWidth())/2), (int) (nextAreaY1 + nextAreaHeight + (9* tetrominoSize)), linesLabelSize.width+ 50, linesLabelSize.height);

            linesCountLabel.setText(""+lines);
            linesCountLabel.setFont(new Font("Monospaced", Font.BOLD, (11*tetrominoSize)/10));
            linesCountLabel.setForeground(Color.WHITE);
            linesCountLabelSize = linesCountLabel.getPreferredSize();
            linesCountLabel.setBounds((int) (nextAreaX1+(nextAreaWidth-linesCountLabelSize.getWidth())/2), (int) (nextAreaY1 + nextAreaHeight + (10.5* tetrominoSize)), linesCountLabelSize.width+ 50, linesCountLabelSize.height);
            linesCountLabel.paintImmediately(linesCountLabel.getVisibleRect());

            strikeLabel.setFont(new Font("Monospaced", Font.BOLD, (10*tetrominoSize)/10));
            strikeLabel.setForeground(Color.WHITE);
            strikeLabelSize = strikeLabel.getPreferredSize();
            strikeLabel.setBounds((int) (holdAreaX1+(holdAreaWidth-strikeLabelSize.getWidth())/2), (int) (holdAreaY1 + holdAreaHeight + (5* tetrominoSize)), strikeLabelSize.width+ 50, strikeLabelSize.height);

            strikeCountLabel.setFont(new Font("Monospaced", Font.BOLD, (10*tetrominoSize)/10));
            strikeCountLabel.setForeground(Color.WHITE);
            strikeCountLabelSize = strikeCountLabel.getPreferredSize();
            strikeCountLabel.setBounds((int) (holdAreaX1+(holdAreaWidth-strikeCountLabelSize.getWidth())/2), (int) (holdAreaY1 + holdAreaHeight + (6.5* tetrominoSize)), strikeCountLabelSize.width+ 50, strikeCountLabelSize.height);

            gameOverLabel.setFont(new Font("Monospaced", Font.BOLD, (17*tetrominoSize)/10));
            gameOverLabel.setForeground(Color.WHITE);
            gameOverLabelSize = gameOverLabel.getPreferredSize();
            gameOverLabel.setBounds(screenWidth/2-(gameOverLabelSize.width/2),screenHeight/2-(gameOverLabelSize.height/2) - (tetrominoSize),gameOverLabelSize.width+50,gameOverLabelSize.height);

            pressEnterLabel.setFont(new Font("Monospaced", Font.BOLD, (12*tetrominoSize)/10));
            pressEnterLabel.setForeground(Color.WHITE);
            pressEnterLabelSize = pressEnterLabel.getPreferredSize();
            pressEnterLabel.setBounds(screenWidth/2-(pressEnterLabelSize.width/2),screenHeight/2-(pressEnterLabelSize.height/2) + (tetrominoSize),pressEnterLabelSize.width+50,pressEnterLabelSize.height);

            gamePausedLabel.setFont(new Font("Monospaced", Font.BOLD, (14*tetrominoSize)/10));
            gamePausedLabel.setForeground(Color.WHITE);
            gamePausedLabelSize = gamePausedLabel.getPreferredSize();
            gamePausedLabel.setBounds(screenWidth/2-(gamePausedLabelSize.width/2),screenHeight/2-(gamePausedLabelSize.height/2),gamePausedLabelSize.width+50,gamePausedLabelSize.height);
        }

        //Paints The Game Areas And Grids.
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            //Updates the positions of all components due to screen size change
            if (!size.equals(getSize())){
                size = getSize();

                screenWidth = getWidth();
                screenHeight = getHeight();
                playAreaX1 = (screenWidth-(12*tetrominoSize))/2;
                playAreaY1 = (screenHeight-(22*tetrominoSize))/2;
                nextAreaX1 = playAreaX1 + playAreaWidth + (tetrominoSize);
                nextAreaY1 = (playAreaY1 + (playAreaHeight-nextAreaHeight)/6);
                holdAreaX1 = playAreaX1 - holdAreaWidth - (tetrominoSize);
                holdAreaY1 = (playAreaY1 + (playAreaHeight-holdAreaHeight)/6);

                //Repositioning labels
                nextLabel.setBounds((int) (nextAreaX1+(nextAreaWidth-nextLabelSize.getWidth())/2), (int) (nextAreaY1-(1.5* tetrominoSize)), nextLabelSize.width + 50, nextLabelSize.height);
                holdLabel.setBounds((int) (holdAreaX1+(holdAreaWidth-holdLabelSize.getWidth())/2), (int) (holdAreaY1-(1.5* tetrominoSize)), holdLabelSize.width + 50, holdLabelSize.height);
                scoreLabel.setBounds((int) (nextAreaX1+(nextAreaWidth-scoreLabelSize.getWidth())/2), (int) (nextAreaY1+nextAreaHeight + (tetrominoSize)), scoreLabelSize.width + 50, scoreLabelSize.height);
                scoreCountLabel.setBounds((int) (nextAreaX1+(nextAreaWidth-scoreCountLabelSize.getWidth())/2), (int) (nextAreaY1+nextAreaHeight + (2.5*tetrominoSize)), scoreCountLabelSize.width + 50, scoreCountLabelSize.height);
                levelLabel.setBounds((int) (nextAreaX1+(nextAreaWidth-levelLabelSize.getWidth())/2), (int) (nextAreaY1 + nextAreaHeight + (5* tetrominoSize)), levelLabelSize.width+ 50, levelLabelSize.height);
                levelCountLabel.setBounds((int) (nextAreaX1+(nextAreaWidth-levelCountLabelSize.getWidth())/2), (int) (nextAreaY1 + nextAreaHeight + (6.5* tetrominoSize)), levelCountLabelSize.width+ 50, levelCountLabelSize.height);
                linesLabel.setBounds((int) (nextAreaX1+(nextAreaWidth-linesLabelSize.getWidth())/2), (int) (nextAreaY1 + nextAreaHeight + (9* tetrominoSize)), linesLabelSize.width+ 50, linesLabelSize.height);
                linesCountLabel.setBounds((int) (nextAreaX1+(nextAreaWidth-linesCountLabelSize.getWidth())/2), (int) (nextAreaY1 + nextAreaHeight + (10.5* tetrominoSize)), linesCountLabelSize.width+ 50, linesCountLabelSize.height);
                strikeLabel.setBounds((int) (holdAreaX1+(holdAreaWidth-strikeLabelSize.getWidth())/2), (int) (holdAreaY1 + holdAreaHeight + (5* tetrominoSize)), strikeLabelSize.width+ 50, strikeLabelSize.height);
                strikeCountLabel.setBounds((int) (holdAreaX1+(holdAreaWidth-strikeCountLabelSize.getWidth())/2), (int) (holdAreaY1 + holdAreaHeight + (6.5* tetrominoSize)), strikeCountLabelSize.width+ 50, strikeCountLabelSize.height);
                gameOverLabel.setBounds(screenWidth/2-(gameOverLabelSize.width/2),screenHeight/2-(gameOverLabelSize.height/2) - (tetrominoSize),gameOverLabelSize.width+50,gameOverLabelSize.height);
                pressEnterLabel.setBounds(screenWidth/2-(pressEnterLabelSize.width/2),screenHeight/2-(pressEnterLabelSize.height/2) + (tetrominoSize),pressEnterLabelSize.width+50,pressEnterLabelSize.height);
                gamePausedLabel.setBounds(screenWidth/2-(gamePausedLabelSize.width/2),screenHeight/2-(gamePausedLabelSize.height/2),gamePausedLabelSize.width+50,gamePausedLabelSize.height);
            }

            //Drawing The Areas
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(playAreaX1,playAreaY1,playAreaWidth,playAreaHeight);   //Drawing the outline of play area
            g.fillRect(nextAreaX1,nextAreaY1,nextAreaWidth,nextAreaHeight);   //Drawing the outline of next area
            if(!gameMode.equals("classic"))
                g.fillRect(holdAreaX1,holdAreaY1,holdAreaWidth,holdAreaHeight);   //Drawing the outline of hold area

            g.setColor(Color.BLACK);
            g.fillRect(playAreaX1+tetrominoSize,playAreaY1+tetrominoSize,playAreaWidth-2*tetrominoSize,playAreaHeight-2*tetrominoSize);
            g.fillRect(nextAreaX1+(tetrominoSize/4), nextAreaY1+(tetrominoSize/4), nextAreaWidth-(tetrominoSize/2),nextAreaHeight-(tetrominoSize/2));
            if(!gameMode.equals("classic"))
                g.fillRect(holdAreaX1+(tetrominoSize/4), holdAreaY1+(tetrominoSize/4), holdAreaWidth-(tetrominoSize/2),holdAreaHeight-(tetrominoSize/2));

            //Drawing the grid
            for(int i=playAreaX1; i<=playAreaX1+playAreaWidth ; i+=tetrominoSize) {
                g.setColor(new Color(31, 31, 31));
                g.drawLine(i, playAreaY1+tetrominoSize , i, playAreaY1+playAreaHeight-tetrominoSize);
            }
            for(int i=playAreaY1; i<=playAreaY1+playAreaHeight ; i+=tetrominoSize) {
                g.setColor(new Color(31, 31, 31));
                g.drawLine(playAreaX1+tetrominoSize, i , playAreaX1+playAreaWidth-tetrominoSize, i);
            }

            paintTetrominoes(g);

            //Painting the top of the play area to prevent Tetrominoes to appear from above.
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(playAreaX1,playAreaY1,playAreaWidth,tetrominoSize);

            g.setColor(Color.BLACK);
            g.drawRect(playAreaX1+tetrominoSize,playAreaY1+tetrominoSize,playAreaWidth-2*tetrominoSize,playAreaHeight-2*tetrominoSize);
            g.drawRect(playAreaX1,playAreaY1,playAreaWidth,playAreaHeight);
            g.fillRect(playAreaX1,playAreaY1-(4*tetrominoSize),playAreaWidth,(4*tetrominoSize));
        }

        //Paints All The Tetrominoes(Contains Placed, Falling, Next, Hold, And Hologram Tetrominoes)
        public void paintTetrominoes(Graphics g){

            if(gameMode != "classic") {
                //Painting the hologram Tetromino
                int minDistance = Integer.MAX_VALUE;   //Min distance to bottom(or another Tetromino)
                int distance;   //Helps to find minDistance
                int minYCoordinate;    //Min y-coordinate of the currentTetromino

                //Stores the blocks of the current Tetromino
                int[][] blocksOfCurrentTetromino = {currentTetromino.block0, currentTetromino.block1, currentTetromino.block2, currentTetromino.block3};

                //Traverses across the blocks of the current Tetromino. Finds the minDistance to bottom.
                for (int[] blocks : blocksOfCurrentTetromino) {
                    distance = 0;
                    minYCoordinate = blocks[1];

                    while (minYCoordinate < gameGrid[0].length) {
                        if (gameGrid[blocks[0]][minYCoordinate] == null || gameGrid[blocks[0]][minYCoordinate].equals(currentTetromino)) {
                            distance++;
                            minYCoordinate++;
                        } else break;
                    }
                    if (minDistance > distance)
                        minDistance = distance;
                }
                //Draws the hologram
                g.setColor(currentTetromino.color.darker());
                g.fillRect(playAreaX1 + tetrominoSize + (currentTetromino.block0[0] * tetrominoSize), playAreaY1 + tetrominoSize + ((currentTetromino.block0[1] - 4) * tetrominoSize) + ((minDistance - 1) * tetrominoSize), tetrominoSize, tetrominoSize);
                g.fillRect(playAreaX1 + tetrominoSize + (currentTetromino.block1[0] * tetrominoSize), playAreaY1 + tetrominoSize + ((currentTetromino.block1[1] - 4) * tetrominoSize) + ((minDistance - 1) * tetrominoSize), tetrominoSize, tetrominoSize);
                g.fillRect(playAreaX1 + tetrominoSize + (currentTetromino.block2[0] * tetrominoSize), playAreaY1 + tetrominoSize + ((currentTetromino.block2[1] - 4) * tetrominoSize) + ((minDistance - 1) * tetrominoSize), tetrominoSize, tetrominoSize);
                g.fillRect(playAreaX1 + tetrominoSize + (currentTetromino.block3[0] * tetrominoSize), playAreaY1 + tetrominoSize + ((currentTetromino.block3[1] - 4) * tetrominoSize) + ((minDistance - 1) * tetrominoSize), tetrominoSize, tetrominoSize);
                g.setColor(Color.BLACK);
                g.fillRect(playAreaX1 + tetrominoSize + (currentTetromino.block0[0] * tetrominoSize) + (2 * tetrominoSize / 20), playAreaY1 + tetrominoSize + ((currentTetromino.block0[1] - 4) * tetrominoSize) + ((minDistance - 1) * tetrominoSize) + (2 * tetrominoSize / 20), tetrominoSize - (4 * tetrominoSize / 20), tetrominoSize - (4 * tetrominoSize / 20));
                g.fillRect(playAreaX1 + tetrominoSize + (currentTetromino.block1[0] * tetrominoSize) + (2 * tetrominoSize / 20), playAreaY1 + tetrominoSize + ((currentTetromino.block1[1] - 4) * tetrominoSize) + ((minDistance - 1) * tetrominoSize) + (2 * tetrominoSize / 20), tetrominoSize - (4 * tetrominoSize / 20), tetrominoSize - (4 * tetrominoSize / 20));
                g.fillRect(playAreaX1 + tetrominoSize + (currentTetromino.block2[0] * tetrominoSize) + (2 * tetrominoSize / 20), playAreaY1 + tetrominoSize + ((currentTetromino.block2[1] - 4) * tetrominoSize) + ((minDistance - 1) * tetrominoSize) + (2 * tetrominoSize / 20), tetrominoSize - (4 * tetrominoSize / 20), tetrominoSize - (4 * tetrominoSize / 20));
                g.fillRect(playAreaX1 + tetrominoSize + (currentTetromino.block3[0] * tetrominoSize) + (2 * tetrominoSize / 20), playAreaY1 + tetrominoSize + ((currentTetromino.block3[1] - 4) * tetrominoSize) + ((minDistance - 1) * tetrominoSize) + (2 * tetrominoSize / 20), tetrominoSize - (4 * tetrominoSize / 20), tetrominoSize - (4 * tetrominoSize / 20));

            }

            //Painting falling and placed Tetrominoes
            for(int i = 0; i < gameGrid.length ; i++){  //i<10
                for (int j = gameGrid[0].length-1; j >= 0 ; j--){  //j<24
                    if(gameGrid[i][j] != null) {
                        g.setColor(gameGrid[i][j].color);
                        g.fillRect(playAreaX1 + tetrominoSize + (i*tetrominoSize) , playAreaY1 + tetrominoSize + ((j-4)*tetrominoSize) , tetrominoSize, tetrominoSize);
                        g.setColor(Color.BLACK);
                        g.drawRect(playAreaX1 + tetrominoSize + (i*tetrominoSize) , playAreaY1 + tetrominoSize + ((j-4)*tetrominoSize) , tetrominoSize, tetrominoSize);
                    }
                }
            }

            //Painting the next area Tetromino
            if (nextTetromino == 'I') {
                int startX = nextAreaX1 + (nextAreaWidth-4*tetrominoSize)/2;
                int startY = nextAreaY1 + (nextAreaHeight-tetrominoSize)/2;
                paintI_Piece(g,startX,startY);
            }
            else if (nextTetromino == 'J') {
                int startX = nextAreaX1 + (nextAreaWidth-3*tetrominoSize)/2;
                int startY = nextAreaY1 + (nextAreaHeight-2*tetrominoSize)/2;
                paintJ_Piece(g,startX,startY);
            }
            else if (nextTetromino == 'L') {
                int startX = nextAreaX1 + (nextAreaWidth-3*tetrominoSize)/2;
                int startY = nextAreaY1 + (nextAreaHeight-2*tetrominoSize)/2;
                paintL_Piece(g,startX,startY);
            }
            else if (nextTetromino == 'O') {
                int startX = nextAreaX1 + (nextAreaWidth-2*tetrominoSize)/2;
                int startY = nextAreaY1 + (nextAreaHeight-2*tetrominoSize)/2;
                paintO_Piece(g,startX,startY);
            }
            else if (nextTetromino == 'S') {
                int startX = nextAreaX1 + (nextAreaWidth-3*tetrominoSize)/2;
                int startY = nextAreaY1 + (nextAreaHeight-2*tetrominoSize)/2;
                paintS_Piece(g,startX,startY);
            }
            else if (nextTetromino == 'T') {
                int startX = nextAreaX1 + (nextAreaWidth-3*tetrominoSize)/2;
                int startY = nextAreaY1 + (nextAreaHeight-2*tetrominoSize)/2;
                paintT_Piece(g,startX,startY);
            }
            else if (nextTetromino == 'Z') {
                int startX = nextAreaX1 + (nextAreaWidth-3*tetrominoSize)/2;
                int startY = nextAreaY1 + (nextAreaHeight-2*tetrominoSize)/2;
                paintZ_Piece(g,startX,startY);
            }

            //Painting the hold area Tetromino
            if (holdTetromino == 'I') {
                int startX = holdAreaX1 + (holdAreaWidth-4*tetrominoSize)/2;
                int startY = holdAreaY1 + (holdAreaHeight-tetrominoSize)/2;
                paintI_Piece(g,startX,startY);
            }
            else if (holdTetromino == 'J') {
                int startX = holdAreaX1 + (holdAreaWidth-3*tetrominoSize)/2;
                int startY = holdAreaY1 + (holdAreaHeight-2*tetrominoSize)/2;
                paintJ_Piece(g,startX,startY);
            }
            else if (holdTetromino == 'L') {
                int startX = holdAreaX1 + (holdAreaWidth-3*tetrominoSize)/2;
                int startY = holdAreaY1 + (holdAreaHeight-2*tetrominoSize)/2;
                paintL_Piece(g,startX,startY);
            }
            else if (holdTetromino == 'O') {
                int startX = holdAreaX1 + (holdAreaWidth-2*tetrominoSize)/2;
                int startY = holdAreaY1 + (holdAreaHeight-2*tetrominoSize)/2;
                paintO_Piece(g,startX,startY);
            }
            else if (holdTetromino == 'S') {
                int startX = holdAreaX1 + (holdAreaWidth-3*tetrominoSize)/2;
                int startY = holdAreaY1 + (holdAreaHeight-2*tetrominoSize)/2;
                paintS_Piece(g,startX,startY);
            }
            else if (holdTetromino == 'T') {
                int startX = holdAreaX1 + (holdAreaWidth-3*tetrominoSize)/2;
                int startY = holdAreaY1 + (holdAreaHeight-2*tetrominoSize)/2;
                paintT_Piece(g,startX,startY);
            }
            else if (holdTetromino == 'Z') {
                int startX = holdAreaX1 + (holdAreaWidth-3*tetrominoSize)/2;
                int startY = holdAreaY1 + (holdAreaHeight-2*tetrominoSize)/2;
                paintZ_Piece(g,startX,startY);
            }
        }

        /* Helps To Paint Tetrominoes In The Next And Hold Area */
        //Gets x And y Coordinates As Parameters And Paints The Corresponded Piece
        public void paintI_Piece(Graphics g, int startX, int startY){
            //Painting the blocks
            g.setColor(I_PieceColor);
            g.fillRect(startX,startY,tetrominoSize,tetrominoSize);
            g.fillRect(startX+(tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.fillRect(startX+(2*tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.fillRect(startX+(3*tetrominoSize),startY,tetrominoSize,tetrominoSize);

            //Drawing the borders of the blocks
            g.setColor(Color.BLACK);
            g.drawRect(startX,startY,tetrominoSize,tetrominoSize);
            g.drawRect(startX+(tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.drawRect(startX+(2*tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.drawRect(startX+(3*tetrominoSize),startY,tetrominoSize,tetrominoSize);
        }
        public void paintJ_Piece (Graphics g, int startX, int startY){
            g.setColor(J_PieceColor);
            g.fillRect(startX,startY,tetrominoSize,tetrominoSize);
            g.fillRect(startX+(tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.fillRect(startX+(2*tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.fillRect(startX+(2*tetrominoSize),startY+(tetrominoSize),tetrominoSize,tetrominoSize);

            g.setColor(Color.BLACK);
            g.drawRect(startX,startY,tetrominoSize,tetrominoSize);
            g.drawRect(startX+(tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.drawRect(startX+(2*tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.drawRect(startX+(2*tetrominoSize),startY+(tetrominoSize),tetrominoSize,tetrominoSize);
        }
        public void paintL_Piece(Graphics g, int startX, int startY){
            g.setColor(L_PieceColor);
            g.fillRect(startX,startY+(tetrominoSize),tetrominoSize,tetrominoSize);
            g.fillRect(startX,startY,tetrominoSize,tetrominoSize);
            g.fillRect(startX+(tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.fillRect(startX+(2*tetrominoSize),startY,tetrominoSize,tetrominoSize);

            g.setColor(Color.BLACK);
            g.drawRect(startX,startY+(tetrominoSize),tetrominoSize,tetrominoSize);
            g.drawRect(startX,startY,tetrominoSize,tetrominoSize);
            g.drawRect(startX+(tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.drawRect(startX+(2*tetrominoSize),startY,tetrominoSize,tetrominoSize);
        }
        public void paintO_Piece(Graphics g, int startX, int startY){
            g.setColor(O_PieceColor);
            g.fillRect(startX,startY+(tetrominoSize),tetrominoSize,tetrominoSize);
            g.fillRect(startX+(tetrominoSize),startY+(tetrominoSize),tetrominoSize,tetrominoSize);
            g.fillRect(startX+(tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.fillRect(startX,startY,tetrominoSize,tetrominoSize);

            g.setColor(Color.BLACK);
            g.drawRect(startX,startY+(tetrominoSize),tetrominoSize,tetrominoSize);
            g.drawRect(startX+(tetrominoSize),startY+(tetrominoSize),tetrominoSize,tetrominoSize);
            g.drawRect(startX+(tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.drawRect(startX,startY,tetrominoSize,tetrominoSize);
        }
        public void paintS_Piece(Graphics g, int startX, int startY){
            g.setColor(S_PieceColor);
            g.fillRect(startX,startY+(tetrominoSize),tetrominoSize,tetrominoSize);
            g.fillRect(startX+(tetrominoSize),startY+(tetrominoSize),tetrominoSize,tetrominoSize);
            g.fillRect(startX+(tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.fillRect(startX+(2*tetrominoSize),startY,tetrominoSize,tetrominoSize);

            g.setColor(Color.BLACK);
            g.drawRect(startX,startY+(tetrominoSize),tetrominoSize,tetrominoSize);
            g.drawRect(startX+(tetrominoSize),startY+(tetrominoSize),tetrominoSize,tetrominoSize);
            g.drawRect(startX+(tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.drawRect(startX+(2*tetrominoSize),startY,tetrominoSize,tetrominoSize);
        }
        public void paintT_Piece(Graphics g, int startX, int startY){
            g.setColor(T_PieceColor);
            g.fillRect(startX,startY,tetrominoSize,tetrominoSize);
            g.fillRect(startX+(tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.fillRect(startX+(2*tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.fillRect(startX+(tetrominoSize),startY+(tetrominoSize),tetrominoSize,tetrominoSize);

            g.setColor(Color.BLACK);
            g.drawRect(startX,startY,tetrominoSize,tetrominoSize);
            g.drawRect(startX+(tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.drawRect(startX+(2*tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.drawRect(startX+(tetrominoSize),startY+(tetrominoSize),tetrominoSize,tetrominoSize);
        }
        public void paintZ_Piece(Graphics g, int startX, int startY){
            g.setColor(Z_PieceColor);
            g.fillRect(startX+(2*tetrominoSize),startY+(tetrominoSize),tetrominoSize,tetrominoSize);
            g.fillRect(startX+(tetrominoSize),startY+(tetrominoSize),tetrominoSize,tetrominoSize);
            g.fillRect(startX+(tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.fillRect(startX,startY,tetrominoSize,tetrominoSize);

            g.setColor(Color.BLACK);
            g.drawRect(startX+(2*tetrominoSize),startY+(tetrominoSize),tetrominoSize,tetrominoSize);
            g.drawRect(startX+(tetrominoSize),startY+(tetrominoSize),tetrominoSize,tetrominoSize);
            g.drawRect(startX+(tetrominoSize),startY,tetrominoSize,tetrominoSize);
            g.drawRect(startX,startY,tetrominoSize,tetrominoSize);
        }

        /* Checks The Keyboard Input */
        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if(!gameOver && !gamePaused) {

                //Move left input (Left Arrow Key By Default)
                if (key == KeyEvent.VK_LEFT) currentTetromino.moveLeft();

                //Move right input (Right Arrow Key By Default)
                if (key == KeyEvent.VK_RIGHT) currentTetromino.moveRight();

                //Move fast input (Down Arrow Key By Default)
                if (key == KeyEvent.VK_DOWN) {
                    //Shortens the wait time of the current Tetromino if there is nothing under
                    if(!checkUnder(currentTetromino)) {
                        currentTetromino.interrupt();
                        gameSpeed = 0.03;
                    }
                }

                //Rotate left input (Z Letter Key By Default)
                if (key == KeyEvent.VK_Z) currentTetromino.rotateLeft();

                //Rotate right input (X Letter Key or Up Arrow Key By Default)
                if (key == KeyEvent.VK_UP || key == KeyEvent.VK_X) currentTetromino.rotateRight();
            }
        }
        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();

            //Pause the game input (ESCAPE button by default)
            if (!gameOver && key == KeyEvent.VK_ESCAPE) {
                if (!gamePaused) {
                    gamePanel.add(gamePanel.gamePausedLabel);
                    gamePaused = true;
                } else {
                    gamePanel.remove(gamePanel.gamePausedLabel);
                    gamePaused = false;
                }
                gameSpeed = Math.pow((0.8 - ((level) * 0.007)), level);
            }

            if (!gameOver && !gamePaused) {

                //Returns the gameSpeed to default value
                if (key == KeyEvent.VK_DOWN) {
                    gameSpeed = Math.pow((0.8 - ((level) * 0.007)), level);
                }

                if (!gameMode.equals("classic")) {

                    //Hold input (C Letter Key By Default)
                    if (key == KeyEvent.VK_C && !holdUsed) {

                        //If it is the first hold input
                        if (holdTetromino == 'N') {
                            holdTetromino = currentTetromino.type;
                            if (deleteTetromino(currentTetromino))
                                generateRandomTetromino();
                        }
                        //If there is already a hold Tetromino
                        else {
                            char temp = holdTetromino;
                            holdTetromino = currentTetromino.type;

                            if (deleteTetromino(currentTetromino))
                                currentTetromino = generateTetromino(temp);
                        }
                        holdUsed = true;  //Sets holdUsed to true to prevent using hold operation again
                    }

                    //Hard Drop Input (SPACE Key By Default)
                    if (key == KeyEvent.VK_SPACE) {
                        currentTetromino.placed = true;

                        //Moves until it reaches to bottom
                        while (!checkUnder(currentTetromino)) {
                            currentTetromino.clearTetromino();
                            currentTetromino.block0[1]++;
                            currentTetromino.block1[1]++;
                            currentTetromino.block2[1]++;
                            currentTetromino.block3[1]++;
                            currentTetromino.addTetromino();
                        }
                        currentTetromino.interrupt();
                        currentTetromino.addTetromino();
                    }
                }
            }

            if (gameOver){

                if (key == KeyEvent.VK_ENTER) {
                    score = 0;
                    level = 0;
                    lines = 0;
                    gameSpeed = Math.pow( (0.8-((level)*0.007)) , level );
                    tetrominoes = new ArrayDeque<>();
                    generateRandomTetromino();
                    currentTetromino.addTetromino();
                    holdTetromino = 'N';
                    gameGrid = new Tetromino[10][24];
                    gamePanel.remove(gameOverLabel);
                    gamePanel.remove(pressEnterLabel);
                    updateLabels();
                    gameOver = false;
                    currentTetromino.addTetromino();
                }
            }
        }
        @Override
        public void keyTyped(KeyEvent e) {
        }
    }

    /*Tetrominoes*/
    //Parent Tetromino Class
    abstract class Tetromino extends Thread{

        //Block components of the tetromino. Each block contains its coordinate. First index is x coordinate, Second index is y coordinate.
        int[] block0 = new int[2];
        int[] block1 = new int[2];
        int[] block2 = new int[2];
        int[] block3 = new int[2];

        Color color;
        char type;    //Stores the type of the tetromino. For example: 'T' , 'Z'
        int rotation;   //Stores the current rotation of the tetromino
        boolean rotated = false;   //Checks if it is rotated.
        boolean placed = false;   //Checks if it is placed
        boolean alive = true;   //Checks if it is deleted

        //Moves The Tetromino One Block Down In Every Time Period
        public void run(){

            //Moves until it reaches the bottom(or top of another Tetromino).
            while (!placed && alive) {
                try {
                    //Waits gameSpeed amount of time.
                    if (!checkUnder(this)) {
                        Thread.sleep((long) (gameSpeed * 1000));

                        if (!checkUnder(this) && !gamePaused) {
                            clearTetromino();
                            block0[1]++;
                            block1[1]++;
                            block2[1]++;
                            block3[1]++;
                            addTetromino();
                        }
                    }
                    //Gives time to move when reached bottom
                    else {
                        Thread.sleep((long) (0.5 * 1000));

                        if (!checkUnder(this)  && !gamePaused ) {
                            clearTetromino();
                            block0[1]++;
                            block1[1]++;
                            block2[1]++;
                            block3[1]++;
                            addTetromino();
                        }
                        else{
                            while(gamePaused){/* Just waiting lol */}
                            placed = true;
                        }
                    }

                } catch (InterruptedException e) {
                    //throw new RuntimeException(e);
                }
            }

            if(alive) {
                checkStrike();   //Checks if it is a strike

                //Generates the next Tetromino if the game is still continuing
                if (!gameOver)
                    generateRandomTetromino();

                //Checks for GameOver
                for (int i = 0; i < 10; i++) {
                    if (gameGrid[i][4] != null && gameGrid[i][4].equals(this)) {
                        System.out.println("GAME OVER");
                        gameOver = true;
                        gamePanel.add(gamePanel.gameOverLabel);
                        gamePanel.add(gamePanel.pressEnterLabel);
                        break;
                    }
                }
            }
        }

        //Checks If The Tetromino Can Move To Left Or Right
        //It Takes "moveLeft" Or "moveRight" As parameter
        public boolean canMove(String direction){

            //Checks if it can move to the left
            if(direction.equals("moveLeft")) {

                //Checks the borders of the playArea
                if (block0[0] != 0 && block1[0] != 0 && block2[0] != 0 && block3[0] != 0)

                    //Checks if there is a Tetromino on the left side
                    if(isCoordinateEmpty(block0[0] -1 , block0[1] , this) && (isCoordinateEmpty(block1[0] -1 , block1[1] , this)))
                        if(isCoordinateEmpty(block2[0] -1 , block2[1] , this) && (isCoordinateEmpty(block3[0] -1 , block3[1] , this)))
                            return true;
            }

            //Checks if it can move to the right
            if(direction.equals("moveRight")) {

                //Checks the borders of the playArea
                if (block0[0] != 9 && block1[0] != 9 && block2[0] != 9 && block3[0] != 9)

                    //Checks if there is a Tetromino on the right side
                    if(isCoordinateEmpty(block0[0] +1 , block0[1] , this) && (isCoordinateEmpty(block1[0] +1 , block1[1] , this)))
                        if(isCoordinateEmpty(block2[0] +1 , block2[1] , this) && (isCoordinateEmpty(block3[0] +1 , block3[1] , this)))
                            return true;
            }
            return false;
        }

        //Rotates Left
        public void rotateLeft(){}

        //Rotates Right
        public void rotateRight(){}

        //Moves The Tetromino One Block Left
        public void moveLeft(){
            if(canMove("moveLeft")) {
                clearTetromino();  //Clears the current position from gameGrid
                //Updates the coordinates
                block0[0]--;
                block1[0]--;
                block2[0]--;
                block3[0]--;
                addTetromino();   //Adds the Tetromino to gameGrid
            }
        }

        //Moves The Tetromino One Block Right
        public void moveRight(){
            if(canMove("moveRight")) {
                //Clears the old one
                clearTetromino();   //Clears the current position from gameGrid
                //Updates the coordinates
                block0[0]++;
                block1[0]++;
                block2[0]++;
                block3[0]++;
                addTetromino();   //Adds the Tetromino to gameGrid
            }
        }

        //Clears The Tetromino From The Grid
        public void clearTetromino(){
            gameGrid[block0[0]][block0[1]] = null;
            gameGrid[block1[0]][block1[1]] = null;
            gameGrid[block2[0]][block2[1]] = null;
            gameGrid[block3[0]][block3[1]] = null;
        }

        //Updates The Tetromino's Coordinate In The Grid
        public void addTetromino(){
            gameGrid[block0[0]][block0[1]] = this;
            gameGrid[block1[0]][block1[1]] = this;
            gameGrid[block2[0]][block2[1]] = this;
            gameGrid[block3[0]][block3[1]] = this;
        }
    }

    class I_Piece extends Tetromino{

        public I_Piece(){
            type = 'I';
            rotation = 0;
            color = I_PieceColor;

            block0[0] = 3;
            block0[1] = 4;

            block1[0] = 4;
            block1[1] = 4;

            block2[0] = 5;
            block2[1] = 4;

            block3[0] = 6;
            block3[1] = 4;
        }

        public void rotateLeft(){
            clearTetromino();

            if (rotation == 0) {
                if (isCoordinateEmpty(block0[0] + 2, block0[1] + 1, this)) {
                    if (isCoordinateEmpty(block1[0] + 1, block1[1], this)) {
                        if (isCoordinateEmpty(block2[0], block2[1] - 1, this)) {
                            if (isCoordinateEmpty(block3[0] - 1, block3[1] - 2, this)) {
                                block0[0] = block0[0] + 2;
                                block0[1] = block0[1] + 1;

                                block1[0] = block1[0] + 1;

                                block2[1] = block2[1] - 1;

                                block3[0] = block3[0] - 1;
                                block3[1] = block3[1] - 2;

                                rotated = true;
                            }
                        }
                    }
                }
            }

            if (rotation == 1) {
                if (isCoordinateEmpty(block0[0] - 2, block0[1] - 1, this)) {
                    if (isCoordinateEmpty(block1[0] - 1, block1[1], this)) {
                        if (isCoordinateEmpty(block2[0], block2[1] + 1, this)) {
                            if (isCoordinateEmpty(block3[0] + 1, block3[1] + 2, this)) {
                                block0[0] = block0[0] - 2;
                                block0[1] = block0[1] - 1;

                                block1[0] = block1[0] - 1;


                                block2[1] = block2[1] + 1;

                                block3[0] = block3[0] + 1;
                                block3[1] = block3[1] + 2;

                                rotated = true;
                            }
                        }
                    }
                }
            }

            if (rotated) {
                rotation = (rotation + 1) % 2;
                rotated = false;
            }
            addTetromino();
        }

        public void rotateRight() {
            rotateLeft();
        }
    }

    class J_Piece extends Tetromino{

        public J_Piece(){
            type = 'J';
            rotation = 0;
            color = J_PieceColor;

            block0[0] = 6;
            block0[1] = 5;

            block1[0] = 6;
            block1[1] = 4;

            block2[0] = 5;
            block2[1] = 4;

            block3[0] = 4;
            block3[1] = 4;
        }

        public void rotateLeft(){
            clearTetromino();

            if (rotation == 0) {
                if (isCoordinateEmpty(block0[0], block0[1] - 2, this)) {
                    if (isCoordinateEmpty(block1[0] - 1, block1[1] - 1, this)) {
                        if (isCoordinateEmpty(block3[0] + 1, block3[1] + 1, this)) {

                            block0[1] = block0[1] - 2;

                            block1[0] = block1[0] - 1;
                            block1[1] = block1[1] - 1;

                            block3[0] = block3[0] + 1;
                            block3[1] = block3[1] + 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 1) {
                if (isCoordinateEmpty(block0[0] + 2, block0[1], this)) {
                    if (isCoordinateEmpty(block1[0] + 1, block1[1] - 1, this)) {
                        if (isCoordinateEmpty(block3[0] - 1, block3[1] + 1, this)) {

                            block0[0] = block0[0] + 2;

                            block1[0] = block1[0] + 1;
                            block1[1] = block1[1] - 1;

                            block3[0] = block3[0] - 1;
                            block3[1] = block3[1] + 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 2) {
                if (isCoordinateEmpty(block0[0], block0[1] + 2, this)) {
                    if (isCoordinateEmpty(block1[0] + 1, block1[1] + 1, this)) {
                        if (isCoordinateEmpty(block3[0] - 1, block3[1] - 1, this)) {

                            block0[1] = block0[1] + 2;

                            block1[0] = block1[0] + 1;
                            block1[1] = block1[1] + 1;

                            block3[0] = block3[0] - 1;
                            block3[1] = block3[1] - 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 3) {
                if (isCoordinateEmpty(block0[0] - 2, block0[1], this)) {
                    if (isCoordinateEmpty(block1[0] - 1, block1[1] + 1, this)) {
                        if (isCoordinateEmpty(block3[0] + 1, block3[1] - 1, this)) {

                            block0[0] = block0[0] - 2;

                            block1[0] = block1[0] - 1;
                            block1[1] = block1[1] + 1;

                            block3[0] = block3[0] + 1;
                            block3[1] = block3[1] - 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotated) {
                rotation = (rotation - 1) % 4;
                if (rotation == -1) rotation = 3;
                rotated = false;
            }
            addTetromino();

        }

        public void rotateRight(){
            clearTetromino();

            if (rotation == 0) {
                if (isCoordinateEmpty(block0[0] - 2, block0[1], this)) {
                    if (isCoordinateEmpty(block1[0] - 1, block1[1] + 1, this)) {
                        if (isCoordinateEmpty(block3[0] + 1, block3[1] - 1, this)) {

                            block0[0] = block0[0] - 2;

                            block1[0] = block1[0] - 1;
                            block1[1] = block1[1] + 1;

                            block3[0] = block3[0] + 1;
                            block3[1] = block3[1] - 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 1) {

                if (isCoordinateEmpty(block0[0], block0[1] - 2, this)) {
                    if (isCoordinateEmpty(block1[0] - 1, block1[1] - 1, this)) {
                        if (isCoordinateEmpty(block3[0] + 1, block3[1] + 1, this)) {

                            block0[1] = block0[1] - 2;

                            block1[0] = block1[0] - 1;
                            block1[1] = block1[1] - 1;

                            block3[0] = block3[0] + 1;
                            block3[1] = block3[1] + 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 2) {
                if (isCoordinateEmpty(block0[0] + 2, block0[1], this)) {
                    if (isCoordinateEmpty(block1[0] + 1, block1[1] - 1, this)) {
                        if (isCoordinateEmpty(block3[0] - 1, block3[1] + 1, this)) {

                            block0[0] = block0[0] + 2;

                            block1[0] = block1[0] + 1;
                            block1[1] = block1[1] - 1;

                            block3[0] = block3[0] - 1;
                            block3[1] = block3[1] + 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 3) {
                if (isCoordinateEmpty(block0[0], block0[1] + 2, this)) {
                    if (isCoordinateEmpty(block1[0] + 1, block1[1] + 1, this)) {
                        if (isCoordinateEmpty(block3[0] - 1, block3[1] - 1, this)) {

                            block0[1] = block0[1] + 2;

                            block1[0] = block1[0] + 1;
                            block1[1] = block1[1] + 1;

                            block3[0] = block3[0] - 1;
                            block3[1] = block3[1] - 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotated) {
                rotation = (rotation + 1) % 4;
                rotated = false;
            }
            addTetromino();

        }
    }

    class L_Piece extends Tetromino{

        public L_Piece(){
            type = 'L';
            rotation = 0;
            color = L_PieceColor;

            block0[0] = 4;
            block0[1] = 5;

            block1[0] = 4;
            block1[1] = 4;

            block2[0] = 5;
            block2[1] = 4;

            block3[0] = 6;
            block3[1] = 4;
        }

        public void rotateLeft(){
            clearTetromino();

            if (rotation == 0) {
                if (isCoordinateEmpty(block0[0] + 2, block0[1], this)) {
                    if (isCoordinateEmpty(block1[0] + 1, block1[1] + 1, this)) {
                        if (isCoordinateEmpty(block3[0] - 1, block3[1] - 1, this)) {

                            block0[0] = block0[0] + 2;

                            block1[0] = block1[0] + 1;
                            block1[1] = block1[1] + 1;

                            block3[0] = block3[0] - 1;
                            block3[1] = block3[1] - 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 1) {

                if (isCoordinateEmpty(block0[0], block0[1] + 2, this)) {
                    if (isCoordinateEmpty(block1[0] - 1, block1[1] + 1, this)) {
                        if (isCoordinateEmpty(block3[0] + 1, block3[1] - 1, this)) {

                            block0[1] = block0[1] + 2;

                            block1[0] = block1[0] - 1;
                            block1[1] = block1[1] + 1;

                            block3[0] = block3[0] + 1;
                            block3[1] = block3[1] - 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 2) {
                if (isCoordinateEmpty(block0[0] - 2, block0[1], this)) {
                    if (isCoordinateEmpty(block1[0] - 1, block1[1] - 1, this)) {
                        if (isCoordinateEmpty(block3[0] + 1, block3[1] + 1, this)) {

                            block0[0] = block0[0] - 2;

                            block1[0] = block1[0] - 1;
                            block1[1] = block1[1] - 1;

                            block3[0] = block3[0] + 1;
                            block3[1] = block3[1] + 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 3) {
                if (isCoordinateEmpty(block0[0], block0[1] - 2, this)) {
                    if (isCoordinateEmpty(block1[0] + 1, block1[1] - 1, this)) {
                        if (isCoordinateEmpty(block3[0] - 1, block3[1] + 1, this)) {

                            block0[1] = block0[1] - 2;

                            block1[0] = block1[0] + 1;
                            block1[1] = block1[1] - 1;

                            block3[0] = block3[0] - 1;
                            block3[1] = block3[1] + 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotated) {
                rotation = (rotation - 1) % 4;

                if (rotation == -1) rotation = 3;

                rotated = false;
            }
            addTetromino();

    }

        public void rotateRight() {
            clearTetromino();

            if (rotation == 0) {

                if (isCoordinateEmpty(block0[0], block0[1] - 2, this)) {
                    if (isCoordinateEmpty(block1[0] + 1, block1[1] - 1, this)) {
                        if (isCoordinateEmpty(block3[0] - 1, block3[1] + 1, this)) {

                            block0[1] = block0[1] - 2;

                            block1[0] = block1[0] + 1;
                            block1[1] = block1[1] - 1;

                            block3[0] = block3[0] - 1;
                            block3[1] = block3[1] + 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 1) {

                if (isCoordinateEmpty(block0[0] + 2, block0[1], this)) {
                    if (isCoordinateEmpty(block1[0] + 1, block1[1] + 1, this)) {
                        if (isCoordinateEmpty(block3[0] - 1, block3[1] - 1, this)) {

                            block0[0] = block0[0] + 2;

                            block1[0] = block1[0] + 1;
                            block1[1] = block1[1] + 1;

                            block3[0] = block3[0] - 1;
                            block3[1] = block3[1] - 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 2) {

                if (isCoordinateEmpty(block0[0], block0[1] + 2, this)) {
                    if (isCoordinateEmpty(block1[0] - 1, block1[1] + 1, this)) {
                        if (isCoordinateEmpty(block3[0] + 1, block3[1] - 1, this)) {

                            block0[1] = block0[1] + 2;

                            block1[0] = block1[0] - 1;
                            block1[1] = block1[1] + 1;

                            block3[0] = block3[0] + 1;
                            block3[1] = block3[1] - 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 3) {

                if (isCoordinateEmpty(block0[0] - 2, block0[1], this)) {
                    if (isCoordinateEmpty(block1[0] - 1, block1[1] - 1, this)) {
                        if (isCoordinateEmpty(block3[0] + 1, block3[1] + 1, this)) {

                            block0[0] = block0[0] - 2;

                            block1[0] = block1[0] - 1;
                            block1[1] = block1[1] - 1;

                            block3[0] = block3[0] + 1;
                            block3[1] = block3[1] + 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotated) {
                rotation = (rotation + 1) % 4;
                rotated = false;
            }
            addTetromino();

        }

    }

    class O_Piece extends Tetromino{

        public O_Piece(){
            type = 'O';
            color = O_PieceColor;

            block0[0] = 4;
            block0[1] = 5;

            block1[0] = 5;
            block1[1] = 5;

            block2[0] = 5;
            block2[1] = 4;

            block3[0] = 4;
            block3[1] = 4;
        }

    }

    class S_Piece extends Tetromino{

        public S_Piece(){
            type = 'S';
            rotation = 0;
            color = S_PieceColor;

            block0[0] = 4;
            block0[1] = 5;

            block1[0] = 5;
            block1[1] = 5;

            block2[0] = 5;
            block2[1] = 4;

            block3[0] = 6;
            block3[1] = 4;
        }

        public void rotateLeft(){

            clearTetromino();

            if (rotation == 0) {
                if (isCoordinateEmpty(block0[0] + 2, block0[1], this)) {
                    if (isCoordinateEmpty(block1[0] + 1, block1[1] - 1, this)) {
                        if (isCoordinateEmpty(block3[0] - 1, block3[1] - 1, this)) {

                            block0[0] = block0[0] + 2;

                            block1[0] = block1[0] + 1;
                            block1[1] = block1[1] - 1;

                            block3[0] = block3[0] - 1;
                            block3[1] = block3[1] - 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 1) {
                if (isCoordinateEmpty(block0[0] - 2, block0[1], this)) {
                    if (isCoordinateEmpty(block1[0] - 1, block1[1] + 1, this)) {
                        if (isCoordinateEmpty(block3[0] + 1, block3[1] + 1, this)) {

                            block0[0] = block0[0] - 2;

                            block1[0] = block1[0] - 1;
                            block1[1] = block1[1] + 1;

                            block3[0] = block3[0] + 1;
                            block3[1] = block3[1] + 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotated) {
                rotation = (rotation + 1) % 2;
                rotated = false;
            }
            addTetromino();
        }

        public void rotateRight() {
            rotateLeft();
        }

    }

    class T_Piece extends Tetromino{

        public T_Piece(){
            type = 'T';
            rotation = 0;
            color = T_PieceColor;

            block0[0] = 4;
            block0[1] = 4;

            block1[0] = 5;
            block1[1] = 4;

            block2[0] = 6;
            block2[1] = 4;

            block3[0] = 5;
            block3[1] = 5;
        }

        public void rotateLeft(){
            clearTetromino();

            if (rotation == 0) {
                if (isCoordinateEmpty(block0[0] + 1, block0[1] + 1, this)) {
                    if (isCoordinateEmpty(block2[0] - 1, block2[1] - 1, this)) {
                        if (isCoordinateEmpty(block3[0] + 1, block3[1] - 1, this)) {

                            block0[0] = block0[0] + 1;
                            block0[1] = block0[1] + 1;

                            block2[0] = block2[0] - 1;
                            block2[1] = block2[1] - 1;

                            block3[0] = block3[0] + 1;
                            block3[1] = block3[1] - 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 1) {
                if (isCoordinateEmpty(block0[0] - 1, block0[1] + 1, this)) {
                    if (isCoordinateEmpty(block2[0] + 1, block2[1] - 1, this)) {
                        if (isCoordinateEmpty(block3[0] + 1, block3[1] + 1, this)) {

                            block0[0] = block0[0] - 1;
                            block0[1] = block0[1] + 1;

                            block2[0] = block2[0] + 1;
                            block2[1] = block2[1] - 1;

                            block3[0] = block3[0] + 1;
                            block3[1] = block3[1] + 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 2) {
                if (isCoordinateEmpty(block0[0] - 1, block0[1] - 1, this)) {
                    if (isCoordinateEmpty(block2[0] + 1, block2[1] + 1, this)) {
                        if (isCoordinateEmpty(block3[0] - 1, block3[1] + 1, this)) {

                            block0[0] = block0[0] - 1;
                            block0[1] = block0[1] - 1;

                            block2[0] = block2[0] + 1;
                            block2[1] = block2[1] + 1;

                            block3[0] = block3[0] - 1;
                            block3[1] = block3[1] + 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 3) {
                if (isCoordinateEmpty(block0[0] + 1, block0[1] - 1, this)) {
                    if (isCoordinateEmpty(block2[0] - 1, block2[1] + 1, this)) {
                        if (isCoordinateEmpty(block3[0] - 1, block3[1] - 1, this)) {

                            block0[0] = block0[0] + 1;
                            block0[1] = block0[1] - 1;

                            block2[0] = block2[0] - 1;
                            block2[1] = block2[1] + 1;

                            block3[0] = block3[0] - 1;
                            block3[1] = block3[1] - 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotated) {
                rotation = (rotation - 1) % 4;
                if (rotation == -1) rotation = 3;
                rotated = false;
            }
            addTetromino();
        }

        public void rotateRight(){
            clearTetromino();

            if (rotation == 0) {

                if (isCoordinateEmpty(block0[0] + 1, block0[1] - 1, this)) {
                    if (isCoordinateEmpty(block2[0] - 1, block2[1] + 1, this)) {
                        if (isCoordinateEmpty(block3[0] - 1, block3[1] - 1, this)) {

                            block0[0] = block0[0] + 1;
                            block0[1] = block0[1] - 1;

                            block2[0] = block2[0] - 1;
                            block2[1] = block2[1] + 1;

                            block3[0] = block3[0] - 1;
                            block3[1] = block3[1] - 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 1) {

                if (isCoordinateEmpty(block0[0] + 1, block0[1] + 1, this)) {
                    if (isCoordinateEmpty(block2[0] - 1, block2[1] - 1, this)) {
                        if (isCoordinateEmpty(block3[0] + 1, block3[1] - 1, this)) {

                            block0[0] = block0[0] + 1;
                            block0[1] = block0[1] + 1;

                            block2[0] = block2[0] - 1;
                            block2[1] = block2[1] - 1;

                            block3[0] = block3[0] + 1;
                            block3[1] = block3[1] - 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 2) {

                if (isCoordinateEmpty(block0[0] - 1, block0[1] + 1, this)) {
                    if (isCoordinateEmpty(block2[0] + 1, block2[1] - 1, this)) {
                        if (isCoordinateEmpty(block3[0] + 1, block3[1] + 1, this)) {

                            block0[0] = block0[0] - 1;
                            block0[1] = block0[1] + 1;

                            block2[0] = block2[0] + 1;
                            block2[1] = block2[1] - 1;

                            block3[0] = block3[0] + 1;
                            block3[1] = block3[1] + 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 3) {

                if (isCoordinateEmpty(block0[0] - 1, block0[1] - 1, this)) {
                    if (isCoordinateEmpty(block2[0] + 1, block2[1] + 1, this)) {
                        if (isCoordinateEmpty(block3[0] - 1, block3[1] + 1, this)) {

                            block0[0] = block0[0] - 1;
                            block0[1] = block0[1] - 1;

                            block2[0] = block2[0] + 1;
                            block2[1] = block2[1] + 1;

                            block3[0] = block3[0] - 1;
                            block3[1] = block3[1] + 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotated) {
                rotation = (rotation + 1) % 4;
                rotated = false;
            }
            addTetromino();
        }

    }

    class Z_Piece extends Tetromino{

        public Z_Piece(){
            type = 'Z';
            rotation = 0;
            color = Z_PieceColor;

            block0[0] = 6;
            block0[1] = 5;

            block1[0] = 5;
            block1[1] = 5;

            block2[0] = 5;
            block2[1] = 4;

            block3[0] = 4;
            block3[1] = 4;
        }

        public void rotateLeft(){
            clearTetromino();

            if (rotation == 0) {
                if (isCoordinateEmpty(block0[0], block0[1] - 2, this)) {
                    if (isCoordinateEmpty(block1[0] + 1, block1[1] - 1, this)) {
                        if (isCoordinateEmpty(block3[0] + 1, block3[1] + 1, this)) {

                            block0[1] = block0[1] - 2;

                            block1[0] = block1[0] + 1;
                            block1[1] = block1[1] - 1;

                            block3[0] = block3[0] + 1;
                            block3[1] = block3[1] + 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotation == 1) {
                if (isCoordinateEmpty(block0[0], block0[1] + 2, this)) {
                    if (isCoordinateEmpty(block1[0] - 1, block1[1] + 1, this)) {
                        if (isCoordinateEmpty(block3[0] - 1, block3[1] - 1, this)) {

                            block0[1] = block0[1] + 2;

                            block1[0] = block1[0] - 1;
                            block1[1] = block1[1] + 1;

                            block3[0] = block3[0] - 1;
                            block3[1] = block3[1] - 1;

                            rotated = true;
                        }
                    }
                }
            }

            if (rotated) {
                rotation = (rotation + 1) % 2;
                rotated = false;
            }
            addTetromino();
        }

        public void rotateRight() {
            rotateLeft();
        }

    }

    //Checks If The Coordinate In The "(x,y)" Is Empty. Takes Tetromino As A Parameter To Prevent Seeing Itself As A Block
    public boolean isCoordinateEmpty(int x, int y, Tetromino tetromino) {
        //Checks if it is in the borders
        if(x<0 || x>9 || y<0 || y>23)
            return false;

        //Returns false if there is another Tetromino in the specified coordinate.
        return !(gameGrid[x][y] != null && !gameGrid[x][y].equals(tetromino));
    }

    //Checks If The Tetromino Has Anything Under.
    public boolean checkUnder(Tetromino tetromino){

        //Checks the borders
        if(tetromino.block0[1]+1 == 24 || tetromino.block1[1]+1 == 24 || tetromino.block2[1]+1 == 24 || tetromino.block3[1]+1 == 24){
            return true;
        }

        //One unit below of the blocks is checked
        if(!isCoordinateEmpty(tetromino.block0[0],tetromino.block0[1]+1,tetromino)) {
            return true;
        }
        else if(!isCoordinateEmpty(tetromino.block1[0],tetromino.block1[1]+1,tetromino)) {
            return true;
        }
        else if(!isCoordinateEmpty(tetromino.block2[0],tetromino.block2[1]+1,tetromino)) {
            return true;
        }
        else if(!isCoordinateEmpty(tetromino.block3[0],tetromino.block3[1]+1,tetromino)) {
            return true;
        }
        return false;
    }

    //Checks If There Is Any Strike. If Yes Then Calls removeLine To Remove It
    public void checkStrike(){

        //strikes = new ArrayList<>();

        int i=gameGrid[0].length-1;

        //Moving from bottom to head
        while(i>=0){

            //Traverses across the i-th line
            for(int j = 0 ; j < gameGrid.length ; j++){
                if(gameGrid[j][i] == null) {
                    break;
                }
                else{
                    //Line is full
                    if(j==9) {
                        if(!strikes.contains(i)) strikes.add(i);
                    }
                }
            }
            i--;
        }

        if(!strikes.isEmpty()){
            int strikeCount = 1;
            int removedCount = 0;

            for(int k = 0; k < strikes.size()-1 ; k ++){

                if(strikes.get(k) == strikes.get(k+1)+1) {
                    strikeCount++;
                }else{
                    score += calculateScore(strikeCount);
                    strikeCount = 1;
                }
                removeLine(strikes.get(k)+removedCount);
                removedCount++;
            }
            score += calculateScore(strikeCount);
            removeLine(strikes.get(strikes.size()-1)+removedCount);

            gamePanel.updateLabels();

            System.out.println("lines: " + lines);
            System.out.println("level: " + level);
            System.out.println("score: " + score);
            System.out.println("---------------");

            strikes = new ArrayList<>();
        }
    }

    //Removes The lineNumber th Line And Moves Everything Above One Block Down
    public void removeLine(int lineNumber){

        for(int i = 0 ; i < gameGrid.length ; i++){
            gameGrid[i][lineNumber] = null;

            for(int j = lineNumber; j > 0 ; j--) {
                gameGrid[i][j] = gameGrid[i][j-1];  //Moving the block
            }
            gameGrid[i][0] = null;
        }

        lines ++;

        //Uf lines parameter is multiple of 10, then Increases the level of the game and updates gameSpeed
        if(lines%10 == 0) {
            level++;
            gameSpeed = Math.pow((0.8 - ((level) * 0.007)), level);
        }
    }

    //Calculates The Score Due To How Many Lines Struck
    public int calculateScore(int lines){

        if(lines == 1){
            System.out.println("Single");
            gamePanel.new strikeUpdater("Single",40*(level+1));
            return 40*(level+1);
        }else if(lines ==2){
            System.out.println("Double");
            gamePanel.new strikeUpdater("Double",100*(level+1));
            return 100*(level+1);
        }else if(lines ==3){
            System.out.println("Triple");
            gamePanel.new strikeUpdater("Triple",300*(level+1));
            return 300*(level+1);
        }else{
            System.out.println("TETRIS!");
            gamePanel.new strikeUpdater("TETRIS!",1200*(level+1));
            return 1200*(level+1);
        }
    }

    //Randomly Generates Tetromino
    public void generateRandomTetromino(){
        Random generator = new Random();

        //Randomly selects two tetrominoes. One is for the next Tetromino and one is for the current Tetromino
        while(tetrominoes.size() < 2) {
            int randomNum = generator.nextInt(7);

            if (randomNum == 0)
                 tetrominoes.add(new I_Piece());

            else if (randomNum == 1)
                tetrominoes.add(new J_Piece());

            else if (randomNum == 2)
                tetrominoes.add(new L_Piece());

            else if (randomNum == 3)
                tetrominoes.add(new O_Piece());

            else if (randomNum == 4)
                tetrominoes.add(new S_Piece());

            else if (randomNum == 5)
                tetrominoes.add(new T_Piece());

            else if (randomNum == 6)
                tetrominoes.add(new Z_Piece());
        }

        currentTetromino = tetrominoes.poll();   //First element of the queue is current Tetromino
        nextTetromino = tetrominoes.peek().type;   //Second element (now there is only one) of the queue is next Tetromino

        currentTetromino.start();
        currentTetromino.addTetromino();

        holdUsed = false;   //Resets the hold chance
    }

    //Generates a Tetromino Due To Given Char Type And Returns It.
    public Tetromino generateTetromino(char type){
        Tetromino tempTetromino = null;

        if (type == 'I')
            tempTetromino = (new I_Piece());

        else if (type == 'J')
            tempTetromino = (new J_Piece());

        else if (type == 'L')
            tempTetromino = (new L_Piece());

        else if (type == 'O')
            tempTetromino = (new O_Piece());

        else if (type == 'S')
            tempTetromino = (new S_Piece());

        else if (type == 'T')
            tempTetromino = (new T_Piece());

        else if (type == 'Z')
            tempTetromino = (new Z_Piece());

        if(tempTetromino != null) {
            tempTetromino.start();
            tempTetromino.addTetromino();
        }
        return tempTetromino;
    }

    //Deletes The Given Tetromino.
    public boolean deleteTetromino(Tetromino tetromino){
        if(tetromino != null){
            tetromino.alive = false;
            tetromino.interrupt();   //Because Tetromino is a Thread, It must be stopped before deleted
            currentTetromino.clearTetromino();
        }
        return true;
    }

    public static void main(String[] args) {
        new Tetris();
    }
}
