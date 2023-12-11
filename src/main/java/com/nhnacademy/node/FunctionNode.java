package com.nhnacademy.node;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.json.simple.JSONObject;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.system.NodeRedSystem;
import com.nhnacademy.wire.Wire;

import lombok.extern.slf4j.Slf4j;

/*
 * 수정 필요
 */

@Slf4j
public class FunctionNode extends InputOutputNode {
    private String func;
    private ScriptEngineManager manager = new ScriptEngineManager();
    private String z;

    public FunctionNode(String id, int outCount, String func, String z) {
        super(id, outCount);
        this.func = func;
        this.z = z;
    }

    public FunctionNode(int outCount, String func, String z) {
        super(outCount);
        this.func = func;
        this.z = z;
    }

    @Override
    void process() {
        for (int i = 0; i < getInputWireCount(); i++) {
            Wire wire = getInputWire(i);
            if (wire == null || !wire.hasMessage())
                continue;
            Message message = wire.get();
            JSONObject jsonObject = ((JsonMessage) message).getJsonObject();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            engine.put("msg", jsonObject);
            engine.put("flow", NodeRedSystem.getInstance().getFlow(z));
            try {
                engine.eval(func);
                JSONObject msgJsonObject = (JSONObject) engine.get("msg");
                JSONObject flowJsonObject = (JSONObject) engine.get("flow");
                NodeRedSystem.getInstance().getFlow(z).setFlowJsonObject(flowJsonObject);
                output(0, new JsonMessage(msgJsonObject));
            } catch (ScriptException e) {
                log.info(getId() + " ScriptException", e);
            }
        }
    }
}