package br.leg.camara.labhacker.edemocracia.tasks;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.liferay.mobile.android.service.JSONObjectWrapper;
import com.liferay.mobile.android.v62.mbmessage.MBMessageService;
import com.squareup.tape.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import br.leg.camara.labhacker.edemocracia.content.Message;
import br.leg.camara.labhacker.edemocracia.util.EDMGetSessionWrapper;
import br.leg.camara.labhacker.edemocracia.util.EDMSession;

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

    public void execute(final Application application, final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final EDMSession session = EDMSession.get(application);

                    assert session != null;

                    JSONObject serviceContextJson = new JSONObject();
                    serviceContextJson.put("addGuestPermissions", true);

                    JSONObjectWrapper serviceContext = new JSONObjectWrapper(
                            "com.liferay.portal.service.ServiceContext", serviceContextJson);

                    MBMessageService service = new MBMessageService(new EDMGetSessionWrapper(session));

                    JSONObject insert = service.addMessage(
                            message.getGroupId(), message.getCategoryId(), message.getThreadId(),
                            message.getRootMessageId(), message.getSubject(), message.getBody(),
                            message.getFormat(), new JSONArray(), message.isAnonymous(),
                            message.getPriority(), message.isAllowPingbacks(), serviceContext);

                    final Message inserted = Message.JSON_READER.fromJSON(insert);

                    MAIN_THREAD.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(inserted);
                        }
                    });
                } catch (Exception e) {
                    // TODO Deal with specific exceptions and trigger onFailure
                    e.printStackTrace();
                    Log.w(getClass().getSimpleName(), e.toString());
                }
            }
        }).start();
    }

    public interface Callback {
        void onSuccess(Message message);
        void onFailure();
    }
}

