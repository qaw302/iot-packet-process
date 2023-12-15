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

    public synchronized boolean hasMessage() {
        while (messageQueue.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return !messageQueue.isEmpty();
    }

    public Message get() {
        return messageQueue.poll();
    }
}
