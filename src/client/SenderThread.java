package client;

import network.*;
import common.Message;

import java.io.*;
import java.util.Scanner;

public class SenderThread implements Runnable {
    private ObjectOutputStream out;
    private Scanner scanner;
    private String username;
    private boolean loggedIn;

    public SenderThread(ObjectOutputStream out) {
        this.out = out;
        this.scanner = new Scanner(System.in);
        this.loggedIn = false;
    }

    @Override
    public void run() {
        try {
            System.out.println("Type /help for available commands\n");

            while (true) {
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) continue;

                if (input.startsWith("/")) {
                    handleCommand(input);
                } else {
                    if (!loggedIn) {
                        System.out.println("Please login first!");
                        continue;
                    }
                    sendChatMessage(input);
                }
            }

        } catch (Exception e) {
            System.out.println("Send error: " + e.getMessage());
        }
    }

    private void handleCommand(String input) throws IOException {
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();

        switch (command) {
            case "/help":
                printHelp();
                break;

            case "/exit":
                System.out.println("Goodbye!");
                System.exit(0);
                break;

            case "/register":
                if (parts.length < 2) {
                    System.out.println("Usage: /register <username>");
                    return;
                }
                username = parts[1].trim();
                sendPacket(PacketType.REGISTER, username);
                break;

            case "/login":
                if (parts.length < 2) {
                    System.out.println("Usage: /login <username>");
                    return;
                }
                username = parts[1].trim();
                sendPacket(PacketType.LOGIN, username);
                loggedIn = true;
                break;

            case "/create":
                if (parts.length < 2) {
                    System.out.println("Usage: /create <roomName>");
                    return;
                }
                sendPacket(PacketType.CREATE_ROOM, parts[1].trim());
                break;

            case "/join":
                if (parts.length < 2) {
                    System.out.println("Usage: /join <roomName>");
                    return;
                }
                sendPacket(PacketType.JOIN_ROOM, parts[1].trim());
                break;

            case "/leave":
                sendPacket(PacketType.LEAVE_ROOM, "");
                break;

            case "/rooms":
                sendPacket(PacketType.LIST_ROOMS, "");
                break;

            case "/users":
                sendPacket(PacketType.LIST_USERS, "");
                break;

            case "/export":
                String[] exportParts = input.split("\\s+");
                if (exportParts.length < 4) {
                    System.out.println("Usage: /export last <N> <savePath>");
                    System.out.println("Example: /export last 10 chat.json");
                    return;
                }

                if (!exportParts[1].equals("last")) {
                    System.out.println("Usage: /export last <N> <savePath>");
                    return;
                }

                String n = exportParts[2];
                String savePath = exportParts[3];

                ReceiverThread.exportSavePath = savePath;

                sendPacket(PacketType.EXPORT_REQ, n);
                System.out.println("Requesting export...");
                break;

            default:
                System.out.println("Unknown command. Type /help for available commands.");
        }
    }
    private void sendChatMessage(String text) throws IOException {
        Message msg = new Message(username, Message.MessageType.TEXT, text);
        Packet<Message> packet = new Packet<>(PacketType.CHAT, msg);
        out.writeObject(packet);
        out.flush();
    }

    private void sendPacket(PacketType type, String payload) throws IOException {
        Packet<String> packet = new Packet<>(type, payload);
        out.writeObject(packet);
        out.flush();
    }
    private void printHelp() {
        System.out.println("\n=== Available Commands ===");
        System.out.println("Authentication:");
        System.out.println("  /register <username>  - Register new account");
        System.out.println("  /login <username>     - Login to system");
        System.out.println();
        System.out.println("Room Management:");
        System.out.println("  /create <roomName>    - Create new room");
        System.out.println("  /join <roomName>      - Join a room");
        System.out.println("  /leave                - Leave current room");
        System.out.println("  /rooms                - List all rooms");
        System.out.println("  /users                - List users in current room");
        System.out.println();
        System.out.println("Messaging:");
        System.out.println("  <text>                - Send message (without /)");
        System.out.println();
        System.out.println("Export:");
        System.out.println("  /export last <N> <savePath>  - Export last N messages (1-200)");
        System.out.println();
        System.out.println("Other:");
        System.out.println("  /help                 - Show this help");
        System.out.println("  /exit                 - Exit application");
        System.out.println("========================\n");
    }
}