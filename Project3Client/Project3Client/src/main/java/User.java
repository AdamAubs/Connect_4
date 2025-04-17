import java.io.Serializable;

public class User implements Serializable {
    static final long serialVersionUID = 42L;

    String username;
    public User(String username) {
        this.username = username;
    }
}
