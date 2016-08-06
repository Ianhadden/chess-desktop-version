import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;

/**
 * Mouse Listener for the board. Deals with detecting drags from one spot to another
 * @author Ian
 *
 */
public class MouseHandler implements MouseListener, MouseMotionListener{
    boolean dragging;
    Display disp;
    int startIndex;
    JLabelPiece pieceBeingDragged;
    int savedX, savedY;
    String cursor = "pointer";
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (!dragging){ // meaning if this is the beginning of a drag
            Position startPos = getPositionFromPixelCoords(e.getY(), e.getX()); //get position on board
            if (!Board.inBounds(startPos) || disp.currentGame.currentBoard.promotingPawn ||
                                            !disp.currentGame.inProgress){
                return;
            }
            dragging = true;
            startIndex = startPos.x - 1 + (startPos.y - 1) * 8; //get fenindex from position
            
            //see if one of the player's pieces is being dragged. If so save it's position and bring it to the front
            //so we can drag it.
            for (JLabelPiece p : disp.stuffHolder.boardPieces){
                if (startPos.x == p.position.x && startPos.y == p.position.y &&
                        p.owner.equals(disp.currentGame.currentBoard.turn) ){
                    
                    disp.frame.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                            new ImageIcon("drag.png").getImage(),
                            new Point(16,12),"drag cursor"));
                    cursor = "drag";
                    pieceBeingDragged = p;
                    disp.stuffHolder.boardLayers.setLayer(p, JLayeredPane.DRAG_LAYER);
                    savedX = p.getX();
                    savedY = p.getY();
                    break;
                }
            }
        } else { // mid drag
            if (pieceBeingDragged != null){
                pieceBeingDragged.setLocation(e.getX() - 32, e.getY() - 32); //drag piece with cursor
            }
        }
        
        
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Position pos = getPositionFromPixelCoords(e.getY(), e.getX());
        boolean changed = false;
        if (!dragging && !cursor.equals("hand")){
            for (JLabelPiece p : disp.stuffHolder.boardPieces){
                if (pos.x == p.position.x && pos.y == p.position.y &&
                        p.owner.equals(disp.currentGame.currentBoard.turn) 
                        && !disp.currentGame.currentBoard.promotingPawn && disp.currentGame.inProgress){
                    disp.frame.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    cursor = "hand";
                    changed = true;
                }
            }
            
        }
        if (!changed && !dragging && !cursor.equals("pointer")){
            boolean shouldStillBeHand = false;
            for (JLabelPiece p : disp.stuffHolder.boardPieces){
                if (pos.x == p.position.x && pos.y == p.position.y &&
                        p.owner.equals(disp.currentGame.currentBoard.turn) 
                        && !disp.currentGame.currentBoard.promotingPawn && disp.currentGame.inProgress){
                    
                    shouldStillBeHand = true;
                    break;
                }
            }
            if (!shouldStillBeHand){
                disp.frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                cursor = "pointer";
            }
        } 
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {
        if (dragging){ //end of drag
            Position endPos = getPositionFromPixelCoords(e.getY(), e.getX()); // get position from pixel coords
            if (Board.inBounds(endPos)){
                int endIndex = endPos.x - 1 + (endPos.y - 1) * 8; // get fenindex from position
                disp.attemptMove(startIndex, endIndex); //attempt move using the fenindexes
            }
            dragging = false;
            if (pieceBeingDragged != null){
                //put the piece back, regardless of whether the move was successful or because if it was
                //successful then the pieceBeingDragged isn't being displayed anymore anyway
                pieceBeingDragged.setLocation(savedX, savedY);
                disp.stuffHolder.boardLayers.setLayer(pieceBeingDragged, JLayeredPane.PALETTE_LAYER);
                pieceBeingDragged = null;
            }
            mouseMoved(e); // to update the cursor back to pointer or hand
        }
        
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {
        if (!dragging){
            disp.frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            cursor = "pointer";
        }
    }
    
    /**
     * Returns the board position of a piece given given coordinates
     * @param y The y pixel coordinate
     * @param x The x pixel coordinate
     * @return The board Position pair
     */
    public Position getPositionFromPixelCoords(int y, int x){
        if (disp.rotated){
            return new Position(y / 64 + 1, (x - 640) / -64);
        } else {
            return new Position((y - 576) / -64, x / 64);
        }
    }
    

}
