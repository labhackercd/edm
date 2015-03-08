package net.labhackercd.edemocracia.task;

import android.app.Application;

import com.google.gson.Gson;
import com.squareup.tape.FileObjectQueue;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {AddMessageTask.class, TaskManagerService.class},
        complete = false,
        library = true
)
@SuppressWarnings("UnusedDeclaration")
public class TaskModule {
    @Provides @Singleton
    FileObjectQueue.Converter<AddMessageTask> provideTaskConverter() {
        // TODO Proper Gson.
        return new GsonConverter<>(new Gson(), AddMessageTask.class);
    }

    @Provides @Singleton
    TaskManager provideTaskManager(
            Application application, FileObjectQueue.Converter<AddMessageTask> converter) {
        return new TaskManager(application, converter);
    }
}
