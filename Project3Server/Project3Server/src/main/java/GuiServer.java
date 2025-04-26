
import java.util.HashMap;

import javafx.application.Application;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.stage.WindowEvent;

public class GuiServer extends Application{
	// Font to use everywhere
	Font customFont = Font.font("Arial", 14);

	// Connection to clients
	Server serverConnection;
	String username;
	boolean loggedIn = false;

	HashMap<String, Scene> sceneMap;
	Stage primaryStage;

	// Connected Clients List
	ListView<String> connectedClientsList;
	ListView<String> loggedInUsersList;
	ObservableList<String> loggedInUsers = FXCollections.observableArrayList();
	ListView<String> messageList;
	ListView<String> gameSessionList;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;

		// Set up the server connection with a client to handle
		// incoming messages
		serverConnection = new Server(message -> handleIncomingMessage(message));

		// Initialize text list
		messageList = new ListView<>();
		messageList.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");
		loggedInUsersList = new ListView<>();
		loggedInUsersList.setItems(loggedInUsers);
		loggedInUsersList.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");
		connectedClientsList = new ListView<>();
		connectedClientsList.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");
		gameSessionList = new ListView<>();
		gameSessionList.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		sceneMap = new HashMap<>();
		sceneMap.put("server", createServerGui());

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent windowEvent) {
				Platform.exit();
				System.exit(0);
			}
		});

		primaryStage.setTitle("This is the Server");
		// Show the server scene first
		primaryStage.setScene(sceneMap.get("server"));
		primaryStage.show();
	}

	// Takes in messages being passed to the server
	private void handleIncomingMessage(Message message) {
		Platform.runLater(() -> {
			try {
				if (message == null) {
					System.out.println("Received null message");
					return;
				}
				// Determine the type of the message being sent to the server
				switch (message.type) {
					case NEWCONNECTION:
						// Add newly connected clients to the connectedClientsList to be displayed
						connectedClientsList.getItems().add("Client #" + message.clientId);
						break;
					case TEXT:
						messageList.getItems().add("From " + message.sender + ": " + message.message);
						break;
					case LOGIN:
						loggedInUsers.add(message.sender);
						break;
					case NEWGAMESESSION:
						gameSessionList.getItems().add(message.message);
						break;
					case LOGOUT:
						loggedInUsers.remove(message.sender);
						break;
					case DISCONNECTED:
						connectedClientsList.getItems().remove("Client #" + message.clientId);
						loggedInUsersList.getItems().remove(message.sender);
						// TODO trying to remove active game when finished...
//						gameSessionList.remove()
						break;
					case WAITING:
						messageList.getItems().add(message.sender + " is waiting for a game");
						break;
				}
			} catch (Exception e) {
				System.err.println("Error handling message: " + e.getMessage());
				e.printStackTrace();
			}
		});
	}

	// Layout to display incoming messages to
	// the server
	public Scene createServerGui() {
		// Add labels for each ListView
		Label clientsLabel = new Label("Connected Clients");
		clientsLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");
		Label usersLabel = new Label("Logged In Users");
		usersLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");
		Label messagesLabel = new Label("Messages");
		messagesLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		VBox clientsBox = new VBox(5, clientsLabel, connectedClientsList);
		VBox usersBox = new VBox(5, usersLabel, loggedInUsersList);
		VBox messagesBox = new VBox(5, messagesLabel, messageList);

		HBox listsBox = new HBox(10, clientsBox, usersBox, messagesBox);

		Label gameSessionsLabel = new Label("Active Game sessions");
		gameSessionsLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

		VBox gameSessionBox = new VBox(5, gameSessionsLabel, gameSessionList);
		VBox verticalBox = new VBox(10, listsBox, gameSessionBox);

		BorderPane mainPane = new BorderPane();
		mainPane.setLeft(verticalBox);
		mainPane.setPadding(new Insets(10));

		return new Scene(mainPane, 800, 700);
	}

}
