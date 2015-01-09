package br.leg.camara.labhacker.edemocracia.liferay;

import com.liferay.mobile.android.util.Validator;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LiferayClient {

    private static final int wsPort = 8080;
    private static final String wsProtocol = "http";
    private static final String wsHost = "192.168.1.11";
    private static final String wsPath = "/api/jsonws";

    private AuthenticationToken token;
    private CookieStore cookies;

    public LiferayClient() {
        this.token = null;
        this.cookies = (new CookieManager()).getCookieStore();
    }

    public LiferayClient(CookieStore cookies) {
        this.token = null;
        this.cookies = cookies;
    }

    public LiferayClient(CookieStore cookies, AuthenticationToken token) {
        this.token = token;
        this.cookies = cookies;
    }

    public static boolean isTokenUsable(AuthenticationToken token) {
        return token != null && Validator.isNotNull(token.getToken()) && !token.isExpired();
    }

    public boolean authenticate(String username, String password) throws IOException {
        token = AuthenticationHelper.authenticateAndGetToken(username, password, cookies);
        return isTokenUsable(token);
    }

    public boolean isAuthenticated() throws IOException {
        return AuthenticationHelper.isAuthenticated(cookies);
    }

    public JSONArray listGroups(int companyId) throws ServerException, IOException, URISyntaxException {
        List<NameValuePair> args = new ArrayList<NameValuePair>();

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

    protected JSONArray call(String method, List<NameValuePair> args) throws ServerException, IOException, URISyntaxException {

        // XXX Always set the default cookie handler to make sure cookies are correctly
        // stored in our internal cookie store.
        CookieHandler.setDefault(new CookieManager(cookies, CookiePolicy.ACCEPT_ALL));

        if (!isTokenUsable(token)) {
            throw new ServerException("Invalid authentication token (locally)");
        }

        URL url = this.buildMethodURL(method, args);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Handle response codes
        int status = connection.getResponseCode();

        if ((status == HttpURLConnection.HTTP_MOVED_PERM) ||
                (status == HttpURLConnection.HTTP_MOVED_TEMP) ||
                (status == HttpURLConnection.HTTP_SEE_OTHER)) {
            throw new RedirectException(connection.getHeaderField("Location"));
        }

        if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new ServerException("Authentication failed");
        }

        if (status != HttpURLConnection.HTTP_OK) {
            throw new ServerException("Request failed with code " + status);
        }

        // Read the response content
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String content = "";

        try {
            String line;
            while (null != (line = reader.readLine())) {
                content += line;
            }
        } finally {
            reader.close();
            connection.disconnect();
        }


        JSONArray result = null;

        try {
            if (isJSONObject(content)) {
                // Check if its an exception
                JSONObject o = new JSONObject(content);

                if (o.has("exception")) {
                    throw new ServerException(o.getString("exception"));
                } else {
                    // XXX FIXME Is this `else` branch really necessary? I mean, can it happen?
                    result = new JSONArray();
                    result.put(o);
                }
            } else {
                result = new JSONArray(content);
            }
        } catch (JSONException e) {
            throw new ServerException("Invalid response. " + e.toString(), e);
        }

        return result;
    }

    private AuthenticationToken fetchAuthenticationToken() throws IOException, URISyntaxException {
        CookieHandler.setDefault(new CookieManager(cookies, CookiePolicy.ACCEPT_ALL));

        URL url = createURL("/");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        // Where the content of the request will be saved
        String content = "";

        try {
            String line;
            while (null != (line = reader.readLine())) {
                content += line;
            }
        } finally {
            reader.close();
            urlConnection.disconnect();
        }

        return AuthenticationHelper.extractAuthToken(content);
    }

    private AuthenticationToken getValidAuthenticationToken() throws IOException, URISyntaxException {
        if (!isTokenUsable(token)) {
            token = fetchAuthenticationToken();
        }
        return token;
    }

    private static Boolean isJSONObject(String s) {
        return Validator.isNotNull(s) && s.startsWith("{");
    }

    public CookieStore getCookies() {
        return cookies;
    }

    public void setCookies(CookieStore cookies) {
        this.cookies = cookies;
    }

    public AuthenticationToken getToken() {
        return token;
    }

    public void setToken(AuthenticationToken token) {
        this.token = token;
    }

    private static URL createURL(String path) throws MalformedURLException {
        return createURL(path, null);
    }

    private static URL createURL(String path, String query) throws MalformedURLException {
        return createURL(path, query, null);
    }

    private static URL createURL(String path, String query, String fragment) throws MalformedURLException {
        try {
            return URIUtils.createURI(wsProtocol, wsHost, wsPort, path, query, fragment).toURL();
        } catch (URISyntaxException e) {
            return new URL(e.getInput());
        }
    }

    protected URL buildMethodURL(String method, List<NameValuePair> args) throws URISyntaxException, MalformedURLException {
        String queryString = URLEncodedUtils.format(args, "UTF-8");

        // Construct the Path
        String path = wsPath;
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String method_ = method;
        while (method_.startsWith("/")) {
            method_ = method_.substring(1);
        }
        path = path + "/" + method_;
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return createURL(path, queryString);
    }
}
