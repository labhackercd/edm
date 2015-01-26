package br.leg.camara.labhacker.edemocracia.tasks;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.squareup.otto.Bus;
import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.ObjectQueue;
import com.squareup.tape.TaskQueue;

import java.io.File;
import java.io.IOException;

public class AddMessageTaskQueue extends TaskQueue<AddMessageTask> {
    private static final String FILENAME = "add_message_task_queue";

    private final Bus bus;
    private final Context context;

    private AddMessageTaskQueue(ObjectQueue<AddMessageTask> delegate, Context context, Bus bus) {
        super(delegate);

        this.bus = bus;
        this.context = context;

        bus.register(this);

        if (size() > 0) {
            startService();
        }
    }

    private void startService() {
        context.startService(new Intent(context, AddMessageTaskService.class));
    }

    @Override
    public void add(AddMessageTask entry) {
        super.add(entry);
        startService();
    }

    public static AddMessageTaskQueue create(Context context, Gson gson, Bus bus) {
        FileObjectQueue.Converter<AddMessageTask> converter =
                new GsonConverter<>(gson, AddMessageTask.class);

        File queueFile = new File(context.getFilesDir(), FILENAME);
        FileObjectQueue<AddMessageTask> delegate;

        try {
            delegate = new FileObjectQueue<>(queueFile, converter);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file queue", e);
        }

        return new AddMessageTaskQueue(delegate, context, bus);
    }
}
