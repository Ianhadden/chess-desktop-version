
public class BasicBoardEvaluator {
    
    /**
     * Evaluates the given fen to give an idea of how good the board is for the player
     * @param fen The board/fen to evaluate
     * @return The score. High number = good for white. negative number = good for black
     */
    public static int evaluate(String fen) {
        int score = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++){
                char currentChar = fen.charAt(i * 8 + j);
                switch (currentChar) {
                    case 'p':
                        score += 10 + i;
                        break;
                    case 'r':
                        score += 40 + i * 2;
                        break;
                    case 'h':
                        score += 40 + i * 2;
                        break;
                    case 'b':
                        score += 40 + i * 2;
                        break;
                    case 'q':
                        score += 100 + i * 2;
                        break;
                    case 'P':
                        score -= (10 + (7 - i));
                        break;
                    case 'R':
                        score -= (40 + (7 - i) * 2);
                        break;
                    case 'H':
                        score -= (40 + (7 - i) * 2);
                        break;
                    case 'B':
                        score -= (40 + (7 - i) * 2);
                        break;
                    case 'Q':
                        score -= (100 + (7 - i) * 2);
                        break;
                    default:
                        break;
                }     
            }
        }
        return score; 
    }
}
