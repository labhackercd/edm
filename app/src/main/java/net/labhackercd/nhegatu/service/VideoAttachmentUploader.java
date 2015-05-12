package net.labhackercd.nhegatu.service;

import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.util.Pair;
import net.labhackercd.nhegatu.R;
import net.labhackercd.nhegatu.ui.preference.PreferenceFragment;
import net.labhackercd.nhegatu.upload.YouTubeUploader;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class VideoAttachmentUploader {
    private final Context context;
    private final YouTubeUploader uploader;
    private final Object lock = new Object();
    private final Map<Object, Observable<YouTubeUploader.UploadProgress>> uploads = new LinkedHashMap<>();
    private final Map<Object, PublishSubject<YouTubeUploader.UploadProgress>> publishers = new WeakHashMap<>();

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

                PublishSubject<YouTubeUploader.UploadProgress> publisher = publishers.get(key);
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
            PublishSubject<YouTubeUploader.UploadProgress> publisher = publishers.get(key);
            if (publisher == null) {
                publisher = PublishSubject.create();
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
