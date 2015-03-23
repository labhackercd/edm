package net.labhackercd.edemocracia.youtube;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;

import net.labhackercd.edemocracia.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ibrahim Ulukaya <ulukaya@google.com>
 *         <p/>
 *         YouTube Resumable Upload controller class.
 */
public class ResumableUpload {
    private static final String TAG = "UploadingActivity";

    private static final int UPLOAD_NOTIFICATION_ID = 1001;
    private static final String VIDEO_FILE_FORMAT = "video/*";

    /**
     * Uploads user selected video in the project folder to the user's YouTube account using OAuth2
     * for authentication.
     */
    public static String upload(YouTube youtube, final InputStream fileInputStream,
                                final long fileSize, final Context context,
                                String title, String description)
            throws UserRecoverableAuthException, IOException {
        final NotificationManager notifyManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setContentTitle(context.getString(R.string.youtube_upload))
                .setContentText(context.getString(R.string.youtube_upload_started))
                .setSmallIcon(R.drawable.ic_videocam_black_36dp);

        notifyManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());

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

        InputStreamContent mediaContent =
                new InputStreamContent(VIDEO_FILE_FORMAT, new BufferedInputStream(fileInputStream));
        mediaContent.setLength(fileSize);

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

        /** Control progress. */
        // It starts as an indeterminate progress.
        builder.setProgress((int) fileSize, (int) uploader.getNumBytesUploaded(), true);

        MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
            public void progressChanged(MediaHttpUploader uploader) throws IOException {
                switch (uploader.getUploadState()) {
                    case INITIATION_STARTED:
                        builder.setContentText(context.getString(R.string.initiation_started))
                                .setProgress((int) fileSize, (int) uploader.getNumBytesUploaded(), true);
                        notifyManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
                        break;
                    case INITIATION_COMPLETE:
                        builder.setContentText(context.getString(R.string.initiation_completed))
                                .setProgress((int) fileSize, (int) uploader.getNumBytesUploaded(), true);
                        notifyManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
                        break;
                    case MEDIA_IN_PROGRESS:
                        builder.setContentTitle(context.getString(R.string.youtube_upload)
                                + " " + (int) (uploader.getProgress() * 100) + "%")
                                .setContentText(context.getString(R.string.upload_in_progress))
                                .setProgress((int) fileSize, (int) uploader.getNumBytesUploaded(), false);
                        notifyManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
                        break;
                    case MEDIA_COMPLETE:
                        builder.setContentTitle(context.getString(R.string.yt_upload_completed))
                                .setContentText(context.getString(R.string.upload_completed))
                                        // Removes the progress bar
                                .setProgress(0, 0, false);
                        notifyManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
                    case NOT_STARTED:
                        Log.d(this.getClass().getSimpleName(), context.getString(R.string.upload_not_started));
                        break;
                }
            }
        };
        uploader.setProgressListener(progressListener);

        // Execute upload.
        Video returnedVideo = videoInsert.execute();

        return returnedVideo.getId();
    }

    private static void requestAuth(Context context,
                                    UserRecoverableAuthIOException userRecoverableException) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        Intent authIntent = userRecoverableException.getIntent();
        Intent runReqAuthIntent = new Intent(Constants.REQUEST_AUTHORIZATION_INTENT);
        runReqAuthIntent.putExtra(Constants.REQUEST_AUTHORIZATION_INTENT_PARAM, authIntent);
        manager.sendBroadcast(runReqAuthIntent);
        Log.d(TAG, String.format("Sent broadcast %s", Constants.REQUEST_AUTHORIZATION_INTENT));
    }

    private static void notifyFailedUpload(Context context, String message, NotificationManager notifyManager,
                                           NotificationCompat.Builder builder) {
        builder.setContentTitle(context.getString(R.string.yt_upload_failed))
                .setContentText(message);
        notifyManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
        Log.e(ResumableUpload.class.getSimpleName(), message);
    }
}
