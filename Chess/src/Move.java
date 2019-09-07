
import java.util.ArrayList;

public class Move {
    
    public ArrayList<BoardUpdate> changes;
    
    public Move(){
        changes = new ArrayList<BoardUpdate>();
    }   
    
    /**
     * Adds a change/BoardUpdate to this move. By convention,
     * the first change should be to the fenIndex of the piece being
     * moved, and the second change should be to the index of the spot
     * it is moving to.
     * @param fenIndex the index to be changed
     * @param newValue the new value to be in said index
     */
    public void addChange(int fenIndex, char newValue){
        BoardUpdate b = new BoardUpdate(fenIndex, newValue);
        changes.add(b);
    }
    
    /**
     * Adds a change/BoardUpdate to this move
     * @param b The BoardUpdate to be added.
     */
    public void addChange(BoardUpdate b){
        changes.add(b);
    }
}
