

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Consumer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javafx.scene.paint.Color;

public class GuiClient extends Application{
	Font customFont = Font.font("Arial", 14);

	// Client connection
	Client clientConnection;
	String username;
	boolean loggedIn = false;

	// Scene management
	HashMap<String, Scene> sceneMap;
	Stage primaryStage;

	// Message box scene
	TextField messageField;
	Button sendButton;
	ListView<String> messageListView;

	// Login scene
	TextField usernameField;
	Button loginButton;
	Label statusLabel;

	// Main menu scene
	Button joinGameButton;
	Button logoutButton;
	ListView<String> onlineUsersListView;
	ListView<String> offlineUsersListView;

	// Waiting List View
	ListView<String> waitingListView;

	// Game State View
	ListView<String> gameStateListView;

	// Game winner/loser pop up display
	StackPane gameDisplay;

	// Gameboard
	Label gameBoardLabel;
	GridPane gameBoardGrid;
	GridPane dropButtonGrid;
	VBox gameBox;

	// Message box
	Label messageBoxLabel;
	ListView<String> messageBoxListView;
	VBox messageBox;
	TextField inputField;

	private GameState currentGameState = GameState.NOT_IN_GAME;
	private int playerNumber;
	private String opponentName;
	private static int ROWS = 6;
	private static int COLS = 7;
	private int[][] gameBoard = new int[ROWS][COLS];
	private Button[] columnButtons = new Button[COLS];
	private StackPane[][] gameBoardDisplay = new StackPane[ROWS][COLS];

	Label gameStatusLabel;
	Button gameActionButton;

	public static void main(String[] args) {
		launch(args);
	}

	// Put Clients GUI code here.
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;

		// Set up the client connection with a consumer to handle
		// incoming messages.
		clientConnection = new Client(message -> handleIncomingMessage(message));
		clientConnection.start();

