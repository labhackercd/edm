package net.labhackercd.edemocracia.data.api;

import android.os.AsyncTask;

import com.google.common.base.CharMatcher;
import com.liferay.mobile.android.auth.Authentication;
import com.liferay.mobile.android.exception.ServerException;
import com.liferay.mobile.android.http.HttpUtil;
import com.liferay.mobile.android.service.Session;
import com.liferay.mobile.android.task.callback.AsyncTaskCallback;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * XXX This is a hack to work around a bug in the portal's webservice router.
 *
 * What happens is that the webservice can't find the method some methods (for more information,
 * see http://stackoverflow.com/questions/17557273).
 *
 * But that shouldn't stop us, since we can call some of those methods through HTTP GET requests
 * to the webservice. And this is exactly what this wrapper does. It calls problematic methods
 * through GET.
 *
 * Note that most of the Session API is not implemented and will only raise a RuntimeError. Only
 * the methods required to call the methods we need are implemented so far.
 */
public class EDMGetSessionWrapper implements Session {
    private Session session;
    private static final List<String> INTERCEPT_COMMANDS = Arrays.asList(
            "/mbmessage/add-message"
    );

    public EDMGetSessionWrapper(Session session) {
        this.session = session;
    }

    @Override
    public Authentication getAuthentication() {
        return session.getAuthentication();
    }

    @Override
    public AsyncTaskCallback getCallback() {
        return session.getCallback();
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
        for (String method : INTERCEPT_COMMANDS) {
            if (command.has(method)) {
                JSONObject args = command.getJSONObject(method);
                List<NameValuePair> argsCopy = new ArrayList<>();
                Iterator<String> it = args.keys();
                while (it.hasNext()) {
                    String key = it.next();

                    if (key.startsWith("+")) {
                        continue;
                    }

                    Object value = args.get(key);
                    argsCopy.add(new BasicNameValuePair(key, value.toString()));
                }
                return CustomHttpUtil.invokeThroughGET(this, method, argsCopy);
            }
        }

        return session.invoke(command);
    }

    @Override
    public void setAuthentication(Authentication authentication) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setCallback(AsyncTaskCallback callback) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setConnectionTimeout(int connectionTimeout) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setServer(String server) {
        throw new RuntimeException("Not implemented");

    }

    @Override
    public AsyncTask upload(JSONObject command) throws Exception {
        throw new UnsupportedOperationException();
    }

    public static class CustomHttpUtil extends HttpUtil {
        protected static JSONArray invokeThroughGET(Session session, String method, List<NameValuePair> args) throws IOException, ServerException, JSONException {
            String url = createMethodURL(session, method, args);

            HttpClient client = HttpUtil.getClient(session);

            HttpGet request = new HttpGet(url);

            Authentication authentication = session.getAuthentication();
            if (authentication != null) {
                try {
                    authentication.authenticate(request);
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    // XXX We should really not do this, but liferay-mobile-sdk-android
                    // won't let us know what's happening.
                    throw new RuntimeException(e);
                }
            }

            HttpResponse response = client.execute(request);

            String json = HttpUtil.getResponseString(response);

            HttpUtil.handleServerException(request, response, json);

            return new JSONArray("[" + json + "]");
        }

        private static String createMethodURL(Session session, String method, List<NameValuePair> args) {
            CharMatcher matcher = CharMatcher.anyOf("/");

            String url = session.getServer();

            url = matcher.trimTrailingFrom(url);

            url += "/api/secure/jsonws/";

            url += matcher.trimFrom(method);

            url += "?" + URLEncodedUtils.format(args, "UTF-8");

            return url;
        }
    }
}
