package net.labhackercd.edemocracia.data.api.model.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class JSON {
    public static Date getJSONLongAsDate(JSONObject json, String key) throws JSONException {
        if (json.isNull(key)) {
            return null;
        } else {
            return new Date(json.getLong(key));
        }
    }
}
