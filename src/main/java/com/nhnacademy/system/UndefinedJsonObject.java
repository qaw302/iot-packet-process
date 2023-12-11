package com.nhnacademy.system;

import java.lang.reflect.InvocationTargetException;

import org.json.simple.JSONObject;

public class UndefinedJsonObject<V, K> extends JSONObject {


    @Override
    public String toString() {
        return "undefined";
    }

    @Override
    public V get(Object key) {
        return (V) this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            return obj.equals("");
        }
        if (obj instanceof UndefinedJsonObject) {
            return true;
        }
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }
}
