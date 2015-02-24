package net.labhackercd.edemocracia.data.api;

import android.os.AsyncTask;

import com.liferay.mobile.android.auth.Authentication;
import com.liferay.mobile.android.exception.ServerException;
import com.liferay.mobile.android.service.Session;
import com.liferay.mobile.android.task.callback.AsyncTaskCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EDMBatchSession implements Session {
    private final Session session;
    private final Object lock = new Object();
    private final List<JSONObject> commands = new ArrayList<>();

    public EDMBatchSession(Session session) {
        this.session = session;
    }

    @Override
    public Authentication getAuthentication() {
        return session.getAuthentication();
    }

    @Override
    public AsyncTaskCallback getCallback() {
        return null;
    }

    @Override
    public int getConnectionTimeout() {
        return session.getConnectionTimeout();
    }

    @Override
    public String getServer() {
        return session.getServer();
    }

    @Override
    public JSONArray invoke(JSONObject command) throws Exception {
        synchronized (lock) {
            commands.add(command);
        }
        return null;
    }

    public JSONArray invoke() throws Exception {
        if (this.commands.size() == 0) {
            return null;
        }

        JSONArray commands;

        synchronized (lock) {
            commands = new JSONArray(this.commands);
            try {
                return HttpUtilMonkeyPatcher.post(this, commands);
            } catch (ServerException e) {
                throw EDMSession.handleException(e);
            } finally {
                this.commands.clear();
            }
        }
    }

    @Override
    public void setAuthentication(Authentication authentication) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCallback(AsyncTaskCallback callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setConnectionTimeout(int connectionTimeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setServer(String server) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncTask upload(JSONObject command) throws Exception {
        throw new UnsupportedOperationException();
    }
}
