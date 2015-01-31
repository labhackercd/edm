package net.labhackercd.edemocracia.task;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;

import com.google.api.services.youtube.YouTube;
import com.squareup.tape.Task;

import java.io.IOException;
import java.io.InputStream;

import net.labhackercd.edemocracia.content.Message;

public class VideoUploadTask implements Task<VideoUploadTask.Callback> {

    private static final String TAG = "VideoUploadTask";

    private static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());

    private String video;
    private String account;
    private Message message;

    public VideoUploadTask(Uri video, String account, Message message) {
        this.video = video.toString();
        this.account = account;
        this.message = message;
    }

    public String getAccount() {
        return account;
    }

    @Override
    public void execute(final Callback callback) {
        throw new RuntimeException("Not implemented");
    }

    public void execute(final Context context, final YouTube youtube, final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver contentResolver = context.getContentResolver();

                Uri video = Uri.parse(VideoUploadTask.this.video);
                InputStream fileInputStream = null;

                try {
                    long fileSize = contentResolver.openFileDescriptor(video, "r").getStatSize();
                    fileInputStream = contentResolver.openInputStream(video);
                    String[] projection = {MediaStore.Images.Media.DATA};
                    Cursor cursor = contentResolver.query(video, projection, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();

                    final String videoId = ResumableUpload.upload(
                            youtube, fileInputStream, fileSize, video,
                            cursor.getString(column_index), context);

                    assert videoId != null;

                    MAIN_THREAD.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(message, videoId);
                        }
                    });
                } catch (final Exception e) {
                    Log.e(TAG, "Failed to upload video " + e.toString());
                    MAIN_THREAD.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(message, e);
                        }
                    });
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
        }).start();
    }

    public interface Callback {
        void onSuccess(Message message, String videoId);
        void onFailure(Message message, Exception e);
    }
}
