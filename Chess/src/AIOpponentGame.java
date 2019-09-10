import java.util.ArrayList;


public class AIOpponentGame extends Game implements GameWithOpponent {
    
    private String team;
    private MiniMax ai;

    @Override
    public String getTeam() {
        return team;
    }
    
    /**
     * Creates a new game using the default fen and given team
     * @param team The team, "white" or "black", that this player
     *             is playing as
     */
    public AIOpponentGame(String team){
        super();
        this.team = team;
        ai = new MiniMax();
    }
    
    /**
     * Creates a new game using the given fen list and given team
     * @param team The team, "white" or "black", that this player
     *             is playing as
     * @param fens The fen list for the game up until this point
     */
    public AIOpponentGame(String team, ArrayList<String> fens){
        super(fens);
        this.team = team;
        ai = new MiniMax();
    }
    
    /**
     * Applies the AI move.
     * @param disp The display to update 
     */
    @Override
    public void doOpponentMove(Display disp) {
        Move aiMove = ai.doMiniMax(disp.currentGame.currentBoard);
        if (aiMove.isPawnPromotion) {
            disp.attemptPawnPromotion("" + aiMove.getPromotionPiece());
        } else {
            int startIndex = aiMove.getStartIndex();
            int endIndex = aiMove.getEndIndex();
            disp.attemptMove(startIndex, endIndex);
        }
    }
    
    /**
     * Returns true if it is the human player's turn
     */
    @Override
    public boolean isLocalPlayersTurn() {
        return getTeam().equals(currentBoard.turn());
    }
}
