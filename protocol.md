# Communication protocol

This document describes the protocol used for communication between the different nodes of the
distributed application.

## Terminology

* Sensor - a device which senses the environment and describes it with a value (an integer value in
  the context of this project). Examples: temperature sensor, humidity sensor.
* Actuator - a device which can influence the environment. Examples: a fan, a window opener/closer,
  door opener/closer, heater.
* Sensor and actuator node - a computer which has direct access to a set of sensors, a set of
  actuators and is connected to the Internet.
* Control-panel node - a device connected to the Internet which visualizes status of sensor and
  actuator nodes and sends control commands to them.
* Graphical User Interface (GUI) - A graphical interface where users of the system can interact with
  it.
* Server - a computer connected to the Internet which is used to relay messages between sensor and
  actuator nodes
* ControlPanelClient - a GUI which is used to interact with the system
* GreenHouseClient - a node in the greenhouse containing sensors and actuators


## The underlying transport protocol

The group choose to use TCP instead of UDP. TCP is a connection-oriented protocol which means that
a connection is established before any useful data is transferred. TCP provides features such as 
error-checking, acknowledgment of received data and retransmission of lost packets.
This ensures that each packet is delivered in the correct order and none is lost or duplicated.
In our greenhouse we need to be sure that each packet is delivered. For instance if one node turns on or off
an actuator, we need to be sure that the other nodes receive this information. TCP also has flow control 
which ensures that the sender doesn't overwhelm the receiver with data.

## Choosing of port number

When deciding which port number to use, the group wanted to have an unassigned port number. Ports above 1024
are usually available and not reserved for specific services. This also ensures that well-known ports like 80 (HTTP)
443 (HTTPS) or 22 (SSH) are not used. The group decided to use port 1234.


## The architecture

The architecture of the system is ControlPanelClient-GreenhouseSimulator-GreenHouseApplication. The ControlPanelClient
is the GUI where the user can interact with the system. The GreenhouseSimulator acts as the server and is connected
to the ClientHandler to handle requests from the ControlPanelClients. The GreenHouseApplication is the greenhouse
itself and keeps track of the currently active nodes.

IMPORTANT: The structure and architecture of the communication is highly inspired by group 15's solution and some places
identical.

![img.png](images/NetworkArchitecture4.png)

## The flow of information and events
IMPORTANT CONTEXT:
Both the client side and the server side runs two threads each in parallel. In both of them, one thread
runs continuously in the JavaFX application, and the other thread runs in the communications class, continuously
waiting for a new input on the reader.readLine(). The thread responsible for reading new lines over the
communications channel is the latter, the thread that runs in the communications class. The thread responsible
for writing new lines to the communications channel in both client and server is the JavaFX applications thread.

Establishing a connection:
1. The GreenhouseSimulator starts a new thread that waits for a connection from a ControlPanelClient.
2. The ControlPanelSocket starts and connects to the GreenhouseSimulator.
3. The GreenhouseSimulator accepts the connection and adds a new ClientHandler to the list of clients.
4. The GreenhouseSimulator thread waits for a message from the ControlPanelSocket.
5. The ControlPanelSocket sends a message to the GreenhouseSimulator.
6. The ClientHandler receives the message and sends it to the GreenHouseApplication.
7. The GreenHouseApplication receives the message and updates accordingly.

Each sensor inside the greenhouse updates every 5 seconds. This can be changed in the SensorActuatorNode
SENSING_DELAY constant. Once a sensor is updated the temperatureSensor chart within the node is updated.
When a ControlPanelClient sends a command such as turning on or off an actuator, then the ClientHandler
handles the command and sends it to the GreenHouseApplication.


## Connection and state
Our communication protocol is connection-oriented because they establish a connection before any useful data is transferred.
Our protocol is both stateful and stateless.
The protocol is stateless since the greenhouse doesn't update connected clients on the changes that happen.

## Types, constants

Value Types: Integer values for sensor and actuator data.
Current sensor types: Temperature, Humidity
Current actuator types: Window, Heater, Fan

Most important constants: 
* SENSING_DELAY - the delay between each sensor update
* SERVER_PORT - the port number used for communication
* SERVER_HOST - the host name used for communication
* MIN_TEMPERATURE - the minimum temperature of the greenhouse
* MAX_TEMPERATURE - the maximum temperature of the greenhouse

