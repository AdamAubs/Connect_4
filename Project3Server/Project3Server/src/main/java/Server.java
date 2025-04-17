import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.ListView;

public class Server{

	// Count how many clients we have
	int count = 1;
	// Stores connected clients
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();

	// Stores logged in users
	HashMap<String, ClientThread> userMap = new HashMap<>(); // Maps username to client thread

	// TODO: Implement active sessions
	ArrayList<GameSession> activeSessions = new ArrayList<>();

	// Servers main thread
	TheServer server;

	private Consumer<Message> callback;
	// Server constructor
	// Takes in a Consumer callback function used by
	// the Gui to handle incoming messages.
	Server(Consumer<Message> call){
		callback = call;
		server = new TheServer();
		server.start();
	}

	public class TheServer extends Thread{
		// Start running the main server thread
		public void run() {
				try(ServerSocket mysocket = new ServerSocket(5556);){
		    		System.out.println("Server is waiting for a client!");

					while(true) {
						// Create a new client thread for the incoming client
						ClientThread c = new ClientThread(mysocket.accept(), count);
						// Add the new connected client to the array list of connected clients
						clients.add(c);
						c.start();

						count++;
					}
				} catch(Exception e) {
					System.err.println("Server did not launch");
				}
			}
		}

		// TODO: finish implementing game sessions on the server
		class GameSession {
			ClientThread player1;
			ClientThread player2;
			String player1Name;
			String player2Name;

			public GameSession(ClientThread p1, ClientThread p2, String p1Name, String p2Name) {
				player1 = p1;
				player2 = p2;
				player1Name = p1Name;
				player2Name = p2Name;
			}

		}

		// Creates a new thread for a client
		class ClientThread extends Thread{

			Socket connection;
			int count;
			ObjectInputStream in;
			ObjectOutputStream out;
			String username = null;
			GameSession currentGame = null;

			// Client constructor gets passed
			// a connection socket, and its identifier of
			// which client it is on the server.
			ClientThread(Socket s, int count){
				this.connection = s;
				this.count = count;	
			}

			// Runs a new client thread on the server
			// setting up its input stream to receive
			// data from the connected client and output stream
			// to send data to the client.
			public void run() {
				try {
					in = new ObjectInputStream(connection.getInputStream());
					out = new ObjectOutputStream(connection.getOutputStream());
					connection.setTcpNoDelay(true);	
				} catch(Exception e) {
					System.err.println("Streams not open");
				}

				// Initially create a newClient message to display on the server
				// GUI
				Message newClientMessage = new Message(MessageType.NEWCONNECTION, count);
				callback.accept(newClientMessage);

				// Event loop that persists until the client
				// disconnects or the server stops running.
				 while(true) {
					    try {
							// read in the data from the client
					    	Message clientMessage = (Message) in.readObject();
							// Display the clientMessage on the server GUI
							callback.accept(clientMessage);

							// Handle different message types
							switch (clientMessage.type) {
								case LOGIN:
									handleLogin(clientMessage);
									break;
								case JOIN_GAME:
									// TODO: implement handleJoinGame(clientMessage)
								case GAME_STATE:
									// TODO: implement handleGameState(clientMessage)
								case GAME_ACTION:
									// TODO: implement handleGameAction(clientMessage)
								case DISCONNECTED:
									// TODO: implement handleDisconnected(clientMessage)
								case TEXT:
									// TODO: implement handleTextMessages(clientMessage)
							}

						} catch(Exception e) {
							callback.accept(new Message("OOOOPPs...Something wrong with the socket from client: " + count + "....closing down!"));
					    	clients.remove(this);
					    	break;
					    }
				 }
			}//end of run

			// Used to check for valid username login.
			// Sends a validation message ("success") back to a client
			// permitting them to log in.
			private void handleLogin(Message loginMsg) {
				// Gets the username, stored as sender, from the loginMsg object.
				String requestedUsername = loginMsg.sender;

				// Check if the username is taken
				if (userMap.containsKey(requestedUsername)) {
					try {
						Message response = new Message(MessageType.LOGIN, "SERVER", "username taken");
						out.writeObject(response);
					} catch (Exception e) {
						System.out.println("Error sending login response: " + e.getMessage());
					}
				} else {
					// username is valid

					// Add the username to the hashMap<String, ClientThread>
					username = requestedUsername;
					userMap.put(username, this);

					try {
						// Send a success message back to this client permitting them to log in
						Message response = new Message(MessageType.LOGIN, "SERVER", "success");
						out.writeObject(response);
					} catch (Exception e) {
						System.out.println("Error handling login: " + e.getMessage());
					}
				}

			}
		}//end of client thread
}


	
	

	
