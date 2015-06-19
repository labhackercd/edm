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

package net.labhackercd.nhegatu.data;

import android.app.Application;

import android.content.Context;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.sqlbrite.SqlBrite;

import net.labhackercd.nhegatu.data.api.ApiModule;
import net.labhackercd.nhegatu.data.api.Portal;
import net.labhackercd.nhegatu.data.api.client.EDMService;
import net.labhackercd.nhegatu.data.cache.Cache;
import net.labhackercd.nhegatu.data.cache.LHMCache;
import net.labhackercd.nhegatu.data.cache.UserCache;
import net.labhackercd.nhegatu.data.db.DbModule;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

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
    LHMCache provideRequestCache() {
        return new LHMCache();
    }

    @Provides @Singleton
    Cache provideCache(LHMCache cache) {
        return cache;
    }

    @Provides @Singleton
    UserCache provideUserCache(Application application) {
        return new UserCache(application.getApplicationContext());
    }

    @Provides @Singleton
    MainRepository provideMainRepository(EDMService service) {
        return new MainRepository(service);
    }

    @Provides @Singleton
    Picasso providePicasso(Application application, OkHttpClient client) {
        Context context = application.getApplicationContext();
        return new Picasso.Builder(context)
                .downloader(new OkHttpDownloader(client))
                .addRequestHandler(new ContentThumbnailRequestHandler(context))
                // XXX This is really annoying.
                //.listener((picasso, uri, exception) ->
                //        Timber.w(exception, "Failed to load image: %s", uri))
                .build();
    }

    @Provides @Singleton
    ImageLoader provideImageLoader(Portal portal, Picasso picasso, MainRepository repository, Cache cache) {
        return new ImageLoader(portal, picasso, repository, cache);
    }

    @Provides @Singleton
    LocalMessageStore provideLocalMessageRepository(Application application, SqlBrite brite) {
        return new LocalMessageStore(application, brite);
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
