import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MiniMax {
    
    public static final int DEPTH = 3; //should always be greater than 0
    
  //map from fen to pair of score and remaining depth that score was determined at
    //Map<String, Pair<Integer, Integer>> boardMap;
    
    public MiniMax() {
        //boardMap = new HashMap<String, Pair<Integer, Integer>>();
    }
    

    /**
     * Recursive minimax function
     * @param board The board to evaluate
     * @param remainingDepth How much deeper to evaluate
     * @return a Pair of Move and score associated with that move
     */   
    private Pair<Move, Integer> doMiniMax(Board board, int remainingDepth){
        if (remainingDepth == 0) {
            //return score of board
            return new Pair<Move, Integer>(null, BasicBoardEvaluator.evaluate(board.fen));
        } else {
            List<Move> moves = board.generateMoves();
            BestMoveGetter bmg;
            if (board.turn().equals("white")){
                bmg = new WhiteBestMoveGetter();
            } else {
                bmg = new BlackBestMoveGetter();
            }
            // go through each move. every time we find a better move, save it
            for (Move move : moves) {
                Board copiedBoard = board.copyBoard();
                copiedBoard.applyMove(move);
                Pair<Move, Integer> result = doMiniMax(copiedBoard, remainingDepth - 1);
                bmg.updateIfBetter(move, result.second);
            }
            return bmg.getBestMoveScorePair();
        }
    }
    
    public Move doMiniMax(Board board){
        //long timeInitial = System.nanoTime();
        Move m = doMiniMax(board, DEPTH).first;
        //long finishedTime = System.nanoTime();
        //System.out.println(finishedTime - timeInitial);
        //System.out.println(movesEvaluated);
        return m;
    }
    
    class Pair<T1, T2> {
        public T1 first;
        public T2 second;
        
        public Pair(T1 first, T2 second){
            this.first = first;
            this.second = second;
        }
    }
    
    interface BestMoveGetter {
        public void updateIfBetter(Move move, int score);
        public Pair<Move, Integer> getBestMoveScorePair();
    }
    
    /**
     * White always wants max score, so start with lowest possible value
     * and every time we see a better move, update 
     */
    class WhiteBestMoveGetter implements BestMoveGetter {
        int maxScore = Integer.MIN_VALUE;
        Move bestMove = null;
        
        @Override
        public void updateIfBetter(Move move, int score) {
            if (score > maxScore) {
                maxScore = score;
                bestMove = move;
            }
        }
        
        @Override
        public Pair<Move, Integer> getBestMoveScorePair() {
            return new Pair<Move, Integer>(bestMove, maxScore);
        }
    }
    
    /**
     * Black always wants lowest score, so start with max possible value
     * and every time we see a better move, update 
     */
    class BlackBestMoveGetter implements BestMoveGetter {
        int minScore = Integer.MAX_VALUE;
        Move bestMove = null;
        
        @Override
        public void updateIfBetter(Move move, int score) {
            if (score < minScore) {
                minScore = score;
                bestMove = move;
            }
        }
        
        @Override
        public Pair<Move, Integer> getBestMoveScorePair() {
            return new Pair<Move, Integer>(bestMove, minScore);
        }
    }
}
