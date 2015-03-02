package net.labhackercd.edemocracia.data.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import nl.qbusict.cupboard.convert.EntityConverter;
import nl.qbusict.cupboard.convert.FieldConverter;

public class UriFieldConverter implements FieldConverter<Uri> {
    @Override
    public Uri fromCursorValue(Cursor cursor, int i) {
        return Uri.parse(cursor.getString(i));
    }

    @Override
    public void toContentValue(Uri uri, String key, ContentValues contentValues) {
        contentValues.put(key, uri.toString());
    }

    @Override
    public EntityConverter.ColumnType getColumnType() {
        return EntityConverter.ColumnType.TEXT;
    }
}
