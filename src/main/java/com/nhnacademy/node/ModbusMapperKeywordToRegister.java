package com.nhnacademy.node;

import org.json.simple.JSONObject;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.system.OutputLogger;
import com.nhnacademy.system.RegisterAddressMappingTable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModbusMapperKeywordToRegister extends InputOutputNode {
        private RegisterAddressMappingTable registerAddressMappingTable;

        public ModbusMapperKeywordToRegister(String id) {
                super(id, 1);
                registerAddressMappingTable = RegisterAddressMappingTable
                                .getRegisterAddressMappingTable("src/main/resources/registerAddressMappingTable.json");
        }

        public static ModbusMapperKeywordToRegister generate(JSONObject jsonObject) {
                String id = (String) jsonObject.get("id");
                return new ModbusMapperKeywordToRegister(id);
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
                        String branch = JsonMessage.getDestJsonObject(jsonObject, new String[] { "payload", "branch" })
                                        .get("branch").toString();
                        String site = JsonMessage.getDestJsonObject(jsonObject, new String[] { "payload", "site" })
                                        .get("site")
                                        .toString();
                        String place = JsonMessage.getDestJsonObject(jsonObject, new String[] { "payload", "place" })
                                        .get("place")
                                        .toString();
                        String devEui = JsonMessage.getDestJsonObject(jsonObject, new String[] { "payload", "devEui" })
                                        .get("devEui").toString();
                        String sensor = JsonMessage.getDestJsonObject(jsonObject, new String[] { "payload", "sensor" })
                                        .get("sensor")
                                        .toString();
                        String key = branch + "." + site + "." + place + "." + devEui + "." + sensor;
                        if (!registerAddressMappingTable.hasKey(key)) {
                                log.info("key not found");
                                continue;
                        }
                        JSONObject result = new JSONObject();
                        JSONObject payload = new JSONObject();
                        payload.put("registerAddress",
                                        (long) registerAddressMappingTable.getRegisterAddressMappingTable().get(key));
                        payload.put("value",
                                        JsonMessage.getDestJsonObject(jsonObject, new String[] { "payload", "value" })
                                                        .get("value"));
                        result.put("payload", payload);
                        output(0, new JsonMessage(result));

                        OutputLogger.getInstance().write(getId(), jsonObject.toString().length(),
                                        result.toString().length(),
                                        error, startTime,
                                        System.currentTimeMillis() - startTime);
                }
        }

        @Override
        void postprocess() {
        }

}
