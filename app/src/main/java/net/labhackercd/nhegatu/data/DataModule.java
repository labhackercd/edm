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
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.sqlbrite.SqlBrite;

import net.labhackercd.nhegatu.data.api.TypedService;
import net.labhackercd.nhegatu.data.db.DbModule;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = {DbModule.class}, complete = false)
@SuppressWarnings("UnusedDeclaration")
public class DataModule {
    private static final String PRODUCTION_PORTAL_URL = "https://edemocracia.camara.gov.br";
    private static final String PRODUCTION_API_URL = "/api/secure/jsonws";
    private static final long DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50 MB

    @Provides @Singleton
    Portal providePortal() {
        // TODO We're using upper level Portal here, so we should probably move this up.
        return Portal.Builder.buildStatic(PRODUCTION_PORTAL_URL);
    }

    @Provides @Singleton
    TypedService provideService(Application application, Portal portal) {
        return new TypedService.Builder()
                .setBaseUrl(portal.getUrl().concat(PRODUCTION_API_URL))
                .build();
    }

    @Provides @Singleton
    OkHttpClient provideOkHttpClient(Application application) {
        return createOkHttpClient(application);
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
    ImageLoader provideImageLoader(Portal portal, Picasso picasso) {
        return null;
    }

    @Provides @Singleton
    LocalMessageStore provideLocalMessageRepository(Application application, SqlBrite brite) {
        return new LocalMessageStore(application, brite);
    }

    private static OkHttpClient createOkHttpClient(Application application) {
        OkHttpClient client = new OkHttpClient();

        File cacheDir = new File(application.getCacheDir(), "http");
        Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);
        client.setCache(cache);

        return client;
    }
}
