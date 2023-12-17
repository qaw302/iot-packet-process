package com.nhnacademy.system;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.nhnacademy.node.ActiveNode;
import com.nhnacademy.node.InputNode;
import com.nhnacademy.node.InputOutputNode;
import com.nhnacademy.node.ModbusServerNode;
import com.nhnacademy.node.MqttInNode;
import com.nhnacademy.node.MqttOutNode;
import com.nhnacademy.node.Node;
import com.nhnacademy.node.OutputNode;
import com.nhnacademy.wire.Wire;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SystemOption {

    private static final String DEFAULT_FLOW_FILE_PATH = "src/main/resources/flows.json";
    private static final String CLASS_PATH = "com.nhnacademy.node.";
    private static final String CLASS_NAMES = "{\n" + //
            "    \"mqtt in\": \"MqttInNode\",\n" + //
            "    \"mqtt out\": \"MqttOutNode\",\n" + //
            "    \"function\": \"FunctionNode\",\n" + //
            "    \"modbus server\": \"ModbusServerNode\"\n" + //
            "    \"mqtt preprocess\": \"MqttMessageProcessingNode\"\n" + //
            "    \"mqtt generator\": \"MqttMessageGenerator\"\n" + //
            "    \"rule engine\": \"RuleEngineNode\"\n" + //
            "    \"modbus keyword to register\": \"ModbusMapperKeywordToRegister\"\n" + //
            "    \"modbus register to keyword\": \"ModbusMapperRegisterToKeyword\"\n" + //
            "}";

    private static JSONParser jsonParser = new JSONParser();

    private HashMap<String, Object> nodeList;
    private HashMap<String, JSONArray> wireInfo;
    private JSONArray nodesInfo;
    private String filePath;
    private boolean isCommandMode = false;

    private String applicationName;
    private static String[] sensors;

    public SystemOption(String[] args) {
        nodeList = new HashMap<>();
        wireInfo = new HashMap<>();
        filePath = DEFAULT_FLOW_FILE_PATH;
        inspectCommandLine(args);
    }

    private void inspectCommandLine(String[] args) {
        if (args.length > 0) {
            Options options = new Options();
            options.addOption("c", false, "command line");
            options.addOption("an", "an", true, "application name");
            options.addOption("s", true, "setting sensor");

            try {
                nodesInfo = (JSONArray) jsonParser.parse(new FileReader(filePath));
                CommandLineParser parser = new DefaultParser();
                CommandLine commandLine = parser.parse(options, args);
                if (commandLine.hasOption("c")) {
                    isCommandMode = true;
                    if (commandLine.hasOption("an")) {
                        applicationName = commandLine.getOptionValue("an");

                    }
                    if (commandLine.hasOption("s")) {
                        sensors = commandLine.getOptionValue("s").split(",");
                    }

                } else if (new File(args[0]).exists()) {
                    filePath = args[0];

                } else {
                    throw new IllegalArgumentException("Invalid command line argument format");
                }
            } catch (ParseException e) {
                log.error("commandLine parsing error");
            } catch (IOException | org.json.simple.parser.ParseException e) {
                log.error("JSON File parsing error");
            }
        }
    }

    public void createNodes() {
        for (Object obj : nodesInfo) {
            if (!(obj instanceof JSONObject)) {
                continue;
            }
            JSONObject node = (JSONObject) obj;

            if (node.containsKey("id") && node.containsKey("type")) {
                Object instance = getInstance(node);
                if (isCommandMode) {
                    node.put("an", applicationName);
                    node.put("sensors", sensors);
                }

                nodeList.put(node.get("id").toString(), instance);
                wireInfo.put(node.get("id").toString(), (JSONArray) node.get("wires"));
            }
        }
    }

    private Object getInstance(JSONObject node) {
        Object instance = null;
        try {
            JSONObject classNames = (JSONObject) jsonParser.parse(CLASS_NAMES);
            Class<?> nodeClass = Class.forName(CLASS_PATH + classNames.get(node.get("type").toString()));

            for (Constructor<?> constructor : nodeClass.getConstructors()) {
                if (InputNode.class.isAssignableFrom(constructor.getDeclaringClass())) {
                    if (constructor.getParameterTypes().length == 4) {
                        instance = constructor.newInstance(node.get("topic").toString(), 1,
                                Broker.getBroker(node.get("server").toString(), 1883),
                                ((JSONArray) node.get("wires")).size());
                        break;
                    }
                } else if (InputOutputNode.class.isAssignableFrom(constructor.getDeclaringClass())) {
                    instance = constructor.newInstance();
                    break;
                } else if (OutputNode.class.isAssignableFrom(constructor.getDeclaringClass())) {
                    if (constructor.getParameterTypes().length == 1) {
                        instance = constructor.newInstance(Broker.getBroker(node.get("server").toString(), 502));
                        break;
                    } else if (constructor.getParameterTypes().length == 0) {
                        instance = constructor.newInstance();
                    }
                    break;
                }
            }
            if (instance == null) {
                throw new IllegalArgumentException("No suitable constructor found for " + nodeClass.getSimpleName());
            }
        } catch (Exception e) {
            log.error("Error creating instance of {}: {}", node.get("type"), e.getMessage());
        }

        return instance;
    }

    public void createFlow() {
        log.trace(nodeList.entrySet().toString());
        log.trace(wireInfo.entrySet().toString());
        int inputNodeIdx = 0;
        int inputOutputNodeIdx = 0;
        Wire[] wires;
        for (Map.Entry<String, Object> entry : nodeList.entrySet()) {
            String id = entry.getKey();
            Object node = entry.getValue();
            if (node == null) {
                continue;
            }
            if (node instanceof InputNode) {
                InputNode inputNode = (InputNode) node;
                wires = createWires(wireInfo.get(id));
                connectWires(inputNode, wires, wireInfo.get(id), nodeList, inputNodeIdx);
            } else if (node instanceof InputOutputNode) {
                InputOutputNode IONode = (InputOutputNode) node;
                wires = createWires(wireInfo.get(id));
                connectWires(IONode, wires, wireInfo.get(id), nodeList, inputOutputNodeIdx);
            }
        }
    }

    private void connectWires(Node node, Wire[] wires, JSONArray wireArrayInfo, Map<String, Object> nodeList,
            int wireIndex) {
        for (Object wireArray : wireArrayInfo) {
            for (int i = 0; i < ((JSONArray) wireArray).size(); i++) {
                if (node instanceof InputNode) {
                    ((InputNode) node).connectOutputWire(i, wires[wireIndex]);
                } else if (node instanceof InputOutputNode) {
                    ((InputOutputNode) node).connectOutputWire(i, wires[wireIndex]);
                }

                Object targetNode = nodeList.get(((JSONArray) wireArray).get(i).toString());
                if (targetNode instanceof OutputNode) {
                    ((OutputNode) targetNode).connectInputWire(wires[wireIndex++]);
                } else if (targetNode instanceof InputOutputNode) {
                    ((InputOutputNode) targetNode).connectInputWire(wires[wireIndex++]);
                }
            }
        }
    }

    public Wire[] createWires(JSONArray wireList) {
        Wire[] wires;
        int wireListSize = 0;
        for (Object array : wireList) {
            wireListSize += ((JSONArray) array).size();
        }
        wires = new Wire[wireListSize];

        for (int i = 0; i < wires.length; i++) {
            wires[i] = new Wire();
        }
        return wires;
    }

    public static String[] getSensors() {
        return sensors;
    }

    public void startFlow() {
        for (Map.Entry<String, Object> entry : nodeList.entrySet()) {
            log.trace(entry.getValue()+"");
            ((ActiveNode) entry.getValue()).start();
        }
    }

}