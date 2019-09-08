import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Class to listen for a new connection from the network.
 * Used for host games
 */
public class ConnectionListener implements Runnable {
    private Display disp;
    private int port;
    private String team;
    private Thread t;
    private ArrayList<String> fens;
    private ServerSocket serverSocket;
    private boolean connectionEstablished;
    
    /**
     * Creates a new ConnectionListener
     * @param disp The display to link to
     * @param port The port to listen on
     * @param team The team this player will be
     */
    public ConnectionListener(Display disp, int port, String team){
        this.disp = disp;
        this.team = team;
        this.port = port;
        this.connectionEstablished = false;
        this.fens = new ArrayList<String>();
        this.fens.add(Game.startFen);
    }
    
    /**
     * Creates a new ConnectionListener
     * @param disp The display to link to
     * @param port The port to listen on
     * @param team The team this player will be
     * @param fens The list of fens so far in the game
     */
    public ConnectionListener(Display disp, int port, String team, ArrayList<String> fens){
        this(disp, port, team);
        this.fens = fens;
    }
    
    /**
     * Listens for a connection. Blocks until there is one
     * Then sends game info across to setup the game
     */
    public void run(){     
        try { 
            serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept(); //blocks
            OutputStream output = clientSocket.getOutputStream();
            PrintWriter sender = new PrintWriter(output);
            //sends all the fens followed by the team the other
            //player should play as
            for (String fen : fens){
                sender.println(fen);
                sender.flush();
            }
            String otherTeam = team.equals("white")? "black" : "white";
            sender.println(otherTeam);
            sender.flush();
            BufferedReader receiver = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            connectionEstablished = true;
            NetworkGame game;
            if (fens == null){
                game = new NetworkGame(team, sender, receiver);
            } else {
                game = new NetworkGame(team, fens, sender, receiver);
            }
            disp.connectionEstablished(game); 
        } catch (IOException e) {
            disp.connectionProblem("Something went wrong :(");
        }
    }
    
    /**
     * Creates a new thread and listens for connections on that thread
     */
    public void start(){
        if (t == null){
            t = new Thread(this);
            t.start();
        }
    }
    
    /**
     * Closes the listener if there's no connection, stopping it from listening
     * @pre Must already have called start.
     */
    public void closeListenerIfNoConnection(){
        try {
            if (serverSocket != null && !connectionEstablished){
                serverSocket.close();
            }
        } catch (IOException e){}
    }
}
