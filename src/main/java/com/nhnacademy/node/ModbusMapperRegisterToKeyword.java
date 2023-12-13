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
import com.nhnacademy.wire.Wire;

public class ModbusMapperRegisterToKeyword extends InputOutputNode {
    public JSONObject registerAddressMappingTable;

    protected ModbusMapperRegisterToKeyword(String id) {
        super(id, 1);
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

    public String getKey(JSONObject jsonObject, long keyValue, String keyWord) {
        String result = null;
        for (Object key : jsonObject.keySet()) {
            if (jsonObject.get(key) instanceof JSONObject) {
                result = getKey((JSONObject) jsonObject.get(key), keyValue, keyWord + key + " ");
            } else {
                if (jsonObject.get(key).equals(keyValue)) {
                    result = keyWord + key;
                }
            }
            if (result != null)
                break;
        }
        return result;
    }

    public String[] getKeyWord(JSONObject jsonObject, long keyValue) {
        String result = getKey((JSONObject) jsonObject.get("branch"), keyValue / 100 * 100, "");
        for (Object key : ((JSONObject) jsonObject.get("sensors")).keySet()) {
            if (((JSONObject) jsonObject.get("sensors")).get(key).equals(keyValue % 100)) {
                result += " " + key;
            }
        }
        return result.split(" ");

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
            String registerAddress = JsonMessage.getDestJsonObject(jsonObject,
                    new String[] { "payload", "registerAddress" }).get("registerAddress").toString();
            String value = JsonMessage.getDestJsonObject(jsonObject,
                    new String[] { "payload", "value" }).get("value").toString();
            if (registerAddress.equals("undefined") || value.equals("undefined"))
                continue;
            JSONObject payload = new JSONObject();
            // dfsFindKey(registerAddressMappingTable, Integer.parseInt(registerAddress));

        }
    }

    @Override
    void postprocess() {
    }

    public static void main(String[] args) {
        ModbusMapperRegisterToKeyword modbusMapperRegisterToKeyword = new ModbusMapperRegisterToKeyword("1");
        String[] result =  modbusMapperRegisterToKeyword.getKeyWord(modbusMapperRegisterToKeyword.registerAddressMappingTable, 11101);
        for(String s : result) {
            System.out.println(s);
        }
    }
}
