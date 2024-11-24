package it.itismeucci;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String nickname;

    public ChatClient(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connesso al server.");
        } catch (IOException e) {
            System.err.println("Errore nella connessione al server: " + e.getMessage());
            System.exit(1);
        }
    }

    public void start() {
        try (Scanner scanner = new Scanner(System.in)) {
            // Registrazione del nickname
            while (nickname == null || nickname.isEmpty()) {
                System.out.print("Inserisci un nickname: ");
                out.println(scanner.nextLine().trim());
                String response = in.readLine();
                if (response.equals("REG")) {
                    System.out.println("Nickname registrato con successo!");
                    nickname = response.substring(1);
                } else {
                    System.out.println("Nickname già in uso o non valido.");
                }
            }

            // Thread per ricevere messaggi dal server
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage); // Mostra i messaggi ricevuti
                    }
                } catch (IOException e) {
                    System.err.println("Connessione al server terminata: " + e.getMessage());
                }
            }).start();
            // Ciclo per inviare messaggi
            while (true) {
                String input = scanner.nextLine(); // Legge il comando inserito dall'utente
                switch (input.split(" ")[0]) { // Considera solo il primo token del comando
                    case "/quit":
                        System.out.println("Disconnessione dal server...");
                        out.println("!"); // Invia un comando specifico al server per disconnettersi
                        closeConnection(); // Chiude la connessione (implementazione specifica altrove)
                        return; // Esce dal ciclo infinito
                    case "/help":
                        System.out.println("""
                            1. /quit - Disconnessione dal server.
                            2. /list - Mostra l'elenco degli utenti connessi.
                            3. /change <nuovoNickname> - Cambia il tuo nickname.
                            4. /msg <utente> <messaggio> - Invia un messaggio privato a un utente.
                            5. /help - Mostra questo messaggio di aiuto.
                            """);
                        break;
                    case "/list":
                        out.println("*"); // Richiede l'elenco degli utenti al server
                        String list = in.readLine(); // Riceve la risposta dal server
                        System.out.println(list); // Mostra l'elenco degli utenti connessi
                        break;
                    default:
                        if (input.startsWith("/change ")) {
                            // Estrai il nuovo nickname
                            String newNickname = input.substring(8).trim();
                            if (!newNickname.isEmpty()) {
                                out.println("@" + newNickname); // Invia il comando di cambio nickname al server
                                String response = in.readLine(); // Riceve la risposta del server
                                if (response.equals("OK")) {
                                    nickname = newNickname; // Aggiorna il nickname
                                    System.out.println("Nickname aggiornato: " + nickname);
                                } else {
                                    System.out.println("Errore: Nickname già in uso o non valido.");
                                }
                            } else {
                                System.out.println("Errore: Inserire un nuovo nickname valido.");
                            }
                        } else if (input.startsWith("/msg ")) {
                            // Estrai il destinatario e il messaggio
                            String[] parts = input.split(" ", 3);
                            if (parts.length >= 3) {
                                String destinatario = parts[1];
                                String messaggio = parts[2];
                                out.println("#" + destinatario + "_" + messaggio); // Invia il messaggio privato
                            } else {
                                System.out.println("Errore: Sintassi corretta: /msg <utente> <messaggio>.");
                            }
                        } else {
                            out.println(nickname + ": " + input); // Invia un messaggio generico al server
                        }
                        break;
                }
            }   
        } catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            if (socket != null)
                socket.close();
            System.out.println("Connessione chiusa.");
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Errore durante la chiusura della connessione: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost"; // Cambia con l'indirizzo del server
        int port = 3645;
        ChatClient client = new ChatClient(serverAddress, port);
        client.start();
    }
}