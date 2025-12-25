package client;

import network.*;
import server.Message;
import java.io.*;
import java.util.Scanner;

public class SenderThread implements Runnable {
    private ObjectOutputStream out;
    private Scanner scanner;
    private String username;

    public SenderThread(ObjectOutputStream out) {
        this.out = out;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        try {
            System.out.print("Enter username: ");
            username = scanner.nextLine();

            Packet<String> loginPacket = new Packet<>(PacketType.LOGIN, username);
            out.writeObject(loginPacket);
            out.flush();

            Thread.sleep(500);
            System.out.println("\nType your messages (or /exit to quit):\n");

            while (true) {
                String text = scanner.nextLine();

                if (text.equals("/exit")) {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }

                if (text.trim().isEmpty()) continue;

                Message msg = new Message(username, Message.MessageType.TEXT, text);
                Packet<Message> chatPacket = new Packet<>(PacketType.CHAT, msg);
                out.writeObject(chatPacket);
                out.flush();
            }

        } catch (Exception e) {
            System.out.println("Send error: " + e.getMessage());
        }
    }
}
