package net.labhackercd.nhegatu.service;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.services.youtube.model.Video;

import net.labhackercd.nhegatu.R;
import net.labhackercd.nhegatu.data.LocalMessageStore;
import net.labhackercd.nhegatu.data.api.EDMService;
import net.labhackercd.nhegatu.data.db.LocalMessage;
import net.labhackercd.nhegatu.data.model.Message;
import net.labhackercd.nhegatu.ui.MainActivity;
import net.labhackercd.nhegatu.upload.YouTubeUploader;

import javax.inject.Inject;

import rx.schedulers.Schedulers;
import timber.log.Timber;

import java.io.IOException;

// TODO Handle errors using RxJava
public class AddMessageTask {

    @Inject EDMService service;
    @Inject YouTubeUploader uploader;
    @Inject Application application;
    @Inject LocalMessageStore messages;
    @Inject VideoAttachmentUploader uploadManager;

    private final LocalMessage message;

    public AddMessageTask(LocalMessage message) {
        this.message = message;
    }

    protected void execute() throws Throwable {
        String body = message.getBody();
        Uri videoAttachment = message.getVideoAttachment();

        if (videoAttachment != null) {
            // TODO FIXME Make this whole operation (upload video, attach video to message, submit message) atomic.
            // TODO FIXME Please. This should be properly configured through injection or something.
            String topicUrl = "https://edemocracia.camara.gov.br"
                    + "/c/message_boards/find_message?p_l_id=&messageId="
                    + String.valueOf(message.getParentMessageId());

            final Video video = uploadManager
                    .upload(message.getId(), message.getVideoAttachment(), message.getSubject(), topicUrl)
                    .subscribeOn(Schedulers.newThread())
                    .doOnNext(this::notifyProgress)
                    .doOnCompleted(this::notifyCompleted)
                    .map(YouTubeUploader.UploadProgress::getInsertedVideo)
                    .doOnError(this::onError)
                    .toBlocking()
                    .last();    // Last emitted item has the inserted video

            if (video == null || video.getId() == null)
                // XXX This is not even supposed to ever happen!
                throw new AssertionError("videoId wasn't supposed to be null.");

            // Save the uploaded video inside the message.
            messages.replaceAttachmentWithYouTubeVideo(message, video.getId());

            // XXX Update body without hitting the db.
            // FIXME We should really cook up a new LocalMessage instance.
            body = LocalMessageStore.attachVideo(body, video.getId());
        }

        // FIXME Publishing messages and marking them as successfully submitted is not atomic!
        Message inserted = service.addMessage(
                message.getUuid(), message.getGroupId(), message.getCategoryId(),
                message.getThreadId(), message.getParentMessageId(), message.getSubject(), body);

        messages.setSuccess(message.getId(), inserted);
    }

    private void notifyProgress(YouTubeUploader.UploadProgress uploadProgress) {
        Context context = application.getApplicationContext();

        NotificationCompat.Builder builder = baseBuilder(context, message)
                .setContentText(context.getString(R.string.sending_message))
                .setOngoing(true);

        // There are basically three states:
        //  1. uploader == null: indeterminate progress, no info.
        //  2. uploader != null AND uploader.getUploadState() in (INITIATION_STARTED, INITIATION_COMPLETE):
        //      indeterminate progress, no info.
        //  3. progress, info.
        double progress = 0;
        String contentInfo = null;
        boolean indeterminate = true;

        MediaHttpUploader uploader = uploadProgress.getMediaHttpUploader();

        if (uploader != null && (
                uploader.getUploadState() == MediaHttpUploader.UploadState.MEDIA_COMPLETE
                || uploader.getUploadState() == MediaHttpUploader.UploadState.MEDIA_IN_PROGRESS)) {
            try {
                progress = uploader.getProgress();
            } catch (IOException e) {
                // Do nothing.
            }
            // TODO Use localized percentage string (I guess NumberFormat does it)
            contentInfo = String.format("%.2f%%", progress * 100);
            indeterminate = false;
        }

        // XXX 1000 to make it smoother than 100.
        final int max = 1000;

        builder.setProgress(max, (int) (progress * max), indeterminate);
        builder.setContentInfo(contentInfo);

        notify(context, builder.build());
    }

    private void notifyCompleted() {
        Context context = application.getApplicationContext();
        NotificationCompat.Builder builder = baseBuilder(context, message);
        builder.setContentText(context.getString(R.string.message_submitted));
        notify(context, builder.build());
    }

    private void notifyError(Throwable error) {
        Context context = application.getApplicationContext();
        NotificationCompat.Builder builder = baseBuilder(context, message);
        builder.setContentText(context.getString(R.string.message_submission_failed));
        notify(context, builder.build());
    }

    private static NotificationCompat.Builder baseBuilder(Context context, LocalMessage message) {
        Intent intent = MainActivity.createIntent(context, message);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_videocam_white_36dp)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    }

    private static void notify(Context context, Notification notification) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(0, notification);
    }

    // TODO Upload the error handling method we're using...
    // ...This code will not catch the errors because they will be wrapped in RxJava exceptions.
    // Now we need to update the whole error handling code!
    public void onError(Throwable error) {
        LocalMessage.Status status = LocalMessage.Status.CANCEL;

        // YEAH :D
        while (error instanceof UserRecoverableAuthIOException) {
            error = ((UserRecoverableAuthIOException) error).getCause();
        }

        if (error instanceof UserRecoverableAuthException) {
            status = LocalMessage.Status.CANCEL_RECOVERABLE_AUTH_ERROR;
            MainActivity.notifyUserRecoverableAuthException(
                    application, message, (UserRecoverableAuthException) error);
        } else {
            Timber.e(error, "Message submission failed.");
            notifyError(error);
        }

        messages.setStatus(message.getId(), status);
    }
}
