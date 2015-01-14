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
import java.net.MalformedURLException;
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

    public JSONArray listGroups(int companyId) throws ServerException, IOException, URISyntaxException {
        List<NameValuePair> args = new ArrayList<>();

        args.add(new BasicNameValuePair("companyId", Integer.toString(companyId)));
        args.add(new BasicNameValuePair("start", Integer.toString(-1)));
        args.add(new BasicNameValuePair("end", Integer.toString(-1)));
        args.add(new BasicNameValuePair("name", "%"));
        args.add(new BasicNameValuePair("description", "%"));
        args.add(new BasicNameValuePair("params", null));

        // Always add token last, and make sure it exists.
        args.add(new BasicNameValuePair("p_auth", getValidAuthenticationToken().getToken()));

        return this.call("/group/search", args);
    }

    public JSONArray listGroupThreads(int groupId) throws IOException, ServerException, URISyntaxException {
        List<NameValuePair> args = new ArrayList<>();

        args.add(new BasicNameValuePair("groupId", Integer.toString(groupId)));
        args.add(new BasicNameValuePair("userId", Integer.toString(-1)));
        args.add(new BasicNameValuePair("status", Integer.toString(0)));
        args.add(new BasicNameValuePair("start", Integer.toString(-1)));
        args.add(new BasicNameValuePair("end", Integer.toString(-1)));

        // Always add token last, and make sure it exists.
        args.add(new BasicNameValuePair("p_auth", getValidAuthenticationToken().getToken()));

        return this.call("/mbthread/get-group-threads", args);
    }

    protected JSONArray call(String method, List<NameValuePair> args) throws ServerException, IOException, URISyntaxException {
        AuthenticationToken token = getToken();

        if (!isTokenUsable(token)) {
            throw new ServerException("Invalid or expired authentication token");
        }

        URL url = createMethodURL(method, args);
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
            throw new ServerException("Request failed with code " + url.toString() + " -- " + status);
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

    protected URL createMethodURL(String method, List<NameValuePair> args) throws URISyntaxException, MalformedURLException {
        URI uri = getURL().toURI();

        uri = uri.resolve(uri.getPath().concat("/").concat(method));

        uri = uri.resolve("?" + URLEncodedUtils.format(args, "UTF-8"));

        return uri.normalize().toURL();
    }

    private AuthenticationToken getValidAuthenticationToken() throws IOException, URISyntaxException {
        if (!isTokenUsable(token)) {
            token = fetchAuthenticationToken();
        }
        return token;
    }

    private AuthenticationToken fetchAuthenticationToken() throws IOException, URISyntaxException {
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
