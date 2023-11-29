package no.ntnu.endclients;

import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.endclients.Server;
import no.ntnu.gui.controlpanel.ControlPanelApplication;

public class ServerRunner {
    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }
}
