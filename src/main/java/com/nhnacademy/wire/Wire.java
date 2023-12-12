package com.nhnacademy.wire;

import java.util.LinkedList;
import java.util.Queue;

import com.nhnacademy.message.Message;


public class Wire {
    Queue<Message> messageQueue;

    public Wire() {
        super();
        messageQueue = new LinkedList<>();
    }

    public synchronized void put(Message message) {
        messageQueue.add(message);
        notifyAll();
    }

    public synchronized boolean hasMessage() throws InterruptedException {
        while (true) {
            if (messageQueue.isEmpty()) {
                wait();
            } else {
                break;
            }
        }
        return !messageQueue.isEmpty();
    }

    public Message get() {
        return messageQueue.poll();
    }
}
