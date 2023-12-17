package com.nhnacademy.node;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.system.Broker;
import com.nhnacademy.system.OutputLogger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MqttOutNode extends OutputNode {
    private Broker broker;

    public MqttOutNode(String id, Broker broker) {
        super(id);
        this.broker = broker;
    }

    public MqttOutNode(Broker broker) {
        super();
        this.broker = broker;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    public static MqttOutNode generate(JSONObject jsonObject) {
        String id = (String) jsonObject.get("id");
        String host = (String) jsonObject.get("host");
        long port = (long) jsonObject.get("port");
        Broker broker = Broker.getBroker(host, port);
        return new MqttOutNode(id, broker);
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

            JsonMessage jsonMessage = (JsonMessage) message;
            try {
                OutputLogger.getInstance().write(getId(), jsonMessage.getJsonObject().toString().length(),
                        jsonMessage.getJsonObject().toString().length(), error, startTime,
                        System.currentTimeMillis() - startTime);
                broker.getClient().publish(jsonMessage.getTopic(),
                        new MqttMessage(jsonMessage.getPayload().toString().getBytes()));

            } catch (MqttException e) {
                log.error("MqttException", e);
            }

        }
    }

    @Override
    void preprocess() {
    }

    @Override
    void postprocess() {
    }

}
