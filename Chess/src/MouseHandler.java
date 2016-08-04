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


public class MouseHandler implements MouseListener, MouseMotionListener{
    boolean dragging;
    JFrame frame;
    Display disp;
    int startIndex;
    JLabelPiece pieceBeingDragged;
    int savedX, savedY;
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (!dragging){ // meaning if this is the beginning of a drag
            dragging = true;
            Position startPos = getPositionFromPixelCoords(e.getY(), e.getX()); //get position on board
            startIndex = startPos.x - 1 + (startPos.y - 1) * 8; //get fenindex from position
            /*
            frame.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                    new ImageIcon("C:\\Users\\Ian\\Documents\\GitHub\\chess\\chess\\pics\\blackpawn.png").getImage(),
                    new Point(16,16),"custom cursor"));;
             */ 
            //see if one of the player's pieces is being dragged. If so save it's position and bring it to the front
            //so we can drag it.
            for (JLabelPiece p : disp.boardPieces){
                if (startPos.x == p.position.x && startPos.y == p.position.y &&
                        p.owner.equals(disp.currentGame.currentBoard.turn) 
                        && !disp.currentGame.currentBoard.promotingPawn){
                    pieceBeingDragged = p;
                    disp.boardLayers.setLayer(p, JLayeredPane.DRAG_LAYER);
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
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        System.out.println("you clicked");
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (dragging){ //end of drag
            //frame.setCursor(Cursor.getDefaultCursor());
            Position endPos = getPositionFromPixelCoords(e.getY(), e.getX()); // get position from pixel coords
            int endIndex = endPos.x - 1 + (endPos.y - 1) * 8; // get fenindex from position
            disp.attemptMove(startIndex, endIndex); //attempt move using the fenindexes
            dragging = false;
            if (pieceBeingDragged != null){
                //put the piece back, regardless of whether the move was successful or because if it was
                //successful then the pieceBeingDragged isn't being displayed anymore anyway
                pieceBeingDragged.setLocation(savedX, savedY);
                disp.boardLayers.setLayer(pieceBeingDragged, JLayeredPane.PALETTE_LAYER);
                pieceBeingDragged = null;
            }
        }
        
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
        
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
