package net.labhackercd.edemocracia.data;

import android.app.Application;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.sqlbrite.SqlBrite;

import net.labhackercd.edemocracia.data.api.ApiModule;
import net.labhackercd.edemocracia.data.api.EDMService;
import net.labhackercd.edemocracia.data.api.Portal;
import net.labhackercd.edemocracia.data.db.DbModule;
import net.labhackercd.edemocracia.task.TaskManager;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

@Module(
        library = true,
        complete = false,
        includes = {DbModule.class, ApiModule.class}
)
@SuppressWarnings("UnusedDeclaration")
public class DataModule {
    private static final long DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50 MB

    @Provides @Singleton
    OkHttpClient provideOkHttpClient(Application application) {
        return createOkHttpClient(application);
    }

    @Provides @Singleton
    Cache provideRequestCache() {
        return new LHMCache();
    }

    @Provides @Singleton
    MainRepository provideMainRepository(EDMService service) {
        return new MainRepository(service);
    }

    @Provides @Singleton
    Picasso providePicasso(Application application, OkHttpClient client) {
        return new Picasso.Builder(application)
                .downloader(new OkHttpDownloader(client))
                .listener((picasso, uri, exception) ->
                        Timber.w(exception, "Failed to load image: %s", uri))
                .build();
    }

    @Provides @Singleton
    ImageLoader provideImageLoader(Portal portal, Picasso picasso) {
        return new ImageLoader(portal, picasso);
    }

    @Provides @Singleton
    LocalMessageRepository provideLocalMessageRepository(TaskManager taskManager, SqlBrite brite) {
        return new LocalMessageRepository(taskManager, brite);
    }

    private static OkHttpClient createOkHttpClient(Application application) {
        OkHttpClient client = new OkHttpClient();

        File cacheDir = new File(application.getCacheDir(), "http");
        com.squareup.okhttp.Cache cache =
                new com.squareup.okhttp.Cache(cacheDir, DISK_CACHE_SIZE);
        client.setCache(cache);

        return client;
    }
}
