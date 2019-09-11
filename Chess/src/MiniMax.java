import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class MiniMax {
    
    public static final int DEPTH = 4; //should always be greater than 0
    public static final int SPLIT_DEPTH = 4; //the shallowest depth at which to split threads
    
    MaxSizeHashMap<String, List<Move>> fenMoveCache;
    int total = 0;
    int hit = 0;
    
  //map from fen to pair of score and remaining depth that score was determined at
    //Map<String, Pair<Integer, Integer>> boardMap;
    
    public MiniMax() {
        fenMoveCache = new MaxSizeHashMap<String, List<Move>>(5000);
    }
    
    /**
     * Entry point for running minimax
     * @param board Board to run on
     * @return Best move for player whose turn it is
     */
    public Move doMiniMax(Board board) {
      long timeInitial = System.currentTimeMillis();
      Move m = doMiniMax(board, DEPTH).first;
      long finishedTime = System.currentTimeMillis();
      System.out.println(finishedTime - timeInitial);
      //System.out.println(hit * 1.0 / total);
      return m;
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
            List<Move> moves = fenMoveCache.get(board.fen);
            if (moves == null) {
                moves = board.generateMoves();
                fenMoveCache.put(board.fen, moves);
            } else {
                hit++;
            }
            total++;
            BestMoveGetter bmg;
            if (board.turn().equals("white")){
                bmg = new WhiteBestMoveGetter();
            } else {
                bmg = new BlackBestMoveGetter();
            }
            if (false && remainingDepth >= SPLIT_DEPTH) {
                List<Pair<Thread, MiniMaxRunnable>> threadList = new ArrayList<Pair<Thread, MiniMaxRunnable>>();
                for (Move move : moves) {
                    Board copiedBoard = board.copyBoard();
                    copiedBoard.applyMove(move);
                    MiniMaxRunnable mmr = new MiniMaxRunnable(copiedBoard, remainingDepth - 1);
                    Thread t = new Thread(mmr);
                    t.start();
                    threadList.add(new Pair<Thread, MiniMaxRunnable>(t, mmr));  
                }
                for (int i = 0; i < threadList.size(); i++) {
                    try {
                        threadList.get(i).first.join();
                    } catch (InterruptedException e) {e.printStackTrace();}
                    bmg.updateIfBetter(moves.get(i), threadList.get(i).second.moveScorePair.second);
                }
            } else {
                // go through each move. every time we find a better move, save it
                for (Move move : moves) {
                    Board copiedBoard = board.copyBoard();
                    copiedBoard.applyMove(move);
                    Pair<Move, Integer> result = doMiniMax(copiedBoard, remainingDepth - 1);
                    bmg.updateIfBetter(move, result.second);
                }
            }
            return bmg.getBestMoveScorePair();
        }
    }
    
    /**
     * Helper classes
     */
    
    class MiniMaxRunnable implements Runnable {
        public Pair<Move, Integer> moveScorePair;
        public Board board;
        public int depth;
        
        public MiniMaxRunnable(Board board, int depth) {
            this.board = board;
            this.depth = depth;
        }
        
        @Override
        public void run() {
            moveScorePair = MiniMax.this.doMiniMax(board, depth);   
        }
    }
    
    /**
     * So I can return both a move and score
     */
    class Pair<T1, T2> {
        public T1 first;
        public T2 second;
        
        public Pair(T1 first, T2 second){
            this.first = first;
            this.second = second;
        }
    }
    
    /**
     * Interface for team specific BestMoveGetters
     */
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
    
    public class MaxSizeHashMap<K, V> extends LinkedHashMap<K, V> {
        private final int maxSize;

        public MaxSizeHashMap(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxSize;
        }
    }
}



//long timeInitial = System.nanoTime();
//Move m = doMiniMax(board, DEPTH).first;
//long finishedTime = System.nanoTime();
//System.out.println(finishedTime - timeInitial);
//System.out.println(movesEvaluated);