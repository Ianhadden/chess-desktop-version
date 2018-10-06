import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Class to listen for a new connection from the network.
 * Used for host games
 */
public class ConnectionSender implements Runnable {
    private Display disp;
    private int port;
    private String hostName;
    private Thread t;
    private Socket clientSocket;
    private boolean connectionEstablished;
    
    /**
     * Creates a new ConnectionListener
     * @param disp The display to link to
     * @param port The port to listen on
     * @param team The team this player will be
     */
    public ConnectionSender(Display disp, int port, String hostName){
        this.disp = disp;
        this.hostName = hostName;
        this.port = port;
        connectionEstablished = false;
    }
    
    /**
     * Listens for a connection. Blocks until there is one
     * Then sends game info across to setup the game
     */
    public void run(){
        try {
            clientSocket = new Socket(InetAddress.getByName(hostName), port);
            BufferedReader receiver = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ArrayList<String >fens = new ArrayList<String>();
            String mostRecentLine = receiver.readLine();
            while (!mostRecentLine.equals("white") && !mostRecentLine.equals("black")){
                fens.add(mostRecentLine);
                mostRecentLine = receiver.readLine();
            }
            OutputStream output = clientSocket.getOutputStream();
            Game game = new Game(fens);
            game.receiver = receiver;
            game.sender = new PrintWriter(output);
            game.team = mostRecentLine;
            game.inProgress = true;
            connectionEstablished = true;
            disp.connectionEstablished(game);
        } catch (IOException e) {
            disp.connectionProblem("Coundn't connect");
        }
    }
    
    /**
     * Creates a new thread and attempts to open a connection with it
     */
    public void start(){
        if (t == null){
            t = new Thread(this);
            t.start();
        }
    }
    
    /**
     * Closes the sender if there's no connection, stopping it from sending
     * @pre Must already have called start.
     */
    public void closeSenderIfNoConnection(){    
        try {
            if (clientSocket != null && !connectionEstablished){
                clientSocket.close();
            }
        } catch (IOException e){}
        
    }
}
