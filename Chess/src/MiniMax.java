import java.util.ArrayList;
import java.util.List;


public class MiniMax {

    private static MoveScorePair doMiniMax(Board board, int remainingDepth){
        if (remainingDepth == 0) {
            //return score of board
            return new MoveScorePair(null, BasicBoardEvaluator.evaluate(board.fen));
        } else {
            List<Move> moves = board.generateMoves();
            Move bestMove = null;
            if (board.turn().equals("white")) {
                int maxScore = Integer.MIN_VALUE;
                //  white always wants max score. find move that yields max score
                for (Move move : moves) {
                    Board copiedBoard = board.copyBoard();
                    copiedBoard.applyMove(move);
                    MoveScorePair result = doMiniMax(copiedBoard, remainingDepth - 1);
                    if (result.score > maxScore) {
                        maxScore = result.score;
                        bestMove = move;
                    }
                }
                return new MoveScorePair(bestMove, maxScore);
            } else {
                int minScore = Integer.MAX_VALUE;
                // black always wants min score. find move that yields min score
                for (Move move : moves) {
                    Board copiedBoard = board.copyBoard();
                    copiedBoard.applyMove(move);
                    MoveScorePair result = doMiniMax(copiedBoard, remainingDepth - 1);
                    if (result.score < minScore) {
                        minScore = result.score;
                        bestMove = move;
                    }
                }
                return new MoveScorePair(bestMove, minScore);
            }
        }
    }
    
    public static Move doMiniMax(Board board){
        int depth = 3; //should always be greater than 0
        return doMiniMax(board, depth).move;
    }
}
