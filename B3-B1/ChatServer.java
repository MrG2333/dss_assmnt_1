/*
    [x] multi client socket server
    [x] socket client
    [x] client user can send messages to all other clients
    [x] registration implemented
        - [x] REGISTER
            - [x] removes client name when socket disconnects
            - [x] doesnot create duplicates
        - [x] UNREGISTER
    [x] client sees who the sender is if the sender is registered
        otherwise the name in "unknown" 
*/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.HashMap;


public class ChatServer {

    //hashmap because you need to get the connection based on the name 
    private static HashMap<String, PrintWriter> cl_name_conn = new HashMap<String, PrintWriter>();

    private static HashSet<String> names = new HashSet<String>();

    
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    
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

    private static class Handler extends Thread {
    private String name;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;


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
            synchronized (names) {
                if (!names.contains(name)) {
                    names.add(name);
                }
                else {out.println("Name is already registered.");}
                }
            }

    public void run() {
        try {

        // Create character streams for the socket.
        in = new BufferedReader(new InputStreamReader(
                                  socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);


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
                    cl_name_conn.put(name,out);
                    }
            }
            else if(first_word.equals("UNREGISTER")){
                name = "unknown";
                names.remove(name);
            }
            
            
            System.out.println("["+name+"] "+input);
            if(!name.equals("unknown"))
            {    
                for (PrintWriter writer : writers) {
                    writer.println(name + ": " + input);
                    writer.flush();
                }
            }
            else{
                out.println("first REGISTER <name>");
            }
        }
        } catch (IOException e) {
        System.out.println(e);
        } finally {
        ///Remove the
        /// hash maps are not as cool as hashsets
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