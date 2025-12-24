package server;

public class UserSession {
    private String username;
    private Room currentRoom;
    public UserSession(String username){
        this.username = username;
    }
}
