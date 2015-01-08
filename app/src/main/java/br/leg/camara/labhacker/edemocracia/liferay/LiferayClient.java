package br.leg.camara.labhacker.edemocracia.liferay;

import android.util.Log;

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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LiferayClient {

    private static final int wsPort = 8080;
    private static final String wsProtocol = "http";
    private static final String wsHost = "192.168.25.9";
    private static final String wsPath = "/api/jsonws";

    private AuthHelper.TokenAndCookies tokenAndCookies;

    public LiferayClient(AuthHelper.TokenAndCookies tokenAndCookies) {
        this.tokenAndCookies = tokenAndCookies;
    }

    public JSONArray listGroups(int companyId) throws ServerException, IOException, URISyntaxException {
        List<NameValuePair> args = new ArrayList<NameValuePair>();

        args.add(new BasicNameValuePair("p_auth", this.tokenAndCookies.getToken()));
        args.add(new BasicNameValuePair("companyId", Integer.toString(companyId)));
        args.add(new BasicNameValuePair("start", Integer.toString(-1)));
        args.add(new BasicNameValuePair("end", Integer.toString(-1)));
        args.add(new BasicNameValuePair("name", "%"));
        args.add(new BasicNameValuePair("description", "%"));
        args.add(new BasicNameValuePair("params", null));

        return this.call("/group/search", args);
    }

    protected URL buildURL(String method, List<NameValuePair> args) throws URISyntaxException, MalformedURLException {
        String queryString = URLEncodedUtils.format(args, "UTF-8");

        // Construct the Path
        String path = this.wsPath;
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

        URI uri = URIUtils.createURI(this.wsProtocol, this.wsHost, this.wsPort, path, queryString, null);
        return uri.toURL();
    }

    protected JSONArray call(String method, List<NameValuePair> args) throws ServerException, IOException, URISyntaxException {

        // Set the default CookieManager for each request. I don't know if this is
        // really necessary, but w/e.
        CookieHandler.setDefault(this.tokenAndCookies.getCookieManager());

        URL url = this.buildURL(method, args);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Handle response codes
        int status = connection.getResponseCode();

        if ((status == HttpURLConnection.HTTP_MOVED_PERM) ||
                (status == HttpURLConnection.HTTP_MOVED_TEMP) ||
                (status == HttpURLConnection.HTTP_SEE_OTHER)) {
            throw new RedirectException(connection.getHeaderField("Location"));
        }

        if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new ServerException("Authentication failed.");
        }

        if (status != HttpURLConnection.HTTP_OK) {
            throw new ServerException("Request failed with code " + status + ".");
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

    private static Boolean isJSONObject(String s) {
        return Validator.isNotNull(s) && s.startsWith("{");
    }
}
