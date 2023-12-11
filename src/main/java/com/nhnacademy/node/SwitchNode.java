package com.nhnacademy.node;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/*
 * 일단 여기 들어오는 데이터는 데이터를 확인한 후 해당 키 가 있는지 확인 하거나 아니면
 * 일단 기준이 프로퍼티가 기준이네
 */

public class SwitchNode extends InputOutputNode {
    private String property;
    private String[] payload;
    private JSONObject nodeSetting;

    public SwitchNode(int inCount, int outCount, JSONObject nodeSetting) {
        super(inCount, outCount);
        this.nodeSetting = nodeSetting;
    }

    public SwitchNode(String name, int inCount, int outCount, JSONObject nodeSetting) {
        super(name, inCount, outCount);
        this.nodeSetting = nodeSetting;
    }

    private void nodeSetting() {
        JSONArray rules = (JSONArray) nodeSetting.get("rules");
        property = nodeSetting.get("property").toString();
    }

    @Override
    void preprocess() {
        // 여기서 프로펄티 기준으로 스플릿 해야함
        payload = property.split(".");

    }

    @Override
    void process() {

    }

    @Override
    void postprocess() {

    }
}
