package server;

public class UserSession {
    private String username;
    private String currentRoom;
    private boolean loggedIn;
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
}
