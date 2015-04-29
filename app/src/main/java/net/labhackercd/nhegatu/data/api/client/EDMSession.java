package net.labhackercd.nhegatu.data.api.client;

import android.os.AsyncTask;

import com.liferay.mobile.android.auth.Authentication;
import com.liferay.mobile.android.exception.ServerException;
import com.liferay.mobile.android.http.HttpUtil;
import com.liferay.mobile.android.service.Session;
import com.liferay.mobile.android.task.callback.AsyncTaskCallback;

import net.labhackercd.nhegatu.data.api.client.exception.AuthorizationException;
import net.labhackercd.nhegatu.data.api.client.exception.NotFoundException;
import net.labhackercd.nhegatu.data.api.client.exception.PrincipalException;

import org.json.JSONArray;
import org.json.JSONObject;

public class EDMSession implements Session {
    private final Endpoint endpoint;
    private Authentication authentication;

    public EDMSession(Endpoint endpoint) {
        this(endpoint, null);
    }

    public EDMSession(Endpoint endpoint, Authentication authentication) {
        this.endpoint = endpoint;
        this.authentication = authentication;
    }

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    public AsyncTaskCallback getCallback() {
        return null;
    }

    @Override
    public int getConnectionTimeout() {
        return 1500;
    }

    @Override
    public String getServer() {
        return endpoint.url();
    }

    @Override
    public JSONArray invoke(JSONObject command) throws Exception {
        try {
            HttpUtilMonkeyPatcher.patch();
            return HttpUtil.post(this, command);
        } catch (ServerException e) {
            throw handleException(e);
        }
    }

    @Override
    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public void setCallback(AsyncTaskCallback callback) {
        throw new UnsupportedOperationException("Asynchronous calls are not supported.");
    }

    @Override
    public void setConnectionTimeout(int connectionTimeout) {
        throw new UnsupportedOperationException("connectionTimeout is final.");
    }

    @Override
    public void setServer(String server) {
        throw new UnsupportedOperationException("server is final.");
    }

    @Override
    public AsyncTask upload(JSONObject command) throws Exception {
        throw new UnsupportedOperationException("Uploads are not supported.");
    }

    public static Exception handleException(Exception e) {
        String err = e.getMessage().toLowerCase().trim();

        if (err.matches(".*principal *exception.*")) {
            return new PrincipalException(e);
        } else if (err.matches(".*(no *such|no *\\w+ *exists).*")) {
            return new NotFoundException(e);
        } else if (err.matches(".*(please *sign|authenticated *access|authentication *failed).*")) {
            return new AuthorizationException(e);
        }

        return e;
    }
}
