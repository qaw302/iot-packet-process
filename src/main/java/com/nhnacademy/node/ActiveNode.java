package com.nhnacademy.node;

/**
 * ActiveNode는 스레드로 구현 됨 노드와 스레드를 구현(Extends, Implements)
 * constructor 는 스레드 생성, 이름 지정.
 */
public abstract class ActiveNode extends Node implements Runnable {
    Thread thread;
    boolean running;
    long interval = 2000;

    ActiveNode() {
        super();
        thread = new Thread(this, getId());
        running = false;
    }

    ActiveNode(String id) {
        super(id);
        thread = new Thread(this, getId());
        running = false;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        running = false;
        thread.interrupt();
    }

    abstract void preprocess();

    abstract void process();

    abstract void postprocess();

    @Override
    public void run() {
        preprocess();

        running = true;

        long startTime = System.currentTimeMillis();
        long previousTime = startTime;

        while (running) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - previousTime;

            if (elapsedTime < interval) {
                try {
                    process();
                    Thread.sleep(interval - elapsedTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            previousTime = startTime + (System.currentTimeMillis() - startTime) / interval * interval;
        }
        postprocess();
    }
}
