package tcp;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Server {
    private static final int PORT = 65432;
    private static final Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());
    private static volatile boolean isRunning = true;

    public static void main(String[] args) {
        System.out.println("Serverul este pornit...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            Thread consoleThread = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (isRunning) {
                    String input = scanner.nextLine();
                    if (input.equalsIgnoreCase("server: exit")) {
                        isRunning = false;
                        System.out.println("Serverul se oprește...");
                        broadcast("Serverul se oprește. Toți clienții vor fi deconectați.");
                        try {
                            serverSocket.close();
                        } catch (IOException e) {
                            System.out.println("Eroare la închiderea serverului: " + e.getMessage());
                        }
                        break;
                    }
                }
                scanner.close();
            });
            consoleThread.start();

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new ClientHandler(clientSocket).start();
                } catch (IOException e) {
                    if (!isRunning) {
                        System.out.println("Serverul a fost oprit.");
                        break;
                    }
                    System.out.println("Eroare la acceptarea conexiunii: " + e.getMessage());
                }
            }

            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.close();
                }
                clientWriters.clear();
            }

            System.out.println("Serverul a fost oprit cu succes.");
        } catch (IOException e) {
            System.out.println("Eroare server: " + e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                clientName = in.readLine();
                System.out.println(clientName + " s-a conectat.");
                broadcast(clientName + " s-a alăturat conversației.");

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase(clientName + ": exit")) {

                        System.out.println(clientName + " s-a deconectat.");
                        broadcast(clientName + " a părăsit conversația.");
                        break;
                    }
                    System.out.println("Mesaj primit: " + message);
                    broadcast(message);
                }
            } catch (IOException e) {
                System.out.println(clientName + " s-a pierdut.");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Eroare la închiderea socketului.");
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
                broadcast(clientName + " a părăsit conversația.");
            }
        }
    }

    private static void broadcast(String message) {
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }
    }
}