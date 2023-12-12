package com.nhnacademy.node;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.system.UndefinedJsonObject;
import com.nhnacademy.wire.Wire;

public class MqttMessageProcessingNode extends InputOutputNode {

    protected MqttMessageProcessingNode(String id) {
        super(id, 1);
    }

    @Override
    void preprocess() {
    }

    @Override
    void process() {
        for (int i = 0; i < getInputWireCount(); i++) {
            Wire wire = getInputWire(i);
            if (wire == null || !wire.hasMessage())
                continue;
            Message message = wire.get();
            if (!(message instanceof JsonMessage))
                continue;
            JsonMessage jsonMessage = (JsonMessage) message;
            // if (!(JsonMessage.getDestJsonObject(jsonMessage,
            // new String[] { "payload", "deviceInfo", "tenantName" }) instanceof
            // UndefinedJsonObject))
            // System.out.println(jsonMessage.getJsonObject());
            System.out.println(JsonMessage.getDestJsonObject(jsonMessage,
                    new String[] { "payload", "deviceInfo", "tenantName" }));
        }
    }

    @Override
    void postprocess() {
    }

}
