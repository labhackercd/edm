package net.labhackercd.edemocracia.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import nl.qbusict.cupboard.Cupboard;

public class CupboardSQLiteOpenHelper extends SQLiteOpenHelper implements DatabaseProvider {

    private static final int DATABASE_VERSION = 3;

    // XXX: Must be "job_holder" because of Path's priority-jobqueue.
    private static final String DATABASE_NAME = "job_holder";

    private final Cupboard cupboard;

    public CupboardSQLiteOpenHelper(Cupboard cupboard, Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.cupboard = cupboard;
    }

    public Cupboard getCupboard() {
        return cupboard;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        cupboard.withDatabase(db).createTables();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        cupboard.withDatabase(db).upgradeTables();
    }
}
