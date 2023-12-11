package com.nhnacademy.node;

import java.util.ArrayList;
import java.util.List;

import com.nhnacademy.exception.AlreadyExistsException;
import com.nhnacademy.exception.NotExistsException;
import com.nhnacademy.exception.OutOfBoundsException;
import com.nhnacademy.message.Message;
import com.nhnacademy.wire.Wire;

public abstract class InputOutputNode extends ActiveNode {
    List<Wire> inputPort;
    List<Wire>[] outputPort;

    protected InputOutputNode(String id, int outCount) {
        super(id);

        if (outCount < 0) {
            throw new OutOfBoundsException("outCount is out of bounds");
        }

        inputPort = new ArrayList<>();
        outputPort = new ArrayList[outCount];
        for (int i = 0; i < outCount; i++) {
            outputPort[i] = new ArrayList<>();
        }
    }

    protected InputOutputNode(int outCount) {
        super();

        inputPort = new ArrayList<>();
        outputPort = new ArrayList[outCount];
        for (int i = 0; i < outCount; i++) {
            outputPort[i] = new ArrayList<>();
        }
    }

    public void connectOutputWire(int index, Wire wire) {
        if (index < 0 || index >= getOutputCount()) {
            throw new OutOfBoundsException("index is out of bounds");
        }

        if (outputPort[index].contains(wire)) {
            throw new AlreadyExistsException("Wire is already connected");
        }

        if (wire == null) {
            throw new NullPointerException("Wire is null");
        }

        outputPort[index].add(wire);
    }

    public void disconnectOutputWire(int index, Wire wire) {
        if (index < 0 || index >= getOutputCount()) {
            throw new OutOfBoundsException();
        }

        if (!outputPort[index].contains(wire)) {
            throw new NotExistsException("Wire is not connected");
        }

        if (wire == null) {
            throw new NullPointerException("Wire is null");
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

    public void connectInputWire(Wire wire) {
        if (inputPort.contains(wire)) {
            throw new AlreadyExistsException("Wire is already connected");
        }

        if (wire == null) {
            throw new NullPointerException("Wire is null");
        }

        inputPort.add(wire);
    }

    public void disconnectInputWire(Wire wire) {
        if (!inputPort.contains(wire)) {
            throw new NotExistsException("Wire is not connected");
        }

        if (wire == null) {
            throw new NullPointerException("Wire is null");
        }

        inputPort.remove(wire);
    }

    public int getInputWireCount() {
        return inputPort.size();
    }

    public Wire getInputWire(int index) {
        if (index < 0 || index >= getInputWireCount()) {
            throw new OutOfBoundsException("index is out of bounds");
        }

        return inputPort.get(index);
    }

    void output(int index, Message message) {
        for (Wire wire : outputPort[index]) {
            if (wire != null) {
                wire.put(message);
            }
        }
    }
}