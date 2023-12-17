package com.nhnacademy.node;

import org.json.simple.JSONObject;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.system.OutputLogger;
import com.nhnacademy.system.RegisterAddressMappingTable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModbusMapperRegisterToKeyword extends InputOutputNode {
    private RegisterAddressMappingTable registerAddressMappingTable;

    public ModbusMapperRegisterToKeyword(String id) {
        super(id, 1);
        registerAddressMappingTable = RegisterAddressMappingTable
                .getRegisterAddressMappingTable("src/main/resources/registerAddressMappingTable.json");
    }

    public ModbusMapperRegisterToKeyword() {
        super(1);
        registerAddressMappingTable = RegisterAddressMappingTable
                .getRegisterAddressMappingTable("src/main/resources/registerAddressMappingTable.json");
    }

    @Override
    void preprocess() {
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
            JSONObject jsonObject = ((JsonMessage) message).getJsonObject();
            String registerAddress = JsonMessage.getDestJsonObject(jsonObject,
                    new String[] { "payload", "registerAddress" }).get("registerAddress").toString();
            String value = JsonMessage.getDestJsonObject(jsonObject,
                    new String[] { "payload", "value" }).get("value").toString();
            if (registerAddress.equals("undefined") || value.equals("undefined"))
                continue;
            if (!registerAddressMappingTable.hasAddress(Long.parseLong(registerAddress))) {
                log.info("address not found");
                continue;
            }
            JSONObject payload = new JSONObject();
            String key = registerAddressMappingTable.getRegisterAddressMappingTable().get(registerAddress).toString();
            String[] keys = key.split(".");
            payload.put("branch", keys[0]);
            payload.put("site", keys[1]);
            payload.put("place", keys[2]);
            payload.put("devEui", keys[3]);
            payload.put("sensor", keys[4]);
            payload.put("value", value);
            payload.put("address", registerAddress);
            JSONObject result = new JSONObject();
            result.put("payload", payload);
            output(0, new JsonMessage(result));

            OutputLogger.getInstance().write(getId(), jsonObject.toString().length(), result.toString().length(), error,
                    startTime,
                    System.currentTimeMillis() - startTime);
        }
    }

    @Override
    void postprocess() {
    }

    public static void main(String[] args) {
        ModbusMapperRegisterToKeyword modbusMapperRegisterToKeyword = new ModbusMapperRegisterToKeyword("1");
    }
}
