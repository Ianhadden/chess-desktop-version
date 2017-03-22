import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Display implements ActionListener {
    JFrame frame;
    StuffBox stuffHolder; // Upper level container for everything but the menu
    ChessJMenuBar menubar;
    
    boolean rotated, autorotate; // true if the board is rotated / autorotate is on
    
    Game currentGame = null;
    Replay currentReplay = null;
    
    private int mode; // should be set through setMode(int) method 
    final int GAMEMODE = 1;
    final int REPLAYMODE = 0;
    final int EMPTYMODE = -1;
    
    /**
     * Create a new Display
     */
    public Display(){
        frame = new JFrame("Chess");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(672, 630));
        
        JMenuBar menubar = setUpMenu();
        frame.setJMenuBar(menubar);
        
        rotated = false;
        autorotate = false;
        setMode(EMPTYMODE);
        frame.pack();
        frame.setVisible(true);
    }
    
    /**
     * Outer container class for the display
     */
    public class StuffBox extends Box{
        
        JLayeredPane boardLayers; // Holds the boardDisplay and the boardPieces
        ArrayList<JLabelPiece> boardPieces; // list of pieces
        JLabel boardDisplay; // JLabel that displays the board itself
        SideBarBox sideButtons; //container for the buttons on the side of the board
            
        public StuffBox(int axis) {
            super(axis);
        }
    }
    
    /**
     * Inner container for the side bar buttons/info
     */
    public class SideBarBox extends Box{
        
        JLabel turnIndicator; // displays whose turn it is or who has won
        
        ArrayList<Component> pawnPromotionButtons;
        /*List containing 6 items. Indices 0 - 3 hold the actual pawn promotion
         * buttons. Index 4 holds the container for those buttons. Index 5 holds
         * a large empty component that is displayed when the pawn promotion buttons
         * are not, so that proper spacing is maintained
         */
        
        public SideBarBox(int axis) {
            super(axis);
        }   
    }
    
    /**
     * JMenuBar class to keep track of references to JMenuItems that
     * need to be changed under certain circumstances
     */
    public class ChessJMenuBar extends JMenuBar {
        JMenuItem saveGame;
        JMenuItem startGameFromHere;
    }
    
    /**
     * Sets up the menu for the display
     */
    public JMenuBar setUpMenu(){
        menubar = new ChessJMenuBar();
        
        JMenu game = new JMenu("Game");
        menubar.add(game);
        JMenuItem newGame = new JMenuItem("New Game");
        JMenuItem loadGame = new JMenuItem("Load Game");
        JMenuItem saveGame = new JMenuItem("Save Game");
        menubar.saveGame = saveGame;
        newGame.addActionListener(this);
        loadGame.addActionListener(this);
        saveGame.addActionListener(this);
        game.add(newGame);
        game.add(loadGame);
        game.add(saveGame);
        
        JMenu network = new JMenu("Network");
        menubar.add(network);
        JMenuItem networkNewGame = new JMenuItem("New Game ");
        JMenuItem networkLoadGame = new JMenuItem("Load Game ");
        JMenuItem networkConnect = new JMenuItem("Connect to Game ");
        networkNewGame.addActionListener(this);
        networkLoadGame.addActionListener(this);
        networkConnect.addActionListener(this);
        network.add(networkNewGame);
        network.add(networkLoadGame);
        network.add(networkConnect);
        
        
        JMenu replays = new JMenu("Replays");
        menubar.add(replays);
        JMenuItem loadReplay = new JMenuItem("Load Replay");
        JMenuItem startGameFromHere = new JMenuItem("Start Game From Current Replay State");
        menubar.startGameFromHere = startGameFromHere;
        replays.add(loadReplay);
        replays.add(startGameFromHere);
        loadReplay.addActionListener(this);
        startGameFromHere.addActionListener(this);
        return menubar;
    }
    
    /**
     * Sets up the board display
     */
    public void setUpGameBoardDisplay(){
        //get rid of the old stuffHolder if we had one
        if (stuffHolder != null){
            frame.remove(stuffHolder);
        }
        stuffHolder = new StuffBox(BoxLayout.LINE_AXIS);
        frame.add(stuffHolder, BorderLayout.WEST);
        
        JLayeredPane boardLayers = new JLayeredPane();
        boardLayers.setPreferredSize(new Dimension(576, 576));
        stuffHolder.add(boardLayers);
        stuffHolder.add(Box.createRigidArea(new Dimension(12,0)));
        stuffHolder.boardLayers = boardLayers;

        JLabel boardDisplay = new JLabel();
        boardDisplay.setBounds(0, 0, 576, 576);
        boardDisplay.setIcon(new ImageIcon("chessboard.png"));
        stuffHolder.boardLayers.add(boardDisplay, JLayeredPane.DEFAULT_LAYER);
        stuffHolder.boardDisplay = boardDisplay;
        if (mode == GAMEMODE){
            //add a mouse listener if in game mode
            MouseHandler handle = new MouseHandler();
            handle.disp = this;
            stuffHolder.boardLayers.addMouseListener(handle);
            stuffHolder.boardLayers.addMouseMotionListener(handle);
        } else if (mode == REPLAYMODE){
            /* if in replay mode, change the cursor back to pointer in case it isn't already */
            frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        stuffHolder.boardPieces = new ArrayList<JLabelPiece>();
        
        //side up normal side bar buttons
        SideBarBox sideButtons = new SideBarBox(BoxLayout.PAGE_AXIS);      
        JButton rotate = new JButton(new ImageIcon("rotate2.png"));
        JButton autorotateButton = new JButton(new ImageIcon("autorotateoff.png"));
        rotate.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                toggleRotate();
            }
        });
        autorotateButton.addActionListener(new ActionListener(){
            JButton theButton = autorotateButton;
            public void actionPerformed(ActionEvent e){
                toggleAutorotate(theButton);
            }
        });
        rotate.setFocusPainted(false);
        autorotateButton.setFocusPainted(false);
        rotate.setMaximumSize(new Dimension(64, 64));
        rotate.setMinimumSize(new Dimension(64, 64));
        autorotateButton.setMaximumSize(new Dimension(64, 64));
        autorotateButton.setMinimumSize(new Dimension(64, 64));
        sideButtons.add(rotate);
        sideButtons.add(Box.createRigidArea(new Dimension(0,12)));
        sideButtons.add(autorotateButton);
        sideButtons.add(Box.createRigidArea(new Dimension(0,12)));
        JLabel turnIndicator = new JLabel("");
        turnIndicator.setFont(new Font("Serif", Font.BOLD, 18));
        sideButtons.add(turnIndicator);         
        sideButtons.turnIndicator = turnIndicator;
        
        if (mode == GAMEMODE){
            sideButtons.add(setUpPawnPromotionInterface(sideButtons));
            Component largeSpace = Box.createRigidArea(new Dimension(0,352));
            sideButtons.add(largeSpace);
            sideButtons.pawnPromotionButtons.add(largeSpace);// index 5 for the space displayed when buttons aren't
        } else { // mode == REPLAYMODE
            sideButtons.add(setUpReplayInterface());
        }
        stuffHolder.add(sideButtons);
        stuffHolder.sideButtons = sideButtons;
        
        frame.revalidate();
    }
    
    public Box setUpPawnPromotionInterface(SideBarBox sideButtons){
        Box promotionInterface = new Box(BoxLayout.PAGE_AXIS);
        ArrayList<Component> pawnPromotionButtons = new ArrayList<Component>();
        sideButtons.pawnPromotionButtons = pawnPromotionButtons;
        for (int i = 0; i < 4; i++){
            JPieceButton jb = new JPieceButton();
            jb.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    currentGame.promotePawn(jb.piece);
                    removePawnPromotionButtons();
                    printBoard();
                }
            });
            pawnPromotionButtons.add(jb); // indices 0 - 3 for piece buttons
            promotionInterface.add(Box.createRigidArea(new Dimension(0,12)));
            jb.setFocusPainted(false);
            jb.setMaximumSize(new Dimension(64, 64));
            promotionInterface.add(jb);
        }
        Component smallSpace = Box.createRigidArea(new Dimension(0,48));
        promotionInterface.add(smallSpace);
        
        pawnPromotionButtons.add(promotionInterface); //index 4 for container of those buttons
        promotionInterface.setVisible(false);
        return promotionInterface;
    }
    
    public Box setUpReplayInterface(){
        Box replayInterface = new Box(BoxLayout.PAGE_AXIS);
        JLabel turnNumberIndicator = new JLabel("<html>&nbsp;Move " + currentReplay.currentFenNumber 
                        + "<br>&nbsp;&nbsp;&nbsp;of " + (currentReplay.fens.size() - 1) + "<html>");
        turnNumberIndicator.setFont(new Font("Serif", Font.BOLD, 18));
        replayInterface.add(Box.createRigidArea(new Dimension(0,12)));
        JButton advance = new JButton(new ImageIcon("stepforward.png"));
        replayInterface.add(advance);
        advance.setFocusPainted(false);
        advance.setMaximumSize(new Dimension(64, 64));
        advance.addActionListener(new ActionListener(){
            JLabel tni = turnNumberIndicator;
            public void actionPerformed(ActionEvent e){
                if (currentReplay.advance() && autorotate){
                    toggleRotate();
                }
                tni.setText("<html>&nbsp;Move " + currentReplay.currentFenNumber 
                        + "<br>&nbsp;&nbsp;&nbsp;of " + (currentReplay.fens.size() - 1) + "<html>");
                printBoard();
            }
        });
        replayInterface.add(Box.createRigidArea(new Dimension(0,12)));
        JButton retreat = new JButton(new ImageIcon("stepbackward.png"));
        replayInterface.add(retreat);
        retreat.setFocusPainted(false);
        retreat.setMaximumSize(new Dimension(64, 64));
        retreat.addActionListener(new ActionListener(){
            JLabel tni = turnNumberIndicator;
            public void actionPerformed(ActionEvent e){
                if (currentReplay.retreat() && autorotate){
                    toggleRotate();
                }
                tni.setText("<html>&nbsp;Move " + currentReplay.currentFenNumber 
                        + "<br>&nbsp;&nbsp;&nbsp;of " + (currentReplay.fens.size() - 1) + "<html>");
                printBoard();
            }
        });
        replayInterface.add(Box.createRigidArea(new Dimension(0,12)));
        replayInterface.add(turnNumberIndicator);
        replayInterface.add(Box.createRigidArea(new Dimension(0,140)));
        
        return replayInterface;
    }
    
    /**
     * Displays the pawn promotion dialogue and buttons
     */
    public void showPawnPromotionButtons(){
        stuffHolder.sideButtons.turnIndicator.setText("<html>&nbsp;Choose<br>&nbsp;&nbsp;piece<html>");
        stuffHolder.sideButtons.pawnPromotionButtons.get(4).setVisible(true); // make promotionInterface visible
        stuffHolder.sideButtons.pawnPromotionButtons.get(5).setVisible(false); // make big space thingy not
        String[] whitePieces = {"whitequeen.png", "whitebishop.png","whiterook.png","whitehorse.png"};
        String[] blackPieces = {"blackqueen.png", "blackbishop.png","blackrook.png","blackhorse.png"};
        String[] inUse = currentGame.currentBoard.turn.equals("black")? whitePieces : blackPieces;
        for (int i = 0; i < 4; i++){
            ((JButton) stuffHolder.sideButtons.pawnPromotionButtons.get(i)).setIcon(new ImageIcon(inUse[i]));
            ((JPieceButton) stuffHolder.sideButtons.pawnPromotionButtons.get(i)).piece = "" + inUse[i].charAt(5);
                                                                // dependent of format above
        }
    }
    
    /**
     * Removes the pawn promotion dialogue and buttons
     */
    public void removePawnPromotionButtons(){
        stuffHolder.sideButtons.turnIndicator.setText("<html>&nbsp;" + 
                currentGame.currentBoard.turn + "'s<br>&nbsp;&nbsp;&nbsp;turn<html>");
        stuffHolder.sideButtons.pawnPromotionButtons.get(4).setVisible(false);
        stuffHolder.sideButtons.pawnPromotionButtons.get(5).setVisible(true);   
    }
    
    
    /**
     * Handles actions associated with the menu
     */
    public void actionPerformed(ActionEvent e) {
        
        if (e.getActionCommand().equals("Load Replay")){
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION){
                File file = fc.getSelectedFile();
                currentReplay = new Replay(Game.getFenListFromFile(file));
                currentGame = null;
                setMode(REPLAYMODE);
                setUpGameBoardDisplay();
                printBoard();
            }
        } else if (e.getActionCommand().equals("Start Game From Current Replay State")){
            if (mode != REPLAYMODE){
                JOptionPane.showMessageDialog(null,
                        "No replay is in progress");
                return;
            }
            currentGame = currentReplay.startGameFromCurrentState();
            currentReplay = null;
            setMode(GAMEMODE);
            setUpGameBoardDisplay();
            printBoard();
            
        } else if (e.getActionCommand().equals("Load Game")){
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION){
                File file = fc.getSelectedFile();
                if (currentGame != null && currentGame.inProgress == true){
                    int reply = JOptionPane.showConfirmDialog(null, 
                        "You have a game in progress. Are you sure you want to load a different one?", 
                            "Load Game?",  JOptionPane.YES_NO_OPTION);
                    if (reply == JOptionPane.NO_OPTION){
                        return;
                    }
                }
                setMode(GAMEMODE);
                setUpGameBoardDisplay();
                currentGame = new Game(Game.getFenListFromFile(file));
                currentReplay = null;
                printBoard();
            }
                    
        } else if (e.getActionCommand().equals("New Game")){
            if (currentGame != null && currentGame.inProgress == true){
                int reply = JOptionPane.showConfirmDialog(null, 
                        "You have a game in progress. Are you sure you want to start a new one?", 
                        "New Game?",  JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.NO_OPTION){
                    return;
                }
            }
            setMode(GAMEMODE);
            setUpGameBoardDisplay();
            currentGame = new Game();
            currentReplay = null;
            printBoard();
            
        } else if (e.getActionCommand().equals("Save Game")){
            if (currentGame == null){
                JOptionPane.showMessageDialog(null,
                        "No game is in progress");
                return;  
            } else if (currentGame != null && currentGame.currentBoard.promotingPawn){
                JOptionPane.showMessageDialog(null,
                        "Complete pawn promotion first");
                return;
            }
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION){
                File dest = fc.getSelectedFile();
                boolean goodToGo = false;
                try {
                    goodToGo = dest.createNewFile();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                if (goodToGo){
                    currentGame.saveGame(dest);
                } else {
                    int reply = JOptionPane.showConfirmDialog(null, 
                            "There's already a file with that name. Overwrite it?", 
                            "Overwrite?",  JOptionPane.YES_NO_OPTION);
                    if (reply == JOptionPane.YES_OPTION){
                        currentGame.saveGame(dest);
                    }
                }
            }
        
            //network actions. Hacky space at end of strings to differentiate atm. Should fix
        } else if (e.getActionCommand().equals("New Game ")){
            if (currentGame != null && currentGame.inProgress == true){
                int reply = JOptionPane.showConfirmDialog(null, 
                        "You have a game in progress. Are you sure you want to start a new one?", 
                        "New Game?",  JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.NO_OPTION){
                    return;
                }
            }
            
            int port = -1;
            boolean needPort = true;
            while (needPort){
                String portS = JOptionPane.showInputDialog("Port Number?");
                if (portS == null){
                    return;
                }
                try {
                    port = Integer.parseInt(portS);
                    needPort = false;
                } catch (Exception e2){}
            }
            System.out.println(port);
            
            setMode(GAMEMODE);
            setUpGameBoardDisplay();
            ArrayList<String> derp = new ArrayList<String>();
            derp.add("rhbqkbhrppppp-pp-----p--------------------------PPPPPPPPRHBQKBHR bffffff00");
            currentGame = new Game(port, derp);
            currentReplay = null;
            printBoard();
        } else if (e.getActionCommand().equals("Connect to Game ")){
            if (currentGame != null && currentGame.inProgress == true){
                int reply = JOptionPane.showConfirmDialog(null, 
                        "You have a game in progress. Are you sure you want to start a new one?", 
                        "New Game?",  JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.NO_OPTION){
                    return;
                }
            }
            
            int port = -1;
            boolean needPort = true;
            while (needPort){
                String portS = JOptionPane.showInputDialog("Port Number?");
                if (portS == null){
                    return;
                }
                try {
                    port = Integer.parseInt(portS);
                    needPort = false;
                } catch (Exception e2){}
            }
            System.out.println(port);
            
            String hostName = JOptionPane.showInputDialog("Host Name?");
            if (hostName == null){
                return;
            }
            System.out.println(hostName);
            setMode(GAMEMODE);
            setUpGameBoardDisplay();
            currentGame = new Game(hostName, port);
            currentReplay = null;
            printBoard();
        } 
    }
    
    /**
     * Attempts a move given the starting and ending index in the fen of the piece being moved
     * Updates the board display as necessary
     * @param startingPosIndex Starting index
     * @param endingPosIndex Ending index
     */
    public void attemptMove(int startingPosIndex, int endingPosIndex){
        boolean moveSuccess = false;
        if (startingPosIndex > -1 && startingPosIndex <  64 &&
            endingPosIndex > -1 && endingPosIndex < 64){
            moveSuccess = currentGame.attemptMove(startingPosIndex, endingPosIndex);
        }
        if (moveSuccess){
            if (currentGame.currentBoard.promotingPawn){
                showPawnPromotionButtons();
            }
            if (autorotate){
                toggleRotate(); //includes board print
            } else {
                printBoard();
            }
        }
    }
    
    /**
     * Updates the mode
     * @param mode
     */
    public void setMode(int mode){
        this.mode = mode;
        if (mode == GAMEMODE){
            menubar.startGameFromHere.setEnabled(false);
            menubar.saveGame.setEnabled(true);
        } else if (mode == REPLAYMODE){
            menubar.startGameFromHere.setEnabled(true);
            menubar.saveGame.setEnabled(false);
        } else {
            menubar.startGameFromHere.setEnabled(false);
            menubar.saveGame.setEnabled(false);
        }
    }
    
    /**
     * Toggles rotation on the board
     */
    public void toggleRotate(){
        rotated = !rotated;
        if (rotated){
            stuffHolder.boardDisplay.setIcon(new ImageIcon("chessboardrotated.png"));
        } else {
            stuffHolder.boardDisplay.setIcon(new ImageIcon("chessboard.png"));
        }
        printBoard();
    }
    
    /**
     * Toggles autorotation on the board
     * @param autorotateButton The JButton that has been pressed to trigger
     *        this toggle
     */
    public void toggleAutorotate(JButton autorotateButton){
        autorotate = !autorotate;
        if (autorotate){
            autorotateButton.setIcon(new ImageIcon("autorotateon.png"));
        } else {
            autorotateButton.setIcon(new ImageIcon("autorotateoff.png"));
        }
    }
    
    public void printBoard(){
        String uneditedFen = mode == GAMEMODE? currentGame.currentBoard.fen : currentReplay.currentFen;
        //remove pieces already being displayed
        if (stuffHolder.boardPieces.size() != 0){
            for (int i = 0; i < stuffHolder.boardPieces.size(); i++){
                stuffHolder.boardLayers.remove(stuffHolder.boardPieces.get(i));
            }
            stuffHolder.boardPieces = new ArrayList<JLabelPiece>();
        }
        //go through the fen, make JLabels for pieces, add them to boardLayers
        for (int i = 0; i < 64; i++){
            if (uneditedFen.charAt(i) != '-'){
                Position pos = new Position((i / 8) + 1, (i % 8) + 1);
                int xCoord, yCoord;
                if (rotated){
                    yCoord = (pos.y - 1) * 64;
                    xCoord = 576 - 64 * pos.x;
                } else {
                    yCoord = -64 * pos.y + 512;
                    xCoord = 64 * pos.x;
                }
                JLabelPiece piece = new JLabelPiece();
                piece.setBounds(xCoord, yCoord, 64, 64);
                piece.position = pos;
                String pieceName = getPieceName(uneditedFen.charAt(i));
                piece.owner = pieceName.substring(0, 5); // gets "white" or "black"
                piece.setIcon(new ImageIcon(pieceName));
                stuffHolder.boardLayers.add(piece, JLayeredPane.PALETTE_LAYER);
                stuffHolder.boardPieces.add(piece);
                
            }
        }
        if (inProgress() && (mode == REPLAYMODE || !currentGame.currentBoard.promotingPawn)){ 
            // update dialogue for whose turn it is
            String turn = getTurn();
            stuffHolder.sideButtons.turnIndicator.setText("<html>&nbsp;" + turn + "'s<br>&nbsp;&nbsp;&nbsp;turn<html>");
            //html allows for multiline jlabel
        } else if (!inProgress() && !isDraw()){
            String winner = getTurn().equals("White")?"Black":"White";
            stuffHolder.sideButtons.turnIndicator.setText("<html>&nbsp;" + winner + "<br>&nbsp;&nbsp;wins!<html>");
        } else if (!inProgress()){
            stuffHolder.sideButtons.turnIndicator.setText("<html>&nbsp;&nbsp;It's a<br>&nbsp;&nbsp;draw!<html>");
        }
        frame.repaint();
    }
    
    /**
     * Given the character representing a piece, returns
     * the file name of that piece's images
     * @param c The character
     * @pre c must be a valid piece. '-' does not count as valid
     * @return the file name
     */
    public String getPieceName(char c){
        switch (c) {
            case 'p': return "whitepawn.png";
            case 'r': return "whiterook.png";
            case 'h': return "whitehorse.png";
            case 'b': return "whitebishop.png";
            case 'q': return "whitequeen.png";
            case 'k': return "whiteking.png";
            case 'P': return "blackpawn.png";
            case 'R': return "blackrook.png";
            case 'H': return "blackhorse.png";
            case 'B': return "blackbishop.png";
            case 'Q': return "blackqueen.png";
            case 'K': return "blackking.png";
            default : return "this should never ever happen";
        }
    }
    
    /**
     * Returns the name of the player whose turn it is with the first
     * letter capitalized
     * @return "White" or "Black"
     */
    public String getTurn(){
        if (mode == GAMEMODE){
            return currentGame.currentBoard.turn.substring(0, 1).toUpperCase() +
                    currentGame.currentBoard.turn.substring(1, 5);
        } else { // mode == REPLAYMODE
            char c = currentReplay.currentFen.charAt(65);
            return c == 'w' ? "White" : "Black";
        }
    }
    
    public boolean inProgress(){
        if (mode == GAMEMODE){
            return currentGame.inProgress;
        } else { // mode == REPLAYMODE
            return currentReplay.inProgress;
        }
    }
    
    public boolean isDraw(){
        if (mode == GAMEMODE){
            return currentGame.isDraw;
        } else {
            return currentReplay.isDraw;
        }
    }
       
    public static void main(String[] args){
        Display disp = new Display();
    }
}