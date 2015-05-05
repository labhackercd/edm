package net.labhackercd.nhegatu.service;

import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.util.Pair;
import net.labhackercd.nhegatu.R;
import net.labhackercd.nhegatu.ui.preference.PreferenceFragment;
import net.labhackercd.nhegatu.upload.YouTubeUploader;
import rx.Observable;

import java.util.LinkedHashMap;
import java.util.Map;

public final class VideoAttachmentUploader {
    private final Context context;
    private final YouTubeUploader uploader;
    private final Object lock = new Object();
    private final Map<Object, Observable<YouTubeUploader.UploadProgress>> uploads = new LinkedHashMap<>();

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
        final Object key = new Pair<>(id, youtubeAccount);

        String description = context.getResources().getString(R.string.youtube_video_description);
        description = String.format(description, topicUrl);

        synchronized (lock) {
            Observable<YouTubeUploader.UploadProgress> observable = uploads.get(key);
            if (observable == null) {
                observable = uploader.uploadVideo(videoAttachment, youtubeAccount, subject, description)
                        .doOnUnsubscribe(() -> removeUpload(key))
                        .doOnTerminate(() -> removeUpload(key))
                        .share();
                uploads.put(key, observable);
            }
            return observable;
        }
    }

    private void removeUpload(Object key) {
        synchronized (lock) {
            uploads.remove(key);
        }
    }
}
