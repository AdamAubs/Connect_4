import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;

public class Client extends Thread{

	// Create a socket object so client can
	// communicate with server
	Socket socketClient;

	// Set up input and output streams
	// that will be used to send and receive
	// a message through the client's socket connection/
	ObjectOutputStream out;
	ObjectInputStream in;

	// Runs the thread
	public void run() {
		// Try connecting the client to the server located
		// at a server on same machine as itself
		try {
			socketClient= new Socket("127.0.0.1",5556);
	    	out = new ObjectOutputStream(socketClient.getOutputStream());
	    	in = new ObjectInputStream(socketClient.getInputStream());
	   	 	socketClient.setTcpNoDelay(true);
		} catch(Exception e) {}

		// Read in the message being from the client
		while(true) {
			try {
				Message message = (Message) in.readObject();
				System.out.println(message);
			}
			catch(Exception e) {}
		}
	
    }

	// Used to send a message to a client
	public void send(Message data) {
		try {
			out.writeObject(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
