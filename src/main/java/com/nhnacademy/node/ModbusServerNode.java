package com.nhnacademy.node;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONObject;

import com.nhnacademy.message.JsonMessage;
import com.nhnacademy.message.Message;
import com.nhnacademy.system.ModbusFunctionCode;
import com.nhnacademy.wire.Wire;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModbusServerNode extends OutputNode {
    private static final String TRANSACTION_ID = "transactionId";
    private static final String PROTOCOL_ID = "protocolId";
    private static final String LENGTH = "length";
    private static final byte UNIT_ID = 1; // slave 장치의 ID , 즉 서버의 아이디
    private static byte[] tempDB = new byte[40000];
    private int port;
    private HashMap<String, byte[]> headerMap;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public ModbusServerNode(int port) {
        super();
        this.port = port;
    }

    @Override
    public void preprocess() {
        try {
            selector = Selector.open();

            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(port));

            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            headerMap = new HashMap<>();
            log.trace("port : " + port + " modbus server started");

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        selector.select();
                        Set<SelectionKey> selectedKeys = selector.selectedKeys();
                        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                        while (keyIterator.hasNext()) {
                            SelectionKey key = keyIterator.next();

                            if (key.isAcceptable()) {
                                ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                                SocketChannel clientChannel = serverChannel.accept();
                                clientChannel.configureBlocking(false);
                                parseData(key, clientChannel);
                                clientChannel.register(selector, SelectionKey.OP_READ);

                            } else if (key.isReadable()) {
                                SocketChannel clientChannel = (SocketChannel) key.channel();
                                parseData(key, clientChannel);
                            }
                            keyIterator.remove();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void process() {
        try {
                                        inputWireToSocket();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postprocess() {
        try {
            serverSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void inputWireToSocket() throws IOException {
        for (Wire wire : inputPort) {
            while (wire.hasMessage()) {
                Message message = wire.get();
                if (message instanceof JsonMessage) {
                    byte[] data = jsonMessageToByte((JsonMessage) message);

                    parseHeader(data);
                    log.info("request : " + Arrays.toString(data));

                    byte[] response = parsePDU(data);
                    log.info("response : " + Arrays.toString(response));
                }
            }
        }
    }

    private byte[] jsonMessageToByte(JsonMessage message) {
        long address = (long) ((JSONObject) message.getJsonObject().get("payload")).get("registerAddress");
        double value = (double) ((JSONObject) message.getJsonObject().get("payload")).get("value");
        byte[] data = new byte[] { 0, 0, 0, 0, 0, 6, 1, 6, 0, 0, 0, 0 };
        ByteBuffer b = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        b.clear();
        b.putLong(address);
        data[8] = b.get(6);
        data[9] = b.get(7);
        b.clear();
        b.putLong(Math.round(value));
        System.out.println("ffffffffffffffff"+Math.round(value));
        data[10] = b.get(6);
        data[11] = b.get(7);
        return data;
    }

    private void parseData(SelectionKey key, SocketChannel clientChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.clear();
        int bytesRead = clientChannel.read(buffer);

        if (bytesRead > 0) {
            buffer.flip();
            byte[] data = new byte[bytesRead];
            buffer.get(data); // data에 buffer를 저장

            parseHeader(data);
            log.info("request : " + Arrays.toString(data));
            byte[] response = parsePDU(data);
            log.info("response : " + Arrays.toString(response));

            ByteBuffer outputBuffer = ByteBuffer.wrap(response);
            while (outputBuffer.hasRemaining()) {
                clientChannel.write(outputBuffer);
            }
        } else if (bytesRead == -1) {
            key.cancel();
            clientChannel.close();
        } else {
            log.info("No data received bytesRead : " + bytesRead);
        }
    }

    public void parseHeader(byte[] inputBuffer) {
        headerMap.put(TRANSACTION_ID, new byte[] { inputBuffer[0], inputBuffer[1] });
        headerMap.put(PROTOCOL_ID, new byte[] { inputBuffer[2], inputBuffer[3] });
        headerMap.put(LENGTH, new byte[] { inputBuffer[4], inputBuffer[5] });
        headerMap.put("unitId", new byte[] { inputBuffer[6] });

        byte[] unitIdArray = headerMap.get("unitId");

        if (unitIdArray.length == 0 || unitIdArray[0] != UNIT_ID) {
            log.error("wrong slave !");
            throw new IllegalArgumentException();
        }
    }

    public byte[] makeMBAP(byte[] pdu) {
        byte[] header = new byte[7];
        header[0] = headerMap.get(TRANSACTION_ID)[0];
        header[1] = headerMap.get(TRANSACTION_ID)[1];
        header[2] = headerMap.get(PROTOCOL_ID)[0];
        header[3] = headerMap.get(PROTOCOL_ID)[1];
        header[4] = headerMap.get(LENGTH)[0];
        header[5] = headerMap.get(LENGTH)[1];
        header[6] = UNIT_ID;

        byte[] mbap = new byte[header.length + pdu.length];

        System.arraycopy(header, 0, mbap, 0, header.length);
        System.arraycopy(pdu, 0, mbap, header.length, pdu.length);

        return mbap;
    }

    public byte[] parsePDU(byte[] adu) {
        ModbusFunctionCode functionCode;

        byte[] body = new byte[adu.length - headerMap.size()];
        functionCode = ModbusFunctionCode.fromCode(adu[7]);
        System.arraycopy(adu, 7, body, 0, adu.length - 7);

        switch (functionCode.getCode()) {
            case 3:
                return readInputRegisters(body);
            case 4:
                return readInputRegisters(body);
            case 6:
                return writeSingleRegister(body);
            case 16:
                return writeMultipleRegisters(body);
            default:
                return new byte[0];
        }
    }

    private byte[] readInputRegisters(byte[] pdu) {
        // 3, 4번 코드일떄
        // fc, address, address, qt, qt

        int address = (pdu[1] << 8) | (pdu[2] & 0xFF);
        int quantity = (pdu[3] << 8) | (pdu[4] & 0xFF);
        byte[] resultArray = new byte[quantity * 2];

        System.arraycopy(tempDB, address, resultArray, 0, quantity * 2);

        if (pdu[0] == 0x03) {
            return makeMBAP(makeResponsePDU(3, address, resultArray, quantity));
        } else if (pdu[0] == 0x04) {
            return makeMBAP(makeResponsePDU(4, address, resultArray, quantity));
        } else {
            throw new IllegalArgumentException();
        }
    }

    private byte[] writeSingleRegister(byte[] pdu) {
        // 6번 코드일떄
        // fc, address, address, val, val
        int address = (pdu[1] << 8) | (pdu[2] & 0xFF);

        tempDB[address] = pdu[3];
        tempDB[address + 1] = pdu[4];
        return makeMBAP(makeResponsePDU(6, address, new byte[] { pdu[3], pdu[4] }, 0));
    }

    public byte[] writeMultipleRegisters(byte[] pdu) {
        // fc, address, address, quantity, quantity, byte count, Register values(Nbytes)
        // address : 10101 (0010 0111 0111 0101)-> {16, 39, 117, x, x, x, x ...}

        int address = (pdu[1] << 8) | (pdu[2] & 0xFF);
        int quantity = (pdu[3] << 8) | (pdu[4] & 0xFF);
        int registersByte = (pdu[5] & 0xFF);
        byte[] registersValues = new byte[pdu.length - 6];

        System.arraycopy(pdu, 5, registersValues, 0, pdu.length - 5);

        if (registersByte != registersValues.length || registersByte / 2 != quantity) {
            throw new IllegalArgumentException();
        }
        int j = 0;
        for (int i = address; i < address + quantity * 2; i++) {
            tempDB[i] = registersValues[j++];
        }
        return makeMBAP(makeResponsePDU(16, address, new byte[0], 0));
    }

    public byte[] makeResponsePDU(int fc, int address, byte[] registerValues, int quantity) {
        /*
         * 3-> fc, byteCount, RegisterVal(N*2)
         * 4-> fc, byteCount, inputReg (N*2)
         * 6 -> fc, add, add, regVal, regVal
         * 16 -> fc, sAd, sAd, qt, qt
         */
        byte[] frame;
        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        switch (fc) {
            case 3:
                frame = new byte[2 + registerValues.length];
                frame[0] = 0x03;
                frame[1] = (byte) (quantity * 2);
                System.arraycopy(registerValues, 0, frame, 2, registerValues.length);
                setLength(frame);
                return frame;
            case 4:
                frame = new byte[2 + registerValues.length];
                frame[0] = 0x04;
                frame[1] = (byte) (quantity * 2);
                System.arraycopy(registerValues, 0, frame, 2, registerValues.length);
                setLength(frame);
                return frame;
            case 6:
                frame = new byte[5];
                frame[0] = 0x06;
                b.clear();
                b.putInt(address);
                frame[1] = b.get(2);
                frame[2] = b.get(3);
                frame[3] = registerValues[0];
                frame[4] = registerValues[1];
                setLength(frame);
                return frame;
            case 16:
                frame = new byte[5];
                frame[0] = 0x10;
                b.clear();
                b.putInt(address);
                frame[1] = b.get(2);
                frame[2] = b.get(3);
                b.clear();
                b.putInt(quantity);
                frame[3] = b.get(2);
                frame[4] = b.get(3);
                setLength(frame);
                return frame;
            default:
                throw new IllegalArgumentException();
        }

    }

    public void setLength(byte[] frame) {
        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);

        int length = frame.length + 1;
        b.clear();
        b.putInt(length);
        headerMap.put(LENGTH, new byte[] { b.get(2), b.get(3) });
    }

    public static void main(String[] args) {
        ModbusServerNode server = new ModbusServerNode(11502);
        server.start();
    }

}
