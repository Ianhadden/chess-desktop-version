import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Game {
    
    public boolean inProgress = true;
    public boolean isDraw = false;
    public ArrayList<String> fens;
    public static final String startFen = "rhbqkbhrpppppppp--------------------------------PPPPPPPPRHBQKBHR wffffff00";
    Board currentBoard;
    
    /**
     * Constructs a new game using the default fen
     */
    public Game(){
        currentBoard = new Board(startFen);
        fens = new ArrayList<String>();
        fens.add(startFen);
        inProgress = true;
    }
    
    /**
     * Creates a new game using the last fen in the provided list
     * @param fens A history of the game, with the most recent
     *             game state fen at the end of the list
     */
    public Game(ArrayList<String> fens){
        currentBoard = new Board(fens.get(fens.size() - 1));
        this.fens = fens;
        inProgress = !currentBoard.checkMate() && !currentBoard.staleMate();
    }
    
    /**
     * Given the fen index of the piece being moved and the
     * fen index of where it is being moved to, attempts to
     * execute that move. Returns true if the move succeeded.
     * @param startIndex the starting index of the piece
     * @param endIndex The ending index of the piece
     * @return true if the move was successful
     */
    public boolean attemptMove(int startIndex, int endIndex){
        List<Move> moves = currentBoard.generateMoves();
        for (Move m : moves){
            if (m.changes.get(0).fenIndex == startIndex &&
                m.changes.get(1).fenIndex == endIndex){
                currentBoard.applyMove(m);

                if (currentBoard.promotingPawn() != null) {
                    return true;
                }
                if (currentBoard.checkMate()){
                    inProgress = false;
                    Move winnerUpdate = new Move();
                    char winner = currentBoard.turn().equals("white")?'b':'w';
                    winnerUpdate.addChange(64, winner);
                    currentBoard.forceFenUpdate(winnerUpdate);
                    System.out.println("We have a winner!" + winner);
                } else if (currentBoard.draw()){
                    inProgress = false;
                    isDraw = true;
                    Move winnerUpdate = new Move();
                    char winner = 'd';
                    winnerUpdate.addChange(64, winner);
                    currentBoard.forceFenUpdate(winnerUpdate);
                    System.out.println("It's a draw!");
                }
                fens.add(currentBoard.fen);
                return true;   
            }
        }
        return false;
    }
  
    /**
     * Given an input string representing the desired piece, finds the pawn
     * on the board to be promoted and promotes that piece
     * @pre There must be a pawn to promote. The input must represent a piece
     *      that can be the result of a promotion.
     * @param input The single character string input for the promotion
     */
    public void promotePawn(String input){
        if (currentBoard.promotingPawn() == null) {
            return;
        }
        if ("black".equals(currentBoard.turn())) {
            input = input.toUpperCase();
        }
        char desired = input.charAt(0);
        //should probably move this logic into Board
        List<Move> moves = currentBoard.generateMovesCore(true); //param irrelevant since we're only getting pawn moves
        for (Move move : moves) {
            if (move.changes.get(0).newValue == desired) {
                //currentBoard.forceFenUpdate(move);
                //currentBoard.promotingPawn = false;
                currentBoard.applyMove(move);
                fens.add(currentBoard.fen);
                return;
            }
        }
    }
    
    public void saveGame(File dest){
        PrintStream output;
        try {
            output = new PrintStream(new FileOutputStream(dest));
            for (int i = 0; i < fens.size(); i++){
                output.println(fens.get(i));
            }
            System.out.println("save complete!");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static ArrayList<String> getFenListFromFile(File f){
        ArrayList<String> fens = new ArrayList<String>();
        try {
            Scanner scanny = new Scanner(f);
            while (scanny.hasNextLine()){
                fens.add(scanny.nextLine());
            }
            scanny.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return fens;
    }
    
    /**
     * 
     * @return whether or not this is a network game.
     */
    public boolean isNetworkGame(){
        return false;
    }
}