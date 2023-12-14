package com.nhnacademy.system;

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

    private IMqttClient client;
    private MqttConnectOptions options = new MqttConnectOptions();

    public Broker(String id, String host, int port) {
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

    public Broker(String host, int port) {
        this("broker" + (++count), host, port);
    }

    void setOption() {
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setKeepAliveInterval(60);
    }

    public String getId(){
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
