

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Consumer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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

	// Waiting List View
	ListView<String> waitingListView;

	// Game State View
	ListView<String> gameStateListView;

	// Game scene
	Label gameBoardLabel;
	GridPane gameBoardGrid;
	VBox gameBox;

	private GameState currentGameState = GameState.NOT_IN_GAME;
	private int playerNumber;
	private String opponentName;
	private static int ROWS = 6;
	private static int COLS = 7;
	private int[][] gameBoard = new int[ROWS][COLS];

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
						// TODO: handleGameAction(message)
						// reflect opponents move on client scene
						break;
					case DISCONNECTED:
						// TODO: handleDisconnect(message)
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
	// TODO: finish handling game states
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

		// Handle different game states

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
			} else {
				currentGameState = GameState.OPPONENT_TURN;
				gameStateListView.getItems().add("Waiting for " + opponentName + " to move");
			}
		}
		// TODO: handle other game states
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
		}
	}

	private Scene createLoginScene() {
		usernameField = new TextField();
		usernameField.setPromptText("Enter username");

		loginButton = new Button("Login");
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
		logoutButton = new Button("Logout");
		logoutButton.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		onlineUsersListView = new ListView<>();
		onlineUsersListView.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		joinGameButton = new Button("Join Button");
		joinGameButton.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		// When clicked, a new message is sent to the server
		// where the player is put into the waiting queue
		joinGameButton.setOnAction(e -> {
			Message joinGameMsg = new Message(MessageType.JOIN_GAME, this.username, "join game request");
			clientConnection.send(joinGameMsg);
		});

		Label usersLabel = new Label("Online Users:");
		usersLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		Label mainMenuLabel = new Label("Main Menu - Welcome " + this.username);
		mainMenuLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		HBox buttonBox = new HBox(10, joinGameButton, logoutButton);

		Label chatLabel = new Label("Chat: ");
		chatLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		TextField messageField = new TextField();

		Button sendMessageToAllClientsBtn = new Button("Send to All Clients");
		sendMessageToAllClientsBtn.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		HBox messageSendBox = new HBox( 10, messageField, sendMessageToAllClientsBtn);

		VBox mainMenuBox = new VBox(10);
		mainMenuBox.setPadding(new Insets(20));
		mainMenuBox.getChildren().addAll(
				mainMenuLabel,
				usersLabel,
				onlineUsersListView,
				buttonBox,
				chatLabel,
				messageListView,
				messageSendBox
		);

		return new Scene(mainMenuBox, 500, 500);
	}

	private Scene createWaitingScene() {
		waitingListView = new ListView<>();
		waitingListView.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		VBox mainMenuBox = new VBox(10);
		mainMenuBox.setPadding(new Insets(20));
		mainMenuBox.getChildren().addAll(
			waitingListView
		);

		return new Scene(mainMenuBox, 500, 500);
	}

	// TODO: Enhance this to be interactive
	private Scene createGameScene() {
		gameBoardLabel = new Label("Game against " + opponentName);
		gameBoardLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		gameBoardGrid = new GridPane();
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				Button cell = new Button();
				cell.setPrefSize(40, 40);

				gameBoardGrid.add(cell, col, row);
			}
		}

		gameStateListView = new ListView<>();
		gameStateListView.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		gameBox = new VBox(10, gameBoardLabel, gameBoardGrid, gameStateListView);
		gameBox.setPadding(new Insets(20));

		return new Scene(gameBox, 800, 800);
	}

	// Called when game state changes, updates based on gameboard passed
	// in message from server. Also updates who turn it is.
	private void updateGameBoardUI() {
		// TODO: implement
	}

	private void makeMove(int column) {
		// TODO: Implementing sending message back to server to update gameboard
		//
	}

}
