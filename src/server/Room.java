package server;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private String name;
    private List<String> members;
    private List<Message> history;

    public Room(String name) {
        this.name = name;
        this.members = new ArrayList<>();
        this.history = new ArrayList<>();
    }

    public synchronized void addMember(String username) {
        if (!members.contains(username)) {
            members.add(username);
            Message sysMsg = new Message("SYSTEM", Message.MessageType.SYSTEM,
                    username + " joined the room");
            history.add(sysMsg);
        }
    }

    public synchronized void removeMember(String username) {
        members.remove(username);
        Message sysMsg = new Message("SYSTEM", Message.MessageType.SYSTEM,
                username + " left the room");
        history.add(sysMsg);
    }

    public synchronized void addMessage(Message message) {
        history.add(message);
    }

    public synchronized List<Message> getLastNMessages(int n) {
        int size = history.size();
        int fromIndex = Math.max(0, size - n);
        return new ArrayList<>(history.subList(fromIndex, size));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMembers() {
        return new ArrayList<>(members);
    }

    public List<Message> getHistory() {
        return new ArrayList<>(history);
    }

    @Override
    public String toString() {
        return "Room{" +
                "name='" + name + '\'' +
                ", members=" + members.size() +
                ", messages=" + history.size() +
                '}';
    }
}
