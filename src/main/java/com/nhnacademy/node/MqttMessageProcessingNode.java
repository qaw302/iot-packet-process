package com.nhnacademy.node;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.system.UndefinedJsonObject;
import com.nhnacademy.wire.Wire;

public class MqttMessageProcessingNode extends InputOutputNode {
    private String[] sensors;
    RegisterAddressMappingTable registerAddressMappingTable;

    protected MqttMessageProcessingNode(String id) {
        super(id, 1);
        sensors = new String[] { "temperature", "humidity", "co2" };
        registerAddressMappingTable = new RegisterAddressMappingTable(
                "src/main/resources/registerAddressMappingTable2.json");
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
            String branch = JsonMessage.getDestJsonObject(jsonObject,
                    new String[] { "payload", "deviceInfo", "tags", "branch" }).get("branch").toString();
            String site = JsonMessage.getDestJsonObject(jsonObject,
                    new String[] { "payload", "deviceInfo", "tags", "site" }).get("site").toString();
            String place = JsonMessage.getDestJsonObject(jsonObject,
                    new String[] { "payload", "deviceInfo", "tags", "place" }).get("place").toString();
            String devEui = JsonMessage.getDestJsonObject(jsonObject,
                    new String[] { "payload", "deviceInfo", "devEui" }).get("devEui").toString();
            if (branch.equals("undefined") || site.equals("undefined") || place.equals("undefined")
                    || devEui.equals("undefined"))
                continue;
            for (String sensor : sensors) {
                JSONObject destObject = JsonMessage.getDestJsonObject(jsonObject,
                        new String[] { "payload", "object", sensor });
                if (destObject instanceof UndefinedJsonObject)
                    continue;
                String dictionaryKey = branch + "/" + site + "/" + place + "/" + devEui + "/" + sensor;
                if (!registerAddressMappingTable.hasKey(dictionaryKey))
                    registerAddressMappingTable.writeKey(dictionaryKey);
                double value = (double) destObject.get(sensor);
                JSONObject payload = new JSONObject();
                payload.put("registerAddress", registerAddressMappingTable.getRegisterAddressMappingTable().get(dictionaryKey));
                payload.put("branch", branch);
                payload.put("site", site);
                payload.put("place", place);
                payload.put("devEui", devEui);
                payload.put("sensor", sensor);
                payload.put("value", value);

                JSONObject result = new JSONObject();
                result.put("payload", payload);

                output(0, new JsonMessage(result));
            }

        }
    }

    @Override
    void postprocess() {
    }

    public static void main(String[] args) {
        MqttMessageProcessingNode mqttMessageProcessingNode = new MqttMessageProcessingNode("test");
    }

}
