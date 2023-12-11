package com.nhnacademy.message;

import org.json.simple.JSONObject;

public class JsonMessage extends Message {
    JSONObject jsonObject;
    String nodeName;

    public JsonMessage(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeName() {
        return nodeName;
    }

    @Override
    public String toString() {
        return "node name : " + this.getNodeName() +
                " , " + jsonObject.toJSONString();
    }

}
