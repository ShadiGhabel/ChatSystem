package server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoomService {
    private static RoomService instance;
    private Map<String, Room> rooms;

    private RoomService() {
        rooms = new ConcurrentHashMap<>();
        rooms.put("lobby", new Room("lobby"));
    }

    public static synchronized RoomService getInstance() {
        if (instance == null) {
            instance = new RoomService();
        }
        return instance;
    }

    public Room createRoom(String roomName) throws Exception {
        if (rooms.containsKey(roomName)) {
            throw new Exception("Room already exists");
        }
        Room room = new Room(roomName);
        rooms.put(roomName, room);
        return room;
    }

    public Room getRoom(String roomName) {
        return rooms.get(roomName);
    }

    public boolean roomExists(String roomName) {
        return rooms.containsKey(roomName);
    }

    public List<String> listRooms() {
        return new ArrayList<>(rooms.keySet());
    }

    public void joinRoom(String roomName, String username) throws Exception {
        Room room = rooms.get(roomName);
        if (room == null) {
            throw new Exception("Room not found");
        }
        room.addMember(username);
    }

    public void leaveRoom(String roomName, String username) throws Exception {
        Room room = rooms.get(roomName);
        if (room == null) {
            throw new Exception("Room not found");
        }
        room.removeMember(username);
    }

    public List<String> getRoomMembers(String roomName) throws Exception {
        Room room = rooms.get(roomName);
        if (room == null) {
            throw new Exception("Room not found");
        }
        return room.getMembers();
    }
}