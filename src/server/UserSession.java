package server;
import java.io.ObjectOutputStream;

public class UserSession {
    private String username;
    private String currentRoom;
    private boolean loggedIn;
    private ObjectOutputStream out;
    public UserSession() {
        this.loggedIn = false;
        this.currentRoom = null;
    }
    public UserSession(String username){
        this.username = username;
        this.loggedIn = false;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public boolean isLoggedIn() {
        return loggedIn;
    }
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
    public String getCurrentRoom() {
        return currentRoom;
    }
    public void setCurrentRoom(String currentRoom) {
        this.currentRoom = currentRoom;
    }
    public ObjectOutputStream getOut() {
        return out;
    }
    public void setOut(ObjectOutputStream out) {
        this.out = out;
    }
}
