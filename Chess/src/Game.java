import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;


public class Game {
    public boolean inProgress = true;
    public ArrayList<String> fens;
    public String startFen = "rhbqkbhrpppppppp--------------------------------PPPPPPPPRHBQKBHR wffffff00";
    Board currentBoard;
    
    public Game(){
        currentBoard = new Board(startFen);
        fens = new ArrayList<String>();
        fens.add(startFen);
        //currentBoard.printBoard();
    }
    
    public Game(ArrayList<String> fens){
        currentBoard = new Board(fens.get(fens.size() - 1));
        this.fens = fens;
        //currentBoard.printBoard();
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
        ArrayList<Move> moves = currentBoard.generateMoves();
        String playerMoving = currentBoard.turn;
        for (Move m : moves){
            if (m.changes.get(0).fenIndex == startIndex &&
                m.changes.get(1).fenIndex == endIndex){
                currentBoard.applyMove(m);
                if (playerInCheck(playerMoving)){
                    currentBoard.undoMove();
                    return false;
                } else {
                    if (!currentBoard.promotingPawn){
                        fens.add(currentBoard.fen);
                    }
                    if (checkMate()){
                        inProgress = false;
                        System.out.println("We have a winner!");
                    } else if (staleMate()){
                        inProgress = false;
                        System.out.println("It's a stalemate!");
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean playerInCheck(String player){
        String trueTurn = currentBoard.turn;
        char king;
        if (player == "white"){
            currentBoard.turn = "black";
            king = 'k';
        } else {
            currentBoard.turn = "white";
            king = 'K';
        }
        int kingIndex = -1; //initialized so compiler will shut up
        for (int i = 0; i < 64; i++){
            if (currentBoard.fen.charAt(i) == king){
                kingIndex = i;
                break;
            }
        }
        ArrayList<Move> movesList = currentBoard.generateMoves();
        for (Move m : movesList){
            if (m.changes.get(1).fenIndex == kingIndex){
                currentBoard.turn = trueTurn;
                return true;
            }
        }
        currentBoard.turn = trueTurn;
        return false;
    }
    
    /**
     * Returns true if every move the current player can make would result
     * in check.
     * @return true if every move the current player can make would result
     * in check
     */
    public boolean everyMoveIsCheck(){
        String currentPlayer = currentBoard.turn;
        ArrayList<Move> movesList = currentBoard.generateMoves();
        for (Move m : movesList){
            currentBoard.applyMoveSPP(m);
            if (!playerInCheck(currentPlayer)){
                currentBoard.undoMove();
                return false;
            } else {
                currentBoard.undoMove();
            }
        }
        return true;
    }
    
    /**
     * Returns true if every move the current player is checkmated
     * @return true if every move the current player is checkmated
     */
    public boolean checkMate(){
        return playerInCheck(currentBoard.turn) && everyMoveIsCheck();
    }
    
    /**
     * Returns true if every move the current player is stalemated
     * @return true if every move the current player is stalemated
     */
    public boolean staleMate(){
        return (!playerInCheck(currentBoard.turn) && everyMoveIsCheck());
    }
    
    /**
     * Given an input string representing the desired piece, finds the pawn
     * on the board to be promoted and promotes that piece
     * @pre There must be a pawn to promote. The input must represent a piece
     *      that can be the result of a promotion.
     * @param input The single character string input for the promotion
     */
    public void promotePawn(String input){
        char desired = input.charAt(0);
        int pawnIndex = -1;
        //scan bottom and top rows to find pawn
        for (int i = 0; i < 8; i++){
            if (currentBoard.fen.charAt(i) == 'P'){
                pawnIndex = i;
                desired = Character.toUpperCase(desired);
                break;
            }
        }
        if (pawnIndex == -1){
            for (int i = 56; i < 64; i++){
                if (currentBoard.fen.charAt(i) == 'p'){
                    pawnIndex = i;
                    desired = Character.toLowerCase(desired);
                    break;
                }
            }
        }
        if (pawnIndex == -1){
            System.out.println("THIS IS NOT GOOD");
        }
        Move m = new Move();
        m.addChange(pawnIndex, desired);
        currentBoard.applyMove(m);
        currentBoard.promotingPawn = false;
        fens.add(currentBoard.fen);
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
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return fens;
    }
}
