package com.nhnacademy.node;

import com.nhnacademy.system.Broker;
import com.nhnacademy.wire.Wire;

public class test {
    public static void main(String[] args) {
        Broker broker = new Broker("1","ems.nhnacademy.com", 1883);
        MqttInNode mqttInNode = new MqttInNode("1","application/#", 2, broker);
        MqttMessageProcessingNode mqttMessageProcessingNode = new MqttMessageProcessingNode("2");
        Wire wire = new Wire();
        mqttInNode.connectOutputWire(wire);
        mqttMessageProcessingNode.connectInputWire(wire);
        mqttInNode.start();
        mqttMessageProcessingNode.start();
    }
}