		// Initialize message list view
		messageListView = new ListView<>();
		messageListView.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		// Add the login scene initially to the hashMap
		sceneMap = new HashMap<>();
		sceneMap.put("login", createLoginScene());

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				Platform.exit();
				System.exit(0);
			}
		});

		primaryStage.setTitle("Client");
		// Show login scene first
		primaryStage.setScene(sceneMap.get("login"));
		primaryStage.show();
	}

	// set status of column buttons
	private void setColumnButtonsEnabled(boolean enabled) {
		for (Button button : columnButtons) {
			button.setDisable(!enabled);
		}
	}

	private void handleIncomingMessage(Message message) {
		Platform.runLater(() -> {
			try {
				if (message == null) {
					System.out.println("Received null message");
					return;
				}

				switch(message.type){
					case TEXT:
						messageListView.getItems().add("From " + message.sender + ": " + message.message);
						break;
					case LOGIN:
						if (message.message.equals("success")) {
							loggedIn = true;
							// Create the main menu scene after the username is set
							sceneMap.put("mainMenu", createMainMenuScene());
							switchToScene("mainMenu");
						} else {
							statusLabel.setText("Login failed: " + message.message);
						}
						break;
					case NEWONLINE:
						if (message.message != null) {
							onlineUsersListView.getItems().add(message.message);
						} else {
							System.out.println("Unable to add new online user to users to List view");
						}
						break;
					case ALREADYONLINE:
						if (message.message != null) {
							onlineUsersListView.getItems().add(message.message);
						} else {
							System.out.println("Unable to add already online users to users to List view");
						}
						break;
					case WAITING:
						if (message.message != null) {
							currentGameState = GameState.WAITING;
							sceneMap.put("waiting", createWaitingScene());
							switchToScene("waiting");
							waitingListView.getItems().add(message.message);
						} else {
							System.out.println("Unable to add waiting message to List view");
						}
						break;
					case GAME_STATE:
						handleGameStateMessage(message);
						break;
					case GAME_ACTION:
						// reflect opponents move on client scene
						handleGameActionMessage(message);
						break;
					case DISCONNECTED:
						if (message.message != null) {
							System.out.println(message.message);
							offlineUsersListView.getItems().add(message.message);
						} else {
							System.out.println("Unable to add already offline user to users to List view");
						}
						break;
					case QUIT_GAME:
						handleQuitMessage(message);
						break;
					default:
						System.out.println("Unhandled message type: " + message.type);
				}
			} catch (Exception e) {
				System.err.println("Error handling message: " + e.getMessage());
				e.printStackTrace();
			}
		});
	}

	// Takes in a message that contains the state of the gameboard
	private void handleGameStateMessage(Message message) {

		// Update clients game board
		if (message.gameboard != null) {
			this.gameBoard = message.gameboard;
		}

		if (message.player1username != null && message.player2username != null) {
			// Set player numbers
			if (message.player1username.equals(this.username)) {
				playerNumber = 1;
				opponentName = message.player2username;
			} else {
				playerNumber = 2;
				opponentName = message.player1username;
			}
		}

		// starting game state, sets up board
		if (message.message.equals("Starting game")) {
			currentGameState = GameState.GAME_STARTING;
			sceneMap.put("game", createGameScene());
			switchToScene("game");
			updateGameBoardUI();
			gameStateListView.getItems().add("Game started against " + opponentName);

			// Set initial turn
			if (message.currentPlayer == playerNumber) {
				currentGameState = GameState.MY_TURN;
				gameStateListView.getItems().add("Your turn!");
				gameStateListView.getItems().add("Player " + message.currentPlayer);
				setColumnButtonsEnabled(true);
			} else {
				currentGameState = GameState.OPPONENT_TURN;
				gameStateListView.getItems().add("Waiting for " + opponentName + " to move");
				setColumnButtonsEnabled(false);
			}
		} else if (message.message.equals("The game is a draw.")) {
			currentGameState = GameState.DRAW;
			if (message.lastMoveColumn >= 0 && message.lastMoveColumn < COLS && message.sender != null) {
				int col = message.lastMoveColumn;
				// Simulate dropping the opponent's token
				for (int row = ROWS - 1; row >= 0; row--) {
					if (gameBoard[row][col] == 0) {
						if (message.currentPlayer == 1) {
							gameBoard[row][col] = 2;
						} else {
							gameBoard[row][col] = 1;
						}
						break;
					}
				}
			}

			showGameResultPopup("Draw", "It's a draw!");

			gameStateListView.getItems().add("It's a draw!");
			gameStateListView.getItems().add("Ending game");
		} else {
			// somebody won
			currentGameState = GameState.GAME_OVER;

			String winner = "";
			// Make the final move
			if (message.lastMoveColumn >= 0 && message.lastMoveColumn < COLS && message.sender != null) {
				int col = message.lastMoveColumn;
				// Simulate dropping the opponent's token
				for (int row = ROWS - 1; row >= 0; row--) {
					if (gameBoard[row][col] == 0) {
						if (message.currentPlayer == 1) {
							gameBoard[row][col] = 2;
							winner = message.player1username;
						} else {
							gameBoard[row][col] = 1;
							winner = message.player2username;
						}
						break;
					}
				}
			}
			updateGameBoardUI();

			String resultMessage = winner.equals(username) ? "You won!" : "You lost!";
			showGameResultPopup("Game over", resultMessage);

			gameStateListView.getItems().add("Player: " + winner + " won!");
			gameStateListView.getItems().add("Ending game");

			// Send message back to server to end the game
		}
	}

	private void showGameResultPopup(String title, String message) {
		Stage popupStage = new Stage();
		popupStage.setTitle(title);

		Label messageLabel = new Label(message);
		messageLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");
		messageLabel.setTextFill(Color.BLACK);

		Button rematchButton = new Button("Rematch");
		rematchButton.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");
		rematchButton.setOnAction(e -> {

			popupStage.close();
		});

		Button mainMenuButton = new Button("Main Menu");
		mainMenuButton.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");
		mainMenuButton.setOnAction(e -> popupStage.close());

		VBox layout = new VBox(10);
		layout.setPadding(new Insets(20));
		layout.getChildren().addAll(messageLabel, rematchButton, mainMenuButton);
		layout.setStyle("-fx-background-color: white; -fx-alignment: center;");

		Scene scene = new Scene(layout, 300, 150);
		popupStage.setScene(scene);
		popupStage.setAlwaysOnTop(true);
		popupStage.showAndWait();

		resetGameBoard();

	}

	private void handleGameActionMessage(Message message) {
		if (message.lastMoveColumn >= 0 && message.lastMoveColumn < COLS && message.sender != null) {
			int col = message.lastMoveColumn;

			System.out.println(message.currentPlayer);
			// Simulate dropping the opponent's token
			for (int row = ROWS - 1; row >= 0; row--) {
				if (gameBoard[row][col] == 0) {
					//gameBoard[row][col] = (message.sender.equals(opponentName)) ? (3 - playerNumber) : playerNumber;
					gameBoard[row][col] = message.currentPlayer;
					break;
				}
			}

			updateGameBoardUI();

			// Get the player who sent the message
			if (message.player1username != null && message.player2username != null) {
				// Set player numbers
				if (message.player1username.equals(this.username)) {
					playerNumber = 1;
					opponentName = message.player2username;
				} else {
					playerNumber = 2;
					opponentName = message.player1username;
				}
			}

			if (message.currentPlayer == playerNumber) {
				// Update gameboard
				currentGameState = GameState.MY_TURN;
				gameStateListView.getItems().add(opponentName + " dropped a token in column " + col);
				gameStateListView.getItems().add("Your turn!");
				setColumnButtonsEnabled(true);
			} else {
				gameStateListView.getItems().add("Waiting for " + opponentName + " to move");
			}
		}
	}

	private void handleQuitMessage(Message message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Quit");
		alert.setContentText(message.message);
		alert.showAndWait();
		switchToScene("mainMenu");
		resetGameBoard();
	}

	private void switchToScene(String sceneName) {
		primaryStage.setScene(sceneMap.get(sceneName));

		switch (sceneName) {
			case "login":
				primaryStage.setTitle("Client - Login");
				break;
			case "mainMenu":
				primaryStage.setTitle("Client - Main Menu");
				break;
			case "waiting":
				primaryStage.setTitle("Client - Waiting");
		}
	}

	private Scene createLoginScene() {
		usernameField = new TextField();
		usernameField.setPromptText("Enter username");

		loginButton = new Button("Login");
		// make button click when user presses ENTER
		loginButton.setDefaultButton(true);
		loginButton.setOnAction(e -> {
			username = usernameField.getText();
			if (username != null && !username.trim().isEmpty()) {
				// Send login message to server
				Message loginMsg = new Message(MessageType.LOGIN, username, "login request");
				clientConnection.send(loginMsg);
				statusLabel.setText("Logging in...");
			} else {
				statusLabel.setText("Please enter a username");
			}
		});

		statusLabel = new Label("Please login to continue");
		statusLabel.setFont(customFont);

		Label welcomeLabel = new Label("Welcome! Please login:");
		welcomeLabel.setFont(customFont);

		VBox loginBox = new VBox(10);
		loginBox.setPadding(new Insets(20));
		loginBox.getChildren().addAll(
				new Label("Welcome! Please login:"),
				usernameField,
				loginButton,
				statusLabel
		);
		loginBox.setStyle("-fx-background-color: lightblue; -fx-font-family: 'Arial'; -fx-font-size: 14px;");

		return new Scene(loginBox, 400, 300);
	}

	private Scene createMainMenuScene() {
		Label mainMenuLabel = new Label("Main Menu - Welcome " + this.username);
		mainMenuLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		Label onlineLabel = new Label("Online Users:");
		onlineLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		onlineUsersListView = new ListView<>();
		onlineUsersListView.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		Label offlineLabel = new Label("Offline Users:");
		offlineLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		offlineUsersListView = new ListView<>();
		offlineUsersListView.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		VBox onlineBox = new VBox(10, onlineLabel, onlineUsersListView);
		VBox offlineBox = new VBox(10, offlineLabel, offlineUsersListView);
		HBox connectionStatusBox = new HBox(10, onlineBox, offlineBox);

		joinGameButton = new Button("Join Button");
		joinGameButton.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		// When clicked, a new message is sent to the server
		// where the player is put into the waiting queue
		joinGameButton.setOnAction(e -> {
			Message joinGameMsg = new Message(MessageType.JOIN_GAME, this.username, "join game request");
			clientConnection.send(joinGameMsg);
		});

		logoutButton = new Button("Logout");
		logoutButton.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		HBox buttonBox = new HBox(10, joinGameButton, logoutButton);

		VBox mainMenuBox = new VBox(10);
		mainMenuBox.setPadding(new Insets(20));
		mainMenuBox.getChildren().addAll(
				mainMenuLabel,
				connectionStatusBox,
				buttonBox
		);

		return new Scene(mainMenuBox, 500, 500);
	}

	private Scene createWaitingScene() {
		waitingListView = new ListView<>();
		waitingListView.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");
		// main menu button
		Button mainMenuButton = new Button("Main Menu");
		mainMenuButton.setOnAction(e -> {
			Message leaveMsg = new Message(MessageType.LEAVE_QUEUE, username);
			clientConnection.send(leaveMsg);
			switchToScene("mainMenu");
		});

		VBox mainMenuBox = new VBox(10);
		mainMenuBox.setPadding(new Insets(20));
		mainMenuBox.getChildren().addAll(
			waitingListView, mainMenuButton
		);

		return new Scene(mainMenuBox, 500, 500);
	}

	private Scene createGameScene() {
		gameBoardLabel = new Label("Game against " + opponentName);
		gameBoardLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		gameBoardGrid = new GridPane();
		dropButtonGrid = new GridPane();

		//create token drop buttons and save to columnButtons[] array
		for (int col = 0; col < COLS; col++) {
			// visuals
			Button dropButton = new Button("↓");
			Circle circle = new Circle(17);
			dropButton.setShape(circle);
			dropButton.setMinSize(34, 34);
			dropButton.setMaxSize(34, 34);
			StackPane stack = new StackPane(dropButton);
			stack.setPrefSize(40, 40);

			// actions
			int currentCol = col;
			dropButton.setOnAction(e -> {
				makeMove(currentCol);
			});

			columnButtons[col] = dropButton; // add reference to array
			dropButtonGrid.add(stack, col, 0);
		}
		// create gameboard grid circles and save to gameBoardDisplay array
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				StackPane stack = new StackPane();
				stack.setPrefSize(40, 40);
				stack.setStyle("-fx-border-color: black; -fx-background-color: gray");
				Circle circle = new Circle(40 / 2.5);
				circle.setFill(Color.WHITE);
				stack.getChildren().add(circle);

				gameBoardDisplay[row][col] = stack; // add reference to array
				gameBoardGrid.add(stack, col, row);
			}
		}

		// Chat box
