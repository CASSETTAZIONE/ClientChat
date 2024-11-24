package it.itismeucci;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientChat {
    public static final String SERVER = "localhost";
    public static final int PORT = 3645;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connesso al server chat. Inserisci i comandi:");
            new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println(response);
                    }
                } catch (IOException e) {
                    System.err.println("Connessione chiusa dal server");
                }
            }).start();

            while (true) {
                String input = scanner.nextLine();
                out.writeBytes(input + "\n");
                if (input.equals("/quit")) {
                    System.out.println("Disconnessione...");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Errore di connessione: " + e.getMessage());
        }
    }
}
