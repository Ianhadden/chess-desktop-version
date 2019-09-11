import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Board {
    String fen; // the fen for this board
    String oldFen; // the immediately previous fen
    String oldOldFen; // fen before that one
    
    /**
     * Creates a new Board using the given fen
     * @param startFen
     */
    public Board(String startFen){
        this.fen = startFen;
    }
    
    /**
     * Creates and returns a deep copy of this board
     * @return the copied Board
     */
    public Board copyBoard(){
        Board copy = new Board(fen);
        copy.oldFen = this.oldFen;
        copy.oldOldFen = this.oldOldFen;
        return copy;
    }
    
    /**
     * Returns a Position representing a board position given a fenIndex
     * @param fenIndex The fenIndex of the piece
     * @return the Position
     */
    public Position position(int fenIndex){
        return new Position((fenIndex / 8) + 1, (fenIndex % 8) + 1);
    }
    
    /**
     * Returns the fenIndex of a position on the board
     * @param pos The position
     * @return The fenIndex
     * @pre Must be a valid board position
     */
    public int fenIndex (Position pos){
        return pos.x - 1 + (pos.y - 1) * 8;
    }
    
    /**
     * Given the index in the fen of a piece, returns the owner
     * @param fenIndex The index in the fen of the piece
     * @return The owner of the piece, "white" or "black"
     * @pre -1 < fenIndex < 64
     */
    public String owner(int fenIndex){
        if (fen.charAt(fenIndex) == '-'){
            return "empty";
        } else if (Character.toUpperCase(fen.charAt(fenIndex)) == fen.charAt(fenIndex)){
            return "black";
        } else {
            return "white";
        }
    }
    
    /**
     * Given the position on the grid of a piece, returns the owner
     * @param pos The position on the board
     * @return "white" or "black"
     */
    public String owner(Position pos){
        return owner(fenIndex(pos));
    }
    
    /**
     * Applies a move to the board
     * @param m The move to be applied
     */
    public void applyMove(Move m){
        //String workingFen = fen;
        //apply updates
        /*
        for (BoardUpdate b : m.changes){
            String preceding = workingFen.substring(0, b.fenIndex);
            String following = workingFen.substring(b.fenIndex + 1, 74);
            workingFen = preceding + b.newValue + following;
        }
        */
        oldOldFen = oldFen;
        oldFen = fen;
        //fen = workingFen;
        
        fen = m.applyToFen(fen);
    }
    
    /**
     * Undoes the last move applied to the board
     */
    public void undoMove(){
        fen = oldFen;
        oldFen = oldOldFen;
    }
    
    /**
     * Returns a list of all possible moves on the board.
     * Does not include moves that would put the player in check
     * @return a list of all possible moves on the board
     */
    public List<Move> generateMoves() {
        List<Move> moves = generateMovesCore(false);
        return moves.stream().filter(m -> !movePutsPlayerInCheck(turn(), m)).collect(Collectors.toList());
    }
    
    /**
     * Generates the list of moves, including those that would put the moving player in check
     * @ignoreCastlingCheckChecks Normally you can only castle if the spaces in between the king
     *                            and where it is moving to would not be check for the king, and its
     *                            current space is not check. If true, this ignores those checks.
     * @return List of moves
     */
    public List<Move> generateMovesCore(boolean ignoreCastlingCheckChecks) {
        ArrayList<Move> moves = new ArrayList<Move>();
        
        // if the player whose turn it is is promoting a pawn, return pawn promotion moves
        // need to check that these values are the same because sometimes we check the opponent's
        // moves even when you're in the middle of promoting.
        if (turn().equals(promotingPawn())) {
            addPawnPromotionMoves(moves);
            return moves;
        }
        for (int i = 0; i < 64; i++){
            if (owner(i).equals(turn())){
                char c = Character.toLowerCase(fen.charAt(i));
                if (c == 'h'){
                    addHorseMoves(moves, i);
                } else if (c == 'p'){
                    addPawnMoves(moves, i);
                } else if (c == 'r'){
                    addRookMoves(moves, i);
                } else if (c == 'b'){
                    addBishopMoves(moves, i);
                } else if (c == 'q'){
                    addRookMoves(moves, i);
                    addBishopMoves(moves, i);
                } else if (c == 'k'){
                    addKingMoves(moves, i, ignoreCastlingCheckChecks);
                }
            }
        }
        return moves;
    }
    
    /**
     * If a player is in the middle of promoting a pawn, returns that player. null otherwise
     * @return "white", "black" or null
     */
    public String promotingPawn() {
        for (int i = 0; i < 8; i++) {
            if (fen.charAt(i) == 'P') {
                return "black";
            }
        }
        for (int i = 56; i < 64; i++) {
            if (fen.charAt(i) == 'p') {
                return "white";
            }
        }
        return null;
    }
    
    /**
     * Checks if the given move puts the given player in check
     * @param player The player whose check status is being checked
     * @param m The move to check
     * @return true if move puts them in check. else false
     */
    public boolean movePutsPlayerInCheck(String player, Move m) {
        applyMove(m);
        boolean check = playerInCheck(player);
        undoMove();
        return check;
    }
    
    /**
     * Returns true if the given player is in check on given current board
     * @param b The board to check
     * @param player The player to test on
     * @return true if in check (or checkmate)
     */
    public boolean playerInCheck(String player) {
        String trueTurn = turn();
        char king;
        Move switchTurn = new Move();
        if ("white".equals(player)){
            switchTurn.addChange(FenUtility.TURN, 'b');
            king = 'k';
        } else {
            switchTurn.addChange(FenUtility.TURN, 'w');
            king = 'K';
        }
        forceFenUpdate(switchTurn);
        int kingIndex = -1; //initialized so compiler will shut up
        for (int i = FenUtility.BOARD_START; i <= FenUtility.BOARD_END; i++){
            if (fen.charAt(i) == king){
                kingIndex = i;
                break;
            }
        }
        Move switchTurnBack = new Move();
        switchTurnBack.addChange(FenUtility.TURN, trueTurn.charAt(0));
        List<Move> movesList = generateMovesCore(true);
        //pawn promotions don't attack the king
        if (movesList.size() > 0 && movesList.get(0).isPawnPromotion) {
            return false;
        }
        for (Move m : movesList) {
            if (m.getEndIndex() == kingIndex){
                forceFenUpdate(switchTurnBack);
                return true;
            }
        }
        forceFenUpdate(switchTurnBack);
        return false;
    }
    
    /**
     * Returns true if every move the current player can make would result
     * in their being in check.
     * @return true if every move the current player can make would result
     * in their being in check.
     */
    public boolean everyMoveIsCheck(){
        String currentPlayer = turn();
        List<Move> movesList = generateMoves();
        for (Move m : movesList){
            applyMove(m);
            if (!playerInCheck(currentPlayer)){
                undoMove();
                return false;
            } else {
                undoMove();
            }
        }
        return true;
    }
    
    /**
     * Returns true if the current player is checkmated
     * @return true if the current player is checkmated
     */
    public boolean checkMate(){
        return playerInCheck(turn()) && everyMoveIsCheck();
    }
    
    /**
     * Returns true if the current player is stalemated
     * @return true if the current player is stalemated
     */
    public boolean staleMate(){
        return (!playerInCheck(turn()) && everyMoveIsCheck());
    }
    
    /**
     * Returns true if the board setup is a draw
     * @return true if the board setup is a draw
     */
    public boolean draw(){
        if (staleMate()){
            return true;
        }
        int horseCount = 0;
        //bishop counts
        int wow = 0; // white on white
        int wob = 0; // white on black
        int bow = 0; // black on white
        int bob = 0; // black on black
        for (int i = FenUtility.BOARD_START; i <= FenUtility.BOARD_END; i++){
            char c = fen.charAt(i);
            if (c == 'p' || c == 'P' || c == 'r' || c == 'R' || c == 'Q' || c == 'q'){
                return false;
            } else if (c == 'h' || c == 'H'){
                horseCount++;
            } else if (c == 'b'){
                Position pos = position(i);
                if ((pos.x + pos.y) % 2 == 0){// 0 == black, 1 == white
                    wob++;
                } else {
                    wow++;
                }
            } else if (c == 'B'){
                Position pos = position(i);
                if ((pos.x + pos.y) % 2 == 0){// 0 == black, 1 == white
                    bob++;
                } else {
                    bow++;
                }
            }
            if (horseCount > 1){
                return false;
            }
        }
        int totalBishops = wow + wob + bow + bob;
        if (totalBishops + horseCount <= 1){
            return true;
        } else if (horseCount == 0){
            if (wow > 0 && bow > 0 && wob == 0 && bob == 0){
                return true;
            }
            if (wob > 0 && bob > 0 && wow == 0 && bow == 0){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Forces changes to the current fen. Cannot be undone.
     * @param m The move containing the changes
     */
    public void forceFenUpdate(Move m){
        fen = m.applyToFen(fen);
    }
    
    /**
     * Adds king moves to the moves list given the fenIndex of the
     * king being moved and the moves list
     * @param moves The moves list
     * @param i The fenIndex of the king
     * @param ignoreCheckChecks Normally you can only castle if the spaces in between the king
     *                          and where it is moving to would not be check for the king, and its
     *                          current space is not check. If true, this ignores those checks.
     */
    public void addKingMoves(ArrayList<Move> moves, int i, boolean ignoreCheckChecks){
        int kingHasMoved, leftRookHasMoved, rightRookHasMoved;
        Position pos = position(i);
        addKingMoveIfValid(moves, i, new Position(pos.y + 1, pos.x + 1));
        addKingMoveIfValid(moves, i, new Position(pos.y + 1, pos.x));
        addKingMoveIfValid(moves, i, new Position(pos.y + 1, pos.x - 1));
        addKingMoveIfValid(moves, i, new Position(pos.y, pos.x - 1));
        addKingMoveIfValid(moves, i, new Position(pos.y - 1, pos.x - 1));
        addKingMoveIfValid(moves, i, new Position(pos.y - 1, pos.x));
        addKingMoveIfValid(moves, i, new Position(pos.y - 1, pos.x + 1));
        addKingMoveIfValid(moves, i, new Position(pos.y, pos.x + 1));
        if (turn().equals("white")){
            leftRookHasMoved = FenUtility.WHITE_LEFT_ROOK_HAS_MOVED;
            kingHasMoved = FenUtility.WHITE_KING_HAS_MOVED;
            rightRookHasMoved = FenUtility.WHITE_RIGHT_ROOK_HAS_MOVED;
        } else {
            leftRookHasMoved = FenUtility.BLACK_LEFT_ROOK_HAS_MOVED;
            kingHasMoved = FenUtility.BLACK_KING_HAS_MOVED;
            rightRookHasMoved = FenUtility.BLACK_RIGHT_ROOK_HAS_MOVED;
        }
        //castling left
        if (fen.charAt(kingHasMoved) == 'f' && fen.charAt(leftRookHasMoved) == 'f'){
            Position leftRookPos = new Position(pos.y, pos.x - 4);
            Position spaceBetween1 = new Position(pos.y, pos.x - 3);
            Position spaceBetween2 = new Position(pos.y, pos.x - 2);
            Position spaceBetween3 = new Position(pos.y, pos.x - 1);
            if (owner(leftRookPos).equals(turn()) && fen.charAt(fenIndex(spaceBetween1)) == '-' &&
                fen.charAt(fenIndex(spaceBetween2)) == '-' && fen.charAt(fenIndex(spaceBetween3)) == '-' &&
                ((ignoreCheckChecks) || !playerInCheck(turn()))){
                boolean inCheck;
                if (ignoreCheckChecks) {
                    inCheck = false;
                } else {
                    //checking that the spot in between isn't check
                    Move nudgeLeft = createStandardMove(i, spaceBetween3);
                    nudgeLeft.addChange(kingHasMoved, 't');
                    String mover = turn();
                    applyMove(nudgeLeft);
                    inCheck = playerInCheck(mover);
                    undoMove();
                }
                if (!inCheck){ //finally adding the move
                    Move m = createStandardMove(i, position(i - 2));
                    m.addChange(fenIndex(leftRookPos), '-');
                    m.addChange(fenIndex(new Position(pos.y, pos.x - 1)), fen.charAt(fenIndex(leftRookPos)));
                    m.addChange(kingHasMoved, 't');
                    m.addChange(leftRookHasMoved, 't');
                    moves.add(m);
                }
            }
        }
        //castling right
        if (fen.charAt(kingHasMoved) == 'f' && fen.charAt(rightRookHasMoved) == 'f'){
            Position rightRookPos = new Position(pos.y, pos.x + 3);
            Position spaceBetween1 = new Position(pos.y, pos.x + 2);
            Position spaceBetween2 = new Position(pos.y, pos.x + 1);
            if (owner(rightRookPos).equals(turn()) && fen.charAt(fenIndex(spaceBetween1)) == '-' &&
                fen.charAt(fenIndex(spaceBetween2)) == '-' && (ignoreCheckChecks || !playerInCheck(turn()))){
                boolean inCheck;
                if (ignoreCheckChecks) {
                    inCheck = false;
                } else {
                    //checking that the spot in between isn't check
                    Move nudgeRight = createStandardMove(i, spaceBetween2);
                    nudgeRight.addChange(kingHasMoved, 't');
                    String mover = turn();
                    applyMove(nudgeRight);
                    inCheck = playerInCheck(mover);
                    undoMove();
                }
                if (!inCheck){ //finally adding the move
                    Move m = createStandardMove(i, position(i + 2));
                    m.addChange(fenIndex(rightRookPos), '-');
                    m.addChange(fenIndex(new Position(pos.y, pos.x + 1)), fen.charAt(fenIndex(rightRookPos)));
                    m.addChange(kingHasMoved, 't');
                    m.addChange(rightRookHasMoved, 't');
                    moves.add(m);
                }
            }
        }
    }
    
    /**
     * Adds bishop moves to the moves list given the fenIndex
     * of the bishop(or queen) and the moves list
     * @param moves The moves list
     * @param i The fenIndex of the bishop(or queen) being moved
     */
    public void addBishopMoves(ArrayList<Move> moves, int i){
        Position pos = position(i);
        String enemy = turn().equals("white")? "black" : "white";
        for (int j = 1; j < 8; j++){
            Position dest = new Position(pos.y + j, pos.x + j);
            if (inBounds(dest) && !owner(fenIndex(dest)).equals(turn())){
                moves.add(createStandardMove(i, dest));
                if (owner(fenIndex(dest)).equals(enemy)){
                    break;
                }
            } else {
                break;
            }
        }
        for (int j = 1; j < 8; j++){
            Position dest = new Position(pos.y - j, pos.x - j);
            if (inBounds(dest) && !owner(fenIndex(dest)).equals(turn())){
                moves.add(createStandardMove(i, dest));
                if (owner(fenIndex(dest)).equals(enemy)){
                    break;
                }
            } else {
                break;
            }
        }
        for (int j = 1; j < 8; j++){
            Position dest = new Position(pos.y + j, pos.x - j);
            if (inBounds(dest) && !owner(fenIndex(dest)).equals(turn())){
                moves.add(createStandardMove(i, dest));
                if (owner(fenIndex(dest)).equals(enemy)){
                    break;
                }
            } else {
                break;
            }
        }
        for (int j = 1; j < 8; j++){
            Position dest = new Position(pos.y - j, pos.x + j);
            if (inBounds(dest) && !owner(fenIndex(dest)).equals(turn())){
                moves.add(createStandardMove(i, dest));
                if (owner(fenIndex(dest)).equals(enemy)){
                    break;
                }
            } else {
                break;
            }
        }
    }
    
    /**
     * Adds a has moved changed to a rook move if applicable
     * @param m The move
     * @param i The fenIndex of the piece being moved
     */
    public void addRookHasMovedIfApplicable(Move m, int i){
        if (Character.toLowerCase(fen.charAt(i)) == 'r'){
            if (turn().equals("white")){
                if (i == 0){
                    m.addChange(FenUtility.WHITE_LEFT_ROOK_HAS_MOVED, 't');
                } else if (i == 7){
                    m.addChange(FenUtility.WHITE_RIGHT_ROOK_HAS_MOVED, 't');
                }
            } else {
                if (i == 56){
                    m.addChange(FenUtility.BLACK_LEFT_ROOK_HAS_MOVED, 't');
                } else if (i == 63){
                    m.addChange(FenUtility.BLACK_RIGHT_ROOK_HAS_MOVED, 't');
                }
            }
        }
    }
    
    /**
     * Adds rook moves to the moves list given the fenIndex
     * of the rook(or queen) and the moves list
     * @param moves The moves list
     * @param i The fenIndex of the rook(or queen) being moved
     */
    public void addRookMoves(ArrayList<Move> moves, int i){
        Position pos = position(i);
        String enemy = turn().equals("white")? "black" : "white";
        for (int j = 1; j < 8; j++){
            Position dest = new Position(pos.y + j, pos.x);
            if (inBounds(dest) && !owner(fenIndex(dest)).equals(turn())){
                Move m = createStandardMove(i, dest);
                addRookHasMovedIfApplicable(m, i);
                moves.add(m);
                if (owner(fenIndex(dest)).equals(enemy)){
                    break;
                }
            } else {
                break;
            }
        }
        for (int j = 1; j < 8; j++){
            Position dest = new Position(pos.y - j, pos.x);
            if (inBounds(dest) && !owner(fenIndex(dest)).equals(turn())){
                Move m = createStandardMove(i, dest);
                addRookHasMovedIfApplicable(m, i);
                moves.add(m);
                if (owner(fenIndex(dest)).equals(enemy)){
                    break;
                }
            } else {
                break;
            }
        }
        for (int j = 1; j < 8; j++){
            Position dest = new Position(pos.y, pos.x + j);
            if (inBounds(dest) && !owner(fenIndex(dest)).equals(turn())){
                Move m = createStandardMove(i, dest);
                addRookHasMovedIfApplicable(m, i);
                moves.add(m);
                if (owner(fenIndex(dest)).equals(enemy)){
                    break;
                }
            } else {
                break;
            }
        }
        for (int j = 1; j < 8; j++){
            Position dest = new Position(pos.y, pos.x - j);
            if (inBounds(dest) && !owner(fenIndex(dest)).equals(turn())){
                Move m = createStandardMove(i, dest);
                addRookHasMovedIfApplicable(m, i);
                moves.add(m);
                if (owner(fenIndex(dest)).equals(enemy)){
                    break;
                }
            } else {
                break;
            }
        }
    }
    
    /**
     * Adds pawn moves to the moves list given the fenIndex of
     * the pawn and the moves list.
     * @param moves The moves list
     * @param i The fenIndex of the pawn being moved
     */
    public void addPawnMoves(ArrayList<Move> moves, int i){
        //Variables vary depending on whose turn it is
        int switcher = turn().equals("white")? 1 : -1;
        int pawnLine = turn().equals("white")? 2 : 7;
        String enemy = turn().equals("white")? "black" : "white";

        Position pos = position(i);
        Position inFront = new Position(pos.y + switcher, pos.x);
        if (inBounds(inFront) && fen.charAt(fenIndex(inFront)) == '-'){
            if (inFront.y == 1 || inFront.y == 8) {
                moves.add(createMoveNoTurnChange(i, inFront)); //will pawn promote so don't switch turn
            } else {
                moves.add(createStandardMove(i, inFront));
            }
        }
        Position frontLeft = new Position(pos.y + switcher, pos.x - 1);
        if (inBounds(frontLeft) && owner(fenIndex(frontLeft)).equals(enemy)){
            moves.add(createStandardMove(i, frontLeft));
            if (frontLeft.y == 1 || frontLeft.y == 8) {
                moves.add(createMoveNoTurnChange(i, frontLeft));
            } else {
                moves.add(createStandardMove(i, frontLeft));
            }
        }
        Position frontRight = new Position(pos.y + switcher, pos.x + 1);
        if (inBounds(frontRight) && owner(fenIndex(frontRight)).equals(enemy)){
            moves.add(createStandardMove(i, frontRight));
            if (frontRight.y == 1 || frontRight.y == 8) {
                moves.add(createMoveNoTurnChange(i, frontRight));
            } else {
                moves.add(createStandardMove(i, frontRight));
            }
        }
        //where this piece would double jump to
        Position doubleJump = new Position(pos.y + (2 * switcher), pos.x);
        if (inBounds(doubleJump) && fen.charAt(fenIndex(doubleJump)) == '-' && pos.y == pawnLine){
            Move m = new Move();
            m.addChange(i, '-');
            m.addChange(fenIndex(doubleJump), fen.charAt(i));
            m.addChange(turnChange());
            m.addChange(FenUtility.DOUBLE_JUMPER_Y, (char) (doubleJump.y + 48)); //Add 48 to get a proper char cast
            m.addChange(FenUtility.DOUBLE_JUMPER_X, (char) (doubleJump.x + 48)); //ie to get '1' from 1 (because ascii)
            moves.add(m);
        }
        //where the piece last turn double jumped to (or (0, 0) if there was no double jump last turn)
        doubleJump = new Position(Character.getNumericValue(fen.charAt(FenUtility.DOUBLE_JUMPER_Y)),
                                  Character.getNumericValue(fen.charAt(FenUtility.DOUBLE_JUMPER_X)));
        //en passant
        if (doubleJump.y == pos.y && Math.abs(pos.x - doubleJump.x) == 1 && 
                                owner(fenIndex(doubleJump)).equals(enemy)){
            Move m = new Move();
            m.addChange(i, '-');
            m.addChange(fenIndex(new Position(doubleJump.y + switcher, doubleJump.x)), fen.charAt(i));
            m.addChange(fenIndex(doubleJump), '-');
            m.addChange(turnChange());
            noDoubleJumpers(m); //because THIS move is not a double jump
            moves.add(m);
        }
    }
    
    /**
     * Scans the board for a pawn being promoted, and adds
     * the available pawn promotion moves to the moves list
     * @param moves The moves list to add to
     * @pre board state must be valid (i.e. exactly one pawn to promote)
     */
    public void addPawnPromotionMoves(ArrayList<Move> moves) {
        char[] upgrades = {'r', 'b', 'h', 'q'};
        int pawnIndex = -1;
        //scan bottom or top row to find pawn
        if (turn().equals("black")) {
            for (int i = FenUtility.BOARD_START; i <= FenUtility.BOARD_FIRST_ROW_END; i++){
                if (fen.charAt(i) == 'P') {
                    pawnIndex = i;
                    break;
                }
            }
            for (int i = 0; i < upgrades.length; i++) {
                Move m = new Move(true);
                m.addChange(pawnIndex, Character.toUpperCase(upgrades[i]));
                m.addChange(turnChange());
                noDoubleJumpers(m);
                moves.add(m);
            }
        } else {
            for (int i = FenUtility.BOARD_LAST_ROW_START; i <= FenUtility.BOARD_END; i++){
                if (fen.charAt(i) == 'p') {
                    pawnIndex = i;
                    break;
                }
            }
            for (int i = 0; i < upgrades.length; i++) {
                Move m = new Move(true);
                m.addChange(pawnIndex, upgrades[i]);
                m.addChange(turnChange());
                noDoubleJumpers(m);
                moves.add(m);
            }
        }
    }
    
    /**
     * Given a fenIndex i of the piece being moved and its end position,
     * returns a Move object representing that move. This is a standard
     * move in that it includes update to the starting position, ending position,
     * a turn change, and no double jumpers.
     * @param i The fenIndex of the piece being moved
     * @param endPos The end destination of the move
     * @return Move m The move
     */
    public Move createStandardMove(int i, Position endPos){
        Move m = createMoveNoTurnChange(i, endPos);
        m.addChange(turnChange());
        return m;
    }
    
    /**
     * Given a fenIndex i of the piece being moved and its end position,
     * returns a Move object representing that move. This is a 
     * move that includes update to the starting position, ending position,
     * and no double jumpers.
     * @param i The fenIndex of the piece being moved
     * @param endPos The end destination of the move
     * @return Move m The move
     */
    public Move createMoveNoTurnChange(int i, Position endPos){
        Move m = new Move();
        m.addChange(i, '-');
        m.addChange(fenIndex(endPos), fen.charAt(i));
        noDoubleJumpers(m);
        return m;
    }
    
    /**
     * Adds horse moves to the moves list given that list and
     * the fenIndex of the horse.
     * @pre The horse belongs to the player whose turn it is
     * @param moves The list of moves to be expanded
     * @param i The fenIndex of the horse making moves
     */
    public void addHorseMoves(ArrayList<Move> moves, int i){
        Position pos = position(i);
        addMoveIfValid(moves, i, new Position(pos.y + 2, pos.x + 1));
        addMoveIfValid(moves, i, new Position(pos.y - 2, pos.x + 1));
        addMoveIfValid(moves, i, new Position(pos.y - 2, pos.x - 1));
        addMoveIfValid(moves, i, new Position(pos.y + 2, pos.x - 1));
        addMoveIfValid(moves, i, new Position(pos.y + 1, pos.x + 2));
        addMoveIfValid(moves, i, new Position(pos.y - 1, pos.x + 2));
        addMoveIfValid(moves, i, new Position(pos.y + 1, pos.x - 2));
        addMoveIfValid(moves, i, new Position(pos.y - 1, pos.x - 2));
    }
    
    /**
     * Adds a move to the moves list given that list, the fenIndex
     * of the piece being moved, and it's ending position. Validity check
     * is based on the end destination being in bounds and the piece in the
     * destination not belonging to the mover
     * @param moves The list of moves to be expanded
     * @param i The fenIndex of the horse moving
     * @param endPos The position of the piece after the move is made
     */
    public void addMoveIfValid(ArrayList<Move> moves, int i, Position endPos){
        if (inBounds(endPos) && !(turn().equals(owner(endPos)))){
            moves.add(createStandardMove(i, endPos));
        }
    }
    
    /**
     * Same as method above except it also sets king has-moved to true
     * @param moves
     * @param i
     * @param endPos
     */
    public void addKingMoveIfValid(ArrayList<Move> moves, int i, Position endPos){
        int hasMovedIndex = 
                turn().equals("white")? FenUtility.WHITE_KING_HAS_MOVED : FenUtility.BLACK_KING_HAS_MOVED;
        if (inBounds(endPos) && !(turn().equals(owner(endPos)))){
            Move m = createStandardMove(i, endPos);
            m.addChange(hasMovedIndex, 't');
            moves.add(m);
        }
    }
    
    /**
     * Returns a BoardUpdate which when attached to a move will switch whose turn it is
     * @returna a BoardUpdate which when attached to a move will switch whose turn it is
     */
    public BoardUpdate turnChange(){
        char newTurn;
        if (turn().equals("white")){
            newTurn = 'b';
        } else {
            newTurn = 'w';
        }
        return new BoardUpdate(FenUtility.TURN, newTurn);
    }
    
    /**
     * Adds BoardUpdates to the given move to indicate that the move is not a pawn double jump
     * @param m
     */
    public void noDoubleJumpers(Move m){
        m.addChange(new BoardUpdate(FenUtility.DOUBLE_JUMPER_Y, '0'));
        m.addChange(new BoardUpdate(FenUtility.DOUBLE_JUMPER_X, '0'));
    }
    
    /**
     * Returns true if the given y and x are within the bounds of the board
     * @param y coordinate
     * @param x coordinate
     * @return true if the given y and x are within the bounds of the board
     */
    public static boolean inBounds(int y, int x){
        return ( x > 0 && x < 9 && y > 0 && y < 9);
    }
    
    /**
     * Returns true if the given Position is within the bounds of the board
     * @param pos The position
     * @return true if the given Position is within the bounds of the board
     */
    public static boolean inBounds(Position pos){
        return (pos.x > 0 && pos.x < 9 && pos.y > 0 && pos.y < 9);
    }
    
    /**
     * Returns "white" or "black" based upon whose turn it is
     * @return "white or "black"
     */
    public String turn() {
        if (fen.charAt(FenUtility.TURN) == 'w') {
            return "white";
        } else {
            return "black";
        }
    }
}
