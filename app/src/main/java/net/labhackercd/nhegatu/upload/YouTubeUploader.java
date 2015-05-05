package net.labhackercd.nhegatu.upload;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.common.collect.Lists;
import net.labhackercd.nhegatu.R;
import rx.Observable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class YouTubeUploader {

    public interface UploadProgress {
        Video getInsertedVideo();
        MediaHttpUploader getMediaHttpUploader();
    }

    private final Context context;

    YouTubeUploader(Context context) {
        this.context = context;
    }

    /**
     * Returns an {@link Observable} that will upload the video when subscribed to.
     *
     * It will emit a bunch of {@link UploadProgress} reporting the upload progress, and then
     * will emit a {@link UploadProgress} when the upload is completed.
     *
     * Note that the returned observable will start a new upload process every time it's
     * subscribed to, so it's heavily advised to use {@link Observable::share} on it.
     *
     * The observable can call onError with {@link java.io.FileNotFoundException} in case the videoUri is
     * not available; with a {@link IOException} if something goes wrong while uploading the video.
     */
    public Observable<UploadProgress> uploadVideo(Uri videoUri, String youtubeAccount, String title, String description) {
        return Observable.<UploadProgress>create(f -> {
            // TODO FIXME Move youtubeAccount out of here. It should be an implementation detail or at least a ...
            // class property or something, since it isn't suppose to change a lot between calls. It is only here
            // as parameter because I'm lazy to code all the getters and setters and observables.
            GoogleAccountCredential credential = GoogleAccountCredential
                    .usingOAuth2(context, Lists.newArrayList(Constants.AUTH_SCOPES));
            credential.setSelectedAccountName(youtubeAccount);
            credential.setBackOff(new ExponentialBackOff());

            final HttpTransport transport = AndroidHttp.newCompatibleTransport();
            final GsonFactory jsonFactory = new GsonFactory();
            final String appName = context.getResources().getString(R.string.app_name);

            YouTube youtube = new YouTube.Builder(transport, jsonFactory, credential)
                    .setApplicationName(appName)
                    .build();

            ContentResolver contentResolver = context.getContentResolver();

            final Video video;
            InputStream fileInputStream = null;
            try {
                fileInputStream = contentResolver.openInputStream(videoUri);

                long fileSize = contentResolver.openFileDescriptor(videoUri, "r").getStatSize();

                InputStreamContent mediaContent =
                        new InputStreamContent("video/*", new BufferedInputStream(fileInputStream));
                mediaContent.setLength(fileSize);

                YouTube.Videos.Insert videoInsert = createVideoInsert(youtube, mediaContent, title, description);

                videoInsert.getMediaHttpUploader().setProgressListener(uploader -> {
                    if (!f.isUnsubscribed())
                        f.onNext(new UploadProgress() {
                            @Override
                            public Video getInsertedVideo() {
                                return null;
                            }

                            @Override
                            public MediaHttpUploader getMediaHttpUploader() {
                                return uploader;
                            }
                        });
                });

                // XXX Check the subscription again to avoid inserting the video if not necessary.
                if (f.isUnsubscribed())
                    return;

                video = videoInsert.execute();

                if (!f.isUnsubscribed())
                    f.onNext(new UploadProgress() {
                        @Override
                        public Video getInsertedVideo() {
                            return video;
                        }

                        @Override
                        public MediaHttpUploader getMediaHttpUploader() {
                            return videoInsert.getMediaHttpUploader();
                        }
                    });

                // XXX Checking it again just in case someone unsubscribed inside onNext
                if (!f.isUnsubscribed())
                    f.onCompleted();

            } catch (Throwable t) {
                if (!f.isUnsubscribed())
                    f.onError(t);
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        });
    }

    private YouTube.Videos.Insert createVideoInsert(YouTube youtube, AbstractInputStreamContent mediaContent,
                                                    String title, String description) throws IOException {
        // Add extra information to the video before uploading.
        Video videoObjectDefiningMetadata = new Video();

        /**
         * Set the video to public, so it is available to everyone (what most people want). This is
         * actually the default, but I wanted you to see what it looked like in case you need to set
         * it to "unlisted" or "private" via API.
         */
        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus("public");
        videoObjectDefiningMetadata.setStatus(status);

        // We set a majority of the metadata with the VideoSnippet object.
        VideoSnippet snippet = new VideoSnippet();

        snippet.setTitle(title);
        snippet.setDescription(description);

        // Set completed snippet to the video object.
        videoObjectDefiningMetadata.setSnippet(snippet);

        /**
         * The upload command includes: 1. Information we want returned after file is successfully
         * uploaded. 2. Metadata we want associated with the uploaded video. 3. Video file itself.
         */
        YouTube.Videos.Insert videoInsert = youtube.videos()
                .insert("snippet,statistics,status", videoObjectDefiningMetadata, mediaContent);

        // Set the upload type and add event listener.
        MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();

        /**
         * Sets whether direct media upload is enabled or disabled. True = whole media content is
         * uploaded in a single request. False (default) = resumable media upload protocol to upload
         * in data chunks.
         */
        uploader.setDirectUploadEnabled(false);

        return videoInsert;
    }
}
