
import java.util.HashMap;

import javafx.application.Application;

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

	public static void main(String[] args) {
		Server serv = new Server();
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// Font to use everywhere
		Font arialFont = Font.font("Arial", 14);

		// UI Components
		Label statusLabel = new Label("Server Status: OFF");
		statusLabel.setFont(arialFont);

		TextArea logArea = new TextArea();
		logArea.setFont(arialFont);
		logArea.setEditable(false);
		logArea.setPrefRowCount(10);

		Button startButton = new Button("Start Server");
		startButton.setFont(arialFont);

		Button stopButton = new Button("Stop Server");
		stopButton.setFont(arialFont);

		Label logLabel = new Label("Logs:");
		logLabel.setFont(arialFont);

		// Layout
		VBox root = new VBox(10);
		root.setStyle("-fx-padding: 10;");
		root.getChildren().addAll(statusLabel, startButton, stopButton, logLabel, logArea);

		// Button Logic (placeholder)
		startButton.setOnAction(e -> {
			statusLabel.setText("Server Status: RUNNING");
			logArea.appendText("Server started...\n");
		});

		stopButton.setOnAction(e -> {
			statusLabel.setText("Server Status: STOPPED");
			logArea.appendText("Server stopped...\n");
		});

		// Stage setup
		primaryStage.setTitle("Simple Server UI");
		primaryStage.setScene(new Scene(root, 400, 300));
		primaryStage.show();
	}

}
