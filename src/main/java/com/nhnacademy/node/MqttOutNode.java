package com.nhnacademy.node;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.system.Broker;

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

    @Override
    void process() {
        while (!thread.isInterrupted()) {
            if (!getMessageQueue().hasMessage())
                continue;

            Message message = getMessageQueue().get();
            if (!(message instanceof JsonMessage))
                continue;

            JsonMessage jsonMessage = (JsonMessage) message;
            try {
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
