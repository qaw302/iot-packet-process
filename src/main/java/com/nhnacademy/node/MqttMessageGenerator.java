package com.nhnacademy.node;

import org.json.simple.JSONObject;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;

public class MqttMessageGenerator extends InputOutputNode {

    public MqttMessageGenerator(String id) {
        super(id, 1);
    }

    @Override
    void preprocess() {
    }

    @Override
    void process() {
        while (!thread.isInterrupted()) {
            if (!getMessageQueue().hasMessage())
                continue;
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
            jsonObject = new JSONObject();
            jsonObject.put("topic", topic);
            jsonObject.put("payload", payload);
            output(0, new JsonMessage(jsonObject));
        }
    }

    @Override
    void postprocess() {
    }

}
