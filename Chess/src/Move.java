
import java.util.ArrayList;

public class Move {
    
    private ArrayList<BoardUpdate> changes;
    public boolean isPawnPromotion;
    
    public Move(){
        changes = new ArrayList<BoardUpdate>();
        isPawnPromotion = false;
    }
    
    public Move(boolean isPawnPromotion){
        changes = new ArrayList<BoardUpdate>();
        this.isPawnPromotion = isPawnPromotion;
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
        if (fenIndex < 0) {
            for (BoardUpdate b : changes) {
                System.out.println(b.fenIndex + " " + b.newValue);
            }
            throw new IllegalArgumentException("bad fen index: " + fenIndex);
        }
        BoardUpdate b = new BoardUpdate(fenIndex, newValue);
        changes.add(b);
    }
    
    /**
     * Applies this move to the given fen
     * @param fen The fen to apply to
     * @return The fen resulting from the move
     */
    public String applyToFen(String fen) {      
        char[] charFen = fen.toCharArray();
        for (BoardUpdate b : changes) {
            charFen[b.fenIndex] = b.newValue;
        }
        return new String(charFen);
    }
    
    /**
     * Returns the start index of this move for a non pawn promotion move
     * @return The start index of the piece of moving
     */
    public int getStartIndex() {
        if (isPawnPromotion) {
            throw new IllegalStateException();
        }
        return changes.get(0).fenIndex;
    }
    
    /**
     * Returns the end index of this move for a non pawn promotion move
     * @return The end index of the piece moving
     */
    public int getEndIndex() {
        if (isPawnPromotion) {
            throw new IllegalStateException();
        }
        return changes.get(1).fenIndex;
    }
    
    /**
     * Returns the char for the piece being moved for a non pawn promotion
     * @return the char
     */
    public char getPieceMoving() {
        if (isPawnPromotion) {
            throw new IllegalStateException();
        }
        return changes.get(1).newValue;
    }
    
    /**
     * Returns the char for the piece being upgraded to if this is a pawn promotion
     */
    public char getPromotionPiece() {
        if (!isPawnPromotion) {
            throw new IllegalStateException();
        }
        return changes.get(0).newValue;
    }
    
    /**
     * Adds a change/BoardUpdate to this move
     * @param b The BoardUpdate to be added.
     */
    public void addChange(BoardUpdate b){
        changes.add(b);
    }
}
