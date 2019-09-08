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
 * Class to send a new connection across the network
 * Used for client games
 */
public class ConnectionSender implements Runnable {
    private Display disp;
    private int port;
    private String hostName;
    private Thread t;
    private Socket clientSocket;
    private boolean connectionEstablished;
    
    /**
     * Creates a new ConnectionSender
     * @param disp The display to link to
     * @param port The port to connect to
     * @param hostName the host name to connect to
     */
    public ConnectionSender(Display disp, int port, String hostName){
        this.disp = disp;
        this.hostName = hostName;
        this.port = port;
        connectionEstablished = false;
    }
    
    /**
     * Sends a connection across the network. Blocks until it finds one
     * Then reads in game data from the host and sets up the game
     */
    public void run(){
        try {
            clientSocket = new Socket(InetAddress.getByName(hostName), port);
            BufferedReader receiver = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ArrayList<String> fens = new ArrayList<String>();
            String mostRecentLine = receiver.readLine();
            while (!mostRecentLine.equals("white") && !mostRecentLine.equals("black")){
                fens.add(mostRecentLine);
                mostRecentLine = receiver.readLine();
            }
            OutputStream output = clientSocket.getOutputStream();
            NetworkGame game = new NetworkGame(mostRecentLine, fens, new PrintWriter(output), receiver);
            connectionEstablished = true;
            disp.connectionEstablished(game);
        } catch (IOException e) {
            disp.connectionProblem("Couldn't connect: " + e.getMessage());
        }
    }
    
    /**
     * Creates a new thread and attempts to establish a connection with it
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
