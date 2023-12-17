package com.nhnacademy.system;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegisterAddressMappingTable {
    private JSONObject jsonObject;
    private String path;
    private static Map<String, RegisterAddressMappingTable> registerAddressMappingTableMap = new HashMap<>();

    private RegisterAddressMappingTable(String path) {
        super();
        this.path = path;
        readFile();
        autoSave();
    }

    public static RegisterAddressMappingTable getRegisterAddressMappingTable(String path) {
        if (!registerAddressMappingTableMap.containsKey(path)) {
            RegisterAddressMappingTable registerAddressMappingTable = new RegisterAddressMappingTable(path);
            registerAddressMappingTableMap.put(path, registerAddressMappingTable);
        }
        return registerAddressMappingTableMap.get(path);
    }

    private void autoSave() {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        FileWriter file = new FileWriter(path);
                        file.write(getRegisterAddressMappingTable().toJSONString());
                        file.flush();
                        file.close();
                        Thread.sleep(10000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        });
        thread.setDaemon(true);
        thread.start();
    }

    private void readFile() {
        File file = new File(path);
        if (file.exists()) {
            try {
                JSONParser jsonParser = new JSONParser();
                Reader reader = new FileReader(file);
                jsonObject = (JSONObject) jsonParser.parse(reader);
                reader.close();
            } catch (IOException e) {
                log.info("file read error");
                log.info("create new json file");
                jsonObject = new JSONObject();
                e.printStackTrace();
            } catch (ParseException e) {
                jsonObject = new JSONObject();
                log.info("json format error");
                log.info("create new json file");
            }
        } else {
            jsonObject = new JSONObject();
        }
    }

    public JSONObject getRegisterAddressMappingTable() {
        return jsonObject;
    }

    public boolean hasKey(String key) {
        return jsonObject.containsKey(key);
    }

    public boolean hasAddress(Long num) {
        for (Object k : getRegisterAddressMappingTable().keySet()) {
            if (getRegisterAddressMappingTable().get(k) instanceof Long) {
                if ((long) getRegisterAddressMappingTable().get(k) == num) {
                    return true;
                }
            }
        }
        return false;
    }

    public void writeKey(String key) {
        long maxAddress = 0;
        for (Object k : getRegisterAddressMappingTable().keySet()) {
            if (getRegisterAddressMappingTable().get(k) instanceof Long) {
                if ((long) getRegisterAddressMappingTable().get(k) > maxAddress) {
                    maxAddress = (long) getRegisterAddressMappingTable().get(k);
                }
            }
        }
        maxAddress++;

        getRegisterAddressMappingTable().put(key, maxAddress);
    }

    public Object get(String key) {
        if(!getRegisterAddressMappingTable().containsKey(key)) {
            writeKey(key);
        }
        return getRegisterAddressMappingTable().get(key);
    }
}
