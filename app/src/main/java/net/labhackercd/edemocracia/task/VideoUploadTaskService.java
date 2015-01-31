package net.labhackercd.edemocracia.task;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.common.collect.Lists;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import net.labhackercd.edemocracia.application.EDMApplication;
import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.content.Message;
import net.labhackercd.edemocracia.ytdl.Auth;

public class VideoUploadTaskService extends Service implements VideoUploadTask.Callback {
    private static final String TAG = VideoUploadTaskService.class.getSimpleName();

    @Inject Bus bus;
    @Inject VideoUploadTaskQueue queue;
    @Inject AddMessageTaskQueue addMessageTaskQueue;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = new GsonFactory();

    private boolean running;

    @Override
    public void onCreate() {
        super.onCreate();

        running = false;

        ((EDMApplication) getApplication()).inject(this);
        Log.i(TAG, "Starting service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        executeNext();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSuccess(Message r, String videoId) {
        running = false;
        queue.remove();

        String newBody = "[center][youtube]" + videoId + "[/youtube][/center]\n" + r.getBody();

        Message message = Message.create(
                r.getGroupId(), r.getCategoryId(), r.getThreadId(), r.getParentMessageId(),
                r.getSubject(), newBody, r.getFormat(), r.isAnonymous(), r.getPriority(),
                r.allowPingbacks());

        addMessageTaskQueue.add(new AddMessageTask(message));
    }

    @Override
    public void onFailure(Message message, Exception e) {
        Log.e(TAG, "Failed to upload video: " + e);

        // Notify everyone that the message failed to be added
        bus.post(new AddMessageFailureEvent(message, e));
    }

    private void executeNext() {
        if (running) {
            return;
        }

        VideoUploadTask task = queue.peek();

        if (task != null) {
            running = true;
            executeTask(task);
        } else {
            Log.i(TAG, "Stopping service");
            stopSelf();
        }
    }

    private void executeTask(VideoUploadTask task) {
        GoogleAccountCredential credential = GoogleAccountCredential
                .usingOAuth2(getApplicationContext(), Lists.newArrayList(Auth.SCOPES));

        credential.setSelectedAccountName(task.getAccount());
        credential.setBackOff(new ExponentialBackOff());

        String appName = getResources().getString(R.string.app_name);

        YouTube youtube = new YouTube.Builder(transport, jsonFactory, credential)
                .setApplicationName(appName).build();

        task.execute(getApplicationContext(), youtube, this);
    }
}
