import java.io.Serializable;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;


    int clientId; // Index of client in ArrayList of clients
    MessageType type; // Can be text, newUser, disconnected
    String sender; // Username of sender
    String message; // General message content
    String player1username;
    String player2username;
    int currentPlayer; // Indicates which players turn it is (player 1 or 2)
    int[][] gameBoard;
    int lastMoveColumn;

    public Message(String input){
        message = input;
        type = MessageType.TEXT;
    }

    public Message(MessageType type, int count) {
        this.type = type;
        this.clientId = count;
    }

    public Message(MessageType type, String sender, String message) {
        this.type = type;
        this.sender = sender;
        this.message = message;
    }

    // Game_Action message
    public Message(MessageType type, String sender, String player1, String player2, String message, int[][] gameBoard, int currentPlayer) {
        this.type = type;
        this.sender = sender;
        this.player1username = player1;
        this.player2username = player2;
        this.message = message;
        this.gameBoard = gameBoard;
        this.currentPlayer = currentPlayer;
    }

    // message for game action
    public Message(MessageType type, String sender, String message, int column) {
        this.type = type;
        this.sender = sender;
        this.message = message;
        this.lastMoveColumn = column;
    }


    public String toString(){
        return message;
    }
}
