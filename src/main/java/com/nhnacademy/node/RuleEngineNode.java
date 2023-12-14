package com.nhnacademy.node;

import org.json.simple.JSONObject;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.system.DataBase;
import com.nhnacademy.wire.Wire;

public class RuleEngineNode extends InputOutputNode {
    private DataBase dataBase;

    protected RuleEngineNode(String id) {
        super(id, 1);
        dataBase = new DataBase();
        dataBase.addCol("site");
        dataBase.addCol("sensor");
        dataBase.addCol("value");
        dataBase.addCol("branch");
        dataBase.addCol("place");
        dataBase.addCol("registerAddress");
        dataBase.addCol("devEui");
        dataBase.setPrimaryKey("devEui");
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

            JsonMessage jsonMessage = (JsonMessage) message;
            JSONObject jsonObject = jsonMessage.getJsonObject();
            JSONObject dataObject = (JSONObject) jsonObject.get("payload");
            dataBase.addData(dataObject);
            output(0, jsonMessage);
        }
    }

    @Override
    void postprocess() {
    }

}
