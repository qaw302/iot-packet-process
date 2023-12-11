package com.nhnacademy.node;

import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.system.SystemOption;
import com.nhnacademy.wire.Wire;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@Slf4j
public class MqttOutNode extends OutputNode {

    private String broker;
    private MqttClient client;
    private String path = "src/main/resources/systemSetting.json";

    public MqttOutNode() {
        super();
    }

    public MqttOutNode(String name) {
        super(name);

        // this.broker = broker;

        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject systemSettings;

            systemSettings = (JSONObject) jsonParser.parse(new FileReader(path));

            JSONObject outputSettings = (JSONObject) systemSettings.get("output");
            if (outputSettings != null) {
                Object serverValue = outputSettings.get("server");
                if (serverValue != null) {
                    this.broker = serverValue.toString();
                }
            }

        } catch (IOException | org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    void preprocess() {
        try {
            client = new MqttClient(broker, MqttClient.generateClientId(),
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