package net.labhackercd.edemocracia.data.db;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * A Gson TypeAdapter for the {@link Date} type.
 */
public class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        return src == null ? null : new JsonPrimitive(src.getTime());
    }

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull()) {
            return null;
        } else {
            return new Date(json.getAsJsonPrimitive().getAsLong());
        }
    }
}
