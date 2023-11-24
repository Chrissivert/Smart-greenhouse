package no.ntnu.endclients;

import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.endclients.ClientHandler;
import no.ntnu.endclients.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private ControlPanelLogic logic;
    private final Server server;

    public ClientHandler(Socket socket, Server server, ControlPanelLogic logic) {
        this.socket = socket;
        this.server = server;
        this.logic = logic;
        server.addClient(this);
    }

    public void sendMessage(String message) {
        // Implementer logikken for å sende melding til klienten
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Handle client communication here using reader and writer
            System.out.println("Client connectdahdbwajdawohj");
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                System.out.println("dadwakda");
                logic.onNodeRemoved(1);
                System.out.println("Client: " + inputLine);
                // Process client input and send response
                writer.println("Server: " + inputLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close(); // Close the socket when done
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
