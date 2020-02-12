import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

/**
 * A multithreaded chat room server.  When a client connects the
 * server requests a screen name by sending the client the
 * text "SUBMITNAME", and keeps requesting a name until
 * a unique one is received.  After a client submits a unique
 * name, the server acknowledges with "NAMEACCEPTED".  Then
 * all messages from that client will be broadcast to all other
 * clients that have submitted a unique screen name.  The
 * broadcast messages are prefixed with "MESSAGE ".
 *
 * Because this is just a teaching example to illustrate a simple
 * chat server, there are a few features that have been left out.
 * Two are very useful and belong in production code:
 *
 *     1. The protocol should be enhanced so that the client can
 *        send clean disconnect messages to the server.
 *
 *     2. The server should do some logging.
 */
public class ChatServer {


    /**
     * The set of all names of clients in the chat room.  Maintained
     * so that we can check that new clients are not registering name
     * already in use.
     */
    private static HashSet<String> names = new HashSet<String>();

    /**
     * The set of all the print writers for all the clients.  This
     * set is kept so we can easily broadcast messages.
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    /**
     * The appplication main method, which just listens on a port and
     * spawns handler threads.
     */
    public static void main(String[] args) throws Exception {
    System.out.println("The chat server is running.");
    if(args.length < 1) {
        System.err.println("Usage: java MessageServer.java <port>");
        return;
    }
    int port = Integer.parseInt(args[0]);
    
    ServerSocket listener = new ServerSocket(port);
    try {
        while (true) {
        new Handler(listener.accept()).start();
        }
    } finally {
        listener.close();
    }
    }

    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     */
    private static class Handler extends Thread {
    private String name;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
    public Handler(Socket socket) {
        this.socket = socket;
    }

        /**
         * Services this thread's client by repeatedly requesting a screen name until a
         * unique one has been submitted, then acknowledges the name and registers the
         * output stream for the client in a global set, then repeatedly gets inputs and
         * broadcasts them.
         * 
         *
         */

        public void RegisterName(String name) {
        names.add(name);
    }

    public void run() {
        try {

        // Create character streams for the socket.
        in = new BufferedReader(new InputStreamReader(
                                  socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Request a name from this client.  Keep requesting until
        // a name is submitted that is not already used.  Note that
        // checking for the existence of a name and adding the name
        // must be done while locking the set of names.
        
        
        //black magic that was not in the lectures 
        /*
        synchronized (names) {
            if (!names.contains(name)) {
                names.add(name);
            }
            }
        **/

        String name = "unknown"; 
        writers.add(out);
        
        String input;
        String first_word;
        
        while (true) {
            input = in.readLine();
            first_word =  input.split(" ")[0];
            //here we implement the dictionary
            // could make classes but meh
            if(first_word.equals("REGISTER")){
                if(input.split(" ").length<2) 
                    out.println("REGISTER <name>");
                else 
                {
                    name = input.split(" ")[1];
                    RegisterName(name);    
                    }
            }
            else if(first_word.equals("UNREGISTER")){
                name = "unknown";
                names.remove(name);
            }
            
            
            System.out.println("["+name+"] "+input);
            for (PrintWriter writer : writers) {
                
                writer.println(name + ": " + input);
                writer.flush();
            }
        }
        } catch (IOException e) {
        System.out.println(e);
        } finally {
        // This client is going down!  Remove its name and its print
        // writer from the sets, and close its socket.
        if (name != null) {
            names.remove(name);
        }
        if (out != null) {
            writers.remove(out);
        }
        try {
            socket.close();
        } catch (IOException e) {
        }
        }
    }
    }
}