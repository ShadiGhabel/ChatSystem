package client;

import network.*;
import common.*;

import java.io.*;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.UUID;

public class SenderThread implements Runnable {
    private ObjectOutputStream out;
    private Scanner scanner;
    private String username;
    private boolean loggedIn;
    private static final long MAX_FILE_SIZE = 200 * 1024;

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

            case "/upload":
                handleUpload(input);
                break;

            case "/download":
                handleDownload(input);
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

    private void handleUpload(String input) {
        String[] parts = input.split("\\s+");
        if (parts.length < 2) {
            System.out.println("Usage: /upload <localPath>");
            System.out.println("Example: /upload document.txt");
            return;
        }

        String filePath = parts[1];
        File file = new File(filePath);

        try {
            if (!file.exists()) {
                System.out.println("[!] Error: File not found: " + filePath);
                return;
            }

            if (!filePath.toLowerCase().endsWith(".txt")) {
                System.out.println("[!] Error: Only .txt files are allowed");
                return;
            }

            long fileSize = file.length();
            if (fileSize > MAX_FILE_SIZE) {
                System.out.println("[!] Error: File too large (max 200KB)");
                System.out.println("    Current size: " + (fileSize / 1024) + "KB");
                return;
            }

            byte[] content = Files.readAllBytes(file.toPath());

            String fileId = UUID.randomUUID().toString();
            FileMetadata metadata = new FileMetadata(
                    fileId,
                    file.getName(),
                    username,
                    fileSize
            );

            FileData fileData = new FileData(metadata, content);

            Packet<FileData> packet = new Packet<>(PacketType.FILE_UPLOAD_REQ, fileData);
            out.writeObject(packet);
            out.flush();

            System.out.println("Uploading file: " + file.getName() + " (" + fileSize + " bytes)...");

        } catch (IOException e) {
            System.out.println("[!] Error uploading file: " + e.getMessage());
        }
    }

    private void handleDownload(String input) {
        String[] parts = input.split("\\s+");
        if (parts.length < 3) {
            System.out.println("Usage: /download <fileId> <savePath>");
            System.out.println("Example: /download abc123 downloaded.txt");
            return;
        }

        String fileId = parts[1];
        String savePath = parts[2];

        try {
            ReceiverThread.downloadSavePath = savePath;

            sendPacket(PacketType.FILE_DOWNLOAD_REQ, fileId);
            System.out.println("Requesting download for fileId: " + fileId);

        } catch (IOException e) {
            System.out.println("[!] Error requesting download: " + e.getMessage());
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
        System.out.println("File Transfer:");
        System.out.println("  /upload <localPath>   - Upload .txt file (max 200KB)");
        System.out.println("                          Example: /upload document.txt");
        System.out.println("  /download <fileId> <savePath>");
        System.out.println("                        - Download file from server");
        System.out.println("                          Example: /download abc123 myfile.txt");
        System.out.println();
        System.out.println("Export:");
        System.out.println("  /export last <N> <savePath>  - Export last N messages (1-200)");
        System.out.println("                                 Example: /export last 10 chat.json");
        System.out.println();
        System.out.println("Other:");
        System.out.println("  /help                 - Show this help");
        System.out.println("  /exit                 - Exit application");
        System.out.println("========================\n");
    }
}