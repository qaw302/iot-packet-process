package com.nhnacademy.system;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OutputLogger {
    private static OutputLogger instance = new OutputLogger();
    private static String path = "src/main/resources/";
    File file;
    String transferLogs = "";

    private OutputLogger() {
        super();
        file = new File(path + "transferInfo" + System.currentTimeMillis() + ".txt");
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

    public void write(String id, String receive, String send, String error, String startTime,
            String runningTime) {
        transferLogs += id + "," + receive + "," + send + "," + error + "," + startTime + "," + runningTime
                + "\n";
    }

    public static OutputLogger getInstance() {
        return instance;
    }
}
