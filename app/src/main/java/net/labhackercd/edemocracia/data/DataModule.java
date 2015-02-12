package net.labhackercd.edemocracia.data;

import android.app.Application;
import android.net.Uri;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import net.labhackercd.edemocracia.data.api.ApiModule;

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import dagger.Provides;
import timber.log.Timber;

@dagger.Module(
        includes = ApiModule.class,
        complete = false,
        library = true
)
@SuppressWarnings("UnusedDeclaration")
public class DataModule {
    private static final long DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50 MB

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Application app) {
        return createOkHttpClient(app);
    }

    @Provides
    @Singleton
    Picasso providePicasso(Application app, OkHttpClient client) {
        return new Picasso.Builder(app)
                .downloader(new OkHttpDownloader(client))
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        Timber.e(exception, "Failed to load image: %s", uri);
                    }
                })
                .build();
    }

    private static OkHttpClient createOkHttpClient(Application app) {
        OkHttpClient client = new OkHttpClient();

        try {
            File cacheDir = new File(app.getCacheDir(), "http");
            Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);
            client.setCache(cache);
        } catch (IOException e) {
            Timber.e(e, "Unable to install disk cache.");
        }

        return client;
    }
}

