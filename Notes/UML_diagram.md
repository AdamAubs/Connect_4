# UML Diagram for starter code

### Server Starter Code

Open /src/main/java

1. ****

```java
// Server Process
public class Server{

    // Count how many clients we have
    int count = 1;
    // Stores connected clients
    ArrayList<ClientThread> clients = new ArrayList<ClientThread>();

    // Servers main thread
    TheServer server;

    Server(){
        server = new TheServer(); // Created in GUI Server
        server.start();
    }
    
    // Servers main thread
    public class TheServer extends Thread{

        public void run() {
            // Set up socket on port 5556 to listen for incoming clients
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

    // Creates a new thread for a client
    class ClientThread extends Thread{

        Socket connection;
        int count;
        ObjectInputStream in;
        ObjectOutputStream out;

        // Client constructor gets passed
        // a connection socket, and its identifier of
        // which client it is on the server.
        ClientThread(Socket s, int count){
            this.connection = s;
            this.count = count;
        }

        // Update each of the clients with a message
        public void updateClients(Message message) {
            System.out.println("Client #" + this.count + " said " + message.message + " to " + message.recipient);
            if (message.recipient == 0) {
                for(ClientThread client : clients) {
                    if (this != client) {
                        client.send(message);
                    }
                }
            } else {
                clients.get(message.recipient).send(message);
            }
        }


        // Sends a message to the client using
        // clients' thread output stream
        public void send(Message message) {
            try {
                out.writeObject(message);
            } catch (Exception e) {
                System.out.println("Unable to send message to client");
            }
        }

        // Runs a new client thread on the server
        // setting up its input stream to receive
        // data from the connected client and output stream
        // to send data to the receiving client.
        public void run(){
            try {
                in = new ObjectInputStream(connection.getInputStream());
                out = new ObjectOutputStream(connection.getOutputStream());
                connection.setTcpNoDelay(true);
            } catch(Exception e) {
                System.err.println("Streams not open");
            }

            // Send this message once to all the clients
            // when a new client thread is created
            updateClients(new Message("new client on server: client #"+count));

            // the loop persists until the client
            // disconnects or the server stops running.
            while(true) {
                try {
                    // read in the data that the client wants to send
                    // to another client
                    Message clientMessage = (Message) in.readObject();

                    updateClients(clientMessage);

                } catch(Exception e) {
                    System.err.println("OOOOPPs...Something wrong with the socket from client: " + count + "....closing down!");
                    updateClients(new Message("Client #"+count+" has left the server!"));
                    clients.remove(this);
                    break;
                }
            }
        }//end of run
    }//end of client thread
}
```

Breaking this down into a UML diagram. We have three classes

1. ```Server```
```
+-------------------+
|      Server       |
+-------------------+
| - count: int      |
| - clients: List<> |
| - server: Thread  |
+-------------------+
| + Server()        |
+-------------------+
```

2. 
