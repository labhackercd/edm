package net.labhackercd.edemocracia.tasks;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.ObjectQueue;
import com.squareup.tape.TaskQueue;

import java.io.File;
import java.io.IOException;

public class VideoUploadTaskQueue extends TaskQueue<VideoUploadTask> {
    private static final String FILENAME = "video_upload_task_queue";
    private static final String TAG = "VideoUploadTaskQueue";

    private final Context context;

    private VideoUploadTaskQueue(ObjectQueue<VideoUploadTask> delegate, Context context) {
        super(delegate);

        this.context = context;

        if (size() > 0) {
            startService();
        }
    }

    private void startService() {
        context.startService(new Intent(context, VideoUploadTaskService.class));
    }

    @Override
    public void add(VideoUploadTask entry) {
        super.add(entry);
        startService();
    }

    public static VideoUploadTaskQueue create(Context context, Gson gson) {
        FileObjectQueue.Converter<VideoUploadTask> converter =
                new GsonConverter<>(gson, VideoUploadTask.class);

        File queueFile = new File(context.getFilesDir(), FILENAME);

        FileObjectQueue<VideoUploadTask> delegate;
        try {
            delegate = new FileObjectQueue<>(queueFile, converter);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file queue", e);
        }

        return new VideoUploadTaskQueue(delegate, context);
    }
}
