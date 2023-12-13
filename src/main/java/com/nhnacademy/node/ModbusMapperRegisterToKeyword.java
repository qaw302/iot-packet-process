package com.nhnacademy.node;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.wire.Wire;

public class ModbusMapperRegisterToKeyword extends InputOutputNode {
    JSONObject registerAddressMappingTable;

    protected ModbusMapperRegisterToKeyword(String id) {
        super(id, 1);
        JSONParser jsonParser = new JSONParser();
        try {
            Reader reader = new FileReader("src/main/resources/registerAddressMappingTable.json");
            registerAddressMappingTable = (JSONObject) jsonParser.parse(reader);
            System.out.println(registerAddressMappingTable);
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
            String registerAddress = JsonMessage.getDestJsonObject(jsonObject,
                    new String[] { "payload", "registerAddress" }).get("registerAddress").toString();
            String value = JsonMessage.getDestJsonObject(jsonObject,
                    new String[] { "payload", "value" }).get("value").toString();
            if(registerAddress.equals("undefined") || value.equals("undefined"))
                continue;
                JSONObject payload = new JSONObject();
            for(int j = 10000;j>0;j/=10) {
                long addr = Long.parseLong(registerAddress)/j%10;
                if(j==10000){
                    payload.put("branch", registerAddressMappingTable.get("branch").toString());
                }
            }

        }
    }

    @Override
    void postprocess() {
    }

}
