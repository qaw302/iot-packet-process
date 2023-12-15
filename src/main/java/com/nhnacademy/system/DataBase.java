package com.nhnacademy.system;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import com.nhnacademy.exception.NullDataBaseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataBase {
    private Object[][] data;
    private int colSize;
    private int rowSize;
    private Object primaryKey;
    private Thread autoSaveThread;
    private static Map<String, DataBase> dataBaseMap = new HashMap<>();
    private String name;

    private DataBase(String name) {
        super();
        this.name = name;
        File file = new File(name);
        if (file.exists()) {
            readFile(file);
        } else {
            data = new Object[1000][100];
            colSize = 0;
            rowSize = 1;
        }

        autoSave();
    }

    private void readFile(File file) {
        try {
            data = new Object[1000][100];
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;
            try {
                while ((line = br.readLine()) != null) {
                    if (line.length() == 0)
                        break;
                    String[] split = line.split(",");
                    for (int i = 0; i < split.length; i++) {
                        data[rowSize][i] = split[i];
                    }
                    rowSize++;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void autoSave() {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        FileWriter fileWriter = new FileWriter(name);
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

    public static DataBase getDataBase(String name) {
        if (!dataBaseMap.containsKey(name)) {
            DataBase dataBase = new DataBase(name);
            dataBaseMap.put(name, dataBase);
        }
        return dataBaseMap.get(name);
    }

    public void addCol(String[] colNames) {
        for (int i = 0; i < colNames.length; i++) {
            Boolean isExist = false;
            for (int j = 0; j < colSize; j++) {
                if (data[0][j].equals(colNames[i])) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                data[0][colSize] = colNames[i];
                colSize++;
            }
        }
    }

    public String[] getCols() {
        String[] result = new String[colSize];
        for (int i = 0; i < colSize; i++) {
            result[i] = data[0][i].toString();
        }
        return result;
    }

    public void setPrimaryKey(Object primaryKey) {
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
}
