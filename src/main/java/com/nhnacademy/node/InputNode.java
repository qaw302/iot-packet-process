package com.nhnacademy.node;

import java.util.ArrayList;
import java.util.List;

import com.nhnacademy.exception.AlreadyExistsException;
import com.nhnacademy.exception.InvalidArgumentException;
import com.nhnacademy.exception.NotExistsException;
import com.nhnacademy.exception.OutOfBoundsException;
import com.nhnacademy.message.Message;
import com.nhnacademy.wire.Wire;

public abstract class InputNode extends ActiveNode {
    List<Wire>[] outputPort;

    protected InputNode(String id, int outCount) {
        super(id);

        if (outCount < 0) {
            throw new OutOfBoundsException("outCount is out of bounds");
        }

        outputPort = new ArrayList[outCount];
        for (int i = 0; i < outCount; i++) {
            outputPort[i] = new ArrayList<>();
        }
    }

    protected InputNode(int outCount) {
        super();

        if (outCount < 0) {
            throw new OutOfBoundsException("outCount is out of bounds");
        }

        outputPort = new ArrayList[outCount];
        for (int i = 0; i < outCount; i++) {
            outputPort[i] = new ArrayList<>();
        }
    }

    public void connectOutputWire(int index, Wire wire) {
        if (index < 0 || index >= getOutputCount()) {
            throw new OutOfBoundsException("index is out of bounds");
        }

        if (wire == null) {
            throw new InvalidArgumentException("Wire is null");
        }

        if (outputPort[index].contains(wire)) {
            throw new AlreadyExistsException("Wire is already connected");
        }

        outputPort[index].add(wire);
    }

    public void disconnectOutputWire(int index, Wire wire) {
        if (index < 0 || index >= getOutputCount()) {
            throw new OutOfBoundsException("index is out of bounds");
        }

        if (wire == null) {
            throw new InvalidArgumentException("Wire is null");
        }

        if (!outputPort[index].contains(wire)) {
            throw new NotExistsException("Wire is not connected");
        }

        outputPort[index].remove(wire);
    }

    public int getOutputCount() {
        return outputPort.length;
    }

    public int getOutputWireCount(int index) {
        if (index < 0 || index >= getOutputCount()) {
            throw new OutOfBoundsException("index is out of bounds");
        }

        return outputPort[index].size();
    }

    void output(Message message, int index) {
        for (Wire wire : outputPort[index]) {
            if (wire != null) {
                wire.put(message);
            }
        }
    }

}