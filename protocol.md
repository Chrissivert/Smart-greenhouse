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
a connection is established before any useful data is transferred.
TCP provides features such as error-checking, acknowledgment of received data and retransmission of lost packets.
This ensures that each packet is delivered in the correct order and none is lost or duplicated.
In our greenhouse we need to be sure that each packet is delivered. For instance if one node turns on or off
an actuator, we need to be sure that the other nodes receive this information. TCP also has flow control 
which ensures that the sender doesn't overwhelm the receiver with data.

## Choosing of port number

When deciding which port number to use, the group wanted to have an unassigned port number. Ports above 1024
are usually available and not reserved for specific services. This also ensures that well-known ports like 80 (HTTP)
443 (HTTPS) or 22 (SSH) are not used. The group decided to use port 1234.


## The architecture

The architecture of the system is ControlPanelClient-Server-GreenHouseClient. The GreenHouseClient is responsible for 
simulating the greenhouse and showing the data from the sensors and actuators. The ControlPanelClient(s) are the GUI(s) where
the user(s) can interact with the system (AKA the greenhouse). The Server is the middleman between the ControlPanelClients and
sends information from the ControlPanelClients to the GreenHouseClients and vice versa. There can be multiple ControlPanelClients
and when a change is made in one of them, the Server sends the information to the GreenHouseClient and other ControlPanelClients.
This means the system is synchronized between all clients. 

![img.png](images/NetworkArchitecture.png)

## The flow of information and events

TODO - describe what each network node does and when. Some periodic events? Some reaction on 
incoming packets? Perhaps split into several subsections, where each subsection describes one 
node type (For example: one subsection for sensor/actuator nodes, one for control panel nodes).

There are no periodic events. Once a change is made in either one of the ControlPanelClients (actuator state changed)
or the GreenHouseClient (sensor value changed), the Server sends the information to the other clients.


## Connection and state

Connection-Oriented: TCP ensures a connection is established before data transfer.

Stateful: The system maintains state for synchronization across clients.

## Types, constants

Value Types: Integer values for sensor and actuator data.

## Message format

TODO - describe the general format of all messages. Then describe specific format for each 
message type in your protocol.

### Error messages

TODO - describe the possible error messages that nodes can send in your system.

Error messages:
* `ERROR: <error message>` - sent by the server to the client when an error occurs

## An example scenario

TODO - describe a typical scenario. How would it look like from communication perspective? When 
are connections established? Which packets are sent? How do nodes react on the packets? An 
example scenario could be as follows:
1. A sensor node with ID=1 is started. It has a temperature sensor, two humidity sensors. It can
   also open a window.
2. A sensor node with ID=2 is started. It has a single temperature sensor and can control two fans
   and a heater.
3. A control panel node is started.
4. Another control panel node is started.
5. A sensor node with ID=3 is started. It has a two temperature sensors and no actuators.
6. The user of the first-control panel presses on the button "ON" for the first fan of
   sensor/actuator node with ID=2.
7. The user of the second control-panel node presses on the button "turn off all actuators".

## Reliability and security

TODO - describe the reliability and security mechanisms your solution supports.
