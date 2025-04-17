
import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Application;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
	ListView<String> messageList;

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
		loggedInUsersList.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");
		connectedClientsList = new ListView<>();
		connectedClientsList.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

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
						connectedClientsList.getItems().add("Client #" + message.clientId + " has connected to the server");
					case TEXT:
						messageList.getItems().add("From " + message.sender + ": " + message.message);
						break;
					case LOGIN:
						loggedInUsersList.getItems().add("Client with username: " + message.sender + " has logged in.");
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

		BorderPane mainPane = new BorderPane();
		mainPane.setLeft(listsBox);
		mainPane.setCenter(messagesBox);
		mainPane.setPadding(new Insets(10));

		return new Scene(mainPane, 800, 800);
	}

}
