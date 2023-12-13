package com.nhnacademy.node;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.system.UndefinedJsonObject;
import com.nhnacademy.wire.Wire;

public class MqttMessageProcessingNode extends InputOutputNode {
    private String[] sensors;
    JSONObject registerAddressMappingTable;

    protected MqttMessageProcessingNode(String id) {
        super(id, 1);
        sensors = new String[] { "temperature", "humidity", "co2" };
        JSONParser jsonParser = new JSONParser();
        try {
            Reader reader = new FileReader("src/main/resources/registerAddressMappingTable.json");
            registerAddressMappingTable = (JSONObject) jsonParser.parse(reader);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    void preprocess() {
    }

    @Override
    void process() {
        for (int i = 0; i < getInputWireCount(); i++) {
            Wire wire = getInputWire(i);
            if (wire == null || !wire.hasMessage())
                continue;
            Message message = wire.get();
            if (!(message instanceof JsonMessage))
                continue;
            JSONObject jsonObject = ((JsonMessage) message).getJsonObject();
            String branch = JsonMessage.getDestJsonObject(jsonObject,
                    new String[] { "payload", "deviceInfo", "tags", "branch" }).get("branch").toString();
            String site = JsonMessage.getDestJsonObject(jsonObject,
                    new String[] { "payload", "deviceInfo", "tags", "site" }).get("site").toString();
            String place = JsonMessage.getDestJsonObject(jsonObject,
                    new String[] { "payload", "deviceInfo", "tags", "place" }).get("place").toString();
            String devEui = JsonMessage.getDestJsonObject(jsonObject,
                    new String[] { "payload", "deviceInfo", "devEui" }).get("devEui").toString();
            if (branch.equals("undefined") || site.equals("undefined") || place.equals("undefined")
                    || devEui.equals("undefined"))
                continue;
            for (String sensor : sensors) {
                JSONObject destObject = JsonMessage.getDestJsonObject(jsonObject,
                        new String[] { "payload", "object", sensor });
                if (destObject instanceof UndefinedJsonObject)
                    continue;
                if (!((JSONObject) registerAddressMappingTable.get("branch")).containsKey(branch)
                        || !((JSONObject) ((JSONObject) registerAddressMappingTable.get("branch")).get(branch))
                                .containsKey(place)
                        || !((JSONObject) ((JSONObject) ((JSONObject) registerAddressMappingTable.get("branch"))
                                .get(branch)).get(place))
                                .containsKey(devEui)
                        || !((JSONObject) registerAddressMappingTable.get("sensors")).containsKey(sensor)) {
                    System.out.println(branch + " " + site + " " + place + " " + devEui + " " + sensor);
                    continue;
                }
                double value = (double) destObject.get(sensor);
                JSONObject payload = new JSONObject();
                payload.put("registerAddress",
                        (long) ((JSONObject) ((JSONObject) ((JSONObject) registerAddressMappingTable.get("branch"))
                                .get(branch)).get(place)).get(devEui)
                                + (long) ((JSONObject) registerAddressMappingTable.get("sensors")).get(sensor));
                payload.put("branch", branch);
                payload.put("site", site);
                payload.put("place", place);
                payload.put("devEui", devEui);
                payload.put("sensor", sensor);
                payload.put("value", value);

                JSONObject result = new JSONObject();
                result.put("payload", payload);

                output(0, new JsonMessage(result));
            }

        }
    }

    @Override
    void postprocess() {
    }

}
