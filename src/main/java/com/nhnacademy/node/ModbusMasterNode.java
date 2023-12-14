package com.nhnacademy.node;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Queue;
import java.util.Set;

import org.json.JSONObject;

import com.nhnacademy.message.BufferMessage;
import com.nhnacademy.system.ModbusClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModbusMasterNode extends InputNode {
    private ModbusClient modbusClient;
    private Queue<JSONObject> dataQueue;

    protected ModbusMasterNode(String id) {
        super(id, 1);
    }

    private static final HashMap<Integer, String> functionCodeMap = new HashMap<>();
    static {
        functionCodeMap.put(3, "makeReadHoldingRegistersRequest");
        functionCodeMap.put(4, "makeReadInputRegistersRequest");
        functionCodeMap.put(6, "makeWriteSingleRegisterRequest");
        functionCodeMap.put(16, "makeWriteMultipleRegistersRequest");
    }

    @Override
    void preprocess() {
        modbusClient = new ModbusClient(1);
        modbusClient.start();
        dataQueue = modbusClient.getQueue();
    }

    @Override
    synchronized void process() {
        while (!thread.isInterrupted()) {
            if (dataQueue.isEmpty()) {
                try {
                    wait();
                    log.info("Queue is empty. Thread is watting");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {

            }
        }

    }

    @Override
    void postprocess() {
        modbusClient.interrupt();
    }

    public static Set<Integer> getFunctionKeys() {
        return functionCodeMap.keySet();
    }

    public static String getMethodName(int functionCode) {
        return functionCodeMap.get(functionCode);
    }

    public static byte[] addMBAPHeader(int transactionId, int unitId, byte[] pdu) {
        byte[] adu = new byte[7 + pdu.length];
        ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);

        buffer.putInt(transactionId);
        adu[0] = buffer.get(2);
        adu[1] = buffer.get(3);

        adu[2] = 0;
        adu[3] = 0;

        buffer.clear();
        buffer.putInt(pdu.length);
        adu[4] = buffer.get(2);
        adu[5] = buffer.get(3);

        buffer.clear();
        buffer.putInt(unitId);
        adu[6] = buffer.get(3);
        System.arraycopy(pdu, 0, adu, 7, pdu.length);

        return adu;
    }

    public static byte[] makeReadHoldingRegistersRequest(int address, int quantity) {
        byte[] frame = new byte[5];
        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);

        // PDU의 function code
        frame[0] = 0x03;

        // PDU의 data
        b.putInt(address);
        frame[1] = b.get(2);
        frame[2] = b.get(3);

        b.clear();
        b.putInt(quantity);
        frame[3] = b.get(2);
        frame[4] = b.get(3);

        return frame;
    }

    /**
     * 0x04 request
     * 
     * @param address
     * @param quantity
     * @return
     */
    public static byte[] makeReadInputRegistersRequest(int address, int quantity) {
        byte[] frame = new byte[5];

        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        b.putInt(address);

        // PDU의 function code
        frame[0] = 0x04;

        // PDU의 data 부분
        frame[1] = b.get(2);
        frame[2] = b.get(3);

        b.clear();
        b.putInt(quantity);
        frame[3] = b.get(2);
        frame[4] = b.get(3);

        return frame;
    }

    public static byte[] makeWriteSingleRegisterRequest(int address, int value) {
        byte[] frame = new byte[5];
        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN); // Big_endian 처리

        frame[0] = 0x06; // 헥사코드, PDU function code

        b.putInt(address); // PDU data code
        frame[1] = b.get(2);
        frame[2] = b.get(3);

        b.clear(); // PDU quantity data
        b.putInt(value);
        frame[3] = b.get(2);
        frame[4] = b.get(3);

        return frame;
    }

    public byte[] makeWriteMultipleRegistersRequest(int address, int quantity, byte[] pdu) {
        // fc, address, address, quantity, quantity, byte count, Register values(Nbytes)
        // address : 10101 (0010 0111 0111 0101)-> {16, 39, 117, x, x, x, x ...}

        byte[] frame = new byte[6 + pdu.length];

        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);

        frame[0] = 0x10;

        b.putInt(address);
        frame[1] = b.get(2);
        frame[2] = b.get(3);

        b.clear();
        b.putInt(quantity);
        frame[3] = b.get(2);
        frame[4] = b.get(3);

        frame[5] = (byte) (quantity < 127 || quantity > 0 ? quantity * 2 : quantity);

        if (quantity * 2 != pdu.length) {
            throw new IllegalArgumentException();
        }

        System.arraycopy(pdu, 0, frame, 6, pdu.length);

        return frame;
    }

}
