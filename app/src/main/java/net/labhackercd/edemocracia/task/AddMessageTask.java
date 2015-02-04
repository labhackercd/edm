package net.labhackercd.edemocracia.task;

import android.os.Handler;
import android.os.Looper;

import com.liferay.mobile.android.service.JSONObjectWrapper;
import com.liferay.mobile.android.v62.mbmessage.MBMessageService;
import com.squareup.tape.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import net.labhackercd.edemocracia.content.Message;
import net.labhackercd.edemocracia.liferay.session.EDMGetSessionWrapper;
import net.labhackercd.edemocracia.liferay.session.EDMSession;

public class AddMessageTask implements Task<AddMessageTask.Callback> {

    private static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());

    private final Message message;

    public AddMessageTask(Message message) {
        this.message = message;
    }

    @Override
    public void execute(final Callback callback) {
        execute(null, callback);
    }

    public void execute(final EDMSession session, final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject serviceContextJson = new JSONObject();
                    serviceContextJson.put("addGuestPermissions", true);

                    JSONObjectWrapper serviceContext = new JSONObjectWrapper(
                            "com.liferay.portal.service.ServiceContext", serviceContextJson);

                    MBMessageService service = new MBMessageService(new EDMGetSessionWrapper(session));

                    JSONObject insert = service.addMessage(
                            message.getGroupId(), message.getCategoryId(), message.getThreadId(),
                            message.getParentMessageId(), message.getSubject(), message.getBody(),
                            message.getFormat(), new JSONArray(), message.isAnonymous(),
                            message.getPriority(), message.allowPingbacks(), serviceContext);

                    final Message inserted = Message.JSON_READER.fromJSON(insert);

                    MAIN_THREAD.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(inserted);
                        }
                    });
                } catch (final Exception e) {
                    MAIN_THREAD.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(message, e);
                        }
                    });
                }
            }
        }).start();
    }

    public interface Callback {
        void onSuccess(Message message);
        void onFailure(Message message, Exception exception);
    }

    public static class Success {
        private Message message;

        public Success(Message message) {
            this.message = message;
        }

        public Message getMessage() {
            return message;
        }
    }

    public static class Failure {
        private Message message;
        private Exception exception;

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

