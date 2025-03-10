package udp;

import java.net.*;
import java.util.Scanner;

public class Client1 {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 65422;
    private static DatagramSocket socket;
    private static InetAddress serverAddress;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Introduceți numele dvs: ");
        String userName = scanner.nextLine();

        try {
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName(SERVER_ADDRESS);
            sendMessage("REGISTER:" + userName);

            Thread receiveThread = new Thread(() -> {
                byte[] receiveData = new byte[1024];
                while (running) {
                    try {
                        DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                        socket.receive(packet);
                        String message = new String(packet.getData(), 0, packet.getLength());

                        if (!message.startsWith(userName + ":")) {
                            System.out.println(message);
                        }
                        if (message.equals("exit"))
                            running = false;
                    } catch (Exception e) {
                        System.out.println("Eroare la primirea mesajului: " + e.getMessage());
                    }
                }
            });

            receiveThread.start();

            while (running) {
                String userInput = scanner.nextLine();
                if (userInput.equalsIgnoreCase("exit")) {
                    exit("exit:" + userName, userName);
                    running = false;
                    socket.close();
                    System.out.println("Te-ai deconectat de la server.");
                    break;
                } else if (userInput.startsWith("private:")) {
                    String[] parts = userInput.split(" ", 2);
                    if (parts.length > 1) {
                        String recipient = parts[0].substring(8);
                        String message = parts[1];
                        sendMessage("private:" + recipient + ":" + message);
                    }
                } else {
                    sendMessage(userName + ": " + userInput);
                }
            }
        } catch (Exception e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private static void sendMessage(String message) {
        try {
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), serverAddress, PORT);
            socket.send(packet);
        } catch (Exception e) {
            System.out.println("Eroare la trimiterea mesajului: " + e.getMessage());
        }
    }

    private static void exit(String message, String userName) {
        try {
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), serverAddress, PORT);
            socket.send(packet);
        } catch (Exception e) {
            System.out.println("Eroare la trimiterea mesajului de ieșire: " + e.getMessage());
        }
    }
}
