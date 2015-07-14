/*
 * This file is part of Nhegatu, the e-Demoracia Client for Android.
 *
 * Nhegatu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nhegatu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nhegatu.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.labhackercd.nhegatu.data.db;

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
