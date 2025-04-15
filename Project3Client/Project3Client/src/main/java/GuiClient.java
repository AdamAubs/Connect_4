

import java.util.Scanner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextField;

import javafx.stage.Stage;

public class GuiClient extends Application{
	
	public static void main(String[] args) {
		// Create a new client thread which
		// is defined in the Client class
		Client clientThread = new Client();
		// Start the new client thread
		clientThread.start();
		// Allow for user input through the terminal
		Scanner s = new Scanner(System.in);
		// Get the user input
		while (s.hasNext()){
			String x = s.nextLine();
			// Use the client thread send method to send
			// the string
			clientThread.send(new Message(x, 2));
		}

		launch(args);
	}

	// Put Clients GUI code here.
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setScene(new Scene(new TextField("I am not yet implemented")));
		primaryStage.setTitle("Client");
		primaryStage.show();
		
	}
}
