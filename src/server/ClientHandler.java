package server;

import network.*;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable{
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private UserSession session;
    private Map<String, UserSession> users;
    private Map<String, String> registeredUsers;
    private RoomService roomService;

    public ClientHandler(Socket socket, Map<String, UserSession> users, Map<String, String> registeredUsers) {
        this.socket = socket;
        this.users = users;
        this.session = new UserSession();
        this.registeredUsers = registeredUsers;
        this.roomService = RoomService.getInstance();
    }
    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            session.setOut(out);
            while (true) {
                Packet<?> packet = (Packet<?>) in.readObject();
                handlePacket(packet);
            }
        } catch (Exception e) {
            cleanup();
        }
    }
    private void handlePacket(Packet<?> packet) throws IOException {
        try {
            switch (packet.getType()) {
                case REGISTER -> handleRegister((String) packet.getPayload());
                case LOGIN -> handleLogin((String) packet.getPayload());
                case CREATE_ROOM -> handleCreateRoom((String) packet.getPayload());
                case JOIN_ROOM -> handleJoinRoom((String) packet.getPayload());
                case LEAVE_ROOM -> handleLeaveRoom();
                case LIST_ROOMS -> handleListRooms();
                case LIST_USERS -> handleListUsers();
                case CHAT -> handleChat((Message) packet.getPayload());
                default -> sendError("Invalid command");
            }
        } catch (Exception e) {
            sendError(e.getMessage());
        }
    }
    private void handleRegister(String username) {
        if (username == null || username.trim().isEmpty()) {
            sendError("Username cannot be empty");
            return;
        }

        username = username.trim();

        if (registeredUsers.containsKey(username)) {
            sendError("Username already exists");
            return;
        }

        registeredUsers.put(username, "registered");
        sendResponse(PacketType.REGISTER, "Registration successful! Please login.");
    }
    private void handleLogin(String username) {
        if (username == null || username.trim().isEmpty()) {
            sendError("Username cannot be empty");
            return;
        }
        username = username.trim();

        if (!registeredUsers.containsKey(username)) {
            sendError("Username not registered. Please register first.");
            return;
        }
        if (users.containsKey(username)) {
            sendError("User already logged in");
            return;
        }
        session.setUsername(username);
        session.setLoggedIn(true);
        users.put(username, session);

        try {
            roomService.joinRoom("lobby", username);
            session.setCurrentRoom("lobby");
            Room lobby = roomService.getRoom("lobby");
            if (lobby != null) {
                List<Message> history = lobby.getHistory();
                if (!history.isEmpty()) {
                    Message joinMsg = history.get(history.size() - 1);
                    broadcastToRoom("lobby", joinMsg);
                }
            }
            sendResponse(PacketType.LOGIN, "Logged in successfully! You are in 'lobby'");
        } catch (Exception e) {
            sendError("Login failed: " + e.getMessage());
        }
    }
    private void handleCreateRoom(String roomName) {
        if (!session.isLoggedIn()) {
            sendError("Please login first");
            return;
        }

        if (roomName == null || roomName.trim().isEmpty()) {
            sendError("Room name cannot be empty");
            return;
        }

        try {
            roomService.createRoom(roomName.trim());
            sendResponse(PacketType.CREATE_ROOM, "Room '" + roomName + "' created successfully");
        } catch (Exception e) {
            sendError(e.getMessage());
        }
    }
    private void handleJoinRoom(String roomName) {
        if (!session.isLoggedIn()) {
            sendError("Please login first");
            return;
        }

        if (roomName == null || roomName.trim().isEmpty()) {
            sendError("Room name cannot be empty");
            return;
        }

        try {
            if (session.getCurrentRoom() != null) {
                String oldRoom = session.getCurrentRoom();
                Room room = roomService.getRoom(oldRoom);

                roomService.leaveRoom(oldRoom, session.getUsername());
                if (room != null) {
                    List<Message> history = room.getHistory();
                    if (!history.isEmpty()) {
                        Message leaveMsg = history.get(history.size() - 1);
                        broadcastToRoom(oldRoom, leaveMsg);
                    }
                }
            }

            roomService.joinRoom(roomName.trim(), session.getUsername());
            session.setCurrentRoom(roomName.trim());
            Room newRoom = roomService.getRoom(roomName.trim());
            if (newRoom != null) {
                List<Message> history = newRoom.getHistory();
                if (!history.isEmpty()) {
                    Message joinMsg = history.get(history.size() - 1);
                    broadcastToRoom(roomName.trim(), joinMsg);
                }
            }
            sendResponse(PacketType.JOIN_ROOM, "Joined room: " + roomName);
        } catch (Exception e) {
            sendError(e.getMessage());
        }
    }
    private void handleLeaveRoom() {
        if (!session.isLoggedIn()) {
            sendError("Please login first");
            return;
        }

        if (session.getCurrentRoom() == null) {
            sendError("You are not in any room");
            return;
        }

        try {
            String roomName = session.getCurrentRoom();
            Room room = roomService.getRoom(roomName);
            if (room != null) {
                List<Message> history = room.getHistory();

                roomService.leaveRoom(roomName, session.getUsername());
                if (!history.isEmpty()) {
                    Message leaveMsg = history.get(history.size() - 1);
                    broadcastToRoom(roomName, leaveMsg);
                }
            }
            session.setCurrentRoom(null);
            sendResponse(PacketType.LEAVE_ROOM, "Left room: " + roomName);
        } catch (Exception e) {
            sendError(e.getMessage());
        }
    }
    private void handleListRooms() {
        if (!session.isLoggedIn()) {
            sendError("Please login first");
            return;
        }

        List<String> rooms = roomService.listRooms();
        String roomList = String.join("\n", rooms);
        sendResponse(PacketType.LIST_ROOMS, roomList);
    }
    private void handleListUsers() {
        if (!session.isLoggedIn()) {
            sendError("Please login first");
            return;
        }
        if (session.getCurrentRoom() == null) {
            sendError("You are not in any room");
            return;
        }
        try {
            List<String> members = roomService.getRoomMembers(session.getCurrentRoom());
            String userList = String.join("\n", members);
            sendResponse(PacketType.LIST_USERS, userList);
        } catch (Exception e) {
            sendError(e.getMessage());
        }
    }
    private void handleChat(Message msg) {
        if (!session.isLoggedIn()) {
            sendError("Please Login first");
            return;
        }
        if (session.getCurrentRoom() == null) {
            sendError("You are not in any room. Join a room first.");
            return;
        }

        try {
            Room room = roomService.getRoom(session.getCurrentRoom());
            if (room != null) {
                room.addMessage(msg);
                broadcastToRoom(session.getCurrentRoom(), msg);
            }
        } catch (Exception e) {
            sendError("Failed to send message: " + e.getMessage());
        }
    }
    private void broadcastToRoom(String roomName, Message message) {
        try {
            Room room = roomService.getRoom(roomName);
            if (room == null) return;

            List<String> members = room.getMembers();
            Packet<Message> packet = new Packet<>(PacketType.CHAT, message);

            for (String memberName : members) {
                UserSession memberSession = users.get(memberName);
                if (memberSession != null && memberSession.getOut() != null) {
                    try {
                        memberSession.getOut().writeObject(packet);
                        memberSession.getOut().flush();
                    } catch (IOException e) {
                        System.err.println("Failed to send to: " + memberName);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Broadcast error: " + e.getMessage());
        }
    }
    private void sendResponse(PacketType type, Object payload) {
        try {
            Packet<Object> packet = new Packet<>(type, payload);
            out.writeObject(packet);
            out.flush();
        } catch (IOException e) {
            System.err.println("Failed to send response: " + e.getMessage());
        }
    }

    private void sendError(String errorMessage) {
        try {
            Packet<String> packet = new Packet<>(PacketType.ERROR, errorMessage);
            out.writeObject(packet);
            out.flush();
        } catch (IOException e) {
            System.err.println("Failed to send error: " + e.getMessage());
        }
    }
    private void cleanup() {
        try {
            if (session.isLoggedIn()) {
                if (session.getCurrentRoom() != null) {
                    String roomName = session.getCurrentRoom();
                    Room room = roomService.getRoom(roomName);
                    roomService.leaveRoom(session.getCurrentRoom(), session.getUsername());
                    if (room != null) {
                        List<Message> history = room.getHistory();
                        if (!history.isEmpty()) {
                            Message leaveMsg = history.get(history.size() - 1);
                            broadcastToRoom(roomName, leaveMsg);
                        }
                    }
                }
                users.remove(session.getUsername());
            }
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null)
                socket.close();
        } catch (Exception e) {
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }
}