## Message format

All messages are sent as strings and parsed. The messages below tell the application what to send

"getNodes" - returns a list of all current nodes to control panel
"updateSensor - returns updated sensor data to control panel

Examples for how the messages are sent:

Format for Client sending Actuator updates: "actuator[" + actuatorId + "] on node " + nodeId + " is " + state

Format for Client sending Sensor updates: "sensor[" + sensorId + "] on node " + nodeId + " is " + value

Format for Client sending a command: "command[" + command + "] on node " + nodeId + " is " + value

Format for Server sending Actuator state updates: "updateActuatorStates:" + actuatorId + "," + nodeId + "," + state

Format for Server sending GetNodes: nodeId1 + ";" + actuatorId1 + "_" +actuatorType1 + actuatorIdN + "_" + actuatorTypeN + "/" + nodeIdN + ";" + actuatorId1 + "_" +actuatorType1 + actuatorIdN + "_" + actuatorTypeN

### Error messages

Error messages:
"actuator not found"

"No actuator section for node " + nodeId

"Incorrect command format: " + rawCommand

"Error encrypting the command."

"Error sending command to actuator " + actuatorId + " on node " + nodeId + ": " + e.getMessage()  e = IOException

"Could not connect to server: " + e.getMessage() e = IOException

"Could not close connection: " + e.getMessage() e = IOException

"while reading from the socket: " + e.getMessage() e = IOException

"Incorrect command format: " + rawCommand

"An error occurred while stopping communication"

"TCP connection not established due to error : " + e.getMessage() e = IOException

"Could not accept client connection: " + e.getMessage() e = IOException

"Failed to toggle an actuator: " + e.getMessage() e = Exception

"Can't remove node " + nodeId + ", there is no Tab for it"

"No sensor section for node " + nodeId

"Error encrypting the command: Data must not be longer than 245 bytes"

## An example scenario

1. The GreenhouseSimulator starts and creates initial nodes.
2. A sensor node with ID=1 is started. It has a temperature sensor and two humidity sensors. It
   also has a window.
3. A sensor node with ID=2 is started. It has a single temperature sensor and two fans.
4. A control panel node is started.
5. The user of the control-panel node presses on the button "ON" for the first fan of
   sensor/actuator node with ID=2.
6. The control-panel node sends a message to the client handler.
7. The client handler receives the message and sends it to the GreenHouseApplication, it also sends a confirmation message to the ControlPanelClient(s),
if there is more than one connected to the server it sends the message to all of them witch will update the GUI for all connected clients.
8. The GreenHouseApplication receives the message and updates accordingly.

## Security

Messages sent between the nodes are encrypted using RSA encryption. The current implementation uses a hardcoded
public key and private key, although the option to generate a random key pair is available. Each message sent
between the nodes is encrypted and decrypted using the same public and private keys. This is because we wanted
confidentiality, integrity and authenticity. We get the confidentiality and integrity by using public-key
encryption. Since an implementation for public key cryptography was already there when we wanted to also
implement authenticity, it would be simplest to make all the keys the same. Then, by not sending the keys over
the network at all, authenticity would be ensured.

## Limitations

There is a 245 byte limit on the encryption, which means that a command will fail to encrypt if it is too long.
This will make the relevant application (depending on who overloads who with information) forever throw a
decryption error, since the encrypted content is null, instead of actual content. This can be replicated by
making a new node at the server side, and fill it with 3 of every actuator and sensor.

This can be fixed by splitting the data into smaller >246 byte sized pieces, and then sending those one at a 
time. This has not been implemented because of time constraints. It is not a very high priority fix, since 
encryption is a neat addition to the project, not a necessity.

All necessary methods for communications for updating sensor data onto the control panels are in place.
Like the updateSensorData() method of the ControlPanelSocket class, and the handleUpdateSensorCommand() of the
ClientHandler() class. However, the limitation is that the ControlPanelSocket stores the updated sensor data
in an unused string, which remain untouched. The missing part is to actually use that data in the control panel
GUI, so the user can see it. This has also been put a lower priority, since the GUI part is not necessarily 
the most important here, but the networks communication part is.