//		Label messageBoxLabel;
//		ListView<String> messageBoxListView;
//		VBox messageBox;
		messageBoxLabel = new Label("Chat Box");
		messageBoxLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");
		messageListView = new ListView<>();
		messageListView.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		inputField = new TextField();
		inputField.setPromptText("Type a message...");
		inputField.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");
		inputField.setPrefWidth(300);

		sendButton = new Button("Send");
		sendButton.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		// Sends a message when clicking send button
		sendButton.setOnAction(e -> {
			String message = inputField.getText().trim();
			if (!message.isEmpty()) {
				messageListView.getItems().add("You: " + message);
				sendTextMessage(message);
				inputField.clear(); // clear after sending
			}
		});

		// Sends a message when pressing ENTER key
		inputField.setOnAction(e -> sendButton.fire());

		// quit game button
		Button quitGameButton = new Button("Quit Game");
		quitGameButton.setOnAction(e -> {
			Message quitMsg = new Message(MessageType.QUIT_GAME, username);
			clientConnection.send(quitMsg);
			switchToScene("mainMenu");
			resetGameBoard();
		});

		HBox inputArea = new HBox(10, inputField, sendButton);
		messageBox = new VBox(10, messageBoxLabel, messageListView, inputArea, quitGameButton);

		gameStateListView = new ListView<>();
		gameStateListView.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		gameBox = new VBox(10, gameBoardLabel, dropButtonGrid, gameBoardGrid, gameStateListView);
		gameBox.setPadding(new Insets(20));

		HBox gameDisplay = new HBox(10, gameBox, messageBox);
		return new Scene(gameDisplay, 800, 700);
	}

	private void sendTextMessage(String message) {
		Message textMessage = new Message(MessageType.TEXT, username, message);
		clientConnection.send(textMessage);
	}

	// Called when game state changes, updates based on gameboard passed
	// in message from server. Also updates whose turn it is.
	private void updateGameBoardUI() {
		// Add the correct color piece to the gameboard
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				StackPane cell = gameBoardDisplay[row][col];
				Circle circle = (Circle) cell.getChildren().get(0);

				switch (gameBoard[row][col]) {
					case 0:
						circle.setFill(Color.WHITE);
						break;
					case 1:
						circle.setFill(Color.RED); // Player 1 color
						break;
					case 2:
						circle.setFill(Color.YELLOW); // Player 2 color
						break;
				}
			}
		}
	}

	// reset game board by changing all cells to 0 and updating UI
	private void resetGameBoard() {
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				gameBoard[row][col] = 0;
			}
		}
		updateGameBoardUI();
	}

	private void makeMove(int column) {
		gameStateListView.getItems().add("You dropped a token in column " + column);
		currentGameState = GameState.OPPONENT_TURN;
		setColumnButtonsEnabled(false);
		Message moveMessage = new Message(MessageType.GAME_ACTION, username, "move", column);
		clientConnection.send(moveMessage);
	}

}
