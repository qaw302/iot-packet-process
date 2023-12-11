package com.nhnacademy.node;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.system.SystemOption;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MqttInNode extends InputNode {
    private IMqttClient client;
    private JSONObject jsonObject;

    public MqttInNode(int count, JSONObject jsonObject) {
        super(count);
        this.jsonObject = jsonObject;
    }

    public MqttInNode(JSONObject jsonObejct) {
        this(1, jsonObejct);
    }

    @Override
    void preprocess() {
        String publisherId;
        publisherId = UUID.randomUUID().toString();

        try {
            client = new MqttClient(jsonObject.get("server").toString(), publisherId,
                    new MqttDefaultFilePersistence("./target/trash"));

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * MQTT 클라이언트를 통해 메시지를 처리하는 메서드입니다.
     * 이 메서드는 노드가 살아있는 동안 반복적으로 실행되며, MQTT 연결 상태를 확인합니다.
     * 연결되지 않은 경우, 새로운 연결을 시도합니다.
     * MQTT 클라이언트를 구독하고, 수신된 메시지를 처리합니다.
     * JSON 형식의 메시지를 파싱하고 로깅합니다.
     *
     * process()는 MqttException을 발생시킬 수 있으며,
     * 특히 이미 연결된 경우 또는 연결에 실패했을 때 이를 처리합니다.
     * MQTTcode 32100 이미 연결된 상태에서 다시 연결을 시도할 경우
     * 
     * @throws MqttException MQTT 연결 오류 또는 구독 중 오류가 발생한 경우
     */
    @Override
    void process() {

        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            if (!(client.isConnected())) {
                client.connect(options);
            }

            String topicDirectory = "application";
            // (sOptions.getApplicationName() != null ? sOptions.getApplicationName()
            //         : "application");

            client.subscribe(topicDirectory + "/+/device/+/event/up/#", (topic, msg) -> {
                try {
                    JSONParser parser = new JSONParser();
                    Object obj = parser.parse(new String(msg.getPayload()));
                    if (!(obj instanceof JSONObject)) {
                        throw new IllegalStateException();
                    }
                    JSONObject jsonObj = (JSONObject) obj;
                    // log.trace(jsonObj.toString());
                    JsonMessage messageObject = new JsonMessage(jsonObj);
                    messageObject.setNodeName("MqttInNode");
                    output(messageObject);
                    log.info(messageObject.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (MqttException e) {
            if (e.getReasonCode() == 32100) {
                log.info("Client is already connected.");
            } else {
                e.printStackTrace();
            }
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
