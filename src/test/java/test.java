

import com.nhnacademy.node.ModbusMapperKeywordToRegister;
import com.nhnacademy.node.ModbusMapperRegisterToKeyword;
import com.nhnacademy.node.ModbusMasterNode;
import com.nhnacademy.node.ModbusServerNode;
import com.nhnacademy.node.MqttInNode;
import com.nhnacademy.node.MqttMessageGenerator;
import com.nhnacademy.node.MqttMessageProcessingNode;
import com.nhnacademy.node.MqttOutNode;
import com.nhnacademy.node.RuleEngineNode;
import com.nhnacademy.system.Broker;
import com.nhnacademy.wire.Wire;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class test {
    public static void main(String[] args) throws InterruptedException {
        Broker broker = Broker.getBroker("broker","ems.nhnacademy.com", 1883);
        MqttInNode mqttInNode = new MqttInNode("mqttin","application/#", 2, broker);
        MqttMessageProcessingNode mqttMessageProcessingNode = new MqttMessageProcessingNode("mqttMessageProcessingNode");
        Wire wire = new Wire();
        mqttInNode.connectOutputWire(wire);
        mqttMessageProcessingNode.connectInputWire(wire);
        mqttInNode.start();
        mqttMessageProcessingNode.start();
        RuleEngineNode ruleEngineNode = new RuleEngineNode("ruleEngineNode");
        ModbusMasterNode modbusMasterNode = new ModbusMasterNode("modbusMasterNode");
        ModbusMapperRegisterToKeyword modbusMapperRegisterToKeyword = new ModbusMapperRegisterToKeyword("modbusMapperRegisterToKeyword");
        Wire wire1 = new Wire();
        mqttMessageProcessingNode.connectOutputWire(0, wire1);
        ruleEngineNode.connectInputWire(wire1);
        Wire wire2 = new Wire();
        modbusMasterNode.connectOutputWire(0, wire2);
        modbusMapperRegisterToKeyword.connectInputWire(wire2);
        MqttMessageGenerator mqttMessageGenerator = new MqttMessageGenerator("mqttMessageGenerator");
        Wire wire4 = new Wire();
        ruleEngineNode.connectOutputWire(0, wire4);
        mqttMessageGenerator.connectInputWire(wire4);
        Broker broker1 = Broker.getBroker("broker1","localhost", 1883);
        MqttOutNode mqttOutNode = new MqttOutNode("mqttout", broker1);
        Wire wire5 = new Wire();
        mqttMessageGenerator.connectOutputWire(0, wire5);
        mqttOutNode.connectInputWire(wire5);
        ModbusMapperKeywordToRegister modbusMapperKeywordToRegister = new ModbusMapperKeywordToRegister("modbusMapperKeywordToRegister");
        Wire wire6 = new Wire();
        ruleEngineNode.connectOutputWire(0, wire6);
        modbusMapperKeywordToRegister.connectInputWire(wire6);
        ModbusServerNode modbusServerNode = new ModbusServerNode(10502);
        Wire wire7 = new Wire();
        modbusMapperKeywordToRegister.connectOutputWire(0, wire7);
        modbusServerNode.connectInputWire(wire7);
        ruleEngineNode.start();
        // modbusMasterNode.start();
        modbusMapperRegisterToKeyword.start();
        mqttMessageGenerator.start();
        // mqttOutNode.start();
        modbusMapperKeywordToRegister.start();
        modbusServerNode.start();
        // while(true){
        //     if(!ruleEngineNode.thread.isAlive()){
        //         log.info("ruleEngineNode is dead");
        //         break;
        //     }
        //     log.info(ruleEngineNode.thread.getState().toString());
        //     Thread.sleep(1000);
        // }
    }
}