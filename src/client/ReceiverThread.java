package client;

import network.*;
import server.Message;
import java.io.*;

public class ReceiverThread implements Runnable {
    private ObjectInputStream in;

    public ReceiverThread(ObjectInputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Packet<?> packet = (Packet<?>) in.readObject();

                if (packet.getType() == PacketType.CHAT) {
                    Message msg = (Message) packet.getPayload();
                    System.out.println("[" + msg.getSender() + "]: " + msg.getContent());

                } else if (packet.getType() == PacketType.ERROR) {
                    System.out.println("Error: " + packet.getPayload());

                } else {
                    System.out.println("Server: " + packet.getPayload());
                }
            }
        } catch (Exception e) {
            System.out.println("Disconnected from server");
        }
    }
}
