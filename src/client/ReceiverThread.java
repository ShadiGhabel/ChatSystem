package client;

import network.*;
import common.Message;
import common.ExportData;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

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

            case EXPORT_RES:
                handleExportResponse((ExportData) packet.getPayload());
                break;

            case ERROR:
                System.out.println("[!] Error: " + packet.getPayload());
                break;

            default:
                System.out.println("Server: " + packet.getPayload());
        }
    }

    private void handleExportResponse(ExportData exportData) {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{\n");

            json.append("  \"room\": \"").append(escapeJson(exportData.getRoomName())).append("\",\n");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String timestamp = dateFormat.format(new Date(exportData.getExportedAt()));
            json.append("  \"exportedAt\": \"").append(timestamp).append("\",\n");

            json.append("  \"messages\": [\n");

            for (int i = 0; i < exportData.getMessages().size(); i++) {
                Message msg = exportData.getMessages().get(i);
                json.append("    {\n");
                json.append("      \"id\": \"").append(escapeJson(msg.getId())).append("\",\n");
                json.append("      \"sender\": \"").append(escapeJson(msg.getSender())).append("\",\n");
                json.append("      \"timestamp\": ").append(msg.getTimestamp()).append(",\n");
                json.append("      \"type\": \"").append(msg.getType()).append("\",\n");
                json.append("      \"content\": \"").append(escapeJson(msg.getContent())).append("\"\n");
                json.append("    }");

                if (i < exportData.getMessages().size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }

            json.append("  ]\n");
            json.append("}\n");

            try (FileWriter writer = new FileWriter(exportSavePath)) {
                writer.write(json.toString());
                System.out.println("\n[✓] Export successful!");
                System.out.println("    Saved to: " + exportSavePath);
                System.out.println("    Messages: " + exportData.getMessages().size());
                System.out.println("    Room: " + exportData.getRoomName());
            }

        } catch (IOException e) {
            System.out.println("[!] Export failed: " + e.getMessage());
        }
    }

    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}