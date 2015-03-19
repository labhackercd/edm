package net.labhackercd.edemocracia.task;

import android.content.Context;
import android.content.Intent;

import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.TaskInjector;
import com.squareup.tape.TaskQueue;

import net.labhackercd.edemocracia.EDMApplication;

import java.io.File;
import java.io.IOException;

public class TaskManager {
    private final Context context;
    private final TaskQueue<AddMessageTask> queue;

    public TaskManager(Context context, FileObjectQueue.Converter<AddMessageTask> converter) {
        this.context = context;
        this.queue = createTaskQueue(context, converter, "defaultTaskQueue");
    }

    public <T extends AddMessageTask> void add(T task) {
        queue.add(task);
        startService();
    }

    Task peek() {
        return queue.peek();
    }

    private void startService() {
        context.startService(new Intent(context, TaskManagerService.class));
    }

    void onTaskCompleted(Task task) {
        queue.remove();
    }

    void onTaskError(Task task, Throwable error) {
        if (!task.shouldRetry(error)) {
            // TODO Should all be done in the same *transaction*
            // XXX But until there, we run task.onCancel first, then remove. This way,
            // if the first code block fails somehow, the task will automatically be
            // executed again later.
            task.onCancel(error);
            queue.remove();
        }
    }

    private static TaskQueue<AddMessageTask> createTaskQueue(
            Context context, FileObjectQueue.Converter<AddMessageTask> converter, String fileName) {
        File queueFile = new File(context.getFilesDir(), fileName);
        FileObjectQueue<AddMessageTask> delegate;
        try {
            delegate = new FileObjectQueue<>(queueFile, converter);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file queue.", e);
        }
        return new TaskQueue<>(delegate, new TaskInjector<AddMessageTask>() {
            @Override
            public void injectMembers(AddMessageTask task) {
                EDMApplication.get(context).getObjectGraph().inject(task);
            }
        });
    }
}
