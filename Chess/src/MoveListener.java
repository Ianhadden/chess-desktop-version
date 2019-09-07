import java.io.BufferedReader;


/**
 * Class to listen for moves from the network
 * @author Ian
 *
 */
public class MoveListener implements Runnable {
    Display disp;
    BufferedReader receiver;
    Thread t;
    
    /**
     * Creates a new MoveListener linked to the given display
     * @param disp The display to link to
     * @param receiver the BufferedReader to listen/read from for moves
     */
    public MoveListener(Display disp, BufferedReader receiver){
        this.disp = disp;
        this.receiver = receiver;
    }
    
    /**
     * Listens for moves. Blocks until there is one
     * Then applies it
     */
    public void run(){
        try {
            String type = receiver.readLine();
            if (type.equals("stop")){
                System.out.println("stop received");
                disp.connectionProblem("The other player stopped the game");
                return;
            }
            int startIndex = Integer.parseInt(receiver.readLine());
            int endIndex = Integer.parseInt(receiver.readLine());
            disp.attemptMove(startIndex, endIndex);
            if (type.equals("promotion")){
                disp.currentGame.promotePawn(receiver.readLine());
                disp.printBoard(); //since promotePawn bypasses board
            }
        } catch (Exception e) {
            disp.connectionProblem("The connection was lost");
        }
    }
    
    /**
     * Creates a new thread and listens for moves on that thread
     * Applies the move when it is sent
     */
    public void start(){
        if (t == null){
            t = new Thread(this);
            t.start();
        }
    }
}
