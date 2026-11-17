package it.unogame;

import java.io.*;
import java.net.*;

public class Server {

    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(1234);

            System.out.println("Server avviato sulla porta 1234");

            while (true) {

                Socket clientSocket = serverSocket.accept();

                System.out.println("Connesso: "+clientSocket.getInetAddress()+";"
                    +clientSocket.getLocalPort());//stampa connessioni sul server

                Thread thread = new Thread(() -> {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                        String msg = in.readLine();
                        System.out.println("Ricevuto: " + msg);
                        out.println("Server: ho ricevuto '" + msg + "'");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                thread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
