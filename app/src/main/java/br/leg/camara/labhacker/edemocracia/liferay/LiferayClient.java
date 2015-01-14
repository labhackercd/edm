package br.leg.camara.labhacker.edemocracia.liferay;

import com.google.common.base.CharMatcher;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LiferayClient {

    private URL url;
    private AuthenticationToken token;
    private CookieCredentials credentials;

    public LiferayClient(URL url, CookieCredentials credentials) {
        this.url = url;
        this.token = null;
        this.credentials = credentials;
    }

    public CookieCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(CookieCredentials credentials) {
        this.credentials = credentials;
    }

    public AuthenticationToken getToken() {
        return token;
    }

    public URL getURL() {
        return url;
    }

    public JSONArray listGroups(int companyId) throws ServerException, IOException, URISyntaxException, JSONException {
        JSONObject args = new JSONObject();

        args.put("companyId", companyId);
        args.put("name", "%");
        args.put("description", "%");
        args.put("params", "");
        args.put("start", -1);
        args.put("end", -1);

        JSONObject cmd = new JSONObject();

        cmd.put("/group/search", args);

        return this.call(cmd);
    }

    public JSONArray listGroupThreads(int groupId) throws IOException, ServerException, URISyntaxException, JSONException {

        // WARNING This method actually issues two method calls on the server.
        // First we get the list of threads, and then we get a list of messages
        // for each rootMessage of each thread.

        // First, we issue a "/mbthread/get-group-threads" command
        // to get the list of threads inside the given group.
        JSONObject args = new JSONObject();

        args.put("groupId", groupId);
        args.put("userId", -1);
        args.put("status", 0);
        args.put("start", -1);
        args.put("end", -1);

        JSONObject cmd = new JSONObject();

        cmd.put("/mbthread/get-group-threads", args);

        JSONArray threads = this.call(cmd);

        // Then we construct a batch call with a /mbmessage/get-message for each rootMessageId
        // of each thread we just got.

        JSONArray commands = new JSONArray();

        for (int i = 0; i < threads.length(); i++) {
            JSONObject thread = threads.getJSONObject(i);

            args = new JSONObject();
            args.put("messageId", thread.getInt("rootMessageId"));

            cmd = new JSONObject();
            cmd.put("/mbmessage/get-message", args);

            commands.put(cmd);
        }

        JSONArray messages = this.batchCall(commands);

        // Then we place each rootMessage in the rootMessage column of the threads

        for (int i = 0; i < threads.length(); i++) {
            JSONObject thread = threads.getJSONObject(i);
            JSONObject message = messages.getJSONObject(i);
            thread.put("rootMessage", message);
        }

        // And then we return :)

        return threads;
    }

    public JSONArray listThreadMessages(int groupId, int categoryId, int threadId) throws IOException, URISyntaxException, ServerException, JSONException {
       JSONObject args = new JSONObject();

        args.put("groupId", groupId);
        args.put("categoryId", categoryId);
        args.put("threadId", threadId);
        args.put("status", 0);
        args.put("start", -1);
        args.put("end", -1);

        JSONObject cmd = new JSONObject();
        cmd.put("/mbmessage/get-thread-messages", args);

        return this.call(cmd);
    }

    protected JSONArray call(JSONObject command) throws ServerException, IOException, URISyntaxException, JSONException {
        JSONArray commands = new JSONArray();

        commands.put(command);

        return this.batchCall(commands).getJSONArray(0);
    }

    protected JSONArray batchCall(JSONArray commands) throws ServerException, IOException, URISyntaxException {
        URL url = getCallURL(commands);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        getCredentials().authenticate(connection);

        int status = connection.getResponseCode();

        if ((status == HttpURLConnection.HTTP_MOVED_PERM) ||
                (status == HttpURLConnection.HTTP_MOVED_TEMP) ||
                (status == HttpURLConnection.HTTP_SEE_OTHER)) {
            throw new RedirectException(connection.getHeaderField("Location"));
        }

        if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new ServerException("Unauthorized");
        }

        if (status != HttpURLConnection.HTTP_OK) {
            throw new ServerException("Request failed with code " + status);
        }

        String content = RequestsHelper.readBody(connection);

        JSONArray result;
        try {
            if (isJSONObject(content)) {
                JSONObject o = new JSONObject(content);

                if (o.has("exception")) {
                    throw new ServerException(o.getString("exception"));
                } else {
                    // XXX Return any other "single object response" into an Array containing
                    // the response object. I don't really know if this is necessary, tho.
                    result = new JSONArray();
                    result.put(o);
                }
            } else {
                result = new JSONArray(content);
            }
        } catch (JSONException e) {
            throw new ServerException("Invalid response: " + e.toString(), e);
        }

        return result;
    }

    private URL getCallURL(JSONArray commands) throws URISyntaxException, IOException, ServerException {
        AuthenticationToken token = getValidAuthenticationToken();

        if (!isTokenUsable(token)) {
            throw new ServerException("Invalid or expired authentication token");
        }

        List<NameValuePair> query = new ArrayList<>();

        query.add(new BasicNameValuePair("p_auth", token.getToken()));
        query.add(new BasicNameValuePair("cmd", commands.toString()));

        URI uri = getURL().toURI();

        String path = uri.getPath().concat("?").concat(URLEncodedUtils.format(query, "UTF-8"));

        return uri.resolve(path).normalize().toURL();
    }

    private AuthenticationToken getValidAuthenticationToken() throws IOException, URISyntaxException {
        if (!isTokenUsable(token)) {
            token = fetchAuthenticationToken();
        }
        return token;
    }

    private AuthenticationToken fetchAuthenticationToken() throws IOException, URISyntaxException {
        URI uri = getURL().toURI();

        // FIXME Can we really guess a valid token URL?
        URL url = uri.resolve(".").normalize().toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        getCredentials().authenticate(connection);

        String body = RequestsHelper.readBody(connection);

        return extractAuthToken(body);
    }

    private static AuthenticationToken extractAuthToken(String content) {
        Pattern pattern = Pattern.compile("authToken\\s*=\\s*(\"[^\"]+\"|'[^']+')");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String token = CharMatcher.anyOf("\"'").trimFrom(matcher.group(1));
            return new AuthenticationToken(token);
        } else {
            return null;
        }
    }

    private static boolean isTokenUsable(AuthenticationToken token) {
        return token != null && token.getToken() != null && !token.getToken().isEmpty() && !token.isExpired();
    }

    private static boolean isJSONObject(String s) {
        return s != null && !s.isEmpty() && s.startsWith("{");
    }
}
