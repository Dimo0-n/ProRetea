package udp;

import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 65432;
    private static DatagramSocket socket;
    private static Map<String, String> clients = new HashMap<>();
    private static Map<String, InetSocketAddress> ipMapping = new HashMap<>();
    private static int ipCounter = 2;

    public static void main(String[] args) {
        try {
            socket = new DatagramSocket(PORT);
            System.out.println("Serverul este pornit...");

            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());

                InetAddress clientIP = packet.getAddress();
                int clientPort = packet.getPort();
                InetSocketAddress clientAddress = new InetSocketAddress(clientIP, clientPort);

                if (message.startsWith("REGISTER:")) {
                    String username = message.split(":")[1].trim();
                    String virtualIP = "127.0.0." + ipCounter++;
                    clients.put(username, virtualIP);
                    ipMapping.put(virtualIP, clientAddress);
                    System.out.println(username + " s-a înregistrat cu IP virtual: " + virtualIP);
                } else if (message.startsWith("private:")) {

                    String[] parts = message.split(":", 3);
                    if (parts.length == 3) {
                        String recipient = parts[1].trim();
                        String privateMessage = parts[2].trim();
                        String sender = getUsernameByIP(clientIP);

                        String recipientIP = clients.get(recipient);
                        if (recipientIP != null && ipMapping.containsKey(recipientIP)) {
                            InetSocketAddress recipientAddress = ipMapping.get(recipientIP);
                            String formattedMessage = sender + " (privat): " + privateMessage;
                            sendMessage(formattedMessage, recipientAddress);
                        } else {
                            sendMessage("Utilizatorul " + recipient + " nu este conectat.", clientAddress);
                        }
                    }
                } else {

                    String sender = getUsernameByIP(clientIP);
                    if (sender != null) {
                        broadcast(message, clientAddress);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendMessage(String message, InetSocketAddress recipientAddress) {
        try {
            byte[] sendData = message.getBytes();
            DatagramPacket packet = new DatagramPacket(sendData, sendData.length, recipientAddress.getAddress(), recipientAddress.getPort());
            socket.send(packet);
        } catch (Exception e) {
            System.out.println("Eroare la trimiterea mesajului: " + e.getMessage());
        }
    }

    private static void broadcast(String message, InetSocketAddress senderAddress) {
        System.out.println("[Canal general]: " + message);

        for (InetSocketAddress clientAddress : ipMapping.values()) {
            if (!clientAddress.equals(senderAddress)) {
                sendMessage(message, clientAddress);
            }
        }
    }

    private static String getUsernameByIP(InetAddress ip) {
        for (Map.Entry<String, InetSocketAddress> entry : ipMapping.entrySet()) {
            if (entry.getValue().getAddress().equals(ip)) {
                return getUsernameByVirtualIP(entry.getKey());
            }
        }
        return null;
    }

    private static String getUsernameByVirtualIP(String virtualIP) {
        for (Map.Entry<String, String> entry : clients.entrySet()) {
            if (entry.getValue().equals(virtualIP)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
