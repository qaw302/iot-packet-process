package com.nhnacademy.wire;

import java.util.LinkedList;
import java.util.Queue;

import com.nhnacademy.message.Message;
import com.nhnacademy.node.Node;

public class Wire {
    Queue<Message> messageQueue;
    Node outNode;

    public Wire() {
        super();
        messageQueue = new LinkedList<>();
    }

    public synchronized void put(Message message) {
        messageQueue.add(message);
    }

    public synchronized void unlock() {
        notifyAll();
    }

    public synchronized boolean hasMessage() {
        return !messageQueue.isEmpty();
    }

    public Message get() {
        return messageQueue.poll();
    }

    public void setOutNode(Node outNode) {
        this.outNode = outNode;
    }   
}
