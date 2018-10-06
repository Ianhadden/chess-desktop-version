
/**
 * Class to listen for moves from the network
 * @author Ian
 *
 */
public class MoveListener implements Runnable {
    Display disp;
    Thread t;
    
    /**
     * Creates a new MoveListener linked to the given display
     * @param disp The display to link to
     */
    public MoveListener(Display disp){
        this.disp = disp;
    }
    
    /**
     * Listens for moves. Blocks until there is one
     * Then applies it
     */
    public void run(){
        try {
            String type = disp.currentGame.receiver.readLine();
            if (type.equals("stop")){
                System.out.println("stop received");
                disp.connectionProblem("The other player stopped the game");
                return;
            }
            int startIndex = Integer.parseInt(disp.currentGame.receiver.readLine());
            int endIndex = Integer.parseInt(disp.currentGame.receiver.readLine());
            disp.attemptMove(startIndex, endIndex);
            if (type.equals("promotion")){
                disp.currentGame.promotePawn(disp.currentGame.receiver.readLine());
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
