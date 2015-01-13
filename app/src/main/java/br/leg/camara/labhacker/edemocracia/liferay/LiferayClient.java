package br.leg.camara.labhacker.edemocracia.liferay;

import com.google.common.base.CharMatcher;
import com.liferay.mobile.android.util.Validator;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.FormElement;

import java.io.BufferedReader;
import java.io.DataOutputStream;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A set of helper methods for dealing with requests made through {@link(HttpURLConnection)}
 */
class RequestsHelper {
    public static String readBody(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String body = "";

        try {
            String line;
            while (null != (line = reader.readLine())) {
                body += line;
            }
        } finally {
            reader.close();
            connection.disconnect();
        }

        return body;
    }

    public static void writeFormData(HttpURLConnection connection, List<NameValuePair> formData) throws IOException {
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formData);

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        DataOutputStream out = null;
        try {
            out = new DataOutputStream(connection.getOutputStream());
            entity.writeTo(out);
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
        }
    }
}


/**
 * A helper class for dealing with Liferay's authentication system.
 *
 * Here are most of the methods you'll use to authenticate a LiferayClient session,
 * grab tokens, etc. Most of these methods are only used by LiferayClient.
 */
class AuthenticationHelper {

    public static boolean authenticate(LiferayClient client, String username, String password) throws IOException {
        client.setupCookieHandler();

        FormElement form = getLoginForm(client);

        List<NameValuePair> loginFormData = getLoginFormData(form);

        for (NameValuePair item : loginFormData) {
            String name = item.getName();
            if (name.endsWith("login")) {
                loginFormData.set(loginFormData.indexOf(item), new BasicNameValuePair(name, username));
            } else if (name.endsWith("password")) {
                loginFormData.set(loginFormData.indexOf(item), new BasicNameValuePair(name, password));
            } else if (name.endsWith("rememberMe")) {
                loginFormData.set(loginFormData.indexOf(item), new BasicNameValuePair(name, "true"));
            }
        }

        URL loginUrl = new URL(form.attr("action"));
        HttpURLConnection connection = (HttpURLConnection) loginUrl.openConnection();

        connection.setInstanceFollowRedirects(true);

        RequestsHelper.writeFormData(connection, loginFormData);

        String loginBody = RequestsHelper.readBody(connection);

        String location = null;
        while (null != (location = connection.getHeaderField("Location"))) {
            connection = (HttpURLConnection) (new URL(location)).openConnection();
            loginBody = RequestsHelper.readBody(connection);
        }

        boolean success = checkIsAuthenticated(loginBody);

        if (checkIsAuthenticated(loginBody)) {
            AuthenticationToken token = extractAuthToken(loginBody);
            client.setToken(token);
        }

        return success;
    }

    public static AuthenticationToken fetchAuthenticationToken(LiferayClient client) throws IOException, URISyntaxException {
        client.setupCookieHandler();

        URL url = client.createURL("/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        String body = RequestsHelper.readBody(connection);

        return extractAuthToken(body);
    }

    public static boolean isAuthenticated(LiferayClient client) throws IOException {
        client.setupCookieHandler();

        URL url = client.createURL("/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        String body = RequestsHelper.readBody(connection);

        return checkIsAuthenticated(body);
    }

    private static boolean checkIsAuthenticated(String body) {
        // FIXME Implement a proper logic to check if the user is authenticated
        return body.contains("/c/portal/logout");
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

    private static FormElement getLoginForm(LiferayClient client) throws IOException {
        URL url = client.createURL("/cadastro");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        String body = RequestsHelper.readBody(connection);

        Document document = Jsoup.parse(body, "UTF-8");

        return (FormElement) document.select("#p_p_id_58_").first().select("form").first();
    }

    private static List<NameValuePair> getLoginFormData(FormElement form) {
        List<NameValuePair> formData = new ArrayList<>();

        for (Connection.KeyVal item : form.formData()) {
            formData.add(new BasicNameValuePair(item.key(), item.value()));
        }

        return formData;
    }
}

public class LiferayClient {
    private static final int wsPort = -1;
    private static final String wsProtocol = "http";
    private static final String wsHost = "edemocracia.camara.gov.br";
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

    public boolean authenticate(String username, String password) throws IOException {
        return AuthenticationHelper.authenticate(this, username, password);
    }

    public boolean isAuthenticated() throws IOException {
        return AuthenticationHelper.isAuthenticated(this);
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

    /**
     * Must be called before doing any requests to URLs from this client. This grants that the
     * cookies will be updated on the client's cookie store.
     */
    public void setupCookieHandler() {
        CookieManager cookieManager = new CookieManager(this.cookies, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
    }

    protected JSONArray call(String method, List<NameValuePair> args) throws ServerException, IOException, URISyntaxException {
        setupCookieHandler();

        if (!isTokenUsable(token)) {
            throw new ServerException("Invalid or expired authentication token");
        }

        URL url = createMethodURL(method, args);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

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

    public static URL createMethodURL(String method, List<NameValuePair> args) throws URISyntaxException, MalformedURLException {
        CharMatcher slashMatcher = CharMatcher.anyOf("/");

        String path = slashMatcher.trimFrom(wsPath) + "/" + slashMatcher.trimFrom(method);

        String query = URLEncodedUtils.format(args, "UTF-8");

        return createURL(path, query);
    }

    public static URL createURL(String path) throws MalformedURLException {
        return createURL(path, null);
    }

    public static URL createURL(String path, String query) throws MalformedURLException {
        return createURL(path, query, null);
    }

    public static URL createURL(String path, String query, String fragment) throws MalformedURLException {
        try {
            return URIUtils.createURI(wsProtocol, wsHost, wsPort, path, query, fragment).toURL();
        } catch (URISyntaxException e) {
            return new URL(e.getInput());
        }
    }

    private static boolean isTokenUsable(AuthenticationToken token) {
        return token != null && Validator.isNotNull(token.getToken()) && !token.isExpired();
    }

    private static boolean isJSONObject(String s) {
        return Validator.isNotNull(s) && s.startsWith("{");
    }

    private AuthenticationToken getValidAuthenticationToken() throws IOException, URISyntaxException {
        if (!isTokenUsable(token)) {
            token = AuthenticationHelper.fetchAuthenticationToken(this);
        }
        return token;
    }
}
