package com.nhnacademy.system;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class OutputLogger {
    private static OutputLogger instance = new OutputLogger();
    private static final String path = "src/main/resources/";
    SimpleDateFormat simpleDateFormatStartTime = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
    SimpleDateFormat simpleDateFormatRunningTime = new SimpleDateFormat("HH:mm:ss");
    File file;
    String transferLogs = "";

    private OutputLogger() {
        super();
        file = new File(path + "transferInfo " + simpleDateFormatStartTime.format(System.currentTimeMillis()) + ".csv");
        transferLogs += "id,수신,송신,에러,시작 시간,동작 시간\n";
        autoSave();
    }

    private void autoSave() {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        FileWriter fileWriter = new FileWriter(file);
                        BufferedWriter br = new BufferedWriter(fileWriter);
                        br.write(transferLogs);
                        br.flush();
                        br.close();
                        Thread.sleep(10000);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

        });
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void write(String id, int receive, int send, int error, Long startTime,
            Long runningTime) {
                long second = runningTime / 1000;
                long minute = second / 60;
                long hour = minute / 60;
                String time = String.format("%02d:%02d:%02d.%04d", hour, minute % 60, second % 60, runningTime % 1000);
        transferLogs += id + "," + receive + "," + send + "," + error + ","
                + simpleDateFormatStartTime.format(startTime) + "," + time
                + "\n";
    }

    public static OutputLogger getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
    }
}
