package com.kci.utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSonParser {

    public static String getString(JSONObject json, String key) {
        if (json.isNull(key)) return "";
        else return json.optString(key, "").trim();
    }

    public static int getInt(JSONObject json, String key) {
        if (json.isNull(key)) return 0;
        else return Integer.valueOf(json.optString(key, "0"));
    }

    public static String getCurrency(JSONObject json, String key) {
        if (json.isNull(key)) return "$";
        else return json.optString(key, "$");
    }

    public static JSONObject getJSONObject(JSONObject json, String key) {
        if (json.isNull(key)) return new JSONObject();
        else return json.optJSONObject(key);
    }

    public static JSONArray getJSONArray(JSONObject json, String key) {
        if (json.isNull(key)) return new JSONArray();
        else return json.optJSONArray(key);
    }
}
