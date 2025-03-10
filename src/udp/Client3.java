package udp;

import java.net.*;
import java.util.Scanner;

public class Client3 {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 65432;
    private static DatagramSocket socket;
    private static InetAddress serverAddress;

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
                while (true) {
                    try {
                        DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                        socket.receive(packet);
                        String message = new String(packet.getData(), 0, packet.getLength());

                        if (!message.startsWith(userName + ":")) {
                            System.out.println(message);
                        }
                    } catch (Exception e) {
                        System.out.println("Eroare la primirea mesajului: " + e.getMessage());
                    }
                }
            });

            receiveThread.start();

            while (true) {
                String userInput = scanner.nextLine();
                if (userInput.equalsIgnoreCase("exit")) {
                    socket.close();
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
}
