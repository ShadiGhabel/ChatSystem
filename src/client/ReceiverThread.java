package client;

import network.*;
import common.Message;
import common.ExportData;
import java.io.*;

public class ReceiverThread implements Runnable {
    private ObjectInputStream in;
    public static String exportSavePath = "export.json";

    public ReceiverThread(ObjectInputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Packet<?> packet = (Packet<?>) in.readObject();
                handlePacket(packet);
            }
        } catch (Exception e) {
            System.out.println("\nDisconnected from server");
            System.exit(0);
        }
    }

    private void handlePacket(Packet<?> packet) {
        switch (packet.getType()) {
            case REGISTER:
                System.out.println("> " + packet.getPayload());
                break;

            case LOGIN:
                System.out.println("> " + packet.getPayload());
                break;

            case CREATE_ROOM:
                System.out.println("> " + packet.getPayload());
                break;

            case JOIN_ROOM:
                System.out.println("> " + packet.getPayload());
                break;

            case LEAVE_ROOM:
                System.out.println("> " + packet.getPayload());
                break;

            case LIST_ROOMS:
                System.out.println("\n=== Available Rooms ===");
                System.out.println(packet.getPayload());
                System.out.println("======================\n");
                break;

            case LIST_USERS:
                System.out.println("\n=== Users in Room ===");
                System.out.println(packet.getPayload());
                System.out.println("====================\n");
                break;

            case CHAT:
                Message msg = (Message) packet.getPayload();
                if (msg.getType() == Message.MessageType.SYSTEM) {
                    System.out.println("*** " + msg.getContent());
                } else {
                    System.out.println("[" + msg.getSender() + "]: " + msg.getContent());
                }
                break;

            case ERROR:
                System.out.println("[!] Error: " + packet.getPayload());
                break;

            default:
                System.out.println("Server: " + packet.getPayload());
        }
    }
}