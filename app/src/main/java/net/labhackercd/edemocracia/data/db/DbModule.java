package net.labhackercd.edemocracia.data.db;

import android.app.Application;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.db.model.LocalMessage;

import java.util.Date;
import java.util.UUID;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.CupboardBuilder;

@Module(library = true, complete = false)
@SuppressWarnings("UnusedDeclaration")
public class DbModule {
    @Provides @Singleton
    Cupboard provideCupboard() {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .create();

        Cupboard cupboard = new CupboardBuilder()
                .registerFieldConverter(UUID.class, new UUIDFieldConverter())
                .registerFieldConverter(Uri.class, new UriFieldConverter())
                .registerFieldConverter(Message.class,
                        new GsonFieldConverter<>(gson, Message.class))
                .build();

        cupboard.register(LocalMessage.class);

        return cupboard;
    }

    @Provides @Singleton
    DatabaseProvider provideDatabaseProvider(Cupboard cupboard, Application application) {
        return new CupboardSQLiteOpenHelper(cupboard, application);
    }
}
