package com.nhnacademy.node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.simple.JSONObject;
import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.wire.Wire;

public class Debug extends OutputNode {

    String path = "src/test/java/logs.json";
    String nodeName = "";
    // Fix constructor name
    public Debug(String name, int inCount) {
        super(name, inCount);
        // TODO Auto-generated constructor stub
    }

    @Override
    void process() {
        sendToLogs();
    }


    public void sendToLogs() {
        try {
            Wire inputWire = getInputWire(0);
            if (inputWire != null) {
                while (inputWire.hasMessage()) {
                    Message message = inputWire.get();

                    if (message instanceof JsonMessage) {
                        JsonMessage jsonMessage = (JsonMessage) message;
                        //parse한 객체 생성

                        JSONObject jsonlog = createJsonLog(jsonMessage);

                        // 파일이 없으면 생성
                        File file = new File(path);
                        if (!file.exists()) {
                            try {
                                file.createNewFile();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        


                        // 기존 데이터 읽어오기
                        List<String> existingData = readExistingData(file);

                        // 새로운 데이터 추가

                        //existingData.add(jsonMessage.getJsonObject().toJSONString());
                        existingData.add(jsonlog.toJSONString());
                       

                        // JSON 파일 쓰기
                        writeDataToFile(file, existingData);

                        
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> readExistingData(File file) throws IOException {
        List<String> existingData = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                existingData.add(line);
            }
        }

        return existingData;
    }

    private void writeDataToFile(File file, List<String> data) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            for (String line : data) {
                out.println(line);
            }
            System.out.println("Write success!");
        }
    }

    private JSONObject createJsonLog(JsonMessage jsonMessage) {
        JSONObject jsonLog = new JSONObject();

        // "time" 값 추출
        long timeValue = extractTimeValue(jsonMessage);
        jsonLog.put("time", timeValue);

        // "/p/class_a" 값 추출
        String topic = jsonMessage.getJsonObject().get("topic").toString();
        String[] topicParts = topic.split("/");
        String placeValue = "";
        for (String part : topicParts) {
            if (part.equals("p")) {
                placeValue = topicParts[Arrays.asList(topicParts).indexOf(part) + 1];
                break;
            }
        }
        jsonLog.put("place", placeValue);

        jsonLog.put("id", this.getNodeName());

        return jsonLog;
    }

    private long extractTimeValue(JsonMessage jsonMessage) {
        Object payloadObj = jsonMessage.getJsonObject().get("payload");
        if (payloadObj instanceof JSONObject) {
            JSONObject payloadJson = (JSONObject) payloadObj;
            Object timeObj = payloadJson.get("time");
            if (timeObj instanceof Long) {
                return (Long) timeObj;
            }
        }
        return 0; // 기본 값 또는 오류 처리에 맞게 반환
    }

    public void setNodeName(String name){
        this.nodeName = name;

    }

    public String getNodeName(){
        return nodeName;
    }

    @Override
    void preprocess() {
        throw new UnsupportedOperationException("Unimplemented method 'preprocess'");
    }

    @Override
    void postprocess() {
        throw new UnsupportedOperationException("Unimplemented method 'postprocess'");
    }
    
}