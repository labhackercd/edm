package net.labhackercd.edemocracia.job;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
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
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.api.EDMService;
import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.db.DatabaseProvider;
import net.labhackercd.edemocracia.data.db.model.LocalMessage;
import net.labhackercd.edemocracia.youtube.Constants;
import net.labhackercd.edemocracia.youtube.ResumableUpload;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import timber.log.Timber;

public class AddMessageJob extends Job {

    /**
     * Posted into the EventBus when the job is successfully executed.
     */
    public static class Success {
        /** The id of the LocalMessage which was submitted. */
        public final long localId;

        /** The actual inserted message. */
        public final Message inserted;

        public Success(long localId, Message inserted) {
            this.localId = localId;
            this.inserted = inserted;
        }
    }

    /**
     * Posted into the EventBus when a job is cancelled.
     */
    public class Cancelled {
        public final LocalMessage message;

        public Cancelled(LocalMessage message) {
            this.message = message;
        }
    }

    private static final int PRIORITY = 1;

    private final long messageId;
    private final String youtubeAccount;
    private LocalMessage localMessage;

    AddMessageJob(long messageId, String youtubeAccount) {
        super(new Params(PRIORITY).requireNetwork().persist());
        this.messageId = messageId;
        this.youtubeAccount = youtubeAccount;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        LocalMessage message = getLocalMessage();

        // Upload the video attachment, if there is any.
        String body = message.body;
        if (message.videoAttachment != null) {
            String videoId = uploadVideo(message.videoAttachment);
            body = attachVideo(body, videoId);
        }

        // Publish the message.
        Message inserted = service.addMessage(
                message.uuid, message.parentMessage, message.subject, body);

        // Notify everybody.
        eventBus.post(new Success(message._id, inserted));
    }

    @Override
    protected void onCancel() {
        LocalMessage message = getLocalMessage();

        message.status = LocalMessage.Status.CANCELLED;

        ContentValues values = new ContentValues(1);

        values.put("status", String.valueOf(message.status));

        databaseHelper.getCupboard()
                .withDatabase(databaseHelper.getWritableDatabase())
                .update(LocalMessage.class, values, "_id = ?", String.valueOf(message._id));

        eventBus.post(new Cancelled(message));
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        // TODO Deal with errors.
        // FileNotFoundException: video file doesn't exist. Should we add it again without video?

        Timber.e(throwable, "Failed to add message.");

        return true;
    }

        // Grab LocalMessage from the database.
    private LocalMessage getLocalMessage() {
        if (localMessage == null) {
            localMessage = databaseHelper.getCupboard()
                    .withDatabase(databaseHelper.getWritableDatabase())
                    .get(LocalMessage.class, messageId);
        }
        return localMessage;
    }

    /** Ugly code. You don't want to read this. */

    // XXX Injected fields are declared transient in order to not be serialized
    @Inject transient EventBus eventBus;
    @Inject transient EDMService service;
    @Inject transient Application application;
    @Inject transient DatabaseProvider databaseHelper;

    /**
     * Attach a YouTube video to a message body.
     *
     * @param body The body of the message.
     * @param videoId The id of the YouTube video.
     *
     * @return A new body, with the video attached.
     */
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

