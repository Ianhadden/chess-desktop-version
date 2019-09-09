/**
 * Class to store a pair of Move and its associated score
 * @author Ian
 *
 */
public class MoveScorePair {
    
    public int score;
    public Move move;

    /**
     * Creates a new MoveScorePair with the given move and score
     * @param m The move
     * @param score The score
     */
    public MoveScorePair(Move move, int score){
        this.move = move;
        this.score = score;
    }
}
