package net.labhackercd.edemocracia.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

final class DbOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "edm";
    private static final int VERSION = 8;


    public DbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LocalMessage.CREATE);
        db.execSQL(LocalMessage.CREATE_INDEX_ROOT_MESSAGE_ID);
        db.execSQL(LocalMessage.CREATE_INDEX_INSERTED_MESSAGE_ID);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("PRAGMA writable_schema = 1;");
        db.execSQL("DELETE FROM sqlite_master WHERE type = 'table'");
        db.execSQL("DELETE FROM sqlite_master WHERE type = 'index'");
        db.execSQL("DROP TABLE IF EXISTS " + LocalMessage.TABLE);
        db.execSQL("DROP INDEX IF EXISTS " + LocalMessage.INDEX_INSERTED_MESSAGE_ID);
        db.execSQL("DROP INDEX IF EXISTS " + LocalMessage.INDEX_ROOT_MESSAGE_ID);
        db.execSQL("PRAGMA writable_schema = 0;");
        onCreate(db);
    }
}
