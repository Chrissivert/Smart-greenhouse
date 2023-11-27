package no.ntnu.controlpanel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RealCommunicationChannel implements CommunicationChannel {

    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public RealCommunicationChannel(String serverAddress, int port) throws IOException {
        this.socket = new Socket(serverAddress, port);
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    @Override
    public void sendActuatorChange(int nodeId, int actuatorId, boolean isOn, String type) {
//        // Implement sending actuator change command over the socket
//        String command = // Construct your command here based on parameters
//        try {
//            outputStream.write(command.getBytes());
//            outputStream.flush();
//        } catch (IOException e) {
//            // Handle write errors
        }
//    }

//    @Override
//    public void sendActuatorChange(int nodeId, int actuatorId, boolean isOn, String Type) {
//        String state = isOn ? "ON" : "off";
//        Logger.info("Sending command to greenhouse: turn " + state + " actuator"
//                + "[" + actuatorId + "] on node " + nodeId);
//    }

    @Override
    public boolean open() {
        return !socket.isClosed();
    }

    public boolean test(){
        return true;
    }

    // Implement other methods based on your application's requirements
    // Remember to handle reading responses from the server/device as well
}
