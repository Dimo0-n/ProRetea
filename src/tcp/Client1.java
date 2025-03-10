package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client1 {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 65432;
//A-64
//a-26 11000

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Introduceți numele dvs: ");
        String userName = scanner.nextLine();

        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(userName);

            Thread readThread = new Thread(() -> {
                try {
                    String fromServer;
                    while ((fromServer = in.readLine()) != null) {
                        System.out.println(fromServer);
                    }
                } catch (IOException e) {
                    System.out.println("Conexiunea cu serverul a fost întreruptă.");
                }
            });
            readThread.start();

            String userInput;
            while (true) {
                userInput = scanner.nextLine();
                if (userInput.equalsIgnoreCase("exit")) {
                    out.println(userName + ": exit");
                    System.out.println("Deconectare...");
                    break;
                }
                out.println(userName + ": " + userInput);
            }

            socket.close();
            readThread.interrupt();
        } catch (IOException e) {
            System.out.println("Eroare la conectare: " + e.getMessage());
        }
    }
}