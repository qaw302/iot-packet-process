package com.nhnacademy.node;

import org.json.simple.JSONObject;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.system.OutputLogger;
import com.nhnacademy.system.RegisterAddressMappingTable;
import com.nhnacademy.system.UndefinedJsonObject;

public class MqttMessageProcessingNode extends InputOutputNode {
    private String[] sensors;
    RegisterAddressMappingTable registerAddressMappingTable;

    public MqttMessageProcessingNode(String id) {
        super(id, 1);
        sensors = new String[] { "temperature", "humidity", "co2" };
        registerAddressMappingTable = RegisterAddressMappingTable
                .getRegisterAddressMappingTable("src/main/resources/registerAddressMappingTable.json");
    }

    @Override
    void preprocess() {
    }

    @Override
    void process() {
        while (!thread.isInterrupted()) {
            if (!getMessageQueue().hasMessage())
                continue;
            Long startTime = System.currentTimeMillis();
            int error = 0;
            Message message = getMessageQueue().get();
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
                String dictionaryKey = branch + "." + site + "." + place + "." + devEui + "." + sensor;
                if (!registerAddressMappingTable.hasKey(dictionaryKey))
                    registerAddressMappingTable.writeKey(dictionaryKey);

                double value = (double) destObject.get(sensor);
                JSONObject payload = new JSONObject();
                payload.put("registerAddress",
                        registerAddressMappingTable.getRegisterAddressMappingTable().get(dictionaryKey));
                payload.put("branch", branch);
                payload.put("site", site);
                payload.put("place", place);
                payload.put("devEui", devEui);
                payload.put("sensor", sensor);
                payload.put("value", value);

                JSONObject result = new JSONObject();
                output(0, new JsonMessage(result));

                result.put("payload", payload);
                OutputLogger.getInstance().write(getId(), jsonObject.toString().length(), result.toString().length(),
                        error, startTime, System.currentTimeMillis() - startTime);
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
