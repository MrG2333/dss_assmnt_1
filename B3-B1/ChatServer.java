
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;


public class ChatServer {

    //hashmap because you need to get the connection based on the name
    private static HashMap<String, PrintWriter> cl_name_conn = new HashMap<String, PrintWriter>();
    private static HashMap<String, HashMap<String, PrintWriter>> groups = new HashMap<String, HashMap<String, PrintWriter>>(); 
    
    private static HashSet<String> names = new HashSet<String>();

    
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    
    public static void main(String[] args) throws Exception {
    System.out.println("The chat server is running.");
    if(args.length < 1) {
        System.err.println("Usage: java MessageServer.java <port>");
        return;
    }
    int port = Integer.parseInt(args[0]);
    Integer counter_connection = 0;
    ServerSocket listener = new ServerSocket(port);
    try {
        while (true) {
        new Handler(listener.accept(),counter_connection).start();
        counter_connection++;
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
        private Integer c_cnn;

        public Handler(Socket socket, Integer counter_conn) {
            this.socket = socket;
            this.c_cnn = counter_conn;
        }

        public void RegisterName(String name,PrintWriter out) {
            synchronized (cl_name_conn) {
                if (!cl_name_conn.containsKey(name)) {
                    cl_name_conn.put(name,out);
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

                
                String conn_counter_str= Integer.toString(c_cnn);
                String name = "unknown" + conn_counter_str ; 
                
                
                
                String input;
                String [] split_input;
                String reconstruct = "";
                String first_word;
                PrintWriter writer_local;
                while (true) {
                    input = in.readLine();
                    System.out.println("["+name+"] "+input);
                    split_input = input.split(" ");
                    first_word =  split_input[0];
                    
                    //here we implement the dictionary
                    // could make classes but meh
                    if(first_word.equals("REGISTER")){
                        if(split_input.length<2)
                            out.println("REGISTER <name>");
                        else 
                        {
                            name = split_input[1];
                            RegisterName(name,out);    
                        }
                    }
                    else if(first_word.equals("UNREGISTER")){
                        if(!name.equals("unknown"))
                            cl_name_conn.remove(name);
                            name = "unknown"+conn_counter_str;
                            RegisterName(name, out);
                    }
                    else if(first_word.equals("SEND")){
                            
                            if(split_input.length<3)
                            {   out.println("SEND <name> message");}
                            else {
                                if (cl_name_conn.containsKey(split_input[1])) {
                                    writer_local = cl_name_conn.get(split_input[1]);
                                    for (int i = 2; i < split_input.length; i++) {                      
                                        reconstruct = reconstruct+" "+split_input[i];
                                    }
                                    System.out.println("["+name+"] "+reconstruct);
                                    writer_local.println(name +": " + reconstruct); 
                                    writer_local.flush();
                            }
                            }    
                        
                        }
                    else if(first_word.equals("CREATE")&&split_input[1].equals("GROUP")){
                        if(split_input.length<3)
                        {   out.println("CREATE GROUP <group>");}
                            else{
                                groups.put(split_input[2],cl_name_conn);
                                groups.get(split_input[2]).get(name).println("this is from the group ");
                        }
                    }
                    
                    
                    if(!name.equals("unknown"+conn_counter_str) && !first_word.equals("SEND"))
                    {    
                        final String local_input = input;
                        final String local_name = name;
                        cl_name_conn.forEach((name_conn,writer_l) ->
                        {
                            writer_l.println(local_name+": "+local_input) ;
                            writer_l.flush();
                        });
                        
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