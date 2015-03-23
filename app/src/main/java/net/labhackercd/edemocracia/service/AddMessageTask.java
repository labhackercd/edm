package net.labhackercd.edemocracia.service;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.common.collect.Lists;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.LocalMessageStore;
import net.labhackercd.edemocracia.data.api.EDMService;
import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.db.LocalMessage;
import net.labhackercd.edemocracia.ui.MainActivity;
import net.labhackercd.edemocracia.ui.preference.PreferenceFragment;
import net.labhackercd.edemocracia.youtube.Constants;
import net.labhackercd.edemocracia.youtube.ResumableUpload;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import timber.log.Timber;

public class AddMessageTask {

    @Inject EDMService service;
    @Inject Application application;
    @Inject
    LocalMessageStore messages;

    private final LocalMessage message;

    public AddMessageTask(LocalMessage message) {
        this.message = message;
    }

    protected void execute() throws Throwable {
        // Upload the video attachment, if there is any.
        Uri videoAttachment = message.videoAttachment();
        String body = message.body();
        if (videoAttachment != null) {
            // TODO Should probably be wrapped in some local PreferenceManager or something.
            String account = PreferenceManager
                    .getDefaultSharedPreferences(application)
                    .getString(PreferenceFragment.PREF_YOUTUBE_ACCOUNT, null);
            if (account == null)
                throw new AssertionError("No YouTube account configured.");

            // FIXME Please.
            String url = "https://edemocracia.camara.gov.br"
                    + "/c/message_boards/find_message?p_l_id=&messageId="
                    + String.valueOf(message.parentMessageId());

            String description = application.getResources()
                    .getString(R.string.youtube_video_description);
            description = String.format(description, url);

            String videoId = uploadVideo(
                    message.videoAttachment(), account, message.subject(), description);
            if (TextUtils.isEmpty(videoId) || "null".equals(videoId))
                throw new AssertionError("videoId is empty or null");
            body = attachVideo(body, videoId);
        }

        // Publish the message.
        Message inserted = service.addMessage(
                message.uuid(), message.groupId(), message.categoryId(), message.threadId(),
                message.parentMessageId(), message.subject(), body);

        Timber.d("Message published.");

        messages.setSuccess(message.id(), inserted);
    }

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

        messages.setStatus(message.id(), status);
    }

    /** Attach a YouTube video to a message body and returns a the body, with the attached video . */
    private String attachVideo(String body, String videoId) {
        return String
                .format("[center][youtube]%s[/youtube][/center]", videoId)
                .concat(body);
    }

    private String uploadVideo(Uri video, String youtubeAccount, String title, String description)
            throws UserRecoverableAuthException, IOException {
        Context context = application.getApplicationContext();

        GoogleAccountCredential credential = GoogleAccountCredential
                .usingOAuth2(context, Lists.newArrayList(Constants.AUTH_SCOPES));
        credential.setSelectedAccountName(youtubeAccount);
        credential.setBackOff(new ExponentialBackOff());

        String appName = context.getResources().getString(R.string.app_name);

        final HttpTransport transport = AndroidHttp.newCompatibleTransport();
        final JsonFactory jsonFactory = new GsonFactory();

        YouTube youtube = new YouTube.Builder(transport, jsonFactory, credential)
                .setApplicationName(appName).build();

        ContentResolver contentResolver = context.getContentResolver();

        InputStream fileInputStream = null;
        try {
            long fileSize = contentResolver.openFileDescriptor(video, "r").getStatSize();
            fileInputStream = contentResolver.openInputStream(video);
            String[] projection = {MediaStore.Images.Media.DATA};

            Cursor cursor = contentResolver.query(video, projection, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            return ResumableUpload.upload(
                    youtube, fileInputStream, fileSize, context, title, description);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}

