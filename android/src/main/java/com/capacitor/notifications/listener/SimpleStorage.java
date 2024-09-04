package com.capacitor.notifications.listener;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.getcapacitor.JSObject;

import org.json.JSONArray;
import org.json.JSONException;
import java.util.Set;

public class SimpleStorage {

    private final SharedPreferences preferences;

    private interface PreferencesOperation {
        void execute(SharedPreferences.Editor editor);
    }

    SimpleStorage(Context context) {
        this.preferences = context.getSharedPreferences("CapacitorStorage", Activity.MODE_PRIVATE);
    }

    public void append(String key, JSObject jsObject) {
        JSONArray currentList;
        try {
            String jsonString = get(key);
            currentList = jsonString != null ? new JSONArray(jsonString) : new JSONArray();
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        currentList.put(jsObject);
        set(key, serializeList(currentList));
    }

    public JSONArray retrieve(String key) {
        String jsonString = get(key);
        return jsonString != null ? deserializeList(jsonString) : null;
    }

    public Number size(String key) {
        String jsonString = get(key);
        return jsonString != null ? deserializeList(jsonString).length() : 0;
    }

    public void remove(String key) {
        executeOperation(editor -> editor.remove(key));
    }

    private String get(String key) {
        return preferences.getString(key, null);
    }

    private void set(String key, String value) {
        executeOperation(editor -> editor.putString(key, value));
    }

    private Set<String> keys() {
        return preferences.getAll().keySet();
    }

    private void clear() {
        executeOperation(SharedPreferences.Editor::clear);
    }

    private void executeOperation(PreferencesOperation op) {
        SharedPreferences.Editor editor = preferences.edit();
        op.execute(editor);
        editor.apply();
    }

    private String serializeList(JSONArray jsonArray) {
        return jsonArray.toString();
    }

    private JSONArray deserializeList(String jsonString) {
        try {
            return new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }
}