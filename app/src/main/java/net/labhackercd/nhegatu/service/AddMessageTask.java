package net.labhackercd.nhegatu.service;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
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

import rx.functions.Action1;
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
        // Upload the video attachment, if there is any.
        String body = message.getBody();
        Uri videoAttachment = message.getVideoAttachment();

        if (videoAttachment != null) {

            // Notification.
            final NotificationCompat.Builder builder = createNotificationBuilder();

            long fileSize = application.getApplicationContext()
                    .getContentResolver()
                    .openFileDescriptor(videoAttachment, "r")
                    .getStatSize();

            Action1<YouTubeUploader.UploadProgress> notifyProgressRx = (p) -> {
                try {
                    notifyProgress(builder, (int) fileSize, p == null ? null : p.getMediaHttpUploader());
                } catch (IOException e) {
                    throw rx.exceptions.OnErrorThrowable.from(e);
                }
            };

            // The video upload.

            // FIXME Please. This should be properly configured through injection or something.
            String topicUrl = "https://edemocracia.camara.gov.br"
                    + "/c/message_boards/find_message?p_l_id=&messageId="
                    + String.valueOf(message.getParentMessageId());

            final Video video = uploadManager
                    .upload(message.getId(), message.getVideoAttachment(), message.getSubject(), topicUrl)
                    .subscribeOn(Schedulers.newThread())
                    .doOnSubscribe(() -> notifyProgressRx.call(null))
                    .doOnNext(notifyProgressRx)
                    .map(YouTubeUploader.UploadProgress::getInsertedVideo)
                    .toBlocking()
                    .last();    // Last emitted item has the inserted video

            if (video == null)
                // XXX This is not supposed to ever happen!
                throw new AssertionError("video wasn't supposed to be null.");

            body = attachVideo(body, video.getId());
        }

        // Publish the message.
        Message inserted = service.addMessage(
                message.getUuid(), message.getGroupId(), message.getCategoryId(),
                message.getThreadId(), message.getParentMessageId(), message.getSubject(),
                body);

        Timber.d("Message published.");

        messages.setSuccess(message.getId(), inserted);
    }

    private NotificationCompat.Builder createNotificationBuilder() {
        final Context context = application.getApplicationContext();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setContentTitle(context.getString(R.string.youtube_upload))
                .setContentText(context.getString(R.string.youtube_upload_started))
                .setSmallIcon(R.drawable.ic_videocam_black_36dp);

        return builder;
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
            MainActivity.notifyMessageSubmissionFailure(application, message, error);
        }

        messages.setStatus(message.getId(), status);
    }

    /** Attach a YouTube video to a message body and returns it (the body with the attached video). */
    private String attachVideo(String body, String videoId) {
        return String.format("[center][youtube]%s[/youtube][/center]", videoId).concat(body);
    }

    private void notifyProgress(NotificationCompat.Builder builder, int fileSize, @Nullable MediaHttpUploader uploader)
            throws IOException {

        // TODO Most of this can be refactored into a field for improved performance?
        final Context context = application.getApplicationContext();

        final NotificationManager notifyManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        final int UPLOAD_NOTIFICATION_ID = 84913;

        if (uploader == null) {
            builder.setProgress(fileSize, 0, true);
            notifyManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
        } else {
            switch (uploader.getUploadState()) {
                case INITIATION_STARTED:
                    builder.setContentText(context.getString(R.string.initiation_started))
                            .setProgress(fileSize, (int) uploader.getNumBytesUploaded(), true);
                    notifyManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
                    break;
                case INITIATION_COMPLETE:
                    builder.setContentText(context.getString(R.string.initiation_completed))
                            .setProgress(fileSize, (int) uploader.getNumBytesUploaded(), true);
                    notifyManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
                    break;
                case MEDIA_IN_PROGRESS:
                    builder.setContentTitle(context.getString(R.string.youtube_upload)
                            + " " + (int) (uploader.getProgress() * 100) + "%")
                            .setContentText(context.getString(R.string.upload_in_progress))
                            .setProgress(fileSize, (int) uploader.getNumBytesUploaded(), false);
                    notifyManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
                    break;
                case MEDIA_COMPLETE:
                    builder.setContentTitle(context.getString(R.string.yt_upload_completed))
                            .setContentText(context.getString(R.string.upload_completed))
                                    // Removes the progress bar
                            .setProgress(0, 0, false);
                    notifyManager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
                case NOT_STARTED:
                    Timber.d(context.getString(R.string.upload_not_started));
                    break;
            }
        }
    }
}

