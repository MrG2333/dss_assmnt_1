import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;



public class ChatClient {

    BufferedReader in;
    BufferedReader user_in;
    PrintWriter out;
    

 
    private void run(String hostname, int port) throws IOException {

        // Make connection and initialize streams
        
        Socket socket = new Socket(hostname, port);
    
        in = new BufferedReader(new InputStreamReader(
                              socket.getInputStream()));
        user_in = new BufferedReader(new InputStreamReader(
                                    System.in));

        out = new PrintWriter(socket.getOutputStream(), true);
        String name="";
        String msg;
        String first_word;
        
        
        while (true) {
            
            while(in.ready())
            {
                msg = in.readLine();
                if(!msg.split(":")[0].equals(name) ){
                    System.out.println(msg);    
                }
            
            }
            
            while(user_in.ready())
            {
                msg = user_in.readLine();
                first_word = msg.split(" ")[0];
                
                if( first_word.equals("REGISTER") && msg.split(" ").length ==2){    
                    name = msg.split(" ")[1];
                }

                out.println(msg);
            }

        }    
    }

    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        
        if(args.length < 2) {
            System.err.println("Usage: java MessageClient <host> <port>");
            return;
        }
        
        String hostname = args[0];
        int port = Integer.parseInt( args[1] );

        ChatClient client = new ChatClient();
        client.run(hostname,port);
    }
}
