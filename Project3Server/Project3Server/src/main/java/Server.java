import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
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

	// Stores active game sessions
	ArrayList<GameSession> activeSessions = new ArrayList<>();

	// Waiting game queue
	Queue<String> waitingQueue = new LinkedList<>();

	// Servers main thread
	TheServer server;

	private Consumer<Message> serverConnectionCallback;
	// Server constructor
	// Takes in a Consumer serverConnectionCallback function used by
	// the Gui to handle incoming messages.
	Server(Consumer<Message> call){
		serverConnectionCallback = call;
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

	// TODO: finish implementing logic for game session on the server
	class GameSession {
		ClientThread player1;
		ClientThread player2;
		String player1Name;
		String player2Name;
		int[][] gameboard = new int[6][7];
		// ********************************* change currentPlayer to be rand() % 2 + 1 for random starting??
		int currentPlayer = 1; // 1 for player1, 2 for player2
		boolean gameOver = false;
		String winner = null;

		public GameSession(ClientThread p1, ClientThread p2, String p1Name, String p2Name) {
			player1 = p1;
			player2 = p2;
			player1Name = p1Name;
			player2Name = p2Name;
		}

		public void startGame() {
			System.out.println("Starting new game session between " + player1Name + " and " + player2Name);

			// Initialize empty board (0 = empty, 1 = player1, 2 = player2)
			for (int row = 0; row < 6; row++) {
				for (int col = 0; col < 7; col++) {
					gameboard[row][col] = 0;
				}
			}

			// ********************************* change currentPlayer to be rand() % 2 + 1 for random starting??
			currentPlayer = 1; // Player 1 goes first
			gameOver = false;
			winner = null;
		}

		// TODO: Add methods for making moves, checking for wins, etc.

		// Takes an int representing player (1: player1, 2: player2) and
		// sets lowest available space in column 1 and returns row
		public int dropCol1(int player) {
			int row = 5;
			while (gameboard[row][0] != 0) {
				row--;
			}
			gameboard[row][0] = player;
			return row;
		}

		// Takes an int representing player (1: player1, 2: player2) and
		// sets lowest available space in column 2 and returns row
		public int dropCol2(int player) {
			int row = 5;
			while (gameboard[row][1] != 0) {
				row--;
			}
			gameboard[row][1] = player;
			return row;
		}

		// Takes an int representing player (1: player1, 2: player2) and
		// sets lowest available space in column 3 and returns row
		public int dropCol3(int player) {
			int row = 5;
			while (gameboard[row][2] != 0) {
				row--;
			}
			gameboard[row][2] = player;
			return row;
		}

		// Takes an int representing player (1: player1, 2: player2) and
		// sets lowest available space in column 4 and returns row
		public int dropCol4(int player) {
			int row = 5;
			while (gameboard[row][3] != 0) {
				row--;
			}
			gameboard[row][3] = player;
			return row;
		}

		// Takes an int representing player (1: player1, 2: player2) and
		// sets lowest available space in column 5 and returns row
		public int dropCol5(int player) {
			int row = 5;
			while (gameboard[row][4] != 0) {
				row--;
			}
			gameboard[row][4] = player;
			return row;
		}

		// Takes an int representing player (1: player1, 2: player2) and
		// sets lowest available space in column 6 and returns row
		public int dropCol6(int player) {
			int row = 5;
			while (gameboard[row][5] != 0) {
				row--;
			}
			gameboard[row][5] = player;
			return row;
		}

		// Takes an int representing player (1: player1, 2: player2) and
		// sets lowest available space in column 7 and returns row
		public int dropCol7(int player) {
			int row = 5;
			while (gameboard[row][6] != 0) {
				row--;
			}
			gameboard[row][6] = player;
			return row;
		}

		// Called by checkForWin, takes in row, col, and player of piece
		// Returns the count of sequential pieces to the left and right for that player
		public int checkHorizontal(int startRow, int startCol, int player) {
			int count = 0;
			// check left
			int col = startCol;
			while (col >= 0) {
				if (gameboard[startRow][col] == player) {
					count++;
					col--;
				} else break;
			}
			// check right
			col = startCol+1;
			while (col <= 6) {
				if (gameboard[startRow][col] == player) {
					count++;
					col++;
				} else break;
			}
			return count;
		}

		// Called by checkForWin, takes in row, col, and player of piece
		// Returns the count of sequential pieces up and down for that player
		public int checkVertical(int startRow, int startCol, int player) {
			int count = 0;
			// check up
			int row = startRow;
			while (row >= 0) {
				if (gameboard[row][startCol] == player) {
					count++;
					row--;
				} else break;
			}
			// check down
			row = startRow+1;
			while (row <= 5) {
				if (gameboard[row][startCol] == player) {
					count++;
					row++;
				} else break;
			}
			return count;
		}

		// Called by checkForWin, takes in row, col, and player of piece
		// Returns the count of sequential pieces up-right and down-left for that player
		public int checkDiagonalRight(int startRow, int startCol, int player) {
			int count = 0;
			// check up-right
			int col = startCol;
			int row = startRow;
			while (col <= 6 && row >= 0) {
				if (gameboard[row][col] == player) {
					count++;
					row--;
					col++;
				} else break;
			}
			// check down-left
			col = startCol-1;
			row = startRow+1;
			while (col >= 0 && row <= 5) {
				if (gameboard[row][col] == player) {
					count++;
					row++;
					col--;
				} else break;
			}
			return count;
		}

		// Called by checkForWin, takes in row, col, and player of piece
		// Returns the count of sequential pieces up-left and down-right for that player
		public int checkDiagonalLeft(int startRow, int startCol, int player) {
			int count = 0;
			// check up-left
			int col = startCol;
			int row = startRow;
			while (col >= 0 && row >= 0) {
				if (gameboard[row][col] == player) {
					count++;
					row--;
					col--;
				} else break;
			}
			// check down-right
			col = startCol+1;
			row = startRow+1;
			while (col <= 6 && row <= 5) {
				if (gameboard[row][col] == player) {
					count++;
					row++;
					col++;
				} else break;
			}
			return count;
		}

		// Takes in the row, col, and player number of a recently placed piece and
		// calls directional check functions to set the winner if one is found
		public void checkForWin(int row, int col, int player) {
			if (checkHorizontal(row, col, player) >= 4) {
				winner = player == 1 ? player1Name : player2Name;
				return;
			}
			if (checkVertical(row, col, player) >= 4) {
				winner = player == 1 ? player1Name : player2Name;
				return;
			}
			if (checkDiagonalRight(row, col, player) >= 4) {
				winner = player == 1 ? player1Name : player2Name;
				return;
			}
			if (checkDiagonalLeft(row, col, player) >= 4) {
				winner = player == 1 ? player1Name : player2Name;
				return;
			}
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
			serverConnectionCallback.accept(newClientMessage);

			// Event loop that persists until the client
			// disconnects or the server stops running.
			 while(true) {
					try {
						// read in the data from the client
						Message clientMessage = (Message) in.readObject();
						// Display the clientMessage on the server GUI
						// serverConnectionCallback.accept(clientMessage);

						// Handle different message types
						switch (clientMessage.type) {
							case LOGIN:
								handleLogin(clientMessage);
								break;
							case JOIN_GAME:
								 handleJoinGame(clientMessage);
								 break;
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
						serverConnectionCallback.accept(new Message("OOOOPPs...Something wrong with the socket from client: " + count + "....closing down!"));
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
				// username is valid so update logged in users log
				serverConnectionCallback.accept(loginMsg);
				// Add the username to the hashMap<String, ClientThread>
				username = requestedUsername;
				userMap.put(username, this);

				try {
					// Send a success message back to this client permitting them to log in
					Message response = new Message(MessageType.LOGIN, "SERVER", "success");
					out.writeObject(response);

					for (Map.Entry<String, ClientThread> t : userMap.entrySet()) {
						try {
							if (this != t.getValue()) {
								// Update its own online users list with users already connected
								Message alreadyOnlineUsers = new Message(MessageType.ALREADYONLINE, "SERVER", "User: " + t.getValue().username + " is online");
								out.writeObject(alreadyOnlineUsers);

								// Update all other clients online users list that this user is online
								Message newOnlineUser = new Message(MessageType.NEWONLINE, "SERVER", "User: " + username + " is online");
								t.getValue().out.writeObject(newOnlineUser);
							}
						} catch (Exception e) {
							System.err.println("Failed to update users with a new online user");
						}
					}
				} catch (Exception e) {
					System.out.println("Error handling login: " + e.getMessage());
				}
			}
		}

		// Adds player who clicks "join button" first to waiting queue.
		// If the waiting queue is empty, send a WAITING message back to the client.
		// If there are more than two players in the queue, create a new game session
		// with the first two players.
		private void handleJoinGame(Message joinGameMsg) {
			// Synchronized block to prevent two players
			// from joining the queue at the same time
			synchronized (waitingQueue) {
				waitingQueue.add(username);

				try {
					System.out.println("Player " + username + " joined the waiting queue");
					Message waitingMsg = new Message(MessageType.WAITING, "SERVER", "Waiting for an opponent to join. Players in queue: " + waitingQueue.size());
					out.writeObject(waitingMsg);

					// If we have 2 players, create a game session
					if (waitingQueue.size() >= 2) {
						// Dequeue
						String player1username = waitingQueue.poll();
						String player2username = waitingQueue.poll();

						// Use the usernames that were dequed to get them from the
						// hashMap of logged-in users
						ClientThread player1 = userMap.get(player1username);
						ClientThread player2 = userMap.get(player2username);

						if (player1 != null && player2 != null) {
							// Create new game session
							GameSession newGame = new GameSession(player1, player2, player1username, player2username);
							activeSessions.add(newGame);

							// Set current game for both players
							player1.currentGame = newGame;
							player2.currentGame = newGame;

							// Initialize the game
							newGame.startGame();

							// Notify server UI
							Message newGameSessionMsg = new Message(MessageType.NEWGAMESESSION, "SERVER",
									"New game started between " + player1username + " and " + player2username);
							serverConnectionCallback.accept(newGameSessionMsg);

							// Notify both players
							int[][] initialBoard = newGame.gameboard;

							// Player 1 notification (goes first) indicated by 1 for current player
							Message p1Msg = new Message(MessageType.GAME_STATE, "SERVER", player1username, player2username,
									"Starting game", initialBoard, 1);
							player1.out.writeObject(p1Msg);

							// Player 2 notification
							Message p2Msg = new Message(MessageType.GAME_STATE, "SERVER", player2username, player1username,
									"Starting game", initialBoard, 2);
							player2.out.writeObject(p2Msg);
						}
					}
				} catch (Exception e) {
					System.out.println("Error creating game: " + e.getMessage());
					e.printStackTrace();
				}
			}

		}


	}//end of client thread
}


	
	

	
