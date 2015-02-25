package net.labhackercd.edemocracia.job;

import android.os.Handler;
import android.os.Looper;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import net.labhackercd.edemocracia.data.api.EDMService;
import net.labhackercd.edemocracia.data.api.model.Message;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import timber.log.Timber;

public class AddMessageJob extends Job {
    public static final int PRIORITY = 1;

    private static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());

    // XXX Injected fields are declared transient in order to not be serialized
    @Inject transient EventBus eventBus;
    @Inject transient EDMService service;

    private final Message message;

    public AddMessageJob(Message message) {
        super(new Params(PRIORITY).requireNetwork().persist());
        this.message = message;
    }

    @Override
    public void onAdded() {
        eventBus.post(new JobAdded(message));
    }

    @Override
    public void onRun() throws Throwable {
        final Message inserted = service.addMessage(message);
        MAIN_THREAD.post(new Runnable() {
            @Override
            public void run() {
                eventBus.post(new Success(inserted));
            }
        });
    }

    @Override
    protected void onCancel() {
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        Timber.e(throwable, "Failed to add message.");
        return true;
    }

    public static class JobAdded {
        private final Message message;

        public JobAdded(Message message) {
            this.message = message;
        }

        public Message getMessage() {
            return message;
        }
    }

    public static class Success {
        private final Message message;

        public Success(Message message) {
            this.message = message;
        }

        public Message getMessage() {
            return message;
        }
    }

    public static class Failure {
        private final Message message;
        private final Exception exception;

        public Failure(Message message, Exception exception) {
            this.message = message;
            this.exception = exception;
        }

        public Message getMessage() {
            return message;
        }

        public Exception getException() {
            return exception;
        }
    }
}

