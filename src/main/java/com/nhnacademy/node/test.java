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
        ModbusMapperKeywordToRegister modbusMapperKeywordToRegister = new ModbusMapperKeywordToRegister("3");
        Wire wire1 = new Wire();
        mqttMessageProcessingNode.connectOutputWire(0, wire1);
        modbusMapperKeywordToRegister.connectInputWire(wire1);
        modbusMapperKeywordToRegister.start();
    }
}