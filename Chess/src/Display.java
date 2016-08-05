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
    Game currentGame = null;
    Replay currentReplay = null;
    ArrayList<JLabelPiece> boardPieces = new ArrayList<JLabelPiece>(); // list of pieces
    ArrayList<Component> pawnPromotionButtons = new ArrayList<Component>();
    JLayeredPane boardLayers; // Holds the board and the pieces
    Box stuffHolder; // Upper level container for everything but the menu
    JLabel turnIndicator; // displays whose turn it is
    boolean rotated, autorotate; // true if the board is rotated / autorotate is on
    JLabel boardDisplay; // JLabel that displays the board itself
    int mode; // to be set to 1 or 0 (modes below)
    Box sideButtons; // container for the buttons on the side of the board
    
    final int REPLAYMODE = 1;
    final int GAMEMODE = 0;
    /**
     * Sets up the display
     */
    public Display(){
        frame = new JFrame("Chess");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(672, 630)); //672, 876
        
        JMenuBar menubar = setUpMenu();
        frame.setJMenuBar(menubar);
        
        stuffHolder = new Box(BoxLayout.LINE_AXIS);
        frame.add(stuffHolder, BorderLayout.WEST);
        
        boardLayers = new JLayeredPane();
        boardLayers.setPreferredSize(new Dimension(576, 576));
        stuffHolder.add(boardLayers);
        stuffHolder.add(Box.createRigidArea(new Dimension(12,0)));
        
        rotated = false;
        autorotate = false;
        frame.pack();
        frame.setVisible(true);
    }
    
    /**
     * Sets up the menu for the display
     */
    public JMenuBar setUpMenu(){
        JMenuBar menubar = new JMenuBar();
        JMenu game = new JMenu("Game");
        menubar.add(game);
        JMenuItem newGame = new JMenuItem("New Game");
        JMenuItem loadGame = new JMenuItem("Load Game");
        JMenuItem saveGame = new JMenuItem("Save Game");
        newGame.addActionListener(this);
        loadGame.addActionListener(this);
        saveGame.addActionListener(this);
        game.add(newGame);
        game.add(loadGame);
        game.add(saveGame);
        
        JMenu replays = new JMenu("Replays");
        menubar.add(replays);
        JMenuItem loadReplay = new JMenuItem("Load Replay");
        replays.add(loadReplay);
        loadReplay.addActionListener(this);
        return menubar;
    }
    
    /**
     * Sets up the board display
     */
    public void setUpGameBoardDisplay(){
        //set up board
        boardDisplay = new JLabel();
        boardDisplay.setBounds(0, 0, 576, 576);
        boardDisplay.setIcon(new ImageIcon("chessboard.png"));
        boardLayers.add(boardDisplay, JLayeredPane.DEFAULT_LAYER);
        if (mode == GAMEMODE){ // add mouse listener if in game mode
            MouseHandler handle = new MouseHandler();
            handle.frame = frame;
            handle.disp = this;
            boardLayers.addMouseListener(handle);
            boardLayers.addMouseMotionListener(handle);
        } else { // mode == REPLAYMODE
            /* if we previously added a mouse listener to the board, remove it, because we
             don't need it in replay mode. Also change the cursor back to pointer in case it isn't already */
            MouseListener[] handlers = boardLayers.getMouseListeners();
            if (handlers.length != 0){
                frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                boardLayers.removeMouseListener(handlers[0]);
                boardLayers.removeMouseMotionListener((MouseMotionListener) handlers[0]);
            }
        }
        
        //side up normal side bar buttons
        if (sideButtons != null){ // get rid of old side buttons if there were any
            stuffHolder.remove(sideButtons);
        }
        sideButtons = new Box(BoxLayout.PAGE_AXIS);      
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
        turnIndicator = new JLabel("");
        turnIndicator.setFont(new Font("Serif", Font.BOLD, 18));
        sideButtons.add(turnIndicator);         
        
        if (mode == GAMEMODE){
            sideButtons.add(setUpPawnPromotionInterface());
            Component largeSpace = Box.createRigidArea(new Dimension(0,352));
            sideButtons.add(largeSpace);
            pawnPromotionButtons.add(largeSpace);// index 5 for the space displayed when buttons aren't
        } else {
            sideButtons.add(setUpReplayInterface());
        }
        stuffHolder.add(sideButtons);
        
        frame.revalidate();
        frame.repaint();
    }
    
    public Box setUpPawnPromotionInterface(){
        Box promotionInterface = new Box(BoxLayout.PAGE_AXIS);
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
                currentReplay.advance();
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
                currentReplay.retreat();
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
        turnIndicator.setText("<html>&nbsp;Choose<br>&nbsp;&nbsp;piece<html>");
        pawnPromotionButtons.get(4).setVisible(true); // make promotionInterface visible
        pawnPromotionButtons.get(5).setVisible(false); // make big space thingy not
        String[] whitePieces = {"whitequeen.png", "whitebishop.png","whiterook.png","whitehorse.png"};
        String[] blackPieces = {"blackqueen.png", "blackbishop.png","blackrook.png","blackhorse.png"};
        String[] inUse = currentGame.currentBoard.turn.equals("black")? whitePieces : blackPieces;
        for (int i = 0; i < 4; i++){
            ((JButton) pawnPromotionButtons.get(i)).setIcon(new ImageIcon(inUse[i]));
            ((JPieceButton) pawnPromotionButtons.get(i)).piece = "" + inUse[i].charAt(5); // dependent of format above
        }
    }
    
    /**
     * Removes the pawn promotion dialogue and buttons
     */
    public void removePawnPromotionButtons(){
        turnIndicator.setText("<html>&nbsp;" + 
                currentGame.currentBoard.turn + "'s<br>&nbsp;&nbsp;&nbsp;turn<html>");
        pawnPromotionButtons.get(4).setVisible(false);
        pawnPromotionButtons.get(5).setVisible(true);   
    }
    
    
    /**
     * Handles actions associated with the menu
     */
    public void actionPerformed(ActionEvent e) {
        //System.out.println(e.getActionCommand());
        
        if (e.getActionCommand().equals("Load Replay")){
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION){
                File file = fc.getSelectedFile();
                currentReplay = new Replay(Game.getFenListFromFile(file));
                currentGame = null;
                mode = REPLAYMODE;
                setUpGameBoardDisplay();
                printBoard();
            }
            
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
                mode = GAMEMODE;
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
            mode = GAMEMODE;
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
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
                if (goodToGo){
                    currentGame.saveGame(dest);
                } else {
                    System.out.println("that file already exists");
                }
            }
        }
    }
    
    public boolean isValidPromotionInput(String input){
        char c = Character.toLowerCase(input.charAt(0));
        return input.length() == 1 && (c == 'r' || c == 'q'
                || c == 'b' || c == 'h');
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
            //System.out.println(currentGame.currentBoard.fen);
            //System.out.println(currentGame.currentBoard.turn);
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
     * Toggles rotation on the board
     */
    public void toggleRotate(){
        rotated = !rotated;
        if (rotated){
            boardDisplay.setIcon(new ImageIcon("chessboardrotated.png"));
        } else {
            boardDisplay.setIcon(new ImageIcon("chessboard.png"));
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
        if (boardPieces.size() != 0){
            for (int i = 0; i < boardPieces.size(); i++){
                boardLayers.remove(boardPieces.get(i));
            }
            boardPieces = new ArrayList<JLabelPiece>();
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
                boardLayers.add(piece, JLayeredPane.PALETTE_LAYER);
                boardPieces.add(piece);
                
            }
        }
        if (inProgress() && (mode == REPLAYMODE || !currentGame.currentBoard.promotingPawn)){ 
            // update dialogue for whose turn it is
            String turn = getTurn();
            turnIndicator.setText("<html>&nbsp;" + turn + "'s<br>&nbsp;&nbsp;&nbsp;turn<html>");
            //html allows for multiline jlabel
        } else if (!inProgress()){
            String winner = getTurn().equals("White")?"Black":"White";
            turnIndicator.setText("<html>&nbsp;" + winner + "<br>&nbsp;&nbsp;wins!<html>");
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
    
    public static void main(String[] args){
        Display disp = new Display();
    }
}