package com.nhnacademy.node;


import org.json.simple.JSONObject;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.wire.Wire;

public class ModbusMapperKeywordToRegister extends InputOutputNode {
    private RegisterAddressMappingTable registerAddressMappingTable;

    protected ModbusMapperKeywordToRegister(String id) {
        super(id, 1);
        registerAddressMappingTable = new RegisterAddressMappingTable("src/main/resources/registerAddressMappingTable2.json");
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
            JSONObject jsonObject = ((JsonMessage) message).getJsonObject();
            String branch = JsonMessage.getDestJsonObject(jsonObject, new String[] { "payload", "branch" })
                    .get("branch").toString();
            String place = JsonMessage.getDestJsonObject(jsonObject, new String[] { "payload", "place" }).get("place")
                    .toString();
            String devEui = JsonMessage.getDestJsonObject(jsonObject, new String[] { "payload", "devEui" })
                    .get("devEui").toString();
            String sensor = JsonMessage.getDestJsonObject(jsonObject, new String[] { "payload", "sensor" })
                    .get("sensor")
                    .toString();
            long address = (long) ((JSONObject) JsonMessage.getDestJsonObject(registerAddressMappingTable,
                    new String[] { "branch", branch, place, devEui }))
                    .get(devEui);
            address += (long) (JsonMessage.getDestJsonObject(registerAddressMappingTable,
                    new String[] { "sensors", sensor })).get(sensor);
            JSONObject result = new JSONObject();
            JSONObject payload = new JSONObject();
            payload.put("registerAddress", address);
            payload.put("value", JsonMessage.getDestJsonObject(jsonObject, new String[] { "payload", "value" })
                    .get("value"));
            result.put("payload", payload);
            output(0, new JsonMessage(result));
        }
    }

    @Override
    void postprocess() {
    }

}
