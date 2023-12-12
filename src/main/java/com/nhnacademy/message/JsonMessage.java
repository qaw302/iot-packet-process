package com.nhnacademy.message;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nhnacademy.exception.NonJSONObjectTypeException;
import com.nhnacademy.exception.PropertyEmptyException;
import com.nhnacademy.system.UndefinedJsonObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonMessage extends Message {
    JSONObject jsonObject;

    public JsonMessage(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public JSONObject getJsonObject() {
        return getDeepCopyJsonObject(jsonObject);
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    /**
     * JsonMessage의 payload를 반환한다.
     * 
     * @return
     *         payload
     */
    public Object getPayload() {
        if (!jsonObject.containsKey("payload"))
            return new UndefinedJsonObject<>();
        return getJsonObject().get("payload");
    }

    /**
     * JsonMessage의 topic을 반환한다.
     * 
     * @return
     *         topic
     */

    public String getTopic() {
        if (!jsonObject.containsKey("topic"))
            return "Undefined";
        return jsonObject.get("topic").toString();
    }

    /**
     * 입력받은 JSONObject를 깊은 복사하여 반환한다.
     * 
     * @param jsonObject
     *                   깊은 복사할 JSONObject
     * 
     * @return
     *         깊은 복사된 JSONObject
     */
    public static JSONObject getDeepCopyJsonObject(JSONObject jsonObject) {
        JSONParser parser = new JSONParser();
        JSONObject result = new JSONObject();
        Object obj;
        try {
            obj = parser.parse(jsonObject.toString());
            result = (JSONObject) obj;
        } catch (ParseException e) {
            log.error("ParseException", e);
        }
        return result;
    }

    /**
     * 주어진 키를 .으로 구분하여 String[]로 반환한다.
     * 
     * @param keys
     *             .으로 구분된 키들
     * @return
     *         주어진 키를 .으로 구분한 String[]
     * @throws PropertyEmptyException
     *                                keys가 null이거나 empty일 때 발생
     */
    public static String[] splitKeys(String keys) throws PropertyEmptyException {
        if (keys == null) {
            throw new PropertyEmptyException("keys is null");
        }
        if (keys.equals("")) {
            throw new PropertyEmptyException("keys is empty");
        }
        if (!keys.contains(".")) {
            return new String[] { keys };
        }
        return keys.split("\\.");
    }

    /**
     * 주어진 키의 마지막 키를 제외한 JSONObject를 반환
     * 주어진 키가 없으면 UndefinedJsonObject를 반환
     * 
     * @param jsonObject
     *                   시작점이 될 JSONObject
     * @param keys
     *                   순회할 키들
     * @return
     *         주어진 키의 마지막 키를 제외한 JSONObject, 주어진 키가 없으면 UndefinedJsonObject
     */
    public static JSONObject getDestJsonObject(Object jsonObject, String[] keys) {
        if (!(jsonObject instanceof JSONObject)) {
            return new UndefinedJsonObject<>();
        }
        JSONObject destJsonObject = (JSONObject) jsonObject;
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            if (!destJsonObject.containsKey(key)) {
                return new UndefinedJsonObject<>();
            }

            if (i == keys.length - 1) {
                break;
            }

            if (!(destJsonObject.get(key) instanceof JSONObject)) {
                destJsonObject = new UndefinedJsonObject<>();
                break;
            }
            destJsonObject = (JSONObject) destJsonObject.get(key);
        }
        return destJsonObject;
    }

    /**
     * json의 마지막 까지 순회하며 도중에 JSONObject가 아닌 값이 있으면 NonJSONObjectTypeException을
     * 발생시킨다.
     * 마지막 값은 JSONObject가 아니어도 된다.
     * 순회 도중 키가 없으면 다른 Method를 호출해서 남은 키의 경로를 만든다.
     * 
     * @param jsonObject
     *                   순회할 JSONObject의 시작점
     * @param keys
     *                   순회할 키들
     */
    public static void checkJsonDestAvailable(JSONObject jsonObject, String[] keys) throws NonJSONObjectTypeException {
        if (!(jsonObject instanceof JSONObject)) {
            throw new NonJSONObjectTypeException();
        }
        JSONObject destJsonObject = jsonObject;
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            if (!destJsonObject.containsKey(key) || i == keys.length - 1) {
                break;
            }
            if (!(destJsonObject.get(key) instanceof JSONObject)) {
                throw new NonJSONObjectTypeException();
            }
            destJsonObject = (JSONObject) destJsonObject.get(key);
        }
    }

    /**
     * 주어진 JsonObject를 순회하며 도중에 키에 해당하는 값이 없으면 JSONObject를 만들며 순회한다.
     * 
     * @param jsonObject
     *                   순회할 JSONObject의 시작점
     * @param keys
     *                   순회할 키들
     * @return
     *         JSONObject를 주어진 키들로 순회한 마지막 JSONObject
     */
    public static JSONObject getDestWithMakeRootJsonObject(JSONObject jsonObject, String[] keys) {
        JSONObject destJsonObject = jsonObject;
        for (String key : keys) {
            if (key.equals(keys[keys.length - 1])) {
                break;
            }
            if (!destJsonObject.containsKey(key)) {
                destJsonObject.put(key, new JSONObject());
            }
            destJsonObject = (JSONObject) destJsonObject.get(key);
        }
        return destJsonObject;
    }

    /**
     * 주어진 JsonObject를 순회하며 도중에 키에 해당하는 값이 없으면 JSONObject를 만들며 순회한다.
     * 키값을 순회하는 도중 JsonObject가 아닌 값이 있으면 NonJSONObjectTypeException을 발생시킨다.
     * 
     * @param jsonObject
     *                   순회할 JSONObject의 시작점
     * @param keys
     *                   순회할 키들
     * @return
     *         JSONObject를 주어진 키들로 경로를 생성하며 순회한 마지막 JSONObject
     * @throws NonJSONObjectTypeException
     *                                    JsonObject가 아닌 값이 있을 때 발생
     */
    public static JSONObject getDestWithCheckAndMakeJsonObject(JSONObject jsonObject, String[] keys)
            throws NonJSONObjectTypeException {
        checkJsonDestAvailable(jsonObject, keys);
        return getDestWithMakeRootJsonObject(jsonObject, keys);
    }
}
