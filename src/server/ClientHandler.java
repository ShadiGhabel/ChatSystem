package server;

import network.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class ClientHandler implements Runnable{
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private UserSession session;
    private Map<String, UserSession> users;

    public ClientHandler(Socket socket, Map<String, UserSession> users) {
        this.socket = socket;
        this.users = users;
        this.session = new UserSession();
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
        switch (packet.getType()) {
            case LOGIN -> handleLogin((String) packet.getPayload());
            case CHAT  -> handleChat((Message) packet.getPayload());
            default    -> sendError("Unsupported command");
        }
    }
    private void handleLogin(String username) {
        if (users.containsKey(username)) {
            sendError("User already logged in");
            return;
        }

        session.setUsername(username);
        session.setLoggedIn(true);
        session.setCurrentRoom("lobby");
        users.put(username, session);

        sendResponse(PacketType.LOGIN, "Logged in");
    }
    private void handleChat(Message msg) {
        if (!session.isLoggedIn()) {
            sendError("Login first");
            return;
        }

        broadcast(msg);
    }
    private void broadcast(Message message) {
        Packet<Message> packet = new Packet<>(PacketType.CHAT, message);

        for (UserSession userSession : users.values()) {
            try {
                if (userSession.getOut() != null) {
                    userSession.getOut().writeObject(packet);
                    userSession.getOut().flush();
                }
            } catch (Exception e) {
                System.err.println("Failed to send to: " + userSession.getUsername());
            }
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
        if (session.getUsername() != null) {
            users.remove(session.getUsername());
        }
    }
}
