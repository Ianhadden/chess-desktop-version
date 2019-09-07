import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;

/**
 * For games played with someone else over the network
 */

public class NetworkGame extends Game {

    //used for network games
    ServerSocket serverSocket;
    BufferedReader receiver;
    PrintWriter sender;
    int lastStartIndex, lastEndIndex;
    
    /**
     * Create a new network game
     * @param team The team this player will be
     * @param fens List of previous fens from the game
     * @param sender PrintWriter to send data over network
     * @param receiver BufferedReader to read data from network
     */
    public NetworkGame(String team, ArrayList<String> fens, PrintWriter sender, BufferedReader receiver){
        super(team, fens);
        this.sender = sender;
        this.receiver = receiver;
    }
    
    /**
     * Create a new network game
     * @param team The team this player will be
     * @param sender PrintWriter to send data over network
     * @param receiver BufferedReader to read data from network
     */
    public NetworkGame(String team, PrintWriter sender, BufferedReader receiver){
        super(team);
        this.sender = sender;
        this.receiver = receiver;
    }
    
    /**
     * Sends a move to the other player, if it's a network game
     * @param startIndex The start index of the move
     * @param endIndex The end index of the move
     */
    public void sendMove(int startIndex, int endIndex){
        if (isNetworkGame() && !team.equals(currentBoard.turn)){
            sender.println("move");
            sender.println(startIndex);
            sender.println(endIndex);
            sender.flush();
        }
    }
    
    /**
     * Sends a pawn promotion move to the other player. Only sends if it's a network game
     * @param startIndex The start index of the pawn
     * @param endIndex The end index of the pawn
     * @param pawnUpgrade String representing the piece being upgraded to
     */
    public void sendMove(int startIndex, int endIndex, String pawnUpgrade){
        if (isNetworkGame() && !team.equals(currentBoard.turn)){
            sender.println("promotion");
            sender.println(startIndex);
            sender.println(endIndex);
            sender.println(pawnUpgrade);
            sender.flush();
        }
    }
    
    /**
     * Tells the other player that this player is stopping the game. Only sends if
     * it's a network game.
     */
    public void sendStop(){
        if (isNetworkGame()){
            sender.println("stop");
            sender.flush();
        }
    }
    
    /**
     * Start listening for a move from the network. Move will be applied
     * once heard.
     * @param disp The display to update once a move head been heard
     */
    public void listenForMove(Display disp){
        MoveListener moveListener = new MoveListener(disp, receiver);
        moveListener.start();
    }
    
    /**
     * Given the fen index of the piece being moved and the
     * fen index of where it is being moved to, attempts to
     * execute that move. Returns true if the move succeeded.
     * @param startIndex the starting index of the piece
     * @param endIndex The ending index of the piece
     * @return true if the move was successful
     */
    @Override
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
                        sendMove(startIndex, endIndex);
                    } else if (draw()){
                        inProgress = false;
                        isDraw = true;
                        Move winnerUpdate = new Move();
                        char winner = 'd';
                        winnerUpdate.addChange(64, winner);
                        currentBoard.forceFenUpdate(winnerUpdate);
                        System.out.println("It's a draw!");
                        sendMove(startIndex, endIndex);
                    }
                    if (!currentBoard.promotingPawn){
                        fens.add(currentBoard.fen);
                        sendMove(startIndex, endIndex);
                    } else {
                        //save to send later
                        lastStartIndex = startIndex;
                        lastEndIndex = endIndex;
                    }
                    return true;
                }
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
    @Override
    public void promotePawn(String input){
        super.promotePawn(input);
        sendMove(lastStartIndex, lastEndIndex, input);
    }
    
    /**
     * 
     * @return whether or not this is a network game.
     */
    @Override
    public boolean isNetworkGame(){
        return true;
    }
}
