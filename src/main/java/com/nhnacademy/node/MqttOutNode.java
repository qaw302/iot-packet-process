package com.nhnacademy.node;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.wire.Wire;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

@Slf4j
public class MqttOutNode extends OutputNode {
    private String broker;
    private MqttClient client;
    private String path = "src/main/resources/systemSetting.json";
    private JSONObject jsonObejct;

    public MqttOutNode(int count, JSONObject jsonObject) {
        super(count);
        this.jsonObejct = jsonObject;
    }

    public MqttOutNode(JSONObject jsonObject) {
        this(1, jsonObject);
    }

    @Override
    void preprocess() {
        try {
            log.trace(jsonObejct.get("server").toString());
            client = new MqttClient(jsonObejct.get("server").toString(), MqttClient.generateClientId(),
                    new MqttDefaultFilePersistence("./target/trash"));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    void process() {
        sendToTelegraf();
    }

    public void sendToTelegraf() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            client.connect(options);

            Wire inputWire = getInputWire(0);
            if (inputWire != null) {
                while (inputWire.hasMessage()) {
                    Message message = inputWire.get();

                    if (message instanceof JsonMessage) {
                        JsonMessage jsonMessage = (JsonMessage) message;
                        JSONObject messageJsonObject = jsonMessage.getJsonObject();

                        client.publish(messageJsonObject.get("topic").toString(), new MqttMessage(
                                messageJsonObject.get("payload").toString().getBytes()));
                    }
                }

            }

            client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    void postprocess() {
        try {
            client.close();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
