package cs3524;
/**============================================================
 * 	Author: 	George
 *  Student ID: 51768284
 * 	
 * 	I like Strudel.	
 * 
 * 	GRADE D3-D1 code
 * 
 * Requirements as requested by the task description:
 * 		- Socket for the server
 * 		- Socket for the client
 * 		- A conversation takes place, it is stopped by CTRL-C
 * 			o The server replies with the message received
 * ============================================================
 */


import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;				//What is this for ???
import java.util.Map;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;



public class MessageServer {
	//on this socket the server listens
	private ServerSocket server = null;
	
	public MessageServer (int serverPort) throws IOException
	{	
		System.out.println("MessengerServer: Started, listening for client connect .");
		this.server = new ServerSocket(serverPort);
		
		///			THIS PART SHOULD GO IN a sepparate class for multi thread
		
		// Client Socket has the accept from server socket
		Socket client = server.accept();
		// create input stream handler? for client 
		BufferedReader in  = new BufferedReader ( new InputStreamReader(client.getInputStream() ));
		
		//create output stream printer handler for client
		PrintWriter out = new PrintWriter( new OutputStreamWriter(client.getOutputStream() ), true);
		
		out.println("MessageServer reply: Connected to server");
		while(true) {
			String message = in.readLine();
			
			// for multi user this would display the name of some sorts or have
			// some prioir comunication just between client and server (no global print in chat)
			// to setup client details 
			System.out.println("Message received ["+message + "]");
			
			// tell client that the message has been received by replying with the message received
			
			out.println(message.toUpperCase());
				
		}
		// read from client input handler
		
		//close the connection just for testing
		// put in while loop later since 
		
	}
	
	///make it a private class ?
	//private acceptClient() throws IOException
	{
		
	}
	
	public static void main(String[] args)
	{
		System.out.println("Messenger> start");
		
			try {
				new MessageServer (Integer.parseInt(args[0]));
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		System.out.println("Messenger> end");
	}
}
