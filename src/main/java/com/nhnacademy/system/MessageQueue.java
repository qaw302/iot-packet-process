package com.nhnacademy.system;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.nhnacademy.message.Message;
import com.nhnacademy.wire.Wire;

public class MessageQueue {
    private Queue<Message> queue;
    private List<Wire> wires;
    private Map<Wire, Thread> wireThreadMap;

    public MessageQueue() {
        super();
        queue = new LinkedList<>();
        wires = new LinkedList<>();
        wireThreadMap = new HashMap<>();
    }

    public synchronized boolean hasMessage() {
        while (queue.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return !queue.isEmpty();
    }

    public synchronized void push(Message message) {
        queue.add(message);
        notifyAll();
    }

    public synchronized Message get() {
        return queue.poll();
    }

    public void addWire(Wire wire) {
        wires.add(wire);
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    if (!wire.hasMessage())
                        continue;
                    push(wire.get());
                }
            }

        });
        wireThreadMap.put(wire, thread);
        thread.start();
    }

    public void removeWire(Wire wire) {
        wireThreadMap.get(wire).interrupt();
        wireThreadMap.remove(wire);
        wires.remove(wire);
    }
}
