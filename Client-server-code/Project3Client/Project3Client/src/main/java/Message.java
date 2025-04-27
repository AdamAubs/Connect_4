import java.io.Serializable;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;

    MessageType type; // Can be text, newUser, disconnected
    String sender; // Username of sender
    String message; // General message content
    String player1username;
    String player2username;
    int[][] gameboard;
    int currentPlayer; // 1 or 2
    int lastMoveColumn; // For GAME_ACTION messages when updating gameboard on server

    public Message(MessageType type, String sender, String message) {
        this.type = type;
        this.sender = sender;
        this.message = message;
    }

    public Message(MessageType type, String sender, String player1, String player2, String message) {
        this.type = type;
        this.sender = sender;
        this.player1username = player1;
        this.player2username = player2;
        this.message = message;
    }

    // message for game action
    public Message(MessageType type, String sender, String message, int column) {
        this.type = type;
        this.sender = sender;
        this.message = message;
        this.lastMoveColumn = column;
    }

    // message for leaving queue, quitting game, or rematch
    public Message(MessageType type, String sender) {
        this.type = type;
        this.sender = sender;
    }

    public String toString(){
        return message;
    }
}
