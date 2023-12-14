package com.nhnacademy.system;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;

import com.nhnacademy.exception.NullDataBaseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataBase {
    private Object[][] data;
    private int colSize;
    private int rowSize;
    private String primaryKey;
    private Thread autoSaveThread;

    public DataBase() {
        super();
        data = new Object[1000][100];
        colSize = 0;
        rowSize = 1;
        primaryKey = null;
        autoSave();
    }

    private void autoSave() {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        FileWriter fileWriter = new FileWriter("dataBase.csv");
                        BufferedWriter br = new BufferedWriter(fileWriter);
                        for (int i = 0; i < rowSize; i++) {
                            for (int j = 0; j < colSize; j++) {
                                br.write(data[i][j].toString());
                                if (j != colSize - 1)
                                    br.write(",");
                            }
                            br.write(System.lineSeparator());
                        }
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
        autoSaveThread = thread;
        autoSaveThread.setDaemon(true);
        autoSaveThread.start();
    }

    public void addCol(String colName) {
        data[0][colSize] = colName;
        colSize++;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public void addData(JSONObject jsonObject) {
        if (!jsonObject.containsKey(primaryKey)) {
            log.info("primaryKey is not in jsonObject");
            return;
        }
        int defaultKeyIndex = -1;
        for (int i = 0; i < colSize; i++) {
            if (data[0][i].equals(primaryKey)) {
                defaultKeyIndex = i;
                break;
            }
        }
        if (defaultKeyIndex == -1) {
            log.info("primaryKey is not in data");
            return;
        }
        for (int i = 0; i < rowSize; i++) {
            if (data[i][defaultKeyIndex].equals(jsonObject.get(primaryKey))) {
                for (int j = 0; j < colSize; j++) {
                    if (jsonObject.containsKey(data[0][j])) {
                        data[i][j] = jsonObject.get(data[0][j]);
                    }
                }
                return;
            }
            if (i == rowSize - 1) {
                data[rowSize][defaultKeyIndex] = jsonObject.get(primaryKey);
                for (int j = 0; j < colSize; j++) {
                    if (jsonObject.containsKey(data[0][j])) {
                        data[rowSize][j] = jsonObject.get(data[0][j]);
                    }
                }
                rowSize++;
                return;
            }
        }
    }

    public JSONObject getData(String primaryKey) throws NullDataBaseException {
        JSONObject result = new JSONObject();
        int defaultKeyIndex = -1;
        for (int i = 0; i < colSize; i++) {
            if (data[0][i].equals(this.primaryKey)) {
                defaultKeyIndex = i;
                break;
            }
        }
        if (defaultKeyIndex == -1) {
            log.info("primaryKey is not in data");
            throw new NullDataBaseException();
        }
        for (int i = 1; i < rowSize; i++) {
            if (data[i][defaultKeyIndex].equals(primaryKey)) {
                for (int j = 0; j < colSize; j++) {
                    result.put(data[0][j], data[i][j]);
                }
                return result;
            }
        }
        throw new NullDataBaseException();
    }

    public static void main(String[] args) throws InterruptedException {
        DataBase dataBase = new DataBase();
        dataBase.addCol("branch");
        dataBase.addCol("site");
        dataBase.addCol("place");
        dataBase.addCol("devEui");
        dataBase.addCol("sensor");
        dataBase.addCol("address");
        dataBase.addCol("value");
        dataBase.setPrimaryKey("devEui");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("branch", "gyeongnam");
        jsonObject.put("site", "nhnacademy");
        jsonObject.put("place", "class_a");
        jsonObject.put("devEui", "af87dsaf8sda876");
        jsonObject.put("sensor", "temperature");
        jsonObject.put("address", "11101");
        jsonObject.put("value", 25.5);
        dataBase.addData(jsonObject);
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("branch", "gyeongnam");
        jsonObject2.put("site", "nhnacademy");
        jsonObject2.put("place", "class_b");
        jsonObject2.put("devEui", "bsefs352");
        jsonObject2.put("sensor", "co2");
        jsonObject2.put("address", "11203");
        jsonObject2.put("value", 500);
        dataBase.addData(jsonObject2);
        System.out.println(dataBase.getData("af87dsaf8sda876"));
        System.out.println(dataBase.getData("bsefs352"));
        Thread.sleep(20000);
    }

}
