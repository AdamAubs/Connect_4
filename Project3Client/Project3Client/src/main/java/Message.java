import java.io.Serializable;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;

    MessageType type; // Can be text, newUser, disconnected
    String sender; // Username of sender
    int recipient; // Id of recipient
    String message; // General message content
    Object data; // Additional data that might be required

    public Message(MessageType type) {
        this.type = type;
    }

    public Message(String input){
        message = input;
        type = MessageType.TEXT;
    }

    public Message(String input, int rec) {
        message = input;
        recipient = rec;
        type = MessageType.TEXT;
    }

    public Message(MessageType type, String sender, String message) {
        this.type = type;
        this.sender = sender;
        this.message = message;
    }
    public String toString(){
        return message;
    }
}
