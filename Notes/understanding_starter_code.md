# Setting up the server-client

## Understanding starter code

### Client

Open Project3Client

Inside of the /src/main/java
you will see three class

1. **GuiClient**

```java
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
			String x = s.next();
			// Use the client thread send method to send
			// the string
			clientThread.send(x);
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
```

2. **Client**

```java


```
