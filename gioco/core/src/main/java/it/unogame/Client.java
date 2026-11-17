package it.unogame;

import java.io.*;
import java.net.*;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void connect() {
        try {
            socket = new Socket("127.0.0.1", 1234);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connesso al server!");

            out.println("Ciao dal client!");
            System.out.println("Risposta server: " + in.readLine());
        } catch (Exception e) {
            System.out.println("Errore di connessione: " + e.getMessage());
        }
    }
}
