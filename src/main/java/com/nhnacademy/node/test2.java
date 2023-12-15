package com.nhnacademy.node;

public class test2 {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(new Runnable() {
            lock lock = new lock();

            @Override
            public void run() {
                try {
                    lock.locking();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            lock lock = new lock();

            @Override
            public void run() {
                lock.unlock();
            }
        });
        t1.start();
        t2.start();
    }
}

class lock {
    public synchronized void locking() throws InterruptedException {
        System.out.println("lock");
        wait();
        System.out.println("unlock");
    }

    public synchronized void unlock() {
        notifyAll();
    }
}
