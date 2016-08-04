import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Display implements ActionListener {
    JFrame frame;
    JMenuItem loadReplay;
    JFileChooser fc = new JFileChooser();
    Game currentGame = null;
    JTextArea gameBoard;
    JTextField moveInputBox;
    ArrayList<JLabelPiece> boardPieces = new ArrayList<JLabelPiece>();
    ArrayList<Component> pawnPromotionButtons = new ArrayList<Component>();
    JLayeredPane boardLayers;
    Box stuffHolder;
    JLabel turnIndicator;
    boolean rotated, autorotate;
    JLabel boardDisplay;
    JButton autorotateButton;
    
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
    
    public void setUpGameBoardDisplay(){
        if (currentGame == null){
            //set up board with mouse listener
            boardDisplay = new JLabel();
            boardDisplay.setBounds(0, 0, 576, 576);
            boardDisplay.setIcon(new ImageIcon("chessboard.png"));
            boardLayers.add(boardDisplay, JLayeredPane.DEFAULT_LAYER);
            MouseHandler handle = new MouseHandler();
            handle.frame = frame;
            handle.disp = this;
            boardLayers.addMouseListener(handle);
            boardLayers.addMouseMotionListener(handle);
            
            //side up normal side bar buttons
            Box sideButtons = new Box(BoxLayout.PAGE_AXIS);      
            JButton rotate = new JButton(new ImageIcon("rotate2.png"));
            autorotateButton = new JButton(new ImageIcon("autorotateoff.png"));
            rotate.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    toggleRotate();
                }
            });
            autorotateButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    toggleAutorotate();
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
            
            //set up pawn promotion buttons
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
            
            Component largeSpace = Box.createRigidArea(new Dimension(0,352));
            sideButtons.add(largeSpace);
            pawnPromotionButtons.add(largeSpace); // index 5 for the space displayed when buttons aren't
            promotionInterface.setVisible(false);     
            
            stuffHolder.add(sideButtons);
            sideButtons.add(promotionInterface);
            frame.revalidate();
        }
    }
    
    public void setUpPawnPromotionButtons(){
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
        System.out.println(e.getActionCommand());
        
        if (e.getActionCommand().equals("Load Replay")){
            int returnVal = fc.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION){
                File file = fc.getSelectedFile();
                System.out.println("you chose a file");
            }
        } else if (e.getActionCommand().equals("Load Game")){
                int returnVal = fc.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION){
                    File file = fc.getSelectedFile();
                    System.out.println("you chose a file");
                    if (currentGame != null && currentGame.inProgress == true){
                        System.out.println("Load game over one in progress?");
                        int reply = JOptionPane.showConfirmDialog(null, 
                            "You have a game in progress. Are you sure you want to load a different one?", 
                                "Load Game?",  JOptionPane.YES_NO_OPTION);
                        if (reply == JOptionPane.NO_OPTION){
                            return;
                        }
                    }
                    System.out.println("Loading Game");
                    setUpGameBoardDisplay();
                    currentGame = new Game(Game.getFenListFromFile(file));
                    printBoard();
                }
                    
        } else if (e.getActionCommand().equals("New Game")){
            if (currentGame != null && currentGame.inProgress == true){
                System.out.println("Starting New Game over one in progress?");
                int reply = JOptionPane.showConfirmDialog(null, 
                        "You have a game in progress. Are you sure you want to start a new one?", 
                        "New Game?",  JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.NO_OPTION){
                    return;
                }
            }
            System.out.println("Starting New Game");
            setUpGameBoardDisplay();
            currentGame = new Game();
            printBoard();
            
        } else if (e.getActionCommand().equals("Save Game")){
            if (currentGame != null && currentGame.currentBoard.promotingPawn){
                JOptionPane.showMessageDialog(null,
                        "Complete pawn promotion first");
                return;
            }
            int returnVal = fc.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION){
                File dest = fc.getSelectedFile();
                System.out.println("you chose " + dest);
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
      
    public void attemptMove(int startingPosIndex, int endingPosIndex){
        boolean moveSuccess = false;
        if (startingPosIndex > -1 && startingPosIndex <  64 &&
            endingPosIndex > -1 && endingPosIndex < 64){
            moveSuccess = currentGame.attemptMove(startingPosIndex, endingPosIndex); 
            System.out.println(currentGame.currentBoard.fen);
            System.out.println(currentGame.currentBoard.turn);
        }
        if (moveSuccess){
            //moveInputBox.setText("");
            if (currentGame.currentBoard.promotingPawn){
                setUpPawnPromotionButtons();
            }
            if (autorotate){
                toggleRotate();
            }
            printBoard();
        }
    }
    
    public void toggleRotate(){
        rotated = !rotated;
        if (rotated){
            boardDisplay.setIcon(new ImageIcon("chessboardrotated.png"));
        } else {
            boardDisplay.setIcon(new ImageIcon("chessboard.png"));
        }
        printBoard();
    }
    
    public void toggleAutorotate(){
        autorotate = !autorotate;
        if (autorotate){
            autorotateButton.setIcon(new ImageIcon("autorotateon.png"));
        } else {
            autorotateButton.setIcon(new ImageIcon("autorotateoff.png"));
        }
        //printBoard();
    }
    
    /**
     * Parses a coordinate pair as a string and returns it's fen index,
     * or -1 if the coordinate pair has no corresponding fen index.
     * @param input The coordinate pair to be parsed. "a2" for instance
     * @return the fenIndex of the input
     */
    public int parseInput(String input){
        String stringX = input.substring(0, 1);
        String stringY = input.substring(1, 2);
        int numX, numY;
        switch (stringX) {
            case "a":
                numX = 1;
                break;
            case "b":
                numX = 2;
                break;
            case "c":
                numX = 3;
                break;
            case "d":
                numX = 4;
                break;
            case "e":
                numX = 5;
                break;
            case "f":
                numX = 6;
                break;
            case "g":
                numX = 7;
                break;
            case "h":
                numX = 8;
                break;
            default:
                numX = -1;
        }
        try {
            numY = Integer.parseInt(stringY);
        } catch (NumberFormatException e){
            numY = -1;
        }
        if (numY == -1 || numX == -1){
            return -1;
        } else {
            Position p = new Position(numY, numX);
            return currentGame.currentBoard.fenIndex(p);
        }
    }
    
    public void printBoard(){
        String uneditedFen = currentGame.currentBoard.fen;
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
        if (!currentGame.currentBoard.promotingPawn){ // update dialog for whose turn it is
            String turn = currentGame.currentBoard.turn.substring(0, 1).toUpperCase() +
                          currentGame.currentBoard.turn.substring(1, 5);
            turnIndicator.setText("<html>&nbsp;" + turn + "'s<br>&nbsp;&nbsp;&nbsp;turn<html>");
            //html allows for multiline jlabel
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
    
    public static void main(String[] args){
        Display disp = new Display();
    }
}

/*
gameBoard = new JTextArea(10, 20);
gameBoard.setEditable(false);
gameBoard.setFont(new Font("Courier New", Font.PLAIN, 40));
stuffHolder.add(gameBoard, BorderLayout.SOUTH);
*/

/*
char[] rows = {'1', '2', '3', '4', '5', '6', '7', '8'};
String toPrint = "";
for (int i = 7; i >= 0; i--){
    toPrint += rows[i];
    for (int j = 0; j < 8; j++){
        toPrint += uneditedFen.charAt(i * 8 + j);
    }
    toPrint += "\n";
}
toPrint += "0abcdefgh";
gameBoard.setText(toPrint);
*/

/**
 * Attempts to make a move given string input. If successful,
 * the board is updated.
 * @param input the the input to be parsed and used as the move
 */
/*
public void attemptMove(String input){
    if (input.length() != 5){
        return;
    }
    int startingPosIndex = parseInput(input.toLowerCase());
    int endingPosIndex = parseInput(input.substring(3, 5).toLowerCase());
    boolean moveSuccess = false;
    if (startingPosIndex != -1 && endingPosIndex != -1){
        moveSuccess = currentGame.attemptMove(startingPosIndex, endingPosIndex);
        printBoard();
        System.out.println(currentGame.currentBoard.fen);
        System.out.println(currentGame.currentBoard.turn);
    }
    if (moveSuccess){
        moveInputBox.setText("");
    }
}
*/

/*
public void handleInput(String input){
    if (currentGame.currentBoard.promotingPawn){
        if (isValidPromotionInput(input)){
            currentGame.promotePawn(input);
            printBoard();
            moveInputBox.setText("");
        }
    } else {
        attemptMove(input);
    }
}
*/


/*
moveInputBox = new JTextField(8);
moveInputBox.addActionListener(new ActionListener(){
    public void actionPerformed(ActionEvent e) {
        handleInput(moveInputBox.getText());
    }
});

//stuffHolder.add(moveInputBox, BorderLayout.SOUTH);
 * 
 */