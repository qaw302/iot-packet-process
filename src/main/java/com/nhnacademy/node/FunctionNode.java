package com.nhnacademy.node;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;

import com.nhnacademy.exception.InvalidArgumentException;
import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.system.SystemOption;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FunctionNode extends InputOutputNode {
    String[] sensors;
    List<String> sensorList;
    HashMap<String, String> value;
    private JSONObject jsonObject;

    public FunctionNode(int inCount, int outCount, JSONObject jsonObject) {
        super(inCount, outCount);
        this.jsonObject = jsonObject;
    }

    @Override
    void preprocess() {
        sensors = SystemOption.getSensors();
        sensorList = Arrays.asList(sensors);
        value = new HashMap<>();
        log.info(id + " node start");
    }

    @Override
    void postprocess() {
        log.info(id + " node end");
    }

    @SuppressWarnings("unchecked")
    @Override
    void process() {
        for (int i = 0; i < getInputWireCount(); i++) {

            if ((getInputWire(i) != null) && (getInputWire(i).hasMessage())) {
                JsonMessage message = (JsonMessage) getInputWire(i).get();

                if (message instanceof JsonMessage) {
                    JSONObject jsonObj = message.getJsonObject();

                    if (jsonObj.containsKey("object")) {
                        saveMap(jsonObj, "object");
                        saveMap(jsonObj, "deviceInfo");

                        for (String sensor : sensorList) {

                            if (value.containsKey(sensor)
                                    && ((JSONObject) (((JSONObject) jsonObj.get("deviceInfo")).get("tags")))
                                            .containsKey("site")) {
                                JSONObject resultJson = new JSONObject();
                                try {
                                    resultJson.put("topic",
                                            "data/s/" + makeValue(jsonObj, "deviceInfo/tags/site") + "/b/"
                                                    + makeValue(jsonObj, "deviceInfo/tenantName") + "/p/"
                                                    + makeValue(jsonObj, "deviceInfo/tags/place") + "/n/"
                                                    + makeValue(jsonObj, "deviceInfo/deviceName").split("\\(")[0]
                                                    + "/e/" + sensor);
                                } catch (Exception e) {
                                    log.error(jsonObj.toJSONString());
                                    log.info(id + " error packet size : " + jsonObj.size());
                                    e.printStackTrace();
                                }

                                JSONObject payloadJson = new JSONObject();
                                payloadJson.put("time", System.currentTimeMillis());
                                payloadJson.put("value", Float.parseFloat(value.get(sensor)));
                                resultJson.put("payload", payloadJson);
                                output(new JsonMessage(resultJson));
                            }
                        }
                    }
                }
            }
        }
    }

    private String makeValue(JSONObject object, String tagDirectory) {
        String[] splitedTags = tagDirectory.split("/");
        Object temp = object;

        for (String tag : splitedTags) {
            if (temp instanceof JSONObject) {
                temp = ((JSONObject) temp).get(tag);
            } else {
                return temp.toString();
            }
        }
        return temp.toString();
    }

    private void saveMap(JSONObject message, String tag) {
        if (message.get(tag) instanceof JSONObject && message.containsKey(tag)) {
            for (Object key : ((JSONObject) message.get(tag)).keySet()) {
                value.put(key.toString(), ((JSONObject) message.get(tag)).get(key).toString());
            }
        } else {
            throw new InvalidArgumentException();
        }
    }

}