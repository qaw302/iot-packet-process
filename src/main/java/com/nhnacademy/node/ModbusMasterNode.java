package com.nhnacademy.node;

import java.util.Queue;
import org.json.simple.JSONObject;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.system.ModbusClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModbusMasterNode extends InputNode {
    private static final int ADDRESS = 11101;
    private ModbusClient modbusClient;
    private Queue<JSONObject> dataQueue;

    public ModbusMasterNode(String id) {
        super(id, 1);
    }

    @Override
    void preprocess() {
        modbusClient = new ModbusClient(1);
        modbusClient.start();
        dataQueue = modbusClient.getQueue();
    }

    @Override
    synchronized void process() {
        modbusClient.setAddress(ADDRESS); // gyeongnam/class_a/24e124128c067999/temperature
        while (!thread.isInterrupted()) {
            if (!dataQueue.isEmpty()) {
                JsonMessage message = new JsonMessage(dataQueue.poll());
                log.info("output message : {}", message.toString());
                output(message, 0);

            } else {
                try {
                    wait();
                    log.info("Queue is empty. Thread is watting");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    void postprocess() {
        modbusClient.interrupt();
    }

}
