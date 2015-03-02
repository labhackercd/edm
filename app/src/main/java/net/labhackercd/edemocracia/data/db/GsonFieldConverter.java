package net.labhackercd.edemocracia.data.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.Gson;

import java.lang.reflect.Type;

import nl.qbusict.cupboard.convert.EntityConverter;
import nl.qbusict.cupboard.convert.FieldConverter;

public class GsonFieldConverter<T> implements FieldConverter<T> {
    private final Gson gson;
    private final Type type;

    public GsonFieldConverter(Gson gson, Type type) {
        this.gson = gson;
        this.type = type;
    }

    @Override
    public T fromCursorValue(Cursor cursor, int i) {
        return gson.fromJson(cursor.getString(i), type);
    }

    @Override
    public void toContentValue(T value, String key, ContentValues contentValues) {
        contentValues.put(key, gson.toJson(value));
    }

    @Override
    public EntityConverter.ColumnType getColumnType() {
        return EntityConverter.ColumnType.TEXT;
    }
}
