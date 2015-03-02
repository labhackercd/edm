package net.labhackercd.edemocracia.data.db;

import android.database.sqlite.SQLiteDatabase;

import nl.qbusict.cupboard.Cupboard;

public interface DatabaseProvider {
    public Cupboard getCupboard();
    public SQLiteDatabase getWritableDatabase();
}
