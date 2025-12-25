package client;

import network.*;
import java.io.*;
import java.net.Socket;

public class MainClient {
    public static void main(String[] args) {
        try {
            System.out.println("=== Chat Client ===");
            Socket socket = new Socket("localhost", 8080);
            System.out.println("Connected!\n");

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            ReceiverThread receiver = new ReceiverThread(in);
            new Thread(receiver).start();

            SenderThread sender = new SenderThread(out);
            new Thread(sender).start();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}