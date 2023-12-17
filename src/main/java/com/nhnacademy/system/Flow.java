package com.nhnacademy.system;

import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.nhnacademy.node.ActiveNode;
import com.nhnacademy.node.InputNode;
import com.nhnacademy.node.Node;
import com.nhnacademy.wire.Wire;

public class Flow {
    private List<Node> nodeList;

    public Flow(JSONArray jsonArray) {
        super();
        nodeList = new ArrayList<>();
        try {
            makeFlow(jsonArray);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void makeFlow(JSONArray jsonArray)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, ClassNotFoundException {
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            String type = (String) jsonObject.get("type");
            Node node = (Node) Class.forName("com.nhnacademy.node." + type).getMethod("generate", JSONObject.class)
                    .invoke(null, jsonObject);

            nodeList.add(node);
        }
        for (int i = 0; i < nodeList.size(); i++) {
            JSONArray wires = (JSONArray) ((JSONObject) jsonArray.get(i)).get("wires");
            Node inputNode = null;
            for (Node node : nodeList) {

                if (node.getId().equals(((JSONObject) jsonArray.get(i)).get("id"))) {
                    inputNode = node;
                }
            }
            for (int j = 0; j < wires.size(); j++) {
                JSONArray wire = (JSONArray) wires.get(j);
                for (Object o : wire) {
                    for (Node node : nodeList) {
                        if (node.getId().equals(o)) {
                            Wire w = new Wire();
                            inputNode.getClass().getMethod("connectOutputWire", int.class, Wire.class).invoke(inputNode, j,
                                    w);
                            node.getClass().getMethod("connectInputWire", Wire.class).invoke(node, w);
                        }
                    }
                }
            }
        }
        for (Node node : nodeList) {
            ((ActiveNode) node).start();
        }
    }

    public static void main(String[] args) {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("src/main/resources/flows.json"));
            JSONArray jsonArray = (JSONArray) obj;
            Flow flow = new Flow(jsonArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
