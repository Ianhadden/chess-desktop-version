import java.util.ArrayList;

public class Board {
    Position doubleJumper;
    String turn;
    String fen;
    String oldFen;
    boolean promotingPawn;
            
    public Board(String startFen){
        this.fen = startFen;
        updateBoardFromFen(startFen);
        promotingPawn = false;
    }
    
    public void updateBoardFromFen(String fen){
        /*
        for (int i = 0; i < 64; i++){
            int y = (i / 8) + 1;
            int x = (i % 8) + 1;
            grid[y][x] = fen.charAt(i);
        }
        */
        if (fen.charAt(65) == 'w'){
            turn = "white";
        } else {
            turn = "black";
        }
    }
    
    public Position position(int fenIndex){
        return new Position((fenIndex / 8) + 1, (fenIndex % 8) + 1);
    }
    
    public int fenIndex (Position pos){
        return pos.x - 1 + (pos.y - 1) * 8;
    }
    
    //Given the index in the fen of a piece, returns the owner
    public String owner(int fenIndex){
        if (fen.charAt(fenIndex) == '-'){
            return "empty";
        } else if (Character.toUpperCase(fen.charAt(fenIndex)) == fen.charAt(fenIndex)){
            return "black";
        } else {
            return "white";
        }
    }
    
    //Given the position on the grid of a piece, returns the owner
    public String owner(Position pos){
        return owner(fenIndex(pos));
    }
    
    //apply move, skip pawn promotion test
    public void applyMoveSPP(Move m){
        String workingFen = fen;
        //apply updates
        for (BoardUpdate b : m.changes){
            String preceding = workingFen.substring(0, b.fenIndex);
            String following = workingFen.substring(b.fenIndex + 1, 74);
            workingFen = preceding + b.newValue + following;
        }
        oldFen = fen;
        fen = workingFen;
        updateBoardFromFen(fen);
    }
    
    public void applyMove(Move m){
        //check for pawn being applicable for promotion
        if (m.changes.size() > 1){
            char piece = Character.toLowerCase(m.changes.get(1).newValue);
            Position endPos = position(m.changes.get(1).fenIndex);
            if ((endPos.y == 1 || endPos.y == 8) && piece == 'p'){
                promotingPawn = true;
            }
        }
        applyMoveSPP(m);
    }
    
    public void undoMove(){
        fen = oldFen;
        updateBoardFromFen(fen);
    }
    
