package com.nhnacademy.node;

import org.json.simple.JSONObject;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.system.OutputLogger;

public class MqttMessageGeneratorNode extends InputOutputNode {

    public MqttMessageGeneratorNode(String id) {
        super(id, 1);
    }

    public static MqttMessageGeneratorNode generate(JSONObject jsonObject) {
        String id = (String) jsonObject.get("id");
        return new MqttMessageGeneratorNode(id);
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
            JSONObject payload = (JSONObject) jsonObject.get("payload");
            String topic = "data/s/" + payload.get("site") + "/b/" + payload.get("branch") + "/p/"
                    + payload.get("place") + "/d/" + payload.get("devEui") + "/e/" + payload.get("sensor");
            payload = new JSONObject();
            payload.put("value", jsonObject.get("value"));
            payload.put("time", System.currentTimeMillis());
            JSONObject result = new JSONObject();
            result.put("topic", topic);
            result.put("payload", payload);
            output(0, new JsonMessage(result));

            OutputLogger.getInstance().write(getId(), jsonObject.toString().length(), result.toString().length(), error,
                    startTime,
                    System.currentTimeMillis() - startTime);
        }
    }

    @Override
    void postprocess() {
    }

}