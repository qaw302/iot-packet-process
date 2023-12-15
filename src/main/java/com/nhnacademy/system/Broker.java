package com.nhnacademy.system;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class Broker {
    private String id;
    private String host;
    private int port;
    private static int count;
    private static Map<String, Broker> brokers = new HashMap<>();

    private IMqttClient client;
    private MqttConnectOptions options = new MqttConnectOptions();

    private Broker(String id, String host, int port) {
        super();
        count++;
        this.id = id;
        this.host = host;
        this.port = port;
        setOption();
        try {
            connect();
        } catch (MqttException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Broker(String host, int port) {
        this("broker" + (++count), host, port);
    }

    public static Broker getBroker(String id, String host, int port) {
        if (!brokers.containsKey(host + ":" + port)) {
            Broker broker = new Broker(id, host, port);
            brokers.put(host + ":" + port, broker);
        }
        return brokers.get(host + ":" + port);
    }

    void setOption() {
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setKeepAliveInterval(60);
    }

    public String getId() {
        return id;
    }

    private void connect() throws MqttException {
        client = new MqttClient("tcp://" + host + ":" + port, id, new MqttDefaultFilePersistence("./target/trash"));
        client.connect(options);
    }

    public void disconnect() throws MqttException {
        client.disconnect();
    }

    public IMqttClient getClient() {
        return client;
    }
}
