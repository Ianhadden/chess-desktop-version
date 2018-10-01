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
        Game game;
        if (fens == null){
            game = new Game(team);
        } else {
            game = new Game(team, fens);
        }
        try { 
            serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept(); //blocks
            OutputStream output = clientSocket.getOutputStream();
            PrintWriter sender = new PrintWriter(output);
            //sends all the fens followed by the team the other
            //player should play as
            for (String fen : game.fens){
                sender.println(fen);
                sender.flush();
            }
            String otherTeam = team.equals("white")? "black" : "white";
            sender.println(otherTeam);
            sender.flush();
            BufferedReader receiver = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            game.sender = sender;
            game.receiver = receiver;
            connectionEstablished = true;
            disp.connectionEstablished(game); 
        } catch (IOException e) {
            System.out.println("There was a problem");
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
