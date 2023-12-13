package com.nhnacademy.node;

import org.json.simple.JSONObject;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.wire.Wire;

public class MqttMessageGenerator extends InputOutputNode {

    protected MqttMessageGenerator(String id) {
        super(id, 1);
    }

    @Override
    void preprocess() {
    }

    @Override
    void process() {
        for (int i = 0; i < getInputWireCount(); i++) {
            Wire inputWire = getInputWire(i);
            if (inputWire == null || !inputWire.hasMessage())
                continue;
            Message message = inputWire.get();
            if (!(message instanceof JsonMessage))
                continue;
            JSONObject jsonObject = ((JsonMessage) message).getJsonObject();
            String topic = "data/s/"+
        }
    }

    @Override
    void postprocess() {
    }

}
