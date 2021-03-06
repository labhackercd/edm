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

package net.labhackercd.nhegatu.service;

import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.util.Pair;
import net.labhackercd.nhegatu.R;
import net.labhackercd.nhegatu.ui.preference.PreferenceFragment;
import net.labhackercd.nhegatu.upload.YouTubeUploader;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class VideoAttachmentUploader {
    private final Context context;
    private final YouTubeUploader uploader;
    private final Object lock = new Object();
    private final Map<Object, Observable<YouTubeUploader.UploadProgress>> uploads = new LinkedHashMap<>();
    private final Map<Object, BehaviorSubject<YouTubeUploader.UploadProgress>> publishers = new WeakHashMap<>();

    VideoAttachmentUploader(Context context, YouTubeUploader uploader) {
        this.context = context;
        this.uploader = uploader;
    }

    public Observable<YouTubeUploader.UploadProgress> upload(long id, Uri videoAttachment, String subject, String topicUrl) {
        // TODO Should probably be wrapped in some local PreferenceManager or something.
        String youtubeAccount = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(PreferenceFragment.PREF_YOUTUBE_ACCOUNT, null);
        
        if (youtubeAccount == null)
            throw new AssertionError("No YouTube account configured.");

        // TODO Shouldn't key also contain the subject and the description?
        final Object key = createKey(id, youtubeAccount);

        String description = context.getResources().getString(R.string.youtube_video_description);
        description = String.format(description, topicUrl);

        synchronized (lock) {
            Observable<YouTubeUploader.UploadProgress> observable = uploads.get(key);
            if (observable == null) {
                observable = uploader.uploadVideo(videoAttachment, youtubeAccount, subject, description)
                        .doOnUnsubscribe(() -> removeUpload(key))
                        .doOnTerminate(() -> removeUpload(key))
                        .share();

                BehaviorSubject<YouTubeUploader.UploadProgress> publisher = publishers.get(key);
                if (publisher != null) {
                    // XXX Shouldn't we keep a reference of the subscription? Damn, this is a huge f*cking mess.
                    observable.subscribe(publisher);
                }

                uploads.put(key, observable);
            }
            return observable;
        }
    }

    public Observable<YouTubeUploader.UploadProgress> getUploadProgressStream(long id, String youtubeAccount) {
        final Object key = createKey(id, youtubeAccount);
        synchronized (lock) {
            BehaviorSubject<YouTubeUploader.UploadProgress> publisher = publishers.get(key);
            if (publisher == null) {
                publisher = BehaviorSubject.create();
                publishers.put(key, publisher);
            }

            Observable<YouTubeUploader.UploadProgress> observable = uploads.get(key);
            if (observable != null) {
                // XXX Shouldn't we keep a reference of the subscription? Damn, this is a huge f*cking mess.
                observable.subscribe(publisher);
            }

            return publisher.asObservable();
        }
    }

    private Object createKey(long id, String youtubeAccount) {
        return new Pair<>(id, youtubeAccount);
    }

    private void removeUpload(Object key) {
        synchronized (lock) {
            uploads.remove(key);
        }
    }
}
