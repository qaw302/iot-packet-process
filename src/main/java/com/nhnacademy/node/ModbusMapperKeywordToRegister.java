package com.nhnacademy.node;

import org.json.simple.JSONObject;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.wire.Wire;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModbusMapperKeywordToRegister extends InputOutputNode {
    private RegisterAddressMappingTable registerAddressMappingTable;

    protected ModbusMapperKeywordToRegister(String id) {
        super(id, 1);
        registerAddressMappingTable = RegisterAddressMappingTable
                .getRegisterAddressMappingTable("src/main/resources/registerAddressMappingTable.json");
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
            String site = JsonMessage.getDestJsonObject(jsonObject, new String[] { "payload", "site" }).get("site")
                    .toString();
            String place = JsonMessage.getDestJsonObject(jsonObject, new String[] { "payload", "place" }).get("place")
                    .toString();
            String devEui = JsonMessage.getDestJsonObject(jsonObject, new String[] { "payload", "devEui" })
                    .get("devEui").toString();
            String sensor = JsonMessage.getDestJsonObject(jsonObject, new String[] { "payload", "sensor" })
                    .get("sensor")
                    .toString();
            String key = branch + "." + site + "." + place + "." + devEui + "." + sensor;
            if (!registerAddressMappingTable.hasKey(key)) {
                log.info("key not found");
                continue;
            }
            JSONObject result = new JSONObject();
            JSONObject payload = new JSONObject();
            payload.put("registerAddress",
                    (long) registerAddressMappingTable.getRegisterAddressMappingTable().get(key));
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
