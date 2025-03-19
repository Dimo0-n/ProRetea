package udp;

import java.net.*;
import java.util.Scanner;

public class Client4 {
    private static final String BROADCAST_ADDRESS = "255.255.255.255";
    private static final int MESSAGE_PORT = 65000;
    private static DatagramSocket socket;
    private static InetAddress broadcastAddress;
    private static volatile boolean running = true;
    private static String userName;
    private static String virtualIP;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Introduceți numele dvs: ");
        userName = scanner.nextLine();

        virtualIP = "127.0.0.5";


        try {
            InetAddress addres = InetAddress.getByName(virtualIP);

            socket = new DatagramSocket(MESSAGE_PORT, addres);

            Thread receiveThread = new Thread(() -> {
                byte[] receiveData = new byte[1024];
                while (running) {
                    try {
                        DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                        socket.receive(packet);
                        String message = new String(packet.getData(), 0, packet.getLength());

                        if (message.startsWith("[private]")) {
                            // Mesaj privat
                            System.out.println("(Privat) " + message.substring(9));
                        } else {
                            // Mesaj general
                            System.out.println(message);
                        }
                    } catch (Exception e) {
                        System.out.println("Eroare la primirea mesajului: " + e.getMessage());
                    }
                }
            });

            receiveThread.start();

            while (running) {
                String userInput = scanner.nextLine();
                if (userInput.startsWith("private:")) {
                    // Format: private:127.0.0.X:Mesaj privat
                    String[] parts = userInput.split(":", 3);
                    if (parts.length == 3) {
                        String recipientIP = parts[1];
                        String message = parts[2];
                        sendPrivateMessage(recipientIP, message);
                    } else {
                        System.out.println("Format invalid pentru mesajul privat. Folosește: private:IP:Mesaj");
                    }
                } else {
                    sendBroadcastMessage(userName + ": " + userInput);
                }
            }
        } catch (Exception e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private static void sendBroadcastMessage(String message) {
        try {
            byte[] buffer = message.getBytes();
            broadcastAddress = InetAddress.getByName(BROADCAST_ADDRESS);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastAddress, MESSAGE_PORT);
            socket.send(packet);

        } catch (Exception e) {
            System.out.println("Eroare la trimiterea mesajului de broadcast: " + e.getMessage());
        }
    }

    private static void sendPrivateMessage(String recipientIP, String message) {
        try {
            InetAddress recipientAddress = InetAddress.getByName(recipientIP);
            String privateMessage = "[private] " + userName + ": " + message;
            DatagramPacket packet = new DatagramPacket(privateMessage.getBytes(), privateMessage.length(), recipientAddress, MESSAGE_PORT);
            socket.send(packet);
        } catch (Exception e) {
            System.out.println("Eroare la trimiterea mesajului privat: " + e.getMessage());
        }
    }
}