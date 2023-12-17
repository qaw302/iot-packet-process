package com.nhnacademy.node;

import org.json.simple.JSONObject;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.system.DataBase;
import com.nhnacademy.system.OutputLogger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RuleEngineNode extends InputOutputNode {
    private DataBase dataBase;

    public RuleEngineNode(String id) {
        super(id, 1);
        dataBase = DataBase.getDataBase("src/main/resources/dataBase.csv");
        String[] colNames = { "site", "sensor", "value", "branch", "place", "registerAddress", "devEui" };
        dataBase.addCol(colNames);
        dataBase.setPrimaryKey("registerAddress");
    }

    public static RuleEngineNode generate(JSONObject jsonObject) {
        String id = (String) jsonObject.get("id");
        return new RuleEngineNode(id);
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
            if (message == null)
                continue;
            JsonMessage jsonMessage = (JsonMessage) message;
            JSONObject jsonObject = jsonMessage.getJsonObject();
            JSONObject dataObject = (JSONObject) jsonObject.get("payload");
            dataBase.addData(dataObject);
            output(0, jsonMessage);

            OutputLogger.getInstance().write(getId(), jsonObject.toString().length(), jsonObject.toString().length(),
                    error, startTime,
                    System.currentTimeMillis() - startTime);
        }
    }

    @Override
    void postprocess() {
    }

}
