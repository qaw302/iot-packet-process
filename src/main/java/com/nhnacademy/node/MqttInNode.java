package com.nhnacademy.node;

import java.net.UnknownHostException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.system.Broker;
import com.nhnacademy.system.OutputLogger;
import com.nhnacademy.wire.Wire;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MqttInNode extends InputNode {
    private String topic;
    private int qos;
    private Broker broker;

    public MqttInNode(String id, String topic, int qos, Broker broker) {
        super(id, 1);
        this.topic = topic;
        this.qos = qos;
        this.broker = broker;
    }

    public MqttInNode(String topic, int qos, Broker broker) {
        super(1);
        this.topic = topic;
        this.qos = qos;
        this.broker = broker;
    }

    public static MqttInNode generate(JSONObject jsonObject) {
        String id = (String) jsonObject.get("id");
        String topic = (String) jsonObject.get("topic");
        int qos = ((Long) jsonObject.get("qos")).intValue();
        String host = (String) jsonObject.get("host");
        long port = (long) jsonObject.get("port");
        Broker broker = Broker.getBroker(host, port);
        return new MqttInNode(id, topic, qos, broker);
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    public void connectOutputWire(Wire wire) {
        connectOutputWire(0, wire);
    }

    void output(Message message) {
        super.output(message, 0);
    }

    @Override
    void preprocess() {
        try {
            broker.getClient().subscribe(topic, qos, (clientTopic, clientMsg) -> {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = new JSONObject();
                Long startTime = System.currentTimeMillis();
                int error = 0;
                jsonObject.put("topic", clientTopic);
                Object payload = parser.parse(new String(clientMsg.getPayload(), "UTF-8"));
                // payload == jsonobject
                if (!(payload instanceof JSONObject))
                    return;
                jsonObject.put("payload", (JSONObject) payload);
                output(new JsonMessage(jsonObject));

                OutputLogger.getInstance().write(getId(), jsonObject.toString().length(),
                        jsonObject.toString().length(), error, startTime,
                        System.currentTimeMillis() - startTime);

            });
        } catch (Exception e) {
            if (e.getCause() instanceof UnknownHostException) {
                System.out.println("Unknown Host");
            } else {
                log.error("Exception", e);
            }
        }
    }

    @Override
    void process() {
    }

    @Override
    void postprocess() {
    }
}