    //Returns an arrayList of all possible moves on the board.
    public ArrayList<Move> generateMoves(){
        ArrayList<Move> moves = new ArrayList<Move>();
        
        for (int i = 0; i < 64; i++){
            if (owner(i).equals(turn)){
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
                    addKingMoves(moves, i);
                }
            }
        }
        return moves;
    }
    
    /**
     * Adds king moves to the moves list given the fenIndex of the
     * king being moved and the moves list
     * @param moves The moves list
     * @param i The fenIndex of the king
     */
    public void addKingMoves(ArrayList<Move> moves, int i){
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
        if (turn.equals("white")){
            leftRookHasMoved = 66; //fen indexes
            kingHasMoved = 67;
            rightRookHasMoved = 68;
        } else {
            leftRookHasMoved = 69;
            kingHasMoved = 70;
            rightRookHasMoved = 71;
        }
        if (fen.charAt(kingHasMoved) == 'f' && fen.charAt(leftRookHasMoved) == 'f'){
            Position leftRookPos = new Position(pos.y, pos.x - 4);
            Position spaceBetween1 = new Position(pos.y, pos.x - 3);
            Position spaceBetween2 = new Position(pos.y, pos.x - 2);
            Position spaceBetween3 = new Position(pos.y, pos.x - 1);
            if (owner(leftRookPos).equals(turn) && fen.charAt(fenIndex(spaceBetween1)) == '-' &&
                fen.charAt(fenIndex(spaceBetween2)) == '-' && fen.charAt(fenIndex(spaceBetween3)) == '-' ){
                Move m = createStandardMove(i, position(i - 2));
                m.addChange(fenIndex(leftRookPos), '-');
                m.addChange(fenIndex(new Position(pos.y, pos.x - 1)), fen.charAt(fenIndex(leftRookPos)));
                m.addChange(kingHasMoved, 't');
                m.addChange(leftRookHasMoved, 't');
                moves.add(m);
            }
        }
        if (fen.charAt(kingHasMoved) == 'f' && fen.charAt(rightRookHasMoved) == 'f'){
            Position rightRookPos = new Position(pos.y, pos.x + 3);
            Position spaceBetween1 = new Position(pos.y, pos.x + 2);
            Position spaceBetween2 = new Position(pos.y, pos.x + 1);
            if (owner(rightRookPos).equals(turn) && fen.charAt(fenIndex(spaceBetween1)) == '-' &&
                fen.charAt(fenIndex(spaceBetween2)) == '-'){
                Move m = createStandardMove(i, position(i + 2));
                m.addChange(fenIndex(rightRookPos), '-');
                m.addChange(fenIndex(new Position(pos.y, pos.x + 1)), fen.charAt(fenIndex(rightRookPos)));
                m.addChange(kingHasMoved, 't');
                m.addChange(rightRookHasMoved, 't');
                moves.add(m);
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
        String enemy = turn.equals("white")? "black" : "white";
        for (int j = 1; j < 8; j++){
            Position dest = new Position(pos.y + j, pos.x + j);
            if (inBounds(dest) && !owner(fenIndex(dest)).equals(turn)){
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
            if (inBounds(dest) && !owner(fenIndex(dest)).equals(turn)){
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
            if (inBounds(dest) && !owner(fenIndex(dest)).equals(turn)){
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
            if (inBounds(dest) && !owner(fenIndex(dest)).equals(turn)){
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
            if (turn.equals("white")){
                if (i == 0){
                    m.addChange(66, 't');
                } else if (i == 7){
                    m.addChange(68, 't');
                }
            } else {
                if (i == 56){
                    m.addChange(69, 't');
                } else if (i == 63){
                    m.addChange(71, 't');
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
        String enemy = turn.equals("white")? "black" : "white";
        for (int j = 1; j < 8; j++){
            Position dest = new Position(pos.y + j, pos.x);
            if (inBounds(dest) && !owner(fenIndex(dest)).equals(turn)){
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
            if (inBounds(dest) && !owner(fenIndex(dest)).equals(turn)){
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
            if (inBounds(dest) && !owner(fenIndex(dest)).equals(turn)){
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
            if (inBounds(dest) && !owner(fenIndex(dest)).equals(turn)){
                Move m = createStandardMove(i, dest);
                addRookHasMovedIfApplicable(m, i);
                moves.add(m);
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
        int switcher = turn.equals("white")? 1 : -1;
        int pawnLine = turn.equals("white")? 2 : 7;
        String enemy = turn.equals("white")? "black" : "white";

        Position pos = position(i);
        Position inFront = new Position(pos.y + switcher, pos.x);
        if (inBounds(inFront) && fen.charAt(fenIndex(inFront)) == '-'){
            moves.add(createStandardMove(i, inFront));
        }
        Position frontLeft = new Position(pos.y + switcher, pos.x - 1);
        if (inBounds(frontLeft) && owner(fenIndex(frontLeft)).equals(enemy)){
            moves.add(createStandardMove(i, frontLeft));
        }
        Position frontRight = new Position(pos.y + switcher, pos.x + 1);
        if (inBounds(frontRight) && owner(fenIndex(frontRight)).equals(enemy)){
            moves.add(createStandardMove(i, frontRight));
        }
        //where this piece would double jump to
        Position doubleJump = new Position(pos.y + (2 * switcher), pos.x);
        if (inBounds(doubleJump) && fen.charAt(fenIndex(doubleJump)) == '-' && pos.y == pawnLine){
            Move m = new Move();
            m.addChange(i, '-');
            m.addChange(fenIndex(doubleJump), fen.charAt(i));
            m.addChange(turnChange());
            m.addChange(72, (char) (doubleJump.y + 48)); //Add 48 to get a proper char cast
            m.addChange(73, (char) (doubleJump.x + 48)); //ie to get '1' from 1 (because ascii)
            moves.add(m);
        }
        //where the piece last turn double jumped to (or (0, 0) if there was no double jump last turn)
        doubleJump = new Position(Character.getNumericValue(fen.charAt(72)),
                                  Character.getNumericValue(fen.charAt(73)));
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
     * Given a fenIndex i of the piece being moved and it's end position,
     * returns a Move object representing that move. This is a standard
     * move in that it includes update to the starting position, ending position,
     * a turn change, and no double jumpers.
     * @param i The fenIndex of the piece being moved
     * @param endPos The end destination of the move
     * @return Move m The move
     */
    public Move createStandardMove(int i, Position endPos){
        Move m = new Move();
        m.addChange(i, '-');
        m.addChange(fenIndex(endPos), fen.charAt(i));
        m.addChange(turnChange());
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
        if (inBounds(endPos) && !(turn.equals(owner(endPos)))){
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
        int hasMovedIndex = turn.equals("white")? 67 : 70;
        if (inBounds(endPos) && !(turn.equals(owner(endPos)))){
            Move m = createStandardMove(i, endPos);
            m.addChange(hasMovedIndex, 't');
            moves.add(m);
        }
    }
    
    public BoardUpdate turnChange(){
        char newTurn;
        if (turn.equals("white")){
            newTurn = 'b';
        } else {
            newTurn = 'w';
        }
        return new BoardUpdate(65, newTurn);
    }
    
    public void noDoubleJumpers(Move m){
        m.addChange(new BoardUpdate(73, '0'));
        m.addChange(new BoardUpdate(72, '0'));
    }
    
    public boolean inBounds(int y, int x){
        return ( x > 0 && x < 9 && y > 0 && y < 9);
    }
    
    public boolean inBounds(Position pos){
        return (pos.x > 0 && pos.x < 9 && pos.y > 0 && pos.y < 9);
    }
}
