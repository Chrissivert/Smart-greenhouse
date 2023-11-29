//package no.ntnu.tools;
//
//import no.ntnu.greenhouse.Actuator;
//import no.ntnu.listeners.common.ActuatorListener;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class EventManager {
//    private List<ActuatorListener> listeners;
//
//    public EventManager() {
//        listeners = new ArrayList<>();
//    }
//
//    public void subscribe(ActuatorListener listener) {
//        listeners.add(listener);
//    }
//
//    public void publishActuatorChange(int nodeId, Actuator actuator) {
//        for (ActuatorListener listener : listeners) {
//            listener.actuatorUpdated(nodeId, actuator);
//        }
//    }
//
//    //CLASS NOT USED
//}
