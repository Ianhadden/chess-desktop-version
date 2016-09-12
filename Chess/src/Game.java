import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;


public class Game {
    public boolean inProgress = true;
    public boolean isDraw = false;
    public ArrayList<String> fens;
    public String startFen = "rhbqkbhrpppppppp--------------------------------PPPPPPPPRHBQKBHR wffffff00";
    Board currentBoard;
    
    public Game(){
        currentBoard = new Board(startFen);
        fens = new ArrayList<String>();
        fens.add(startFen);
        inProgress = true;
    }
    
    public Game(ArrayList<String> fens){
        currentBoard = new Board(fens.get(fens.size() - 1));
        this.fens = fens;
        inProgress = !checkMate();
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
                    if (checkMate()){
                        inProgress = false;
                        Move winnerUpdate = new Move();
                        char winner = currentBoard.turn.equals("white")?'b':'w';
                        winnerUpdate.addChange(64, winner);
                        currentBoard.forceFenUpdate(winnerUpdate);
                        System.out.println("We have a winner!" + winner);
                    } else if (draw()){
                        inProgress = false;
                        isDraw = true;
                        Move winnerUpdate = new Move();
                        char winner = 'd';
                        winnerUpdate.addChange(64, winner);
                        currentBoard.forceFenUpdate(winnerUpdate);
                        System.out.println("It's a draw!");
                    }
                    if (!currentBoard.promotingPawn){
                        fens.add(currentBoard.fen);
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Returns true if the given player is in check on given current board
     * @param b The board to check
     * @param player The player to test on
     * @return true if in check (or checkmate)
     */
    public static boolean playerInCheck(Board b, String player){
        String trueTurn = b.turn;
        char king;
        if (player == "white"){
            b.turn = "black";
            king = 'k';
        } else {
            b.turn = "white";
            king = 'K';
        }
        int kingIndex = -1; //initialized so compiler will shut up
        for (int i = 0; i < 64; i++){
            if (b.fen.charAt(i) == king){
                kingIndex = i;
                break;
            }
        }
        ArrayList<Move> movesList = b.generateMoves();
        for (Move m : movesList){
            if (m.changes.get(1).fenIndex == kingIndex){
                b.turn = trueTurn;
                return true;
            }
        }
        b.turn = trueTurn;
        return false;
    }
    
    /**
     * Retunrs true if the given player is in check on this game's current board.
     * @param player The player to check on
     * @return true if in check
     */
    public boolean playerInCheck(String player){
        return playerInCheck(currentBoard, player);
    }
    
    /**
     * Returns true if every move the current player can make would result
     * in their being in check.
     * @return true if every move the current player can make would result
     * in their being in check.
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
     * Returns true if the board setup is a draw
     * @return true if the board setup is a draw
     */
    public boolean draw(){
        if (staleMate()){
            return true;
        }
        int horseCount = 0;
        //bishop counts
        int wow = 0; // white on white
        int wob = 0; // white on black
        int bow = 0; // black on white
        int bob = 0; // black on black
        for (int i = 0; i < 63; i++){
            char c = currentBoard.fen.charAt(i);
            if (c == 'p' || c == 'P' || c == 'r' || c == 'R' || c == 'Q' || c == 'q'){
                return false;
            } else if (c == 'h' || c == 'H'){
                horseCount++;
            } else if (c == 'b'){
                Position pos = currentBoard.position(i);
                if ((pos.x + pos.y) % 2 == 0){// 0 == black, 1 == white
                    wob++;
                } else {
                    wow++;
                }
            } else if (c == 'B'){
                Position pos = currentBoard.position(i);
                if ((pos.x + pos.y) % 2 == 0){// 0 == black, 1 == white
                    bob++;
                } else {
                    bow++;
                }
            }
            if (horseCount > 1){
                return false;
            }
        }
        int totalBishops = wow + wob + bow + bob;
        if ((totalBishops == 0 && horseCount == 0) || (totalBishops == 1 && horseCount == 0) ||
                (totalBishops == 0 && horseCount == 1)){
            return true;
        } else if (horseCount == 0){
            if (wow > 0 && bow > 0 && wob == 0 && bob == 0){
                return true;
            }
            if (wob > 0 && bob > 0 && wow == 0 && bow == 0){
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
        currentBoard.forceFenUpdate(m);
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
            scanny.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return fens;
    }
}
