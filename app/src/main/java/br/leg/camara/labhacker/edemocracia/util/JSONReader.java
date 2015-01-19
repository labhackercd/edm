package br.leg.camara.labhacker.edemocracia.util;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class JSONReader<T> {
    public abstract T fromJSONObject(JSONObject json) throws JSONException;

    public static <T> List<T> fromJSON(JSONArray json, JSONReader<T> reader) throws JSONException {
        List<T> list = new ArrayList<T>();
        for (int i = 0; i < json.length(); i++) {
            list.add(fromJSON(json.getJSONObject(i), reader));
        }
        return list;
    }

    public static <T> T fromJSON(JSONObject json, JSONReader<T> reader) throws JSONException {
        return reader.fromJSONObject(json);
    }
}
