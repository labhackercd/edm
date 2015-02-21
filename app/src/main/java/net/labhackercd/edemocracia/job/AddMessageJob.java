package net.labhackercd.edemocracia.job;

import android.os.Handler;
import android.os.Looper;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import net.labhackercd.edemocracia.data.api.GroupService;
import net.labhackercd.edemocracia.data.model.Message;
import net.romenor.deathray.action.ObjectWrapper;

import org.json.JSONObject;

import java.util.Collections;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import timber.log.Timber;

public class AddMessageJob extends Job {
    public static final int PRIORITY = 1;

    // XXX Injected fields are declared transient in order to not be serialized
    @Inject transient EventBus eventBus;
    @Inject transient GroupService groupService;

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
        JSONObject serviceContextJson = new JSONObject();
        serviceContextJson.put("addGuestPermissions", true);

        ObjectWrapper serviceContext = new ObjectWrapper.Builder()
                .setClassName("com.liferay.portal.service.ServiceContext")
                .put("addGuestPermissions", true)
                .build();

        final Message inserted = groupService.addMessage(
                message.getGroupId(), message.getCategoryId(), message.getThreadId(),
                message.getParentMessageId(), message.getSubject(), message.getBody(),
                message.getFormat(), Collections.emptyList(), message.isAnonymous(),
                message.getPriority(), message.allowPingbacks(), serviceContext);

        eventBus.post(new Success(inserted));
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

