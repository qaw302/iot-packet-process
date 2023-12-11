package com.nhnacademy.node;

import java.util.ArrayList;
import java.util.List;

import com.nhnacademy.exception.InvalidArgumentException;
import com.nhnacademy.wire.Wire;

public abstract class OutputNode extends ActiveNode {
    private List<Wire> inputPort;

    protected OutputNode(String id) {
        super(id);
        inputPort = new ArrayList<>();
    }

    protected OutputNode() {
        super();
        inputPort = new ArrayList<>();
    }

    public void connectInputWire(Wire wire) {
        if (wire == null) {
            throw new InvalidArgumentException();
        }

        inputPort.add(wire);
    }

    public int getInputWireCount() {
        return inputPort.size();
    }

    public Wire getInputWire(int index) {
        if (index < 0 || index >= getInputWireCount()) {
            throw new InvalidArgumentException();
        }

        return inputPort.get(index);
    }
}