import java.io.Serializable;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;

    int recipient;

    String message;

    public Message(String input){
        message = input;
    }
    public Message(String input, int rec) {
        message = input;
        recipient = rec;
    }
    public String toString(){
        return message;
    }
}
