package com.nhnacademy.node;

import com.nhnacademy.wire.Wire;

public class MqttMessageProcessingNode extends InputOutputNode {

    protected MqttMessageProcessingNode(String id) {
        super(id, 1);
        // TODO Auto-generated constructor stub
    }

    @Override
    void preprocess() {
    }

    @Override
    void process() {
        for(int i = 0;i<getInputWireCount();i++){
            Wire wire = getInputWire(i);
            if(wire == null || !wire.hasMessage())
                continue;
        }
    }

    @Override
    void postprocess() {
    }

}
