package com.nhnacademy.node;

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

public class RegisterAddressMappingTable {
    private JSONObject jsonObject;
    private String path;
    private static Map<String, RegisterAddressMappingTable> registerAddressMappingTableMap = new HashMap<>();

    private RegisterAddressMappingTable() {
        super();
    }

    public static RegisterAddressMappingTable getRegisterAddressMappingTable(String path) {
        if (!registerAddressMappingTableMap.containsKey(path)) {
            RegisterAddressMappingTable registerAddressMappingTable = new RegisterAddressMappingTable();
            registerAddressMappingTable.path = path;
            registerAddressMappingTable.readFile();
            registerAddressMappingTableMap.put(path, registerAddressMappingTable);
        }
        return registerAddressMappingTableMap.get(path);
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
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
        try {
            FileWriter file = new FileWriter(path);
            file.write(getRegisterAddressMappingTable().toJSONString());
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        readFile();
    }
}
