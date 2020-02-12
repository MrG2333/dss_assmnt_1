import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


/**
 * A simple Swing-based client for the chat server.  Graphically
 * it is a frame with a text field for entering messages and a
 * textarea to see the whole dialog.
 *
 * The client follows the Chat Protocol which is as follows.
 * When the server sends "SUBMITNAME" the client replies with the
 * desired screen name.  The server will keep sending "SUBMITNAME"
 * requests as long as the client submits screen names that are
 * already in use.  When the server sends a line beginning
 * with "NAMEACCEPTED" the client is now allowed to start
 * sending the server arbitrary strings to be broadcast to all
 * chatters connected to the server.  When the server sends a
 * line beginning with "MESSAGE " then all characters following
 * this string should be displayed in its message area.
 */
public class ChatClient {

    BufferedReader in;
    BufferedReader user_in;
    PrintWriter out;
    

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Return in the
     * listener sends the textfield contents to the server.  Note
     * however that the textfield is initially NOT editable, and
     * only becomes editable AFTER the client receives the NAMEACCEPTED
     * message from the server.
     */
    
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
        
        // Process all messages from server, according to the protocol.
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
