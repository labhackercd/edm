package net.labhackercd.edemocracia.data.db;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.UUID;

import nl.qbusict.cupboard.convert.EntityConverter;
import nl.qbusict.cupboard.convert.FieldConverter;

public class UUIDFieldConverter implements FieldConverter<UUID> {
    @Override
    public UUID fromCursorValue(Cursor cursor, int i) {
        return UUID.fromString(cursor.getString(i));
    }

    @Override
    public void toContentValue(UUID uuid, String key, ContentValues contentValues) {
        contentValues.put(key, uuid.toString());
    }

    @Override
    public EntityConverter.ColumnType getColumnType() {
        return EntityConverter.ColumnType.TEXT;
    }
}
