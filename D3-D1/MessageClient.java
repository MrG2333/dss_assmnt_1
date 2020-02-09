package cs3524;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class MessageClient 
{
	
	public MessageClient ( String hostname, int port) throws UnknownHostException, IOException, ClassNotFoundException
	{
		//define stdin so that the client message can be read 
		BufferedReader stdin = new BufferedReader( new InputStreamReader( System.in ));
		
		//connect to the socket listening on the server 
		Socket clientside_server_socket = new Socket(hostname,port);
		
		BufferedReader in = new BufferedReader( new InputStreamReader ( clientside_server_socket.getInputStream() ));
		PrintWriter   out = new PrintWriter  (new OutputStreamWriter ( clientside_server_socket.getOutputStream()));
		
		//At this moment we know that the server has the welcome message waiting for us.
		//We know this because we put it there
		
		System.out.println( in.readLine());
		while(true) {
			System.out.println("ClientSocket> ");
			String message = stdin.readLine();
			
			//pass it to server torught out
			
			out.println(message);
			out.flush();
			//use out.flush() ???
			
			/// get reply from server
			message = in.readLine();
			System.out.println(message);
		}
		//Read from client stdin
		
	}


	static public void main(String args[]) throws UnknownHostException, ClassNotFoundException, IOException
	{
		//tell user what format the command should be
		if(args.length < 2) {
			System.err.println("Usage: java MessageClient <host> <port>");
			return;
		}
		
		String hostname = args[0];
		int port = Integer.parseInt( args[1] );
		new MessageClient( hostname, port );
		
	}
}