
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
            System.out.println("LISTENING1");
            String type = disp.currentGame.receiver.readLine();
            System.out.println("LISTENING2");
            int startIndex = Integer.parseInt(disp.currentGame.receiver.readLine());
            System.out.println("LISTENING3");
            int endIndex = Integer.parseInt(disp.currentGame.receiver.readLine());
            System.out.println("LISTENING4");
            disp.attemptMove(startIndex, endIndex);
            System.out.println("LISTENING5");
            if (type.equals("promotion")){
                disp.currentGame.promotePawn(disp.currentGame.receiver.readLine());
                disp.printBoard(); //since promotePawn bypasses board
            }
        } catch (Exception e) {System.out.println("NO BUENO: " + e.getMessage());}
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
