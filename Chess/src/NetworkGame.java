import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * For games played with someone else over the network
 */

public class NetworkGame extends Game implements GameWithOpponent {

    //used for network games
    ServerSocket serverSocket;
    BufferedReader receiver;
    PrintWriter sender;
    int lastStartIndex, lastEndIndex;
    private String team;
    
    /**
     * Creates a new game using the default fen and given team
     * @param team The team, "white" or "black", that this player
     *             is playing as
     */
    public NetworkGame(String team){
        super();
        this.team = team;
    }
    
    /**
     * Creates a new game using the given team and the last fen in the provided list
     * @param team The team, "white" or "black", that this player
     *             is playing as
     * @param fens A history of the game, with the most recent
     *             game state fen at the end of the list
     */
    public NetworkGame(String team, ArrayList<String> fens){
        super(fens);
        this.team = team;
    }
    
    /**
     * Create a new network game
     * @param team The team this player will be
     * @param fens List of previous fens from the game
     * @param sender PrintWriter to send data over network
     * @param receiver BufferedReader to read data from network
     */
    public NetworkGame(String team, ArrayList<String> fens, PrintWriter sender, BufferedReader receiver){
        this(team, fens);
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
        this(team);
        this.sender = sender;
        this.receiver = receiver;
    }
    
    /**
     * Sends a move to the other player, if it's a network game
     * @param startIndex The start index of the move
     * @param endIndex The end index of the move
     */
    public void sendMove(int startIndex, int endIndex){
        if (isNetworkGame() && !team.equals(currentBoard.turn())){
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
        if (isNetworkGame() && !team.equals(currentBoard.turn())){
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
     * once heard
     * @param disp The display to update once a move has been heard
     */
    @Override
    public void doOpponentMove(Display disp) {
        listenForMove(disp);
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
    public boolean attemptMove(int startIndex, int endIndex) {     
        boolean success = super.attemptMove(startIndex, endIndex);
        if (success) {
            if (currentBoard.promotingPawn() != null) {
                //save to send later
                lastStartIndex = startIndex;
                lastEndIndex = endIndex;
            } else {
                sendMove(startIndex, endIndex);
            }
        }
        return success;
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
    
    /**
     * Return the team name of the local player
     */
    @Override
    public String getTeam(){
        return this.team;
    }
    
    /**
     * Returns true if it is the local human player's turn
     */
    @Override
    public boolean isLocalPlayersTurn() {
        return getTeam().equals(currentBoard.turn());
    }
}
