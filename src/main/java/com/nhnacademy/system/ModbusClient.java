package com.nhnacademy.system;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import org.json.JSONObject;

import lombok.extern.slf4j.Slf4j;

/* 
 * Modbus server로부터 센서값을 받아오기위해 Soket 통신을 하는 역할
*/
@Slf4j
public class ModbusClient extends Thread {
    private static Queue<JSONObject> dataQueue = new LinkedList<>();
    static final String HOST = "localhost";
    static final int PORT = 11502;
    private final int unitId;
    static int transactionId = 0;

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
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Queue<JSONObject> getQueue() {
        return dataQueue;
    }

    private void requestToServer() {

    }

    private void receiveResponse() {
        byte[] receivedData = new byte[1024];
        int receiveLength;
        JSONObject data = new JSONObject();
        while (socket.isConnected()) {
            try {
                receiveLength = inputStream.read(new byte[1024], 0, 1024);
                byte[] pdu = new byte[receiveLength - 7];
                System.arraycopy(receivedData, 7, pdu, 0, receiveLength);
                data.append("transactionId", ++transactionId);
                data.append("unitId", unitId);
                data.append("pdu", pdu);

                dataQueue.add(data);
                log.info("[Client] - data : {}", data.toString());
                notifyAll();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendData() {

    }
}
