/**
 * A Game which has an opponent (i.e. a network game or AI game)
 * @author Ian
 *
 */
public interface GameWithOpponent {
    
    /**
     * Returns the team of the local human player
     * @return "white" or "black"
     */
    public String getTeam();
    
    /**
     * Apply the opponent's move.
     * @param disp The display to update when move is complete
     */
    public void doOpponentMove(Display disp);
    
    /**
     * Returns true if it is the local human player's turn
     */
    public boolean isLocalPlayersTurn();
}
