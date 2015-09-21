/*
 * This file is part of Nhegatu, the e-Demoracia Client for Android.
 *
 * Nhegatu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nhegatu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nhegatu.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.labhackercd.nhegatu.data.api.client;

import android.os.AsyncTask;

import com.google.common.base.CharMatcher;
import com.liferay.mobile.android.auth.Authentication;
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
import org.json.JSONObject;

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
class GETSessionWrapper implements Session {
    private Session session;
    private static final List<String> INTERCEPT_COMMANDS = Arrays.asList(
            "/mbmessage/add-message"
    );

    GETSessionWrapper(Session session) {
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
                return GETHttpUtil.invokeThroughGET(this, method, argsCopy);
            }
        }

        return session.invoke(command);
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

    /** We must extend HttpUtil because {@link HttpUtil.handleServerException} is protected. */
    private static class GETHttpUtil extends HttpUtil {

        private static JSONArray invokeThroughGET(Session session, String method, List<NameValuePair> args) throws Exception {
            String url = createMethodURL(session.getServer(), method, args);

            HttpClient client = HttpUtil.getClient(session);

            HttpGet request = new HttpGet(url);

            Authentication authentication = session.getAuthentication();
            if (authentication != null) {
                authentication.authenticate(request);
            }

            HttpResponse response = client.execute(request);

            String json = HttpUtil.getResponseString(response);

            HttpUtil.handleServerException(request, response, json);

            return new JSONArray("[" + json + "]");
        }

        private static String createMethodURL(String endpoint, String method, List<NameValuePair> args) {
            CharMatcher matcher = CharMatcher.anyOf("/");
            return matcher.trimTrailingFrom(endpoint)
                    .concat("/").concat(matcher.trimFrom(method))
                    .concat("?").concat(URLEncodedUtils.format(args, "UTF-8"));
        }
    }
}
