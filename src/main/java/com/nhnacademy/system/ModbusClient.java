package com.nhnacademy.system;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.json.simple.JSONObject;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

/* 
 * Modbus server로부터 센서값을 받아오기위해 Soket 통신을 하는 역할
 * 서버 당 MasterNode 하나씩 생성
 * 필드
 * - serverList
 * - dataQueue : server에서 받은 response를 저장해 두는 큐
*/
@Slf4j
public class ModbusClient extends Thread {
    private static Queue<JSONObject> dataQueue = new LinkedList<>();
    static final String HOST = "172.19.0.6";
    static final int PORT = 502;
    private final int unitId;
    static int transactionId = 0;
    private int address = -1;

    private Socket socket;
    private BufferedInputStream inputStream;
    private BufferedOutputStream outputStream;

    public ModbusClient(int unitId) {
        this.unitId = unitId;
    }

    @Override
    public synchronized void run() {
        log.info("[Client] - thread is running");
        try {
            socket = new Socket(HOST, PORT);
            log.info("[Client] - connect server");
            inputStream = new BufferedInputStream(socket.getInputStream());
            outputStream = new BufferedOutputStream(socket.getOutputStream());

            sendAndReceive();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Queue<JSONObject> getQueue() {
        return dataQueue;
    }

    private synchronized void sendAndReceive() {
        Thread requestThread = new Thread() {
            @Override
            public void run() {
                log.info("request Thread is running");
                setName("request");
                while (!this.isInterrupted()) {
                    try {

                        while (address == -1) {
                            wait();
                        }

                        // request 생성 후 server에 요청 보내기
                        byte[] request = ModbusRequsetMaker.addMBAPHeader(++transactionId, unitId,
                                ModbusRequsetMaker.makeReadHoldingRegistersRequest(address, 1));
                        log.info("request : {}", Arrays.toString(request));
                        outputStream.write(request);
                        outputStream.flush();
                        log.info("request sent");

                        // 요청에대한 응답 받기
                        byte[] receivedData = new byte[16];
                        int receiveLength = inputStream.read(receivedData, 0, 16);
                        log.info("received response");
                        JSONObject data = new JSONObject();
                        data.put("registerAddress", address);
                        data.put("value", ((receivedData[receiveLength - 2] & 0xff) << 8)
                                | (receivedData[receiveLength - 1] & 0xff));
                        log.info("address : {}, value : {}", address, ((receivedData[receiveLength - 2] & 0xff) << 8)
                                | (receivedData[receiveLength - 1] & 0xff));
                        dataQueue.add(data);
                        log.info("[Client] - data : {}", data.toString());
                        Thread.sleep(1000);

                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        requestThread.start();
    }

    public synchronized void setAddress(int address) {
        this.address = address;
        notifyAll();
    }

    public int getAddres() {
        return address;
    }
}
