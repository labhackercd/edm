package net.labhackercd.edemocracia.task;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.common.collect.Lists;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.LocalMessageRepository;
import net.labhackercd.edemocracia.data.api.EDMService;
import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.db.LocalMessage;
import net.labhackercd.edemocracia.youtube.Constants;
import net.labhackercd.edemocracia.youtube.ResumableUpload;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import timber.log.Timber;

public class AddMessageTask extends Task {

    private final long messageId;
    private final String youtubeAccount;

    @Inject transient EDMService service;
    @Inject transient Application application;
    @Inject transient LocalMessageRepository messages;
    private transient LocalMessage message;

    public AddMessageTask(long messageId, String youtubeAccount) {
        this.messageId = messageId;
        this.youtubeAccount = youtubeAccount;
    }

    @Override
    protected void execute() throws Throwable {
        LocalMessage message = getMessage();

        // Upload the video attachment, if there is any.
        String body = message.body();
        if (message.videoAttachment() != null) {
            String videoId = uploadVideo(message.videoAttachment());
            body = attachVideo(body, videoId);
        }

        // Publish the message.
        Message inserted = service.addMessage(
                message.uuid(), message.groupId(), message.categoryId(), message.threadId(),
                message.parentMessageId(), message.subject(), body);

        Timber.d("Task succeeded.");
        messages.setSuccess(messageId, inserted);
    }

    @Override
    public boolean shouldRetry(Throwable error) {
        Timber.e(error, "Error while trying to publish message.");
        return false;
    }

    @Override
    public void onCancel(Throwable error) {
        Timber.d("Task cancelled.");
        messages.setCancel(messageId);
    }

    /** Grab the local message from the database. */
    private LocalMessage getMessage() {
        if (message == null)
            message = messages.getMessage(messageId).toBlocking().first();
        return message;
    }

    /** Attach a YouTube video to a message body and returns a the body, with the attached video . */
    private String attachVideo(String body, String videoId) {
        return String
                .format("[center][youtube]%s[/youtube][/center]", videoId)
                .concat(body);
    }

    private String uploadVideo(Uri video) throws FileNotFoundException {
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
                    youtube, fileInputStream, fileSize,
                    video, cursor.getString(column_index), context);
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

