package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainServer {
    private static final int PORT = 8080;
    private Map<String, UserSession> users;
    private Map<String, String> registeredUsers;

    public MainServer() {
        users = new ConcurrentHashMap<>();
        registeredUsers = new ConcurrentHashMap<>();
    }

    public void start() {
        System.out.println("Server starting on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                ClientHandler handler = new ClientHandler(clientSocket, users, registeredUsers);
                new Thread(handler).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MainServer().start();
    }
}