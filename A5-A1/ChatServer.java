
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
    private static HashMap<String, HashMap<String, PrintWriter>> topics = new HashMap<String, HashMap<String, PrintWriter>>(); 
   
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

                out.println("Connection to the Server established");
                String conn_counter_str= Integer.toString(c_cnn);
                String name = "unknown" + conn_counter_str ; 
                
                
                
                
                String reconstruct;
                String first_word;
                PrintWriter writer_local;
                while (true) {
                    reconstruct = "";
                    final String input = in.readLine();
                    System.out.println("["+name+"] "+input);
                    final String[] split_input = input.split(" ");
                    first_word =  split_input[0];
                    final String local_name_0 = name;
                    
                    //go trough connections
                    cl_name_conn.forEach((name_conn,writer_l) ->
                    {
                        for(int i = 0; i<split_input.length;i++){
                            //if word is a topic
                            if(topics.containsKey(split_input[i])){
                                // if connection subscribed to topic
                                if(topics.get(split_input[i]).containsKey(name_conn)){
                                    
                                    writer_l.println("["+split_input[i]+"]"+local_name_0+":"+input);
                                    writer_l.flush();
                                    //only need to display the message once
                                    i  = split_input.length;
                                }
                            }
                        }
                    });
                    

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
                                if(groups.containsKey(split_input[1])){
                                    if(groups.get(split_input[1]).containsKey(name)){
                                        for (int i = 2; i < split_input.length; i++) {                      
                                            reconstruct = reconstruct+" "+split_input[i];
                                        }
                                        final String group_name=split_input[1];
                                        final String local_name = name;
                                        final String local_reconstruct = reconstruct;
                                    
                                        groups.get(split_input[1]).forEach((name_conn,writer_l) ->
                                        {
                                            writer_l.println("["+group_name+"]"+local_name+": "+local_reconstruct) ;
                                            writer_l.flush();
                                        });
                                   
                                    }
                                    else{
                                        out.println("You first have to JOIN <group>");
                                    }
                                }
                                
                                else if (cl_name_conn.containsKey(split_input[1])) {
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
                                groups.put(split_input[2],new HashMap<String, PrintWriter>());
                                groups.get(split_input[2]).put(name,out);
                                groups.get(split_input[2]).get(name).println("Group created, you are part of it.");
                        }
                    }

                    else if(first_word.equals("JOIN")){
                        if(split_input.length<2)
                        {   out.println("JOIN <group>");}
                        else{
                            if(!groups.containsKey(split_input[1])){
                                out.println("Does not contain group.");
                            }
                            else{
                                groups.get(split_input[1]).put(name,out);
                                out.println("Connected to group: " + split_input[1]);
                            }
                        }
                    }
                    else if(first_word.equals("LEAVE")){
                        if(split_input.length<2)
                        { out.println("LEAVE <group>");}
                        else{
                            if(!groups.containsKey(split_input[1])){
                                out.println("Group does not exists.");
                            }
                            else if(!groups.get(split_input[1]).containsKey(name) ) {
                                out.println("You are already not part of this group.");
                            }
                            else{
                                groups.get(split_input[1]).remove(name);
                            }
                        }
                    }
                    else if(first_word.equals("REMOVE")){
                        if(split_input.length < 2 || split_input.length > 2)
                        {   out.println("REMOVE <group>");}
                        else {
                            groups.remove(split_input[1]);
                        }
                    }
                    else if(first_word.equals("TOPIC")){
                        if(split_input.length < 2 || split_input.length > 2)
                        { out.println("TOPIC <keyword>");}
                        else{
                            topics.put(split_input[1],new HashMap<String, PrintWriter>());
                            topics.get(split_input[1]).put(name,out);
                            topics.get(split_input[1]).get(name).println("You are subscribed to "+ split_input[1]);
                        } 
                    }
                    else if(first_word.equals("TOPICS")){
                        topics.forEach((name_topic,writer_l) ->
                        {
                            out.println(name_topic);
                        });
                    }
                    else if(first_word.equals("SUBSCRIBE")){
                        if(split_input.length < 2 || split_input.length > 2)
                        {   out.println("SUBSCRIBE <keyword>");}
                        else {
                            if(topics.containsKey(split_input[1])){
                            topics.get(split_input[1]).put(name,out);
                            topics.get(split_input[1]).get(name).println("You subscribed to "+ split_input[1]);
                            }
                            else {
                                out.println("Topic does not exist");
                            }
                        }
                        
                    }
                    else if(first_word.equals("UNSUBSCRIBE"))
                        if(split_input.length<2 || split_input.length >2)
                        {out.println("UNSUBSCRIBE <keyword>");}
                        else{
                            if(topics.containsKey(split_input[1])){
                                if(topics.get(split_input[1]).containsKey(name)){
                                    topics.get(split_input[1]).remove(name);
                                    out.println("You have been unsubscribed from "+ split_input[1]);
                                }
                                else {
                                    out.println("You are not subscribed to "+split_input[1]);
                                }
                            }
                            else {
                                out.println("The topic does not exist.");
                            }
                        }

                    if(!name.equals("unknown"+conn_counter_str) && !first_word.equals("SEND"))
                    {    
                        final String local_input_2 = input;
                        final String local_name_2 = name;
                        cl_name_conn.forEach((name_conn,writer_l) ->
                        {
                            writer_l.println(local_name_2+": "+local_input_2) ;
                            writer_l.flush();
                        });  
                    } 
                }
            } catch (IOException e) {
               System.out.println("A client has closed the connection.");
            } finally {
                ///Remove the
                /// hash maps are not as cool as hashsets
                System.out.println("A client has closed the connection. Removing him from groups, topics and connections.");
                if (name != null) {
                    cl_name_conn.remove(name);
                    groups.forEach((group_name,group_map) ->
                        {
                            if(group_map.containsKey(name)){
                                group_map.remove(name);
                            }
                        });

                    topics.forEach((topic_name,topic_map) ->
                        {
                            if(topic_map.containsKey(name)){
                                topic_map.remove(name);
                            }
                        });
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