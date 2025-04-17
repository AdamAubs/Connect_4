

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

	// Game scene
	VBox gameBox;
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

		// Create all scenes
		sceneMap = new HashMap<>();
		sceneMap.put("login", createLoginScene());
		sceneMap.put("mainMenu", createMainMenuScene());

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
					case LOGIN:
						if (message.message.equals("success")) {
							loggedIn = true;
							switchToScene("mainMenu");
						} else {
							statusLabel.setText("Login failed: " + message.message);
						}
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
		joinGameButton = new Button("Join Button");
		joinGameButton.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		logoutButton = new Button("Logout");
		logoutButton.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		onlineUsersListView = new ListView<>();
		Label usersLabel = new Label("Online Users:");
		usersLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		Label mainMenuLabel = new Label();
		mainMenuLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		Label chatLabel = new Label();
		chatLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		HBox buttonBox = new HBox(10, joinGameButton, logoutButton);

		VBox mainMenuBox = new VBox(10);
		mainMenuBox.setPadding(new Insets(20));
		mainMenuBox.getChildren().addAll(
				new Label("Main Menu - Welcome " + username),
				usersLabel,
				onlineUsersListView,
				buttonBox,
				new Label("Chat:"),
				messageListView,
				new HBox(10, messageField = new TextField(), sendButton = new Button("Send"))
		);

		return new Scene(mainMenuBox, 500, 500);
	}


}
