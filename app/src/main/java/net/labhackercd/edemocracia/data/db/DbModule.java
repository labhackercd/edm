package net.labhackercd.edemocracia.data.db;

import android.app.Application;
import android.database.sqlite.SQLiteOpenHelper;

import com.squareup.sqlbrite.SqlBrite;

import net.labhackercd.edemocracia.EDMApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(library = true, complete = false, injects= EDMApplication.class)
@SuppressWarnings("UnusedDeclaration")
public class DbModule {
    @Provides @Singleton
    SQLiteOpenHelper provideSQLiteOpenHelper(Application application) {
        return new DbOpenHelper(application);
    }

    @Provides @Singleton
    SqlBrite provideSqlBrite(SQLiteOpenHelper helper) {
        return SqlBrite.create(helper);
    }
}
