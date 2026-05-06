package com.honeywell.aidc;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes3.dex */
class JsonUtil {
    JsonUtil() {
    }

    static JSONArray arrayToJson(Object obj) throws JSONException {
        if (!obj.getClass().isArray()) {
            throw new JSONException("Not a primitive data: " + obj.getClass());
        }
        int length = Array.getLength(obj);
        JSONArray jSONArray = new JSONArray();
        for (int i = 0; i < length; i++) {
            jSONArray.put(wrap(Array.get(obj, i)));
        }
        return jSONArray;
    }

    static JSONArray collectionToJson(Collection<?> collection) {
        JSONArray jSONArray = new JSONArray();
        if (collection != null) {
            Iterator<?> it = collection.iterator();
            while (it.hasNext()) {
                jSONArray.put(wrap(it.next()));
            }
        }
        return jSONArray;
    }

    static JSONObject mapToJson(Map<?, ?> map) {
        JSONObject jSONObject = new JSONObject();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String str = (String) entry.getKey();
            if (str == null) {
                throw new NullPointerException("key == null");
            }
            try {
                jSONObject.put(str, wrap(entry.getValue()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jSONObject;
    }

    private static Object wrap(Object obj) {
        if (obj == null) {
            return null;
        }
        if ((obj instanceof JSONArray) || (obj instanceof JSONObject)) {
            return obj;
        }
        try {
            if (obj instanceof Collection) {
                return collectionToJson((Collection) obj);
            }
            if (obj.getClass().isArray()) {
                return arrayToJson(obj);
            }
            if (obj instanceof Map) {
                return mapToJson((Map) obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ((obj instanceof Boolean) || (obj instanceof Byte) || (obj instanceof Character) || (obj instanceof Double) || (obj instanceof Float) || (obj instanceof Integer) || (obj instanceof Long) || (obj instanceof Short) || (obj instanceof String)) {
            return obj;
        }
        if (obj.getClass().getPackage().getName().startsWith("java.")) {
            return obj.toString();
        }
        return null;
    }
}